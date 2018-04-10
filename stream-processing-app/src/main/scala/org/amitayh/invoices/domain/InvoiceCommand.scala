package org.amitayh.invoices.domain

import java.time.LocalDate
import java.util.UUID

import org.amitayh.invoices.{CommandExecutionResult, EventSourcedCommand, Snapshot, SnapshotReducer}

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

case class CommandAndInvoice(command: EventSourcedCommand, snapshot: Snapshot[Invoice]) {
  def execute: CommandExecutionResult = command(snapshot)
}

object CommandAndInvoice {
  private val snapshotReducer = new SnapshotReducer(InvoiceReducer)

  def from(command: EventSourcedCommand, snapshot: Snapshot[Invoice]): CommandAndInvoice =
    CommandAndInvoice(command, Option(snapshot).getOrElse(snapshotReducer.initializer()))
}
