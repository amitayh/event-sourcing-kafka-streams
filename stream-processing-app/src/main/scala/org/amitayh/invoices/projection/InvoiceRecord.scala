package org.amitayh.invoices.projection

import org.amitayh.invoices.domain.Invoice

case class InvoiceRecord(customerName: String,
                         customerEmail: String,
                         issueDate: String,
                         dueDate: String,
                         total: Double,
                         status: String)

object InvoiceRecord {
  def apply(invoice: Invoice): InvoiceRecord = {
    val customer = invoice.customer
    InvoiceRecord(
      customerName = customer.name,
      customerEmail = customer.email,
      issueDate = invoice.issueDate.toString,
      dueDate = invoice.dueDate.toString,
      total = invoice.total,
      status = invoice.status.toString)
  }
}
