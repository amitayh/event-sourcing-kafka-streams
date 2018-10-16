package org.amitayh.invoices.projector

import java.util.UUID

import cats.effect.IO
import org.amitayh.invoices.common.domain.CommandResult
import org.apache.kafka.streams.kstream.ForeachAction

object PushCommandResult
  extends ForeachAction[UUID, CommandResult] {

  override def apply(id: UUID, result: CommandResult): Unit = {
    val channel = s"command-results-${result.originId}"
    val event = CommandResultEvent(result)
    Pusher[IO].trigger(channel, event.name, event).unsafeRunSync()
  }

}

sealed trait CommandResultEvent {
  def commandId: UUID
  def name: String
}

object CommandResultEvent {
  case class Success(commandId: UUID) extends CommandResultEvent {
    override def name: String = "command-succeeded"
  }

  case class Failure(commandId: UUID, cause: String) extends CommandResultEvent {
    override def name: String = "command-failed"
  }

  def apply(result: CommandResult): CommandResultEvent = result match {
    case CommandResult(_, commandId, _: CommandResult.Success) =>
      Success(commandId)

    case CommandResult(_, commandId, CommandResult.Failure(cause)) =>
      Failure(commandId, cause.message)
  }
}
