package org.amitayh.invoices.web

import java.time.LocalDate

import io.circe.Decoder
import io.circe.generic.semiauto._
import org.amitayh.invoices.common.domain.Command.Payload
import org.amitayh.invoices.common.domain.{Command, LineItem}

import scala.util.Try

object CommandDto {

  implicit val decodeLocalDate: Decoder[LocalDate] =
    Decoder.decodeString.emapTry(string => Try(LocalDate.parse(string)))

  implicit val decodeLineItem: Decoder[LineItem] = deriveDecoder

  implicit val decodePayload: Decoder[Payload] = deriveDecoder

  implicit val decodeCommand: Decoder[Command] = deriveDecoder

}
