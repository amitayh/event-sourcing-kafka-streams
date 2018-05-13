package org.amitayh.invoices.domain

import java.time.LocalDate
import java.util.UUID

sealed trait InvoiceEvent extends (Invoice => Invoice) {
  override def apply(invoice: Invoice): Invoice = invoice
}

case class InvoiceCreated(customerName: String,
                          customerEmail: String,
                          issueDate: LocalDate,
                          dueDate: LocalDate) extends InvoiceEvent {
  override def apply(invoice: Invoice): Invoice =
    invoice
      .setCustomer(customerName, customerEmail)
      .setDates(issueDate, dueDate)
}

case class LineItemAdded(lineItemId: UUID,
                         description: String,
                         quantity: BigDecimal,
                         price: BigDecimal) extends InvoiceEvent {
  override def apply(invoice: Invoice): Invoice =
    invoice.addLineItem(lineItemId, description, quantity, price)
}

case class LineItemRemoved(lineItemId: UUID) extends InvoiceEvent {
  override def apply(invoice: Invoice): Invoice =
    invoice.removeLineItem(lineItemId)
}

case class PaymentReceived(amount: BigDecimal) extends InvoiceEvent {
  override def apply(invoice: Invoice): Invoice = invoice.pay(amount)
}

case class InvoiceSentToCustomer() extends InvoiceEvent
