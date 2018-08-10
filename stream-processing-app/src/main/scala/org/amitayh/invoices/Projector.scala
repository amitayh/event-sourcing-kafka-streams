package org.amitayh.invoices

import java.util.UUID

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.pusher.rest.Pusher
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.amitayh.invoices.JsonSerde._
import org.amitayh.invoices.domain._
import org.amitayh.invoices.projection.{InvoiceListWriter, InvoiceRecord}
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream._

object Projector extends App with StreamProcessor {

  override def appId: String = Config.ProjectorGroupId

  override def topology: Topology = {
    val builder = new StreamsBuilder
    val snapshots: KStream[UUID, Snapshot[Invoice]] = snapshotStream(builder)
    val records: KStream[UUID, InvoiceRecord] = recordStream(snapshots)

    val writer = creatWriter
    records.foreach { (id: UUID, record: InvoiceRecord) =>
      writer.update(id, record)
    }

//    val pusher = createPusher
//    val commandEncoder: Encoder[InvoiceCommand] = deriveEncoder
//    records.foreach { (id: UUID, record: InvoiceRecord) =>
//      RecordSerde.serializer().serialize()
//      pusher.trigger(Config.RecordsTopic, "updated", "")
//    }

    records.to(Config.RecordsTopic, Produced.`with`(UuidSerde, RecordSerde))

    builder.build()
  }

  start()

  def snapshotStream(builder: StreamsBuilder): KStream[UUID, Snapshot[Invoice]] = {
    builder.stream(Config.SnapshotsTopic, Consumed.`with`(UuidSerde, SnapshotSerde))
  }

  def recordStream(snapshots: KStream[UUID, Snapshot[Invoice]]): KStream[UUID, InvoiceRecord] = {
    val mapper: ValueMapper[Snapshot[Invoice], InvoiceRecord] = {
      case Snapshot(invoice, _) => InvoiceRecord(invoice)
    }
    snapshots.mapValues(mapper)
  }

  def creatWriter: InvoiceListWriter = {
    val db = AmazonDynamoDBClientBuilder.defaultClient()
    new InvoiceListWriter(db)
  }

  def createPusher: Pusher = {
    val appId = sys.env("PUSHER_APP_ID")
    val key = sys.env("PUSHER_KEY")
    val secret = sys.env("PUSHER_SECRET")
    val pusher = new Pusher(appId, key, secret)
    pusher.setCluster("eu")
    pusher.setEncrypted(true)
    pusher
  }

}
