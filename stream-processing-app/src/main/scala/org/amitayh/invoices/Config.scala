package org.amitayh.invoices

object Config {
  val CommandsTopic = "invoice-commands"
  val EventsTopic = "invoice-events"
  val SnapshotsTopic = "invoice-snapshots"
  val RecordsTopic = "invoice-records"
  val CommandResultTopic = "invoice-command-results"

  val Topics = Set(
    CommandsTopic,
    EventsTopic,
    SnapshotsTopic,
    SnapshotsTopic,
    CommandResultTopic)

  val SnapshotsStore = "snapshots-store"
  val CommandsGroupId = "invoice-commands-processor"
  val ProjectorGroupId = "invoice-projections"
  val BootstrapServers = "localhost:9092"
}
