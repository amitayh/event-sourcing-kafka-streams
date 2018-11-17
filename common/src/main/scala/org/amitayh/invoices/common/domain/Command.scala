package org.amitayh.invoices.common.domain

import java.time.{Instant, LocalDate}
import java.util.UUID

import scala.collection.immutable.Seq

case class Command(originId: UUID,
                   commandId: UUID,
                   expectedVersion: Option[Int],
                   payload: Command.Payload) {

  def apply(timestamp: Instant, snapshot: InvoiceSnapshot): CommandResult = {
    val outcome = snapshot
      .validateVersion(expectedVersion)
      .flatMap(payload(_))
      .fold(
        CommandResult.Failure,
        success(timestamp, snapshot, _))

    CommandResult(originId, commandId, outcome)
  }

  private def success(timestamp: Instant,
                      snapshot: InvoiceSnapshot,
                      payloads: Seq[Event.Payload]): CommandResult.Outcome = {
    payloads.foldLeft(CommandResult.Success(snapshot)) { (acc, payload) =>
      acc.update(timestamp, commandId, payload)
    }
  }

}

object Command {
  type Result = Either[InvoiceError, Seq[Event.Payload]]

  sealed trait Payload {
    def apply(invoice: Invoice): Result
  }

  case class CreateInvoice(customerName: String,
                           customerEmail: String,
                           issueDate: LocalDate,
                           dueDate: LocalDate,
                           lineItems: List[LineItem]) extends Payload {

    override def apply(invoice: Invoice): Result = {
      val createdEvent = Event.InvoiceCreated(customerName, customerEmail, issueDate, dueDate)
      val lineItemEvents = lineItems.map(toLineItemEvent)
      success(createdEvent :: lineItemEvents)
    }

    private def toLineItemEvent(lineItem: LineItem): Event.Payload =
      Event.LineItemAdded(
        description = lineItem.description,
        quantity = lineItem.quantity,
        price = lineItem.price)
  }

  case class AddLineItem(description: String,
                         quantity: Double,
                         price: Double) extends Payload {
    override def apply(invoice: Invoice): Result =
      success(Event.LineItemAdded(description, quantity, price))
  }

  case class RemoveLineItem(index: Int) extends Payload {
    override def apply(invoice: Invoice): Result = {
      if (invoice.hasLineItem(index)) success(Event.LineItemRemoved(index))
      else failure(LineItemDoesNotExist(index))
    }
  }

  case class PayInvoice() extends Payload {
    override def apply(invoice: Invoice): Result =
      success(Event.PaymentReceived(invoice.total))
  }

  case class DeleteInvoice() extends Payload {
    override def apply(invoice: Invoice): Result =
      success(Event.InvoiceDeleted())
  }

  private def success(events: Event.Payload*): Result = success(events.toList)

  private def success(events: List[Event.Payload]): Result = Right(events)

  private def failure(error: InvoiceError): Result = Left(error)
}
