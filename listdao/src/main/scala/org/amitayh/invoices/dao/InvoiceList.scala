package org.amitayh.invoices.dao

import cats.effect.IO
import com.zaxxer.hikari.HikariDataSource
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.transactor.Transactor

trait InvoiceList[F[_]] {
  def save(record: InvoiceRecord): F[Unit]
  def get: F[List[InvoiceRecord]]
}

object InvoiceList {
  def apply[F[_]](implicit F: InvoiceList[F]): InvoiceList[F] = F

  implicit val invoiceListIO: InvoiceList[IO] = new InvoiceList[IO] {
    private val transactor: Transactor[IO] = {
      val dataSource = new HikariDataSource
      dataSource.setDriverClassName(sys.env("DB_DRIVER"))
      dataSource.setJdbcUrl(sys.env("DB_URL"))
      dataSource.setUsername(sys.env("DB_USER"))
      dataSource.setPassword(sys.env("DB_PASS"))
      HikariTransactor[IO](dataSource)
    }

    override def save(record: InvoiceRecord): IO[Unit] =
      MySqlInvoiceList.save(record).transact(transactor)

    override val get: IO[List[InvoiceRecord]] =
      MySqlInvoiceList.get.transact(transactor)
  }
}
