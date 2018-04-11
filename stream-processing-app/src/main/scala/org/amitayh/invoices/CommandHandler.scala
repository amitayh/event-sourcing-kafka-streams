package org.amitayh.invoices

import java.util.concurrent.CountDownLatch
import java.util.{Collections, Properties, UUID}

import org.amitayh.invoices.JsonSerde._
import org.amitayh.invoices.domain._
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.processor.WallclockTimestampExtractor
import org.apache.kafka.streams.state._

import scala.collection.JavaConverters._

object CommandHandler extends App {

  val latch = new CountDownLatch(1)
  val builder = new StreamsBuilder
  val reducer = new SnapshotReducer(InvoiceReducer)
  val snapshots: KTable[UUID, Snapshot[Invoice]] = snapshotsTable(builder)
  val commands: KStream[UUID, EventSourcedCommand] = commandsStream(builder)
  val results: KStream[UUID, CommandExecutionResult] = executeCommands(commands, snapshots)
  val events: KStream[UUID, InvoiceEvent] = eventsStream(results)

  snapshots.toStream.to(Config.SnapshotsTopic, Produced.`with`(UuidSerde, SnapshotSerde))
  events.to(Config.EventsTopic, Produced.`with`(UuidSerde, EventSerde))
  results.to(Config.CommandResultTopic, Produced.`with`(UuidSerde, CommandResultSerde))

  val streams: KafkaStreams = {
    val props = new Properties
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, Config.CommandsGroupId)
    props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE)
    props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, classOf[WallclockTimestampExtractor])
    val s = new KafkaStreams(builder.build, props)
    s.setUncaughtExceptionHandler((_: Thread, e: Throwable) => {
      e.printStackTrace()
      latch.countDown()
    })
    s
  }
  start()

  def start(): Unit = try {
    println("Starting...")
    streams.start()
    println("Started.")
    sys.ShutdownHookThread(close())
    latch.await()
  } finally {
    close()
  }

  def close(): Unit = {
    println("Shutting down...")
    streams.close()
  }

  def executeCommands(commands: KStream[UUID, EventSourcedCommand],
                      snapshots: KTable[UUID, Snapshot[Invoice]]): KStream[UUID, CommandExecutionResult] = {
    val storeName = snapshots.queryableStoreName()
    val storeType: QueryableStoreType[ReadOnlyKeyValueStore[UUID, Snapshot[Invoice]]] =
      QueryableStoreTypes.keyValueStore()

    commands.map { (id: UUID, command: EventSourcedCommand) =>
      val store = streams.store(storeName, storeType)
      val snapshot = Option(store.get(id)).getOrElse(reducer.initializer())
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
