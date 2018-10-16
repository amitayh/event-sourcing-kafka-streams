package org.amitayh.invoices.common.domain

sealed trait InvoiceError {
  def message: String
}

case class VersionMismatch(actual: Int, expected: Option[Int]) extends InvoiceError {
  override def message: String = s"Version mismatch - expected $expected, actually $actual"
}

case class LineItemDoesNotExist(index: Int) extends InvoiceError {
  override def message: String = s"Line item #$index does not exist"
}
