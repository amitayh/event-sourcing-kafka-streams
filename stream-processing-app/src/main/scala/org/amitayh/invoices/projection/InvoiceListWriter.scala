package org.amitayh.invoices.projection

import java.util.UUID

import com.github.takezoe.scala.jdbc._

class InvoiceListWriter(db: DB) {

  def update(id: UUID, record: InvoiceRecord): Unit = db.update {
    sql"""
      REPLACE INTO invoices (id, customer_name, customer_email, total)
      VALUES (${id.toString}, ${record.customerName}, ${record.customerEmail}, ${record.total})
    """
  }

}
