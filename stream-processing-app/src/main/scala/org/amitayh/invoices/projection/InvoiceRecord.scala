package org.amitayh.invoices.projection

import org.amitayh.invoices.domain.Invoice

case class InvoiceRecord(customerName: String,
                         customerEmail: String,
                         total: Double)

object InvoiceRecord {
  def apply(invoice: Invoice): InvoiceRecord = {
    val customer = invoice.customer
    InvoiceRecord(
      customerName = customer.name,
      customerEmail = customer.email,
      total = invoice.total)
  }
}
