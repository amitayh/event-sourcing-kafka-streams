package org.amitayh.invoices.domain

import java.util.UUID

import org.amitayh.invoices.Reducer
import org.apache.kafka.streams.kstream.{Aggregator, Initializer}

object InvoiceReducer extends Reducer[Invoice, InvoiceEvent] {
  override val initializer: Initializer[Invoice] = () => Invoice.Draft
  override val aggregator: Aggregator[UUID, InvoiceEvent, Invoice] =
    (_: UUID, event: InvoiceEvent, invoice: Invoice) => event(invoice)
}
