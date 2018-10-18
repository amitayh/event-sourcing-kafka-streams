package org.amitayh.invoices.common

import java.util.Properties

import org.amitayh.invoices.common.Config.Topics
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig}
import org.log4s.getLogger

import scala.collection.JavaConverters._
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
  val createTopic = Topics.All.map(_.toNewTopic)
  val createResult = Try(admin.createTopics(createTopic.asJava).all().get())
  logger.info(createResult.toString)

  admin.close()

}
