package org.amitayh.invoices.projector

import java.util.UUID

import cats.effect.IO
import org.amitayh.invoices.dao.InvoiceRecord
import org.apache.kafka.streams.kstream.ForeachAction

object PushInvoiceUpdates
  extends ForeachAction[UUID, InvoiceRecord] {

  private val channel = "invoice-records"

  private val eventName = "invoice-updated"

  override def apply(id: UUID, record: InvoiceRecord): Unit =
    Pusher[IO].trigger(channel, eventName, record).unsafeRunSync()

}
