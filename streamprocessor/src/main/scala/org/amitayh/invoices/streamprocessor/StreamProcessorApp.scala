package org.amitayh.invoices.streamprocessor

import java.util.Properties
import java.util.concurrent.CountDownLatch

import org.amitayh.invoices.common.Config
import org.apache.kafka.streams.KafkaStreams.State
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.log4s.getLogger

trait StreamProcessorApp extends App {

  def appId: String

  def topology: Topology

  private val logger = getLogger

  private val latch = new CountDownLatch(1)

  private val streams: KafkaStreams = {
    val props = new Properties
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId)
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
    props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE)
    new KafkaStreams(topology, props)
  }

  streams.setStateListener((newState: State, oldState: State) => {
    logger.info(s"$oldState -> $newState")
  })

  streams.setUncaughtExceptionHandler((_: Thread, e: Throwable) => {
    logger.error(e)(s"Exception was thrown in stream processor $appId")
    latch.countDown()
  })

  def start(): Unit = {
    logger.info("Starting...")
    streams.start()
    sys.ShutdownHookThread(close())
    latch.await()
  }

  def close(): Unit = {
    logger.info("Shutting down...")
    streams.close()
  }

  start()

}
