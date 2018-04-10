package org.amitayh.invoices.projection

import java.util.UUID

import org.amitayh.invoices.Config
import org.amitayh.invoices.JsonSerde.{RecordSerde, UuidSerde}

class InvoicesListConsumer(writer: InvoiceListWriter) {

  private val groupId = "invoices-list-projection-v3"

  private val consumer = new Consumer[UUID, InvoiceRecord](
    groupId = groupId,
    topic = Config.InvoicesTopic,
    keyDeserializer = UuidSerde.deserializer,
    valueDeserializer = RecordSerde.deserializer)

  def start(): Unit = consumer.start { (id, record) =>
    writer.update(id, record)
  }

  def close(): Unit = consumer.close()

}
