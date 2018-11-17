package org.amitayh.invoices.common.serde

import java.util

import org.amitayh.invoices.common.domain.Command
import org.amitayh.invoices.common.serde.AvroSerde._
import org.apache.kafka.common.serialization.Serializer

object CommandSerializer extends Serializer[Command] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
  override def serialize(topic: String, command: Command): Array[Byte] = toBytes(command)
  override def close(): Unit = ()
}
