package org.amitayh.invoices.dao

trait InvoiceList[F[_]] {
  def save(record: InvoiceRecord): F[Unit]
  def get: F[List[InvoiceRecord]]
}
