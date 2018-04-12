package org.amitayh.invoices.projection

import java.util.UUID

import com.github.takezoe.scala.jdbc._

class InvoiceListWriter(db: DB) {

  def update(id: UUID, record: InvoiceRecord): Unit = db.transaction {
    val idString = id.toString
    db.update(sql"INSERT OR IGNORE INTO invoices (id) VALUES ($idString)")
    db.update {
      sql"""
        UPDATE invoices
        SET customer_name = ${record.customerName},
          customer_email = ${record.customerEmail},
          issue_date = ${record.issueDate},
          due_date = ${record.dueDate},
          total = ${record.total},
          status = ${record.status}
        WHERE id = $idString
      """
    }
  }

}
