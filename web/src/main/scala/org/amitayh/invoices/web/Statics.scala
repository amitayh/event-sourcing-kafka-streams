package org.amitayh.invoices.web

import cats.effect.Sync
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpService, StaticFile}

class Statics[F[_]: Sync] extends Http4sDsl[F] {

  val service: HttpService[F] = HttpService[F] {
    case request @ GET -> fileName =>
      StaticFile
        .fromResource(
          name = s"/statics$fileName",
          req = Some(request),
          preferGzipped = true)
        .getOrElseF(NotFound())
  }

}

object Statics {
  def apply[F[_]: Sync]: Statics[F] = new Statics[F]
}
