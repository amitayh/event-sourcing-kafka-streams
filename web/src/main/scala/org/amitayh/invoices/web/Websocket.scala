package org.amitayh.invoices.web

import java.util.UUID

import cats.effect._
import fs2.Sink
import fs2.async.mutable.Topic
import io.circe.generic.auto._
import io.circe.syntax._
import org.amitayh.invoices.common.domain.{CommandResult, InvoiceSnapshot}
import org.amitayh.invoices.dao.InvoiceRecord
import org.amitayh.invoices.web.Websocket._
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket._
import org.http4s.websocket.WebsocketBits.{Text, WebSocketFrame}

import scala.concurrent.ExecutionContext.Implicits.global

class Websocket[F[_]: Effect] extends Http4sDsl[F] {

  private val maxQueued = 16

  private val ignore: Sink[F, WebSocketFrame] = _.map(_ => ())

  def service(commandResultsTopic: Topic[F, CommandResultRecord],
              invoiceUpdatesTopic: Topic[F, InvoiceSnapshotRecord]): HttpService[F] = HttpService[F] {
    case GET -> Root / UuidVar(originId) =>
      val commandResults = commandResultsTopic.subscribe(maxQueued).collect {
        case Some((_, result)) if result.originId == originId =>
          WebsocketMessage(result).asFrame
      }
      val invoiceUpdates = invoiceUpdatesTopic.subscribe(maxQueued).collect {
        case Some((id, snapshot)) => WebsocketMessage(id, snapshot).asFrame
      }
      WebSocketBuilder[F].build(
        send = commandResults merge invoiceUpdates,
        receive = ignore)
  }

}

object Websocket {
  type CommandResultRecord = Option[(UUID, CommandResult)]
  type InvoiceSnapshotRecord = Option[(UUID, InvoiceSnapshot)]

  def apply[F[_]: Effect]: Websocket[F] = new Websocket[F]
}

sealed trait WebsocketMessage {
  def asFrame: WebSocketFrame = Text(this.asJson.noSpaces)
}

case class CommandSucceeded(commandId: UUID) extends WebsocketMessage
case class CommandFailed(commandId: UUID, cause: String) extends WebsocketMessage
case class InvoiceUpdated(record: InvoiceRecord) extends WebsocketMessage

object WebsocketMessage {
  def apply(result: CommandResult): WebsocketMessage = result match {
    case CommandResult(_, commandId, _: CommandResult.Success) =>
      CommandSucceeded(commandId)

    case CommandResult(_, commandId, CommandResult.Failure(cause)) =>
      CommandFailed(commandId, cause.message)
  }

  def apply(id: UUID, snapshot: InvoiceSnapshot): WebsocketMessage =
    InvoiceUpdated(InvoiceRecord(id, snapshot))
}
