package org.amitayh.invoices.web

import java.time.Duration
import java.util.Collections.singletonList
import java.util.{Properties, UUID}

import cats.effect.IO
import fs2._
import org.amitayh.invoices.common.Config
import org.amitayh.invoices.common.Config.Topics.Topic
import org.amitayh.invoices.common.domain.Command
import org.amitayh.invoices.common.serde.{CommandSerializer, UuidSerializer}
import org.apache.kafka.clients.consumer._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.Deserializer

import scala.collection.JavaConverters._

trait Producer[F[_]] {
  def produce(invoiceId: UUID, command: Command): F[RecordMetadata]
}

object Producer {
  def apply[F[_]](implicit F: Producer[F]): Producer[F] = F

  implicit val producerIO: Producer[IO] = new Producer[IO] {
    private val producer: KafkaProducer[UUID, Command] = {
      val props = new Properties
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[UuidSerializer])
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[CommandSerializer])
      new KafkaProducer[UUID, Command](props)
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
  def apply[F[_]](implicit F: Consumer[F]): Consumer[F] = F

  implicit val consumerIO: Consumer[IO] = new Consumer[IO] {
    private val pollTimeout = Duration.ofSeconds(1)

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
      IO(consumer.close())
  }
}
