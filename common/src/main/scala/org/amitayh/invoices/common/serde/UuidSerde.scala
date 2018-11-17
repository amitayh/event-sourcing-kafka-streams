package org.amitayh.invoices.common.serde

import java.util
import java.util.UUID

import org.amitayh.invoices.common.serde.UuidConverters.{fromBytes, toBytes}
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}

object UuidSerializer extends Serializer[UUID] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
  override def serialize(topic: String, uuid: UUID): Array[Byte] = toBytes(uuid)
  override def close(): Unit = ()
}

object UuidDeserializer extends Deserializer[UUID] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
  override def deserialize(topic: String, data: Array[Byte]): UUID = fromBytes(data)
  override def close(): Unit = ()
}

object UuidSerde extends Serde[UUID] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
  override val serializer: Serializer[UUID] = UuidSerializer
  override val deserializer: Deserializer[UUID] = UuidDeserializer
  override def close(): Unit = ()
}
