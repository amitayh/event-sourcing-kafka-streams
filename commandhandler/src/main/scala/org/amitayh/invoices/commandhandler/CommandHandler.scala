package org.amitayh.invoices.commandhandler

import java.lang.{Iterable => JIterable}
import java.util.Collections.{emptyList, singletonList}

import org.amitayh.invoices.common.Config
import org.amitayh.invoices.common.domain.{CommandResult, Event, InvoiceSnapshot}
import org.amitayh.invoices.common.serde.AvroSerde.{CommandResultSerde, CommandSerde, EventSerde, SnapshotSerde}
import org.amitayh.invoices.common.serde.UuidSerde
import org.amitayh.invoices.streamprocessor.StreamProcessorApp
import org.apache.kafka.streams.kstream.{Consumed, Produced, ValueMapper}
import org.apache.kafka.streams.state.Stores
import org.apache.kafka.streams.{StreamsBuilder, Topology}

import scala.collection.JavaConverters._

object CommandHandler extends StreamProcessorApp {

  override def appId: String = "invoices.processor.command-handler"

  override def topology: Topology = {
    val builder = new StreamsBuilder

    builder.addStateStore(
      Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(Config.Stores.Snapshots),
        UuidSerde,
        SnapshotSerde))

    val commands = builder.stream(
      Config.Topics.Commands.name,
      Consumed.`with`(UuidSerde, CommandSerde))

    val results = commands.transform(
      CommandToResultTransformer.Supplier,
      Config.Stores.Snapshots)

    val successfulResults = results.flatMapValues[CommandResult.Success](ToSuccessful)
    val snapshots = successfulResults.mapValues[InvoiceSnapshot](ToSnapshots)
    val events = successfulResults.flatMapValues[Event](ToEvents)

    results.to(
      Config.Topics.CommandResults.name,
      Produced.`with`(UuidSerde, CommandResultSerde))

    snapshots.to(
      Config.Topics.Snapshots.name,
      Produced.`with`(UuidSerde, SnapshotSerde))

    events.to(
      Config.Topics.Events.name,
      Produced.`with`(UuidSerde, EventSerde))

    builder.build()
  }

}

object ToSuccessful extends ValueMapper[CommandResult, JIterable[CommandResult.Success]] {
  override def apply(result: CommandResult): JIterable[CommandResult.Success] = result match {
    case CommandResult(_, _, success: CommandResult.Success) => singletonList(success)
    case _ => emptyList[CommandResult.Success]
  }
}

object ToSnapshots extends ValueMapper[CommandResult.Success, InvoiceSnapshot] {
  override def apply(result: CommandResult.Success): InvoiceSnapshot = result.newSnapshot
}

object ToEvents extends ValueMapper[CommandResult.Success, JIterable[Event]] {
  override def apply(result: CommandResult.Success): JIterable[Event] = result.events.asJava
}
