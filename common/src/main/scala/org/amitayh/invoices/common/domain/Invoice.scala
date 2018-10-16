package org.amitayh.invoices.common.domain

import java.time.LocalDate

case class Invoice(customer: Customer,
                   issueDate: LocalDate,
                   dueDate: LocalDate,
                   lineItems: Vector[LineItem],
                   status: InvoiceStatus,
                   paid: BigDecimal) {

  def setCustomer(name: String, email: String): Invoice =
    copy(customer = Customer(name, email))

  def setDates(newIssueDate: LocalDate, newDueDate: LocalDate): Invoice =
    copy(issueDate = newIssueDate, dueDate = newDueDate)

  def addLineItem(description: String,
                  quantity: BigDecimal,
                  price: BigDecimal): Invoice = {
    val lineItem = LineItem(description, quantity, price)
    copy(lineItems = lineItems :+ lineItem)
  }

  def removeLineItem(index: Int): Invoice = {
    val before = lineItems.take(index)
    val after = lineItems.drop(index + 1)
    copy(lineItems = before ++ after)
  }

  def pay(amount: BigDecimal): Invoice = {
    val newStatus = if (amount == balance) InvoiceStatus.Paid else status
    copy(paid = paid + amount, status = newStatus)
  }

  def delete: Invoice =
    copy(status = InvoiceStatus.Deleted)

  def hasLineItem(index: Int): Boolean =
    lineItems.indices contains index

  def total: BigDecimal =
    lineItems.foldLeft[BigDecimal](0)(_ + _.total)

  def balance: BigDecimal = total - paid

}

object Invoice {
  val Draft = Invoice(
    customer = Customer.Empty,
    issueDate = LocalDate.MIN,
    dueDate = LocalDate.MAX,
    lineItems = Vector.empty,
    status = InvoiceStatus.New,
    paid = 0)
}

case class Customer(name: String, email: String)

object Customer {
  val Empty = Customer("", "")
}

case class LineItem(description: String,
                    quantity: BigDecimal,
                    price: BigDecimal) {
  def total: BigDecimal = quantity * price
}

sealed trait InvoiceStatus
object InvoiceStatus {
  case object New extends InvoiceStatus
  case object Paid extends InvoiceStatus
  case object Deleted extends InvoiceStatus
}
