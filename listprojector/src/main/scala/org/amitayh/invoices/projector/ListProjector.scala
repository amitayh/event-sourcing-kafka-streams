package org.amitayh.invoices.projector

import java.util.UUID

import cats.effect.concurrent.Deferred
import cats.effect.{ContextShift, IO}
import cats.syntax.apply._
import org.amitayh.invoices.common.Config
import org.amitayh.invoices.common.domain.InvoiceSnapshot
import org.amitayh.invoices.common.serde.AvroSerde.SnapshotSerde
import org.amitayh.invoices.common.serde.UuidSerde
import org.amitayh.invoices.dao.{InvoiceList, InvoiceRecord, MySqlInvoiceList}
import org.amitayh.invoices.streamprocessor.StreamProcessorApp
import org.apache.kafka.streams.kstream.{Consumed, ForeachAction, KeyValueMapper}
import org.apache.kafka.streams.{KeyValue, StreamsBuilder, Topology}

import scala.concurrent.ExecutionContext.global

object ListProjector extends StreamProcessorApp {

  override def appId: String = "invoices.processor.list-projector"

  override def topology: Topology = ListProjectorTopology.create.unsafeRunSync()

}

object ListProjectorTopology {
  implicit val contextShift: ContextShift[IO] = IO.contextShift(global)

  def create: IO[Topology] = for {
    deferred <- Deferred[IO, Topology]
    _ <- MySqlInvoiceList.resource[IO].use { invoiceList =>
      buildTopology(invoiceList).flatMap(deferred.complete) *> IO.never
    }.start
    topology <- deferred.get
  } yield topology

  private def buildTopology(invoiceList: InvoiceList[IO]): IO[Topology] = IO {
    val builder = new StreamsBuilder

    val snapshots = builder.stream(
      Config.Topics.Snapshots.name,
      Consumed.`with`(UuidSerde, SnapshotSerde))

    snapshots
      .map[UUID, InvoiceRecord](ToRecord)
      .foreach(new SaveInvoiceRecord(invoiceList))

    builder.build()
  }
}

object ToRecord extends KeyValueMapper[UUID, InvoiceSnapshot, KeyValue[UUID, InvoiceRecord]] {
  override def apply(id: UUID, snapshot: InvoiceSnapshot): KeyValue[UUID, InvoiceRecord] =
    KeyValue.pair(id, InvoiceRecord(id, snapshot))
}

class SaveInvoiceRecord(invoicesList: InvoiceList[IO])
  extends ForeachAction[UUID, InvoiceRecord] {

  override def apply(id: UUID, value: InvoiceRecord): Unit =
    invoicesList.save(value).unsafeRunSync()

}
