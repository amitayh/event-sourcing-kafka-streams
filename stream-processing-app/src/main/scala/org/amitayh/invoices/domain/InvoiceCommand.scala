package org.amitayh.invoices.domain

import java.time.LocalDate
import java.util.UUID

import org.amitayh.invoices.domain.InvoiceCommand._

import scala.collection.immutable.Seq

sealed trait InvoiceCommand extends (Invoice => Result)

object InvoiceCommand {
  type Result = Either[InvoiceError, Seq[InvoiceEvent]]
  def success(events: InvoiceEvent*): Result = Right(events.toList)
  def success(events: Seq[InvoiceEvent]): Result = Right(events)
  def failure(error: InvoiceError): Result = Left(error)
}

case class CreateInvoice(customerName: String,
                         customerEmail: String,
                         issueDate: LocalDate,
                         dueDate: LocalDate,
                         lineItems: List[LineItem]) extends InvoiceCommand {

  override def apply(invoice: Invoice): Result = {
    val createdEvent = InvoiceCreated(customerName, customerEmail, issueDate, dueDate)
    val lineItemEvents = lineItems.map(toLineItemEvent)
    success(createdEvent :: lineItemEvents)
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
  override def apply(invoice: Invoice): Result =
    success(LineItemAdded(UUID.randomUUID(), description, quantity, price))
}

case class RemoveLineItem(lineItemId: UUID) extends InvoiceCommand {
  override def apply(invoice: Invoice): Result = {
    if (invoice.hasLineItem(lineItemId)) success(LineItemRemoved(lineItemId))
    else failure(LineItemDoesNotExist(lineItemId))
  }
}

case class PayInvoice() extends InvoiceCommand {
  override def apply(invoice: Invoice): Result =
    success(PaymentReceived(invoice.total))
}

case class DeleteInvoice() extends InvoiceCommand {
  override def apply(invoice: Invoice): Result =
    success(InvoiceDeleted())
}
