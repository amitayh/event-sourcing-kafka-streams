package org.amitayh.invoices.common

import scala.concurrent.duration._

object Config {
  val BootstrapServers = sys.env("BOOTSTRAP_SERVERS")

  object Stores {
    val Snapshots = "invoices.store.snapshots"
  }

  object Topics {
    sealed trait CleanupPolicy
    object CleanupPolicy {
      case object Compact extends CleanupPolicy
    }

    case class Topic(name: String,
                     numPartitions: Int = 4,
                     replicationFactor: Short = 1,
                     retention: Option[Duration] = None,
                     cleanupPolicy: Option[CleanupPolicy] = None)

    val Events = Topic("invoices.topic.events", retention = Some(Duration.Inf))
    val Commands = Topic("invoices.topic.commands", retention = Some(5.minutes))
    val CommandResults = Topic("invoices.topic.command-results", retention = Some(5.minutes))
    val Snapshots = Topic("invoices.topic.snapshots", cleanupPolicy = Some(CleanupPolicy.Compact))

    val All = Set(
      Events,
      Commands,
      CommandResults,
      Snapshots)
  }
}
