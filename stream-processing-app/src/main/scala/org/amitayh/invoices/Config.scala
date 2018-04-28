package org.amitayh.invoices

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions
import scala.concurrent.duration._

object Config {
  val CommandsTopic = "invoice-commands"
  val CommandResultTopic = "invoice-command-results"
  val EventsTopic = "invoice-events"
  val SnapshotsTopic = "invoice-snapshots"
  val RecordsTopic = "invoice-records"

  val Topics = Set(
    topic(CommandsTopic, TopicConfig.RETENTION_MS_CONFIG -> 5.minutes),
    topic(CommandResultTopic, TopicConfig.RETENTION_MS_CONFIG -> 5.minutes),
    topic(EventsTopic, TopicConfig.RETENTION_MS_CONFIG -> "-1"),
    topic(
      SnapshotsTopic,
      TopicConfig.CLEANUP_POLICY_CONFIG -> TopicConfig.CLEANUP_POLICY_COMPACT,
      TopicConfig.RETENTION_MS_CONFIG -> 5.minutes),
    topic(
      RecordsTopic,
      TopicConfig.CLEANUP_POLICY_CONFIG -> TopicConfig.CLEANUP_POLICY_COMPACT,
      TopicConfig.RETENTION_MS_CONFIG -> 5.minutes))

  val SnapshotsStore = "snapshots-store"
  val CommandsGroupId = "invoice-commands-processor"
  val ProjectorGroupId = "invoice-projections"
  val BootstrapServers = "localhost:9092"

  def topic(name: String, configs: (String, String)*): NewTopic =
    new NewTopic(name, 4, 1)
      .configs(configs.toMap.asJava)

  implicit def `duration->string`(duration: FiniteDuration): String =
    duration.toMillis.toString
}
