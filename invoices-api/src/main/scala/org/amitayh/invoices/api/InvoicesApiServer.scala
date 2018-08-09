package org.amitayh.invoices.api

import cats.effect.{Effect, IO}
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext

object InvoicesApiServer extends StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    ServerStream.stream[IO]
}

object ServerStream {
  def invoicesApi[F[_]: Effect]: HttpService[F] =
    new InvoicesApi[F].service

  def stream[F[_]: Effect](implicit ec: ExecutionContext): Stream[F, ExitCode] =
    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(invoicesApi, "/api")
      .serve
}
