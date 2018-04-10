package org.amitayh.invoices.projection

import java.sql.DriverManager

import com.github.takezoe.scala.jdbc.DB

object ProjectionWriter extends App {

  def connect(): DB = {
    Class.forName("com.mysql.cj.jdbc.Driver")
    val conn = DriverManager.getConnection(
      "jdbc:mysql://localhost:3306/invoices",
      "root",
      "")

    DB(conn)
  }

  val db = connect()
  val writer = new InvoiceListWriter(db)
  val consumer = new InvoicesListConsumer(writer)

  try {
    println("Starting...")
    consumer.start()
  } finally {
    consumer.close()
    db.close()
  }

}
