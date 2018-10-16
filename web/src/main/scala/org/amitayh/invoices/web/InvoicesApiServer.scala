package org.amitayh.invoices.web

import cats.effect.IO
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.middleware.CORS

import scala.concurrent.ExecutionContext.Implicits.global

object InvoicesApiServer extends StreamApp[IO] {

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(invoicesApi, "/api")
      .serve

  private def invoicesApi: HttpService[IO] =
    CORS(new InvoicesApi[IO].service)

}
