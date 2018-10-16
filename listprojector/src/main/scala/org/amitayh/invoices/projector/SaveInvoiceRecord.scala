package org.amitayh.invoices.projector

import java.util.UUID

import cats.effect.IO
import org.amitayh.invoices.dao.{InvoiceList, InvoiceRecord}
import org.apache.kafka.streams.kstream.ForeachAction

object SaveInvoiceRecord
  extends ForeachAction[UUID, InvoiceRecord] {

  override def apply(id: UUID, value: InvoiceRecord): Unit =
    InvoiceList[IO].save(value).unsafeRunSync()

}
