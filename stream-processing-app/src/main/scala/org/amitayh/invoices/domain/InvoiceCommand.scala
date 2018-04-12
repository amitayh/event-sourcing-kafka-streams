package org.amitayh.invoices.domain

import java.time.LocalDate
import java.util.UUID

import scala.collection.immutable.Seq
import scala.util.{Failure, Success, Try}

sealed trait InvoiceCommand extends (Invoice => Try[Seq[InvoiceEvent]])

case class CreateInvoice(customerName: String,
                         customerEmail: String,
                         issueDate: LocalDate,
                         dueDate: LocalDate,
                         lineItems: List[LineItem]) extends InvoiceCommand {

  override def apply(invoice: Invoice): Try[Seq[InvoiceEvent]] = {
    val createdEvent = InvoiceCreated(customerName, customerEmail, issueDate, dueDate)
    val lineItemEvents = lineItems.map(toLineItemEvent)
    Success(createdEvent :: lineItemEvents)
  }

  private def toLineItemEvent(lineItem: LineItem): InvoiceEvent =
    LineItemAdded(
      lineItemId = UUID.randomUUID(),
      description = lineItem.description,
      quantity = lineItem.quantity,
      price = lineItem.price)
}

case class AddLineItem(description: String,
                       quantity: Double,
                       price: Double) extends InvoiceCommand {
  override def apply(invoice: Invoice): Try[Seq[InvoiceEvent]] =
    Success(LineItemAdded(UUID.randomUUID(), description, quantity, price) :: Nil)
}

case class RemoveItem(lineItemId: UUID) extends InvoiceCommand {
  override def apply(invoice: Invoice): Try[Seq[InvoiceEvent]] = {
    if (invoice.hasLineItem(lineItemId)) Success(LineItemRemoved(lineItemId) :: Nil)
    else Failure(new RuntimeException(s"Line item with ID $lineItemId does not exist"))
  }
}

case class PayInvoice() extends InvoiceCommand {
  override def apply(invoice: Invoice): Try[Seq[InvoiceEvent]] =
    Success(PaymentReceived(invoice.total) :: Nil)
}
