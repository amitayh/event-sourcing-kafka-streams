package org.amitayh.invoices

object Config {
  val CommandsTopic = "invoice-commands"
  val EventsTopic = "invoice-events"
  val InvoicesTopic = "invoice-states"
  val CommandResultTopic = "invoice-command-results"

  val Topics = Set(CommandsTopic, EventsTopic, InvoicesTopic, CommandResultTopic)

  val InvoicesStore = "invoices-store"
  val CommandsGroupId = "invoice-commands-processor"
  val BootstrapServers = "localhost:9092"
}
