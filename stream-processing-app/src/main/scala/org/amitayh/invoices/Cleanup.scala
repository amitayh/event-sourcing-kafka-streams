package org.amitayh.invoices

import java.util.Properties

import org.amitayh.invoices.Config.Topics
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig}

import scala.collection.JavaConverters._
import scala.util.Try

object Cleanup extends App {

  val admin = {
    val props = new Properties
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
    AdminClient.create(props)
  }

  println(s"Deleting topics...")
  val topicNames = Topics.map(_.name())
  val deleteResult = Try(admin.deleteTopics(topicNames.asJava).all().get())
  println(deleteResult)
  println("-")

  Thread.sleep(100)

  println(s"Creating topics...")
  val createResult = Try(admin.createTopics(Topics.asJava).all().get())
  println(createResult)
  println("-")

  admin.close()

}
