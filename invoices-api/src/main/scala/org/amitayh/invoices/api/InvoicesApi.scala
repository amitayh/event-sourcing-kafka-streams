package org.amitayh.invoices.api

import cats.Applicative
import cats.effect.Effect
import cats.implicits._
import io.circe.Json
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpService}

class InvoicesApi[F[_]: Effect] extends Http4sDsl[F] {

  val listInvoices: F[List[String]] =
    Applicative[F].pure("foo" :: "bar" :: Nil)

  def asJson(invoices: List[String]): Json =
    Json.fromValues(invoices.map(Json.fromString))

  sealed trait Command {
    def execute[F[_]]: F[Result]
  }
  case object Create extends Command {
    override def execute[F[_]]: F[Result] = ???
  }
  sealed trait Result
  case object Success extends Result
  implicit val commandDecoder: EntityDecoder[F, Command] = ???

  val service: HttpService[F] = {
    HttpService[F] {
      case GET -> Root / "hello" / name =>
        Ok(Json.obj("message" -> Json.fromString(s"Hello, $name")))

      case GET -> Root / "invoices" =>
        listInvoices.flatMap(invoices => Ok(asJson(invoices)))

      case request @ POST -> Root / "execute" =>
        request.as[Command].flatMap(_.execute)
    }
  }

}
