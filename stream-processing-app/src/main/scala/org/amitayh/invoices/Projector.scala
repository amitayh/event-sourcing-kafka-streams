package org.amitayh.invoices

import java.sql.DriverManager
import java.util.concurrent.CountDownLatch
import java.util.{Properties, UUID}

import com.github.takezoe.scala.jdbc.DB
import org.amitayh.invoices.JsonSerde._
import org.amitayh.invoices.domain._
import org.amitayh.invoices.projection.{InvoiceListWriter, InvoiceRecord}
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.processor.WallclockTimestampExtractor

object Projector extends App {

  val builder = new StreamsBuilder
  val snapshotStream: KStream[UUID, Snapshot[Invoice]] = ProjectorTopology.snapshotStream(builder)
  val recordStream = ProjectorTopology.recordStream(snapshotStream)

  val db = ProjectorTopology.connect()
  val writer = new InvoiceListWriter(db)
  recordStream.foreach { (id: UUID, record: InvoiceRecord) =>
    writer.update(id, record)
  }

  recordStream.to(Config.RecordsTopic, Produced.`with`(UuidSerde, RecordSerde))

  val streams: KafkaStreams = {
    val props = new Properties
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, Config.ProjectorGroupId)
    props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE)
    props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams")
    props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, classOf[WallclockTimestampExtractor])
    new KafkaStreams(builder.build, props)
  }

  val latch = new CountDownLatch(1)
  streams.setUncaughtExceptionHandler((_: Thread, e: Throwable) => {
    e.printStackTrace()
    latch.countDown()
  })

  def close(): Unit = {
    println("Shutting down...")
    streams.close()
    db.close()
  }

  try {
    println("Starting...")
    streams.start()
    println("Started.")
    sys.ShutdownHookThread(close())
    latch.await()
  } finally {
    close()
  }

}

object ProjectorTopology {
  def connect(): DB = {
    Class.forName("com.mysql.cj.jdbc.Driver")
    val conn = DriverManager.getConnection(
      "jdbc:mysql://localhost:3306/invoices",
      "root",
      "")

    DB(conn)
  }

  def snapshotStream(builder: StreamsBuilder): KStream[UUID, Snapshot[Invoice]] = {
    builder.stream(Config.SnapshotsTopic, Consumed.`with`(UuidSerde, SnapshotSerde))
  }

  def recordStream(snapshots: KStream[UUID, Snapshot[Invoice]]): KStream[UUID, InvoiceRecord] = {
    val mapper: ValueMapper[Snapshot[Invoice], InvoiceRecord] = {
      case Snapshot(invoice, _) => InvoiceRecord(invoice)
    }
    snapshots.mapValues(mapper)
  }
}
