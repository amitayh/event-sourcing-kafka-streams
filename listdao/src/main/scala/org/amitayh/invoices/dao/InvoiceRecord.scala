package org.amitayh.invoices.dao

import java.util.UUID

import org.amitayh.invoices.common.domain.InvoiceSnapshot

case class InvoiceRecord(id: String,
                         version: Int,
                         updatedAt: String,
                         customerName: String,
                         customerEmail: String,
                         issueDate: String,
                         dueDate: String,
                         total: Double,
                         status: String)

object InvoiceRecord {
  def apply(id: UUID, snapshot: InvoiceSnapshot): InvoiceRecord = {
    val invoice = snapshot.invoice
    val customer = invoice.customer
    InvoiceRecord(
      id = id.toString,
      version = snapshot.version,
      updatedAt = snapshot.timestamp.toString,
      customerName = customer.name,
      customerEmail = customer.email,
      issueDate = invoice.issueDate.toString,
      dueDate = invoice.dueDate.toString,
      total = invoice.total.toDouble,
      status = invoice.status.toString)
  }
}
