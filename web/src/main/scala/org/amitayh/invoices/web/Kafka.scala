package org.amitayh.invoices.web

import java.time.Duration
import java.util.Collections.singletonList
import java.util.{ConcurrentModificationException, Properties}

import cats.effect.{Async, IO}
import cats.syntax.apply._
import cats.syntax.monoid._
import fs2._
import org.amitayh.invoices.common.Config
import org.amitayh.invoices.common.Config.Topics.Topic
import org.amitayh.invoices.web.Kafka.Producer
import org.apache.kafka.clients.consumer._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.{Deserializer, Serializer}
import org.log4s.{Logger, getLogger}
import retry.RetryPolicies._
import retry._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait Kafka[F[_]] {
  def producer[K, V](topic: Topic,
                     keySerializer: Serializer[K],
                     valueSerializer: Serializer[V]): Stream[F, Producer[F, K, V]]

  def subscribe[K, V](topic: Topic,
                      groupId: String,
                      keyDeserializer: Deserializer[K],
                      valueDeserializer: Deserializer[V]): Stream[F, (K, V)]
}

object Kafka {
  def apply[F[_]](implicit F: Kafka[F]): Kafka[F] = F

  trait Producer[F[_], K, V] {
    def send(key: K, value: V): F[RecordMetadata]
  }

  object Producer {
    def apply[F[_]: Async, K, V](producer: KafkaProducer[K, V], topic: Topic): Producer[F, K, V] =
      (key: K, value: V) => Async[F].async { cb =>
        val record = new ProducerRecord(topic.name, key, value)
        producer.send(record, (metadata: RecordMetadata, exception: Exception) => {
          if (exception != null) cb(Left(exception))
          else cb(Right(metadata))
        })
      }
  }

  implicit val kafkaIO: Kafka[IO] = new Kafka[IO] {
    private val logger: Logger = getLogger

    override def producer[K, V](topic: Topic,
                                keySerializer: Serializer[K],
                                valueSerializer: Serializer[V]): Stream[IO, Producer[IO, K, V]] = {
      val create = IO {
        val props = new Properties
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
        new KafkaProducer[K, V](props, keySerializer, valueSerializer)
      }
      Stream.bracket(create)(p => Stream(Producer[IO, K, V](p, topic)), close)
    }

    override def subscribe[K, V](topic: Topic,
                                 groupId: String,
                                 keyDeserializer: Deserializer[K],
                                 valueDeserializer: Deserializer[V]): Stream[IO, (K, V)] = {
      val create = IO {
        val props = new Properties
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        val consumer = new KafkaConsumer(props, keyDeserializer, valueDeserializer)
        consumer.subscribe(singletonList(topic.name))
        consumer
      }
      Stream.bracket(create)(consume, close)
    }

    private def consume[K, V](consumer: KafkaConsumer[K, V]): Stream[IO, (K, V)] = for {
      records <- Stream.repeatEval(IO(consumer.poll(Duration.ofSeconds(1))))
      record <- Stream.emits(records.iterator.asScala.toSeq)
    } yield record.key -> record.value

    private def close(producer: KafkaProducer[_, _]): IO[Unit] =
      IO(producer.close()) *> IO(logger.info(s"Producer closed"))

    private def close(consumer: KafkaConsumer[_, _]): IO[Unit] = {
      implicit val sleepIO: Sleep[IO] = (delay: FiniteDuration) => IO.sleep(delay)
      val logWaiting = IO(logger.info("Waiting for consumer to close"))
      retryingOnSomeErrors(
        policy = constantDelay[IO](1.second) |+| limitRetries[IO](10),
        isWorthRetrying = (_: Throwable).isInstanceOf[ConcurrentModificationException],
        onError = (_: Throwable, _: RetryDetails) => logWaiting)(IO(consumer.close())) *>
        IO(logger.info("Consumer closed"))

    }
  }
}
