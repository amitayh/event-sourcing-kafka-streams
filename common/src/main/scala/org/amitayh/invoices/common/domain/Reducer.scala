package org.amitayh.invoices.common.domain

import java.time.Instant

trait Reducer[S, E] {
  def empty: S
  def handle(s: S, e: E): S
}

object InvoiceReducer extends Reducer[Invoice, Event.Payload] {
  override val empty: Invoice = Invoice.Draft

  override def handle(invoice: Invoice, event: Event.Payload): Invoice = event match {
    case Event.InvoiceCreated(customerName, customerEmail, issueDate, dueDate) =>
      invoice
        .setCustomer(customerName, customerEmail)
        .setDates(issueDate, dueDate)

    case Event.LineItemAdded(description, quantity, price) =>
      invoice.addLineItem(description, quantity, price)

    case Event.LineItemRemoved(index) =>
      invoice.removeLineItem(index)

    case Event.PaymentReceived(amount) =>
      invoice.pay(amount)

    case Event.InvoiceDeleted() =>
      invoice.delete

    case _ => invoice
  }
}

object SnapshotReducer extends Reducer[InvoiceSnapshot, Event] {
  override val empty: InvoiceSnapshot =
    InvoiceSnapshot(InvoiceReducer.empty, 0, Instant.MIN)

  override def handle(snapshot: InvoiceSnapshot, event: Event): InvoiceSnapshot = {
    if (versionsMatch(snapshot, event)) updateSnapshot(snapshot, event)
    else throw new RuntimeException(s"Unexpected version $snapshot / $event")
  }

  private def versionsMatch(snapshot: InvoiceSnapshot, event: Event): Boolean =
    snapshot.version == (event.version - 1)

  private def updateSnapshot(snapshot: InvoiceSnapshot, event: Event): InvoiceSnapshot = {
    val invoice = InvoiceReducer.handle(snapshot.invoice, event.payload)
    InvoiceSnapshot(invoice, event.version, event.timestamp)
  }
}
