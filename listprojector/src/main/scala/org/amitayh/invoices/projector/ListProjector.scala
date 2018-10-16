package org.amitayh.invoices.projector

import java.util.UUID

import org.amitayh.invoices.common.Config
import org.amitayh.invoices.common.serde.AvroSerde.{CommandResultSerde, SnapshotSerde}
import org.amitayh.invoices.common.serde.UuidSerde
import org.amitayh.invoices.dao.InvoiceRecord
import org.amitayh.invoices.streamprocessor.StreamProcessorApp
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.{StreamsBuilder, Topology}

object ListProjector extends StreamProcessorApp {

  override def appId: String = "invoices.processor.list-projector"

  override def topology: Topology = {
    val builder = new StreamsBuilder

    val results = builder.stream(
      Config.Topics.CommandResults.name,
      Consumed.`with`(UuidSerde, CommandResultSerde))

    val snapshots = builder.stream(
      Config.Topics.Snapshots.name,
      Consumed.`with`(UuidSerde, SnapshotSerde))

    val records = snapshots.map[UUID, InvoiceRecord](ToRecord)

    results.foreach(PushCommandResult)
    records.foreach(PushInvoiceUpdates)
    records.foreach(SaveInvoiceRecord)

    builder.build()
  }

}
