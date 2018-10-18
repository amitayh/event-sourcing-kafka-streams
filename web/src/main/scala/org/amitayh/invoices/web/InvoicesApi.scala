package org.amitayh.invoices.web

import cats.effect.Sync
import cats.implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.amitayh.invoices.common.domain.Command
import org.amitayh.invoices.dao.InvoiceList
import org.amitayh.invoices.web.CommandDto._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpService}

class InvoicesApi[F[_]: Sync: InvoiceList: Producer] extends Http4sDsl[F] {

  implicit val commandEntityDecoder: EntityDecoder[F, Command] = jsonOf[F, Command]

  val service: HttpService[F] = HttpService[F] {
    case GET -> Root / "invoices" =>
      InvoiceList[F].get.flatMap(invoices => Ok(invoices.asJson))

    case request @ POST -> Root / "execute" / UuidVar(invoiceId) =>
      request
        .as[Command]
        .flatMap(Producer[F].produce(invoiceId, _))
        .flatMap(metaData => Ok(Json.fromLong(metaData.timestamp)))
  }

}

object InvoicesApi {
  def apply[F[_]: Sync: InvoiceList: Producer]: InvoicesApi[F] =
    new InvoicesApi[F]
}
