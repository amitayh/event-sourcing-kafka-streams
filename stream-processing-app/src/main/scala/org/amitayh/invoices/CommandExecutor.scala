package org.amitayh.invoices

import java.util.UUID

import org.amitayh.invoices.CommandHandler.reducer
import org.amitayh.invoices.domain.Invoice
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.{Transformer, TransformerSupplier}
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.state.KeyValueStore

class CommandExecutor extends Transformer[UUID, EventSourcedCommand, KeyValue[UUID, CommandExecutionResult]] {

  private var store: KeyValueStore[UUID, Snapshot[Invoice]] = _

  override def init(context: ProcessorContext): Unit = {
    store = context
      .getStateStore(Config.SnapshotsStore)
      .asInstanceOf[KeyValueStore[UUID, Snapshot[Invoice]]]
  }

  override def transform(id: UUID, command: EventSourcedCommand): KeyValue[UUID, CommandExecutionResult] = {
    val snapshot = loadSnapshot(id)
    val result = command(snapshot)
    updateStore(id, snapshot, result)
    KeyValue.pair(id, result)
  }

  override def close(): Unit = ()

  private def loadSnapshot(id: UUID): Snapshot[Invoice] =
    Option(store.get(id)).getOrElse(reducer.initializer())

  private def updateStore(id: UUID,
                          snapshot: Snapshot[Invoice],
                          result: CommandExecutionResult): Unit = result match {
    case CommandSucceeded(_, events) =>
      val updated = events.foldLeft(snapshot) { (acc, event) =>
        reducer.aggregator.apply(id, event, acc)
      }
      store.put(id, updated)

    case _ => ()
  }

}

object CommandExecutor {
  val Supplier: TransformerSupplier[UUID, EventSourcedCommand, KeyValue[UUID, CommandExecutionResult]] =
    () => new CommandExecutor
}
