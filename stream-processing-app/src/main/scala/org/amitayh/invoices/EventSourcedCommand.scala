package org.amitayh.invoices

import java.util.UUID

import org.amitayh.invoices.domain.{Invoice, InvoiceCommand, InvoiceEvent}

import scala.collection.immutable.Seq

case class EventSourcedCommand
  (commandId: UUID, toEvents: InvoiceCommand, expectedVersion: Option[Int] = None)
  extends (Snapshot[Invoice] => CommandExecutionResult) {

  override def apply(snapshot: Snapshot[Invoice]): CommandExecutionResult = {
    snapshot
      .validateVersion(expectedVersion)
      .flatMap(toEvents)
      .fold(
        CommandExecutionResult.failed(commandId),
        CommandExecutionResult.succeeded(commandId))
  }

}

sealed trait CommandExecutionResult {
  def commandId: UUID
  def isSuccessful: Boolean
}

object CommandExecutionResult {
  def succeeded(commandId: UUID)(events: Seq[InvoiceEvent]): CommandExecutionResult =
    CommandSucceeded(commandId, events)

  def failed(commandId: UUID)(cause: Throwable): CommandExecutionResult =
    CommandFailed(commandId, cause.getMessage)
}

case class CommandSucceeded(commandId: UUID, events: Seq[InvoiceEvent])
  extends CommandExecutionResult {
  override def isSuccessful: Boolean = true
}

case class CommandFailed(commandId: UUID, cause: String)
  extends CommandExecutionResult {
  override def isSuccessful: Boolean = false
}
