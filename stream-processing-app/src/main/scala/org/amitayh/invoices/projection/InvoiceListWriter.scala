package org.amitayh.invoices.projection

import java.util.UUID

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue

import scala.collection.JavaConverters._

class InvoiceListWriter(db: AmazonDynamoDB) {

  def update(id: UUID, record: InvoiceRecord): Unit = {
    db.putItem("invoices", Map(
      "tenant_id" -> new AttributeValue("1"),
      "invoice_id" -> new AttributeValue(id.toString),
      "customer_name" -> new AttributeValue(record.customerName),
      "customer_email" -> new AttributeValue(record.customerEmail),
      "invoice_issue_date" -> new AttributeValue(record.issueDate),
      "invoice_due_date" -> new AttributeValue(record.dueDate),
      "invoice_status" -> new AttributeValue(record.status),
      "invoice_total" -> new AttributeValue().withN(record.total.toString)
    ).asJava)
  }

}
