package org.amitayh.invoices.dao

import cats.Monad
import cats.effect.{Async, ContextShift, Resource}
import cats.syntax.functor._
import doobie.free.connection.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor

class MySqlInvoiceList[F[_]: Monad](transactor: Transactor[F]) extends InvoiceList[F] {
  override def save(record: InvoiceRecord): F[Unit] =
    MySqlInvoiceList.save(record).transact(transactor)

  override def get: F[List[InvoiceRecord]] =
    MySqlInvoiceList.get.transact(transactor)
}

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

  def get: ConnectionIO[List[InvoiceRecord]] = {
    val sql = sql"""
      SELECT id, version, updated_at, customer_name, customer_email, issue_date, due_date, total, status
      FROM invoices
      WHERE status IN ('New', 'Paid')
      ORDER BY created_at DESC
    """
    sql.query[InvoiceRecord].to[List]
  }

  def resource[F[_]: Async: ContextShift]: Resource[F, MySqlInvoiceList[F]] = for {
    connectEC <- ExecutionContexts.fixedThreadPool[F](32)
    transactEC <- ExecutionContexts.cachedThreadPool[F]
    transactor <- HikariTransactor.newHikariTransactor[F](
      driverClassName = sys.env("DB_DRIVER"),
      url = sys.env("DB_URL"),
      user = sys.env("DB_USER"),
      pass = sys.env("DB_PASS"),
      connectEC = connectEC,
      transactEC = transactEC)
  } yield new MySqlInvoiceList[F](transactor)
}
