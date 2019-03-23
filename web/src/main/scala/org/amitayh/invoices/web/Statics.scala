package org.amitayh.invoices.web

import cats.effect.{ContextShift, Sync}
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, StaticFile}

import scala.concurrent.ExecutionContext.global

class Statics[F[_]: Sync: ContextShift] extends Http4sDsl[F] {

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ GET -> fileName =>
      StaticFile
        .fromResource(
          name = s"/statics$fileName",
          blockingExecutionContext = global,
          req = Some(request),
          preferGzipped = true)
        .getOrElseF(NotFound())
  }

}

object Statics {
  def apply[F[_]: Sync: ContextShift]: Statics[F] = new Statics[F]
}
