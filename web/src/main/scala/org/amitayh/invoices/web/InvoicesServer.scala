package org.amitayh.invoices.web

import java.util.UUID

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.Stream
import fs2.concurrent.Topic
import org.amitayh.invoices.common.Config.Topics
import org.amitayh.invoices.common.domain.{Command, CommandResult, InvoiceSnapshot}
import org.amitayh.invoices.dao.{InvoiceList, MySqlInvoiceList}
import org.amitayh.invoices.web.PushEvents._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

object InvoicesServer extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    stream.compile.drain.as(ExitCode.Success)

  private val stream: Stream[IO, ExitCode] = for {
    invoiceList <- Stream.resource(MySqlInvoiceList.resource[IO])
    producer <- Stream.resource(Kafka.producer[IO, UUID, Command](Topics.Commands))
    commandResultsTopic <- Stream.eval(Topic[IO, CommandResultRecord](None))
    invoiceUpdatesTopic <- Stream.eval(Topic[IO, InvoiceSnapshotRecord](None))
    server <- httpServer(invoiceList, producer, commandResultsTopic, invoiceUpdatesTopic) concurrently
      commandResults.through(commandResultsTopic.publish) concurrently
      invoiceUpdates.through(invoiceUpdatesTopic.publish)
  } yield server

  private def commandResults: Stream[IO, CommandResultRecord] =
    Kafka.subscribe[IO, UUID, CommandResult](
      topic = Topics.CommandResults,
      groupId = "invoices.websocket.command-results").map(Some(_))

  private def invoiceUpdates: Stream[IO, InvoiceSnapshotRecord] =
    Kafka.subscribe[IO, UUID, InvoiceSnapshot](
      topic = Topics.Snapshots,
      groupId = "invoices.websocket.snapshots").map(Some(_))

  private def httpServer(invoiceList: InvoiceList[IO],
                         producer: Kafka.Producer[IO, UUID, Command],
                         commandResultsTopic: Topic[IO, CommandResultRecord],
                         invoiceUpdatesTopic: Topic[IO, InvoiceSnapshotRecord]): Stream[IO, ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(
        Router(
          "/api" -> InvoicesApi[IO].service(invoiceList, producer, commandResultsTopic),
          "/events" -> PushEvents[IO].service(commandResultsTopic, invoiceUpdatesTopic),
          "/" -> Statics[IO].service).orNotFound)
      .serve

}
