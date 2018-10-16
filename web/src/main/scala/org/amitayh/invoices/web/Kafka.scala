package org.amitayh.invoices.web

import java.util.{Properties, UUID}

import cats.effect.IO
import org.amitayh.invoices.common.Config
import org.amitayh.invoices.common.domain.Command
import org.amitayh.invoices.common.serde.{CommandSerializer, UuidSerializer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}

trait Kafka[F[_]] {
  def produce(invoiceId: UUID, command: Command): F[RecordMetadata]
}

object Kafka {
  def apply[F[_]](implicit F: Kafka[F]): Kafka[F] = F

  implicit val kafkaIO: Kafka[IO] = new Kafka[IO] {
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
