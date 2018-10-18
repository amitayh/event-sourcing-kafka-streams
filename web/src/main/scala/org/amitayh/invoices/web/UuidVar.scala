package org.amitayh.invoices.web

import java.util.UUID

import scala.util.Try

object UuidVar {
  def unapply(arg: String): Option[UUID] =
    Try(UUID.fromString(arg)).toOption
}
