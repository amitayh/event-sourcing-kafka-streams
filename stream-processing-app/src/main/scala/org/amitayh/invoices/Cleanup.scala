package org.amitayh.invoices

import java.util.Properties

import com.github.takezoe.scala.jdbc._
import org.amitayh.invoices.projection.ProjectionWriter
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, NewTopic}

import scala.collection.JavaConverters._
import scala.util.Try

object Cleanup extends App {

  val admin = {
    val props = new Properties
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
    AdminClient.create(props)
  }

  println("Deleting topics...")
  Try(admin.deleteTopics(Config.Topics.asJava).all().get())

  println("Creating topics...")
  Try(admin.createTopics(Config.Topics.map(topic => new NewTopic(topic, 4, 1)).asJava).all().get())

  println("Truncating tables...")
  val db = ProjectionWriter.connect()
  db.update(sql"TRUNCATE TABLE invoices")

  admin.close()
  db.close()

}
