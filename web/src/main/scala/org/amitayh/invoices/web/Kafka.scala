package org.amitayh.invoices.web

import java.time.Duration
import java.util.Collections.singletonList
import java.util.{ConcurrentModificationException, Properties, UUID}

import cats.effect.IO
import cats.syntax.monoid._
import fs2._
import org.amitayh.invoices.common.Config
import org.amitayh.invoices.common.Config.Topics.Topic
import org.amitayh.invoices.common.domain.Command
import org.amitayh.invoices.common.serde.{CommandSerializer, UuidSerializer}
import org.apache.kafka.clients.consumer._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.Deserializer
import org.log4s.{Logger, getLogger}
import retry.RetryPolicies._
import retry._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait Producer[F[_]] {
  def produce(invoiceId: UUID, command: Command): F[RecordMetadata]
}

object Producer {
  def apply[F[_]](implicit F: Producer[F]): Producer[F] = F

  implicit val producerIO: Producer[IO] = new Producer[IO] {
    private val producer: KafkaProducer[UUID, Command] = {
      val props = new Properties
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
      new KafkaProducer[UUID, Command](props, UuidSerializer, CommandSerializer)
    }

    override def produce(invoiceId: UUID, command: Command): IO[RecordMetadata] = IO.async { cb =>
      val record = new ProducerRecord(Config.Topics.Commands.name, invoiceId, command)
      producer.send(record, (metadata: RecordMetadata, exception: Exception) => {
        if (exception != null) cb(Left(exception))
        else cb(Right(metadata))
      })
    }
  }
}

trait Consumer[F[_]] {
  def subscribe[K, V](topic: Topic,
                      groupId: String,
                      keyDeserializer: Deserializer[K],
                      valueDeserializer: Deserializer[V]): Stream[F, (K, V)]
}

object Consumer {
  private val logger: Logger = getLogger

  implicit private val sleepIO: Sleep[IO] =
    (delay: FiniteDuration) => IO.sleep(delay)

  def apply[F[_]](implicit F: Consumer[F]): Consumer[F] = F

  implicit val consumerIO: Consumer[IO] = new Consumer[IO] {
    override def subscribe[K, V](topic: Topic,
                                 groupId: String,
                                 keyDeserializer: Deserializer[K],
                                 valueDeserializer: Deserializer[V]): Stream[IO, (K, V)] = {
      val consumer = IO {
        val props = new Properties
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        val consumer = new KafkaConsumer(props, keyDeserializer, valueDeserializer)
        consumer.subscribe(singletonList(topic.name))
        consumer
      }
      Stream.bracket(consumer)(consume, close)
    }

    private def consume[K, V](consumer: KafkaConsumer[K, V]): Stream[IO, (K, V)] = for {
      records <- Stream.repeatEval(IO(consumer.poll(pollTimeout)))
      record <- Stream.emits(records.iterator.asScala.toSeq)
    } yield record.key -> record.value

    private def close(consumer: KafkaConsumer[_, _]): IO[Unit] =
      retryingOnSomeErrors(
        policy = constantDelay[IO](1.second) |+| limitRetries[IO](10),
        isWorthRetrying = (_: Throwable).isInstanceOf[ConcurrentModificationException],
        onError = (_: Throwable, _: RetryDetails) => logWaiting)(IO(consumer.close()))

    private val logWaiting = IO(logger.info("Waiting for consumer to close"))

    private val pollTimeout = Duration.ofSeconds(1)
  }
}
