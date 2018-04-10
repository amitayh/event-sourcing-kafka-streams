package org.amitayh.invoices

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util
import java.util.UUID

import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.encoding.DerivedObjectEncoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.jawn.parseByteBuffer
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.amitayh.invoices.domain._
import org.amitayh.invoices.projection.InvoiceRecord
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}
import shapeless.Lazy

import scala.util.Try

object JsonSerde {

  implicit val listItemDecoder: Decoder[LineItem] = deriveDecoder
  implicit val listItemEncoder: Encoder[LineItem] = deriveEncoder
  implicit val customerDecoder: Decoder[Customer] = deriveDecoder
  implicit val customerEncoder: Encoder[Customer] = deriveEncoder
  implicit val dateDecoder: Decoder[LocalDate] = Decoder.decodeString.map(LocalDate.parse)
  implicit val dateEncoder: Encoder[LocalDate] = (date: LocalDate) => date.toString.asJson
  implicit val invoiceDecoder: Decoder[Invoice] = deriveDecoder
  implicit val invoiceEncoder: Encoder[Invoice] = deriveEncoder
  implicit val eventDecoder: Decoder[InvoiceEvent] = deriveDecoder
  implicit val eventEncoder: Encoder[InvoiceEvent] = deriveEncoder
  implicit val commandDecoder: Decoder[InvoiceCommand] = deriveDecoder
  implicit val commandEncoder: Encoder[InvoiceCommand] = deriveEncoder
  implicit val commandResultDecoder: Decoder[CommandExecutionResult] = deriveDecoder
  implicit val commandResultEncoder: Encoder[CommandExecutionResult] = deriveEncoder

  val InvoiceSerde: Serde[Invoice] = deriveSerde
  val EventSerde: Serde[InvoiceEvent] = deriveSerde
  val CommandSerde: Serde[InvoiceCommand] = deriveSerde
  val SnapshotSerde: Serde[Snapshot[Invoice]] = JsonSerde.deriveSerde
  val RecordSerde: Serde[InvoiceRecord] = JsonSerde.deriveSerde
  val EventSourcedCommandSerde: Serde[EventSourcedCommand] = deriveSerde
  val CommandResultSerde: Serde[CommandExecutionResult] = deriveSerde

  val UuidSerde: SimpleSerde[UUID] = new SimpleSerde[UUID] {
    override def serialize(data: UUID): Array[Byte] = {
      val bytes = ByteBuffer.allocate(16)
      bytes.putLong(data.getMostSignificantBits)
      bytes.putLong(data.getLeastSignificantBits)
      bytes.array()
    }
    override def deserialize(data: Array[Byte]): UUID = {
      val bytes = ByteBuffer.wrap(data)
      new UUID(bytes.getLong, bytes.getLong)
    }
  }

  def deriveSerde[T <: AnyRef](implicit decode: Lazy[DerivedDecoder[T]],
                               encode: Lazy[DerivedObjectEncoder[T]]): Serde[T] = new SimpleSerde[T] {
    private val encoder: Encoder[T] = deriveEncoder
    private val decoder: Decoder[T] = deriveDecoder

    override def serialize(data: T): Array[Byte] =
      encoder(data).noSpaces.getBytes(StandardCharsets.UTF_8)

    override def deserialize(data: Array[Byte]): T = {
      Try(ByteBuffer.wrap(data)).flatMap { buffer =>
        parseByteBuffer(buffer).flatMap(decoder.decodeJson).toTry
      }.getOrElse(null.asInstanceOf[T])
    }
  }

  trait SimpleSerde[T] extends Serde[T] { self =>
    def serialize(data: T): Array[Byte]
    def deserialize(data: Array[Byte]): T

    override val deserializer: Deserializer[T] = new Deserializer[T] {
      override def deserialize(topic: String, data: Array[Byte]): T = self.deserialize(data)
      override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
      override def close(): Unit = ()
    }
    override val serializer: Serializer[T] = new Serializer[T] {
      override def serialize(topic: String, data: T): Array[Byte] = self.serialize(data)
      override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
      override def close(): Unit = ()
    }
    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
    override def close(): Unit = ()
  }

}
