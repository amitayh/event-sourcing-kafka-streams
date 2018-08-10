package org.amitayh.invoices

import java.util.{Collections, UUID}

import org.amitayh.invoices.JsonSerde._
import org.amitayh.invoices.domain.{InvoiceEvent, _}
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.state._

import scala.collection.JavaConverters._

object CommandHandler extends App with StreamProcessor {

  lazy val reducer = new SnapshotReducer(InvoiceReducer)

  override def appId: String = Config.CommandsGroupId

  override def topology: Topology = {
    val builder = new StreamsBuilder

    builder.addStateStore(
      Stores.keyValueStoreBuilder(
        Stores.persistentKeyValueStore(Config.SnapshotsStore),
        UuidSerde,
        SnapshotSerde))

    val commands = commandsStream(builder)
    val results = commands.transform(CommandExecutor.Supplier, Config.SnapshotsStore)
    val events = eventsStream(results)

    results.to(Config.CommandResultTopic, Produced.`with`(UuidSerde, CommandResultSerde))
    events.to(Config.EventsTopic, Produced.`with`(UuidSerde, EventSerde))

    builder.build()
  }

  start()

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
