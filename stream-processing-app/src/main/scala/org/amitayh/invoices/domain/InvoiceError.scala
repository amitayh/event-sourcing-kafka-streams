package org.amitayh.invoices.domain

import java.util.UUID

sealed trait InvoiceError {
  def message: String
}

case class VersionMismatch(actual: Int, expected: Option[Int]) extends InvoiceError {
  override def message: String = s"Version mismatch - expected $expected, actually $actual"
}

case class LineItemDoesNotExist(lineItemId: UUID) extends InvoiceError {
  override def message: String = s"Line item with ID $lineItemId does not exist"
}
