package org.amitayh.invoices.web

import java.util.UUID

import cats.effect.{Concurrent, Timer}
import cats.implicits._
import fs2.Stream
import fs2.concurrent.Topic
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.amitayh.invoices.common.domain.CommandResult.{Failure, Success}
import org.amitayh.invoices.common.domain.{Command, CommandResult}
import org.amitayh.invoices.dao.InvoiceList
import org.amitayh.invoices.web.CommandDto._
import org.amitayh.invoices.web.PushEvents.CommandResultRecord
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes, Response}

import scala.concurrent.duration._

class InvoicesApi[F[_]: Concurrent: Timer] extends Http4sDsl[F] {

  private val maxQueued = 16

  implicit val commandEntityDecoder: EntityDecoder[F, Command] = jsonOf[F, Command]

  def service(invoiceList: InvoiceList[F],
              producer: Kafka.Producer[F, UUID, Command],
              commandResultsTopic: Topic[F, CommandResultRecord]): HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "invoices" =>
      invoiceList.get.flatMap(invoices => Ok(invoices.asJson))

    case request @ POST -> Root / "execute" / "async" / UuidVar(invoiceId) =>
      request
        .as[Command]
        .flatMap(producer.send(invoiceId, _))
        .flatMap(metaData => Accepted(Json.fromLong(metaData.timestamp)))

    case request @ POST -> Root / "execute" / UuidVar(invoiceId) =>
      request.as[Command].flatMap { command =>
        val response = resultStream(commandResultsTopic, command.commandId) merge timeoutStream
        producer.send(invoiceId, command) *> response.head.compile.toList.map(_.head)
      }
  }

  private def resultStream(commandResultsTopic: Topic[F, CommandResultRecord],
                           commandId: UUID): Stream[F, Response[F]] =
    commandResultsTopic.subscribe(maxQueued).collectFirst {
      case Some((_, CommandResult(_, `commandId`, outcome))) => outcome
    }.flatMap {
      case Success(_, _, snapshot) => Stream.eval(Ok(snapshot.asJson))
      case Failure(cause) => Stream.eval(UnprocessableEntity(cause.message))
    }

  private def timeoutStream: Stream[F, Response[F]] =
    Stream.eval(Timer[F].sleep(5.seconds) *> RequestTimeout("timeout"))

}

object InvoicesApi {
  def apply[F[_]: Concurrent: Timer]: InvoicesApi[F] = new InvoicesApi[F]
}
