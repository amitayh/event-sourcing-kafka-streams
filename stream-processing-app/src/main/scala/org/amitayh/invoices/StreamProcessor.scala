package org.amitayh.invoices

import java.util.Properties
import java.util.concurrent.CountDownLatch

import org.apache.kafka.streams.processor.WallclockTimestampExtractor
import org.apache.kafka.streams.state.{QueryableStoreType, QueryableStoreTypes, ReadOnlyKeyValueStore}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}

trait StreamProcessor {

  def topology: Topology

  def appId: String

  private val latch = new CountDownLatch(1)

  private val streams: KafkaStreams = {
    val props = new Properties
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId)
    props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE)
    props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, classOf[WallclockTimestampExtractor])
    new KafkaStreams(topology, props)
  }

  streams.setUncaughtExceptionHandler((_: Thread, e: Throwable) => {
    e.printStackTrace()
    latch.countDown()
  })

  def getStore[K, V](storeName: String): ReadOnlyKeyValueStore[K, V] = {
    val storeType: QueryableStoreType[ReadOnlyKeyValueStore[K, V]] =
      QueryableStoreTypes.keyValueStore()

    streams.store(storeName, storeType)
  }

  def start(): Unit = try {
    println("Starting...")
    streams.start()
    println("Started.")
    sys.ShutdownHookThread(close())
    latch.await()
  } finally {
    close()
  }

  def close(): Unit = {
    println("Shutting down...")
    streams.close()
  }

}
