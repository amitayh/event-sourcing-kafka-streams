package org.amitayh.invoices.common

import java.util.Properties

import org.amitayh.invoices.common.Config.Topics
import org.amitayh.invoices.common.Config.Topics.{CleanupPolicy, Topic}
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, NewTopic}
import org.apache.kafka.common.config.TopicConfig
import org.log4s.getLogger

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.util.Try

object CreateTopics extends App {

  private val logger = getLogger

  val admin: AdminClient = {
    val props = new Properties
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
    AdminClient.create(props)
  }

  logger.info(s"Deleting topics...")
  val topicNames = Topics.All.map(_.name).asJava
  val deleteResult = Try(admin.deleteTopics(topicNames).all().get())
  logger.info(deleteResult.toString)

  Thread.sleep(1000)

  logger.info(s"Creating topics...")
  val createTopic = Topics.All.map(toNewTopic)
  val createResult = Try(admin.createTopics(createTopic.asJava).all().get())
  logger.info(createResult.toString)

  admin.close()

  private def toNewTopic(topic: Topic): NewTopic = {
    val emptyConfigs = Map.empty[String, String]
    val withRetention = topic.retention.foldLeft(emptyConfigs)(_ + toRetentionConfig(_))
    val withCleanup = topic.cleanupPolicy.foldLeft(withRetention)(_ + toCleanupConfig(_))
    new NewTopic(topic.name, topic.numPartitions, topic.replicationFactor)
      .configs(withCleanup.asJava)
  }

  private def toRetentionConfig(retention: Duration): (String, String) = {
    val millis = if (retention.isFinite) retention.toMillis else -1
    TopicConfig.RETENTION_MS_CONFIG -> millis.toString
  }

  private def toCleanupConfig: CleanupPolicy => (String, String) = {
    case CleanupPolicy.Compact =>
      TopicConfig.CLEANUP_POLICY_CONFIG ->
        TopicConfig.CLEANUP_POLICY_COMPACT
  }

}
