package org.amitayh.invoices

import java.util.{Collections, UUID}

import org.amitayh.invoices.JsonSerde._
import org.amitayh.invoices.domain.{InvoiceEvent, _}
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.processor.{AbstractProcessor, ProcessorContext, ProcessorSupplier}
import org.apache.kafka.streams.state._

import scala.collection.JavaConverters._

object CommandHandler extends App with StreamProcessor {

  lazy val reducer = new SnapshotReducer(InvoiceReducer)

  val updateSnapshot = new AbstractProcessor[UUID, CommandExecutionResult] {
    private var store: KeyValueStore[UUID, Snapshot[Invoice]] = _

    override def init(context: ProcessorContext): Unit = {
      store = context
        .getStateStore(Config.SnapshotsStore)
        .asInstanceOf[KeyValueStore[UUID, Snapshot[Invoice]]]
    }

    override def process(key: UUID, value: CommandExecutionResult): Unit = {
      println(key, value)
      value match {
        case CommandSucceeded(_, invoiceEvents) =>
          val snapshot = load(key)
          val updated = invoiceEvents.foldLeft(snapshot) { (acc, event) =>
            reducer.aggregator.apply(key, event, acc)
          }
          println("BEFORE", snapshot)
          println("AFTER", updated)
          store.put(key, updated)

        case _ => ()
      }
    }

    override def close(): Unit = store.close()

    def load(id: UUID): Snapshot[Invoice] =
      Option(store.get(id)).getOrElse(reducer.initializer())
  }

  override def appId: String = Config.CommandsGroupId

  override def topology: Topology = {
    val builder = new StreamsBuilder
    val snapshots: KTable[UUID, Snapshot[Invoice]] = snapshotsTable(builder)
    val commands: KStream[UUID, EventSourcedCommand] = commandsStream(builder)
    val results: KStream[UUID, CommandExecutionResult] = executeCommands(commands)
    val events: KStream[UUID, InvoiceEvent] = eventsStream(results)

    val processorSupplier: ProcessorSupplier[UUID, CommandExecutionResult] = () => updateSnapshot
    results.process(processorSupplier, Config.SnapshotsStore)
    results.to(Config.CommandResultTopic, Produced.`with`(UuidSerde, CommandResultSerde))
    snapshots.toStream.to(Config.SnapshotsTopic, Produced.`with`(UuidSerde, SnapshotSerde))
    events.to(Config.EventsTopic, Produced.`with`(UuidSerde, EventSerde))

    builder.build()
  }

  start()

  def executeCommands(commands: KStream[UUID, EventSourcedCommand]): KStream[UUID, CommandExecutionResult] = {
    commands.map { (id: UUID, command: EventSourcedCommand) =>
      val snapshot = updateSnapshot.load(id)
      KeyValue.pair(id, command(snapshot))
    }
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
