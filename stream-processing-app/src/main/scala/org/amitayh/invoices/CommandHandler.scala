package org.amitayh.invoices

import java.util.concurrent.CountDownLatch
import java.util.{Collections, Properties, UUID}

import org.amitayh.invoices.JsonSerde._
import org.amitayh.invoices.domain._
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream.{Joined, _}
import org.apache.kafka.streams.processor.WallclockTimestampExtractor
import org.apache.kafka.streams.state._

import scala.collection.JavaConverters._

object CommandHandler extends App {

  val builder = new StreamsBuilder
  val snapshots: KTable[UUID, Snapshot[Invoice]] = CommandHandlerTopology.invoicesTable(builder)
  val commands: KStream[UUID, CommandAndInvoice] = CommandHandlerTopology.commandsStream(builder, snapshots)
  val results: KStream[UUID, CommandExecutionResult] = CommandHandlerTopology.executeCommands(commands)
  val events: KStream[UUID, InvoiceEvent] = CommandHandlerTopology.eventsStream(results)

  commands.foreach { (id: UUID, command: CommandAndInvoice) =>
    println(id, command)
  }

  snapshots.toStream.to(Config.SnapshotsTopic, Produced.`with`(UuidSerde, SnapshotSerde))
  events.to(Config.EventsTopic, Produced.`with`(UuidSerde, EventSerde))
  results.to(Config.CommandResultTopic, Produced.`with`(UuidSerde, CommandResultSerde))

  val streams: KafkaStreams = {
    val props = new Properties
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, Config.CommandsGroupId)
    props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE)
    props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, classOf[WallclockTimestampExtractor])
    new KafkaStreams(builder.build, props)
  }

  val latch = new CountDownLatch(1)
  streams.setUncaughtExceptionHandler((_: Thread, e: Throwable) => {
    e.printStackTrace()
    latch.countDown()
  })

  def close(): Unit = {
    println("Shutting down...")
    streams.close()
  }

  try {
    println("Starting...")
    streams.start()
    println("Started.")
    sys.ShutdownHookThread(close())
    latch.await()
  } finally {
    close()
  }

}

object CommandHandlerTopology {
  def invoicesTable(builder: StreamsBuilder): KTable[UUID, Snapshot[Invoice]] = {
    val snapshotReducer = new SnapshotReducer(InvoiceReducer)

    val materialized: Materialized[UUID, Snapshot[Invoice], KeyValueStore[Bytes, Array[Byte]]] =
      Materialized
        .as(Config.SnapshotsStore)
        .withKeySerde(UuidSerde)
        .withValueSerde(SnapshotSerde)

    builder
      .stream(Config.EventsTopic, Consumed.`with`(UuidSerde, EventSerde))
      .groupByKey()
      .aggregate(
        snapshotReducer.initializer,
        snapshotReducer.aggregator,
        materialized)
  }

  def commandsStream(builder: StreamsBuilder,
                     invoices: KTable[UUID, Snapshot[Invoice]]): KStream[UUID, CommandAndInvoice] = {
    val joiner: ValueJoiner[EventSourcedCommand, Snapshot[Invoice], CommandAndInvoice] =
      (command: EventSourcedCommand, snapshot: Snapshot[Invoice]) =>
        CommandAndInvoice.from(command, snapshot)

    val joined: Joined[UUID, EventSourcedCommand, Snapshot[Invoice]] =
      Joined.`with`(UuidSerde, EventSourcedCommandSerde, SnapshotSerde)

    builder
      .stream(Config.CommandsTopic, Consumed.`with`(UuidSerde, EventSourcedCommandSerde))
      .leftJoin[Snapshot[Invoice], CommandAndInvoice](invoices, joiner, joined)
  }

  def executeCommands(commands: KStream[UUID, CommandAndInvoice]): KStream[UUID, CommandExecutionResult] = {
    commands.map { (id: UUID, command: CommandAndInvoice) =>
      new KeyValue(id, command.execute)
    }
  }

  def eventsStream(results: KStream[UUID, CommandExecutionResult]): KStream[UUID, InvoiceEvent] = {
    val valueMapper: ValueMapper[CommandExecutionResult, java.lang.Iterable[InvoiceEvent]] = {
      case CommandSucceeded(_, invoiceEvents) => invoiceEvents.asJava
      case _ => Collections.emptyList()
    }

    results.flatMapValues[InvoiceEvent](valueMapper)
  }
}
