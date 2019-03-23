package org.amitayh.invoices.web

import java.time.Duration
import java.util.Collections.singletonList
import java.util.Properties

import cats.effect._
import cats.syntax.apply._
import cats.syntax.functor._
import fs2._
import org.amitayh.invoices.common.Config
import org.amitayh.invoices.common.Config.Topics.Topic
import org.apache.kafka.clients.consumer._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.log4s.{Logger, getLogger}

import scala.collection.JavaConverters._

object Kafka {

  trait Producer[F[_], K, V] {
    def send(key: K, value: V): F[RecordMetadata]
  }

  object Producer {
    def apply[F[_]: Async, K, V](producer: KafkaProducer[K, V], topic: Topic[K, V]): Producer[F, K, V] =
      (key: K, value: V) => Async[F].async { cb =>
        val record = new ProducerRecord(topic.name, key, value)
        producer.send(record, (metadata: RecordMetadata, exception: Exception) => {
          if (exception != null) cb(Left(exception))
          else cb(Right(metadata))
        })
      }
  }

  def producer[F[_]: Async, K, V](topic: Topic[K, V]): Resource[F, Producer[F, K, V]] = Resource {
    val create = Sync[F].delay {
      val props = new Properties
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
      new KafkaProducer[K, V](props, topic.keySerializer, topic.valueSerializer)
    }
    create.map(producer => (Producer(producer, topic), close(producer)))
  }

  def subscribe[F[_]: Sync, K, V](topic: Topic[K, V], groupId: String): Stream[F, (K, V)] = {
    val create = Sync[F].delay {
      val props = new Properties
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
      props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
      val consumer = new KafkaConsumer(props, topic.keyDeserializer, topic.valueDeserializer)
      consumer.subscribe(singletonList(topic.name))
      consumer
    }
    Stream.bracket(create)(close[F]).flatMap(consume[F, K, V])
  }

  private val logger: Logger = getLogger

  def log[F[_]: Sync](msg: String): F[Unit] = Sync[F].delay(logger.info(msg))

  private def consume[F[_]: Sync, K, V](consumer: KafkaConsumer[K, V]): Stream[F, (K, V)] = for {
    records <- Stream.repeatEval(Sync[F].delay(consumer.poll(Duration.ofSeconds(1))))
    record <- Stream.emits(records.iterator.asScala.toSeq)
  } yield record.key -> record.value

  private def close[F[_]: Sync](producer: KafkaProducer[_, _]): F[Unit] =
    Sync[F].delay(producer.close()) *> log(s"Producer closed")

  private def close[F[_]: Sync](consumer: KafkaConsumer[_, _]): F[Unit] =
    Sync[F].delay(consumer.close()) *> log("Consumer closed")

}
