package org.amitayh.invoices

import java.util.{Collections, UUID}

import org.amitayh.invoices.JsonSerde._
import org.amitayh.invoices.domain._
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.state._

import scala.collection.JavaConverters._

object CommandHandler extends App with StreamProcessor {

  lazy val reducer = new SnapshotReducer(InvoiceReducer)

  override def appId: String = Config.CommandsGroupId

  override def topology: Topology = {
    val builder = new StreamsBuilder
    val snapshots: KTable[UUID, Snapshot[Invoice]] = snapshotsTable(builder)
    val commands: KStream[UUID, EventSourcedCommand] = commandsStream(builder)
    val results: KStream[UUID, CommandExecutionResult] = executeCommands(commands)
    val events: KStream[UUID, InvoiceEvent] = eventsStream(results)

    snapshots.toStream.to(Config.SnapshotsTopic, Produced.`with`(UuidSerde, SnapshotSerde))
    events.to(Config.EventsTopic, Produced.`with`(UuidSerde, EventSerde))
    results.to(Config.CommandResultTopic, Produced.`with`(UuidSerde, CommandResultSerde))

    builder.build()
  }

  start()

  def executeCommands(commands: KStream[UUID, EventSourcedCommand]): KStream[UUID, CommandExecutionResult] = {
    commands.map { (id: UUID, command: EventSourcedCommand) =>
      val snapshot = loadSnapshot(id)
      KeyValue.pair(id, command(snapshot))
    }
  }

  def loadSnapshot(id: UUID): Snapshot[Invoice] = {
    val store = getStore[UUID, Snapshot[Invoice]](Config.SnapshotsStore)
    Option(store.get(id)).getOrElse(reducer.initializer())
  }

  def snapshotsTable(builder: StreamsBuilder): KTable[UUID, Snapshot[Invoice]] = {
    val materialized: Materialized[UUID, Snapshot[Invoice], KeyValueStore[Bytes, Array[Byte]]] =
      Materialized
        .as(Config.SnapshotsStore)
        .withKeySerde(UuidSerde)
        .withValueSerde(SnapshotSerde)

    builder
      .stream(Config.EventsTopic, Consumed.`with`(UuidSerde, EventSerde))
      .groupByKey()
      .aggregate(
        reducer.initializer,
        reducer.aggregator,
        materialized)
  }

  def commandsStream(builder: StreamsBuilder): KStream[UUID, EventSourcedCommand] = {
    builder.stream(Config.CommandsTopic, Consumed.`with`(UuidSerde, EventSourcedCommandSerde))
  }

  def eventsStream(results: KStream[UUID, CommandExecutionResult]): KStream[UUID, InvoiceEvent] = {
    val valueMapper: ValueMapper[CommandExecutionResult, java.lang.Iterable[InvoiceEvent]] = {
      case CommandSucceeded(_, invoiceEvents) => invoiceEvents.asJava
      case _ => Collections.emptyList()
    }

    results.flatMapValues[InvoiceEvent](valueMapper)
  }

}
