package org.amitayh.invoices

import java.util.UUID

import org.apache.kafka.streams.kstream.{Aggregator, Initializer}

trait Reducer[State, Event] {
  def initializer: Initializer[State]
  def aggregator: Aggregator[UUID, Event, State]
}
