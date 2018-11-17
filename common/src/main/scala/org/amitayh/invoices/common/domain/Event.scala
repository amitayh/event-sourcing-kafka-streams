package org.amitayh.invoices.common.domain

import java.time.{Instant, LocalDate}
import java.util.UUID

case class Event(version: Int,
                 timestamp: Instant,
                 commandId: UUID,
                 payload: Event.Payload)

object Event {
  sealed trait Payload {
    def apply(invoice: Invoice): Invoice = invoice
  }

  case class InvoiceCreated(customerName: String,
                            customerEmail: String,
                            issueDate: LocalDate,
                            dueDate: LocalDate) extends Payload {
    override def apply(invoice: Invoice): Invoice =
      invoice
        .setCustomer(customerName, customerEmail)
        .setDates(issueDate, dueDate)
  }

  case class LineItemAdded(description: String,
                           quantity: BigDecimal,
                           price: BigDecimal) extends Payload {
    override def apply(invoice: Invoice): Invoice =
      invoice.addLineItem(description, quantity, price)
  }

  case class LineItemRemoved(index: Int) extends Payload {
    override def apply(invoice: Invoice): Invoice =
      invoice.removeLineItem(index)
  }

  case class PaymentReceived(amount: BigDecimal) extends Payload {
    override def apply(invoice: Invoice): Invoice = invoice.pay(amount)
  }

  case class InvoiceDeleted() extends Payload {
    override def apply(invoice: Invoice): Invoice = invoice.delete
  }

  case class InvoiceSentToCustomer() extends Payload

}
