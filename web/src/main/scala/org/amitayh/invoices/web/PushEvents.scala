package org.amitayh.invoices.web

import java.util.UUID

import cats.effect._
import fs2.concurrent.Topic
import io.circe.generic.auto._
import io.circe.syntax._
import org.amitayh.invoices.common.domain.{CommandResult, InvoiceSnapshot}
import org.amitayh.invoices.dao.InvoiceRecord
import org.amitayh.invoices.web.PushEvents._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, ServerSentEvent}

class PushEvents[F[_]: Concurrent] extends Http4sDsl[F] {

  private val maxQueued = 16

  def service(commandResultsTopic: Topic[F, CommandResultRecord],
              invoiceUpdatesTopic: Topic[F, InvoiceSnapshotRecord]): HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UuidVar(originId) =>
      val commandResults = commandResultsTopic.subscribe(maxQueued).collect {
        case Some((_, result)) if result.originId == originId =>
          Event(result).asServerSentEvent
      }
      val invoiceUpdates = invoiceUpdatesTopic.subscribe(maxQueued).collect {
        case Some((id, snapshot)) => Event(id, snapshot).asServerSentEvent
      }
      Ok(commandResults merge invoiceUpdates)
  }

}

object PushEvents {
  type CommandResultRecord = Option[(UUID, CommandResult)]
  type InvoiceSnapshotRecord = Option[(UUID, InvoiceSnapshot)]

  def apply[F[_]: Concurrent]: PushEvents[F] = new PushEvents[F]
}

sealed trait Event {
  def asServerSentEvent: ServerSentEvent =
    ServerSentEvent(this.asJson.noSpaces)
}

case class CommandSucceeded(commandId: UUID) extends Event
case class CommandFailed(commandId: UUID, cause: String) extends Event
case class InvoiceUpdated(record: InvoiceRecord) extends Event

object Event {
  def apply(result: CommandResult): Event = result match {
    case CommandResult(_, commandId, _: CommandResult.Success) =>
      CommandSucceeded(commandId)

    case CommandResult(_, commandId, CommandResult.Failure(cause)) =>
      CommandFailed(commandId, cause.message)
  }

  def apply(id: UUID, snapshot: InvoiceSnapshot): Event =
    InvoiceUpdated(InvoiceRecord(id, snapshot))
}
