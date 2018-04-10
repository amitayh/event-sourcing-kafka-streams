package org.amitayh.invoices

import java.util.concurrent.CountDownLatch
import java.util.{Collections, Properties, UUID}

import org.amitayh.invoices.JsonSerde._
import org.amitayh.invoices.domain._
import org.amitayh.invoices.projection.InvoiceRecord
import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream.{Joined, _}
import org.apache.kafka.streams.processor.WallclockTimestampExtractor
import org.apache.kafka.streams.state._

import scala.collection.JavaConverters._

object InvoicesService extends App {

  val builder = new StreamsBuilder
  val invoicesTable: KTable[UUID, Snapshot[Invoice]] = Topology.invoicesTable(builder)
  val commands: KStream[UUID, CommandAndInvoice] = Topology.commandsStream(builder, invoicesTable)
  val results: KStream[UUID, CommandExecutionResult] = Topology.executeCommands(commands)
  val events: KStream[UUID, InvoiceEvent] = Topology.eventsStream(results)
  val invoicesStream: KStream[UUID, InvoiceRecord] = Topology.invoicesStream(invoicesTable)

  invoicesStream.to(Config.InvoicesTopic, Produced.`with`(UuidSerde, RecordSerde))
  events.to(Config.EventsTopic, Produced.`with`(UuidSerde, EventSerde))
  results.to(Config.CommandResultTopic, Produced.`with`(UuidSerde, CommandResultSerde))

  val streams: KafkaStreams = {
    val props = new Properties
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, Config.CommandsGroupId)
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, classOf[StringSerde])
    props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE)
    props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams")
    props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, classOf[WallclockTimestampExtractor])
    new KafkaStreams(builder.build, props)
  }

  val latch = new CountDownLatch(1)
  streams.setUncaughtExceptionHandler((_: Thread, e: Throwable) => {
    e.printStackTrace()
    latch.countDown()
  })

  try {
    println("Starting...")
    streams.start()
    println("Started.")
    latch.await()
  } finally {
    streams.close()
  }

}

object Topology {
  def invoicesTable(builder: StreamsBuilder): KTable[UUID, Snapshot[Invoice]] = {
    val snapshotReducer = new SnapshotReducer(InvoiceReducer)

    val materialized: Materialized[UUID, Snapshot[Invoice], KeyValueStore[Bytes, Array[Byte]]] =
      Materialized
        .as(Config.InvoicesStore)
        .withKeySerde(UuidSerde)
        .withValueSerde(SnapshotSerde)

    builder.table("foo")

    builder
      .stream(Config.EventsTopic, Consumed.`with`(UuidSerde, EventSerde))
      .groupByKey()
      .aggregate(
        snapshotReducer.initializer,
        snapshotReducer.aggregator,
        materialized)
  }

  def invoicesStream(invoices: KTable[UUID, Snapshot[Invoice]]): KStream[UUID, InvoiceRecord] = {
    val mapper: ValueMapper[Snapshot[Invoice], InvoiceRecord] = {
      case Snapshot(invoice, _) => InvoiceRecord(invoice)
    }
    invoices.toStream.mapValues(mapper)
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
