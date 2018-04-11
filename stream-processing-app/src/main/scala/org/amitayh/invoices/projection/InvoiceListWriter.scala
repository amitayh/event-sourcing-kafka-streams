package org.amitayh.invoices.projection

import java.util.UUID

import com.github.takezoe.scala.jdbc._

class InvoiceListWriter(db: DB) {

  def update(id: UUID, record: InvoiceRecord): Unit = db.update {
    sql"""
      REPLACE INTO invoices (id, customer_name, customer_email, issue_date, due_date, total, status)
      VALUES (
        ${id.toString},
        ${record.customerName},
        ${record.customerEmail},
        ${record.issueDate},
        ${record.dueDate},
        ${record.total},
        ${record.status}
      )
    """
  }

}
