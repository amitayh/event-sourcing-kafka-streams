package org.amitayh.invoices.web

import cats.effect.IO
import fs2.StreamApp.ExitCode
import fs2.async.mutable.Topic
import fs2.{Stream, StreamApp}
import org.amitayh.invoices.common.Config.Topics
import org.amitayh.invoices.common.serde.AvroSerde.{CommandResultSerde, SnapshotSerde}
import org.amitayh.invoices.common.serde.UuidSerde
import org.amitayh.invoices.web.PushEvents._
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object InvoicesServer extends StreamApp[IO] {

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] = for {
    commandResultsTopic <- Stream.eval(Topic[IO, CommandResultRecord](None))
    invoiceUpdatesTopic <- Stream.eval(Topic[IO, InvoiceSnapshotRecord](None))
    server <- httpServer(commandResultsTopic, invoiceUpdatesTopic) concurrently
      commandResults.to(commandResultsTopic.publish) concurrently
      invoiceUpdates.to(invoiceUpdatesTopic.publish)
  } yield server

  private def commandResults: Stream[IO, CommandResultRecord] =
    Consumer[IO]
      .subscribe(
        topic = Topics.CommandResults,
        groupId = "invoices.websocket.command-results",
        keyDeserializer = UuidSerde.deserializer,
        valueDeserializer = CommandResultSerde.deserializer)
      .map(Some(_))

  private def invoiceUpdates: Stream[IO, InvoiceSnapshotRecord] =
    Consumer[IO]
      .subscribe(
        topic = Topics.Snapshots,
        groupId = "invoices.websocket.snapshots",
        keyDeserializer = UuidSerde.deserializer,
        valueDeserializer = SnapshotSerde.deserializer)
      .map(Some(_))

  private def httpServer(commandResultsTopic: Topic[IO, CommandResultRecord],
                         invoiceUpdatesTopic: Topic[IO, InvoiceSnapshotRecord]): Stream[IO, ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(InvoicesApi[IO].service(commandResultsTopic), "/api")
      .mountService(PushEvents[IO].service(commandResultsTopic, invoiceUpdatesTopic), "/events")
      .mountService(Statics[IO].service, "/")
      .serve

}
