package org.amitayh.invoices.projector

import java.util.UUID

import org.amitayh.invoices.common.domain.InvoiceSnapshot
import org.amitayh.invoices.dao.InvoiceRecord
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KeyValueMapper

object ToRecord extends KeyValueMapper[UUID, InvoiceSnapshot, KeyValue[UUID, InvoiceRecord]] {
  override def apply(id: UUID, snapshot: InvoiceSnapshot): KeyValue[UUID, InvoiceRecord] =
    KeyValue.pair(id, InvoiceRecord(id, snapshot))
}
