package org.amitayh.invoices

import java.util.Collections.singletonList
import java.util.Properties

import com.github.takezoe.scala.jdbc._
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, NewTopic}

import scala.util.Try

object Cleanup extends App {

  val admin = {
    val props = new Properties
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
    AdminClient.create(props)
  }


  Config.Topics.foreach { topic =>
    println(s"Deleting topic $topic...")
    val deleteResult = Try(admin.deleteTopics(singletonList(topic)).all().get())
    println(deleteResult)
    println("-")

    Thread.sleep(100)

    println(s"Creating topic $topic...")
    val newTopic = new NewTopic(topic, 4, 1)
    val createResult = Try(admin.createTopics(singletonList(newTopic)).all().get())
    println(createResult)
    println("-")
  }

  println("Truncating tables...")
  val db = ProjectorTopology.connect()
  db.update(sql"TRUNCATE TABLE invoices")

  admin.close()
  db.close()

}
