package org.amitayh.invoices.commandhandler

import java.time.Instant
import java.util.UUID

import org.amitayh.invoices.common.Config
import org.amitayh.invoices.common.domain.{Command, CommandResult, InvoiceSnapshot, SnapshotReducer}
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.{Transformer, TransformerSupplier}
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.state.KeyValueStore

class CommandToResultTransformer
  extends Transformer[UUID, Command, KeyValue[UUID, CommandResult]] {

  private var context: ProcessorContext = _

  private var store: KeyValueStore[UUID, InvoiceSnapshot] = _

  override def init(context: ProcessorContext): Unit = {
    this.context = context
    store = context
      .getStateStore(Config.Stores.Snapshots)
      .asInstanceOf[KeyValueStore[UUID, InvoiceSnapshot]]
  }

  /**
    * By using the invoice ID as the partition key for the commands topic,
    * we get serializability over command handling. This means that no
    * concurrent commands will run for the same invoice, so we can safely
    * handle commands as read-process-write without a race condition. We
    * are still able to scale out by adding more partitions.
    */
  override def transform(id: UUID, command: Command): KeyValue[UUID, CommandResult] = {
    val snapshot = loadSnapshot(id)
    val result = command(timestamp(), snapshot)
    updateSnapshot(id, result.outcome)
    KeyValue.pair(id, result)
  }

  override def close(): Unit = ()

  private def loadSnapshot(id: UUID): InvoiceSnapshot =
    Option(store.get(id)).getOrElse(SnapshotReducer.empty)

  private def timestamp(): Instant =
    Instant.ofEpochMilli(context.timestamp())

  private def updateSnapshot(id: UUID, outcome: CommandResult.Outcome): Unit = outcome match {
    case CommandResult.Success(_, _, snapshot) => store.put(id, snapshot)
    case _ => ()
  }

}

object CommandToResultTransformer {
  val Supplier: TransformerSupplier[UUID, Command, KeyValue[UUID, CommandResult]] =
    () => new CommandToResultTransformer
}
