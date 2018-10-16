package org.amitayh.invoices.dao

import cats.syntax.functor._
import doobie.free.connection.ConnectionIO
import doobie.implicits._

object MySqlInvoiceList {
  def save(record: InvoiceRecord): ConnectionIO[Unit] = {
    import record._
    val sql = sql"""
      INSERT INTO invoices (id, version, updated_at, customer_name, customer_email, issue_date, due_date, total, status)
      VALUES ($id, $version, $updatedAt, $customerName, $customerEmail, $issueDate, $dueDate, $total, $status)
      ON DUPLICATE KEY UPDATE
        version = VALUES(version),
        updated_at = VALUES(updated_at),
        customer_name = VALUES(customer_name),
        customer_email = VALUES(customer_email),
        issue_date = VALUES(issue_date),
        due_date = VALUES(due_date),
        total = VALUES(total),
        status = VALUES(status)
    """
    sql.update.run.void
  }

  val get: ConnectionIO[List[InvoiceRecord]] = {
    val sql = sql"""
      SELECT id, version, updated_at, customer_name, customer_email, issue_date, due_date, total, status
      FROM invoices
      WHERE status != 'Deleted'
      ORDER BY created_at DESC
    """
    sql.query[InvoiceRecord].to[List]
  }
}
