package org.amitayh.invoices.common.serde

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.time.Instant
import java.util
import java.util.UUID

import com.sksamuel.avro4s._
import UuidConverters.{fromByteBuffer, toByteBuffer}
import org.amitayh.invoices.common.domain._
import org.apache.avro.Schema
import org.apache.avro.Schema.Field
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}

object AvroSerde {
  implicit val instantToSchema: ToSchema[Instant] = new ToSchema[Instant] {
    override val schema: Schema = Schema.create(Schema.Type.STRING)
  }

  implicit val instantToValue: ToValue[Instant] = new ToValue[Instant] {
    override def apply(value: Instant): String = value.toString
  }

  implicit val instantFromValue: FromValue[Instant] = new FromValue[Instant] {
    override def apply(value: Any, field: Field): Instant =
      Instant.parse(value.toString)
  }

  implicit val uuidToSchema: ToSchema[UUID] = new ToSchema[UUID] {
    override val schema: Schema = Schema.create(Schema.Type.BYTES)
  }

  implicit val uuidToValue: ToValue[UUID] = new ToValue[UUID] {
    override def apply(value: UUID): ByteBuffer = toByteBuffer(value)
  }

  implicit val uuidFromValue: FromValue[UUID] = new FromValue[UUID] {
    override def apply(value: Any, field: Field): UUID =
      fromByteBuffer(value.asInstanceOf[ByteBuffer])
  }

  val CommandSerde: Serde[Command] = serdeFor[Command]

  val CommandResultSerde: Serde[CommandResult] = serdeFor[CommandResult]

  val SnapshotSerde: Serde[InvoiceSnapshot] = serdeFor[InvoiceSnapshot]

  val EventSerde: Serde[Event] = serdeFor[Event]

  def toBytes[T: SchemaFor: ToRecord](data: T): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val output = AvroOutputStream.binary[T](baos)
    output.write(data)
    output.close()
    baos.toByteArray
  }

  def fromBytes[T: SchemaFor: FromRecord](data: Array[Byte]): T = {
    val input = AvroInputStream.binary[T](data)
    input.iterator.next()
  }

  private def serdeFor[T: SchemaFor: ToRecord: FromRecord]: Serde[T] = new Serde[T] {
    override val serializer: Serializer[T] = new Serializer[T] {
      override def serialize(topic: String, data: T): Array[Byte] = toBytes(data)
      override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
      override def close(): Unit = ()
    }
    override val deserializer: Deserializer[T] = new Deserializer[T] {
      override def deserialize(topic: String, data: Array[Byte]): T = fromBytes(data)
      override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
      override def close(): Unit = ()
    }
    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
    override def close(): Unit = ()
  }
}
