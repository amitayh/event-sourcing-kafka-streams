package org.amitayh.invoices.projection

import java.util.Collections.singletonList
import java.util.Properties

import org.amitayh.invoices.Config
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.Deserializer

class Consumer[K, V](groupId: String,
                     topic: String,
                     keyDeserializer: Deserializer[K],
                     valueDeserializer: Deserializer[V]) {

  private val props = new Properties
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
  props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  private lazy val consumer: KafkaConsumer[K, V] =
    new KafkaConsumer[K, V](props, keyDeserializer, valueDeserializer)

  def start(update: (K, V) => Unit): Unit = {
    consumer.subscribe(singletonList(Config.InvoicesTopic))
    while (true) {
      val records = consumer.poll(1000)
      records.forEach { record =>
        update(record.key(), record.value())
      }
    }
  }

  def close(): Unit = consumer.close()

}
