package org.amitayh.invoices

import java.util.UUID

import org.apache.kafka.streams.kstream.{Aggregator, Initializer}

import scala.util.{Failure, Success, Try}

case class Snapshot[T](aggregate: T, version: Int) {
  def validateVersion(expectedVersion: Option[Int]): Try[T] =
    if (expectedVersion.forall(_ == version)) Success(aggregate)
    else Failure(new RuntimeException("Invalid version"))
}

class SnapshotReducer[Aggregate, Event](aggregateReducer: Reducer[Aggregate, Event])
  extends Reducer[Snapshot[Aggregate], Event] {

  override val initializer: Initializer[Snapshot[Aggregate]] = () =>
    Snapshot(aggregateReducer.initializer(), 0)

  override val aggregator: Aggregator[UUID, Event, Snapshot[Aggregate]] =
    (id: UUID, event: Event, snapshot: Snapshot[Aggregate]) =>
      Snapshot(
        aggregate = aggregateReducer.aggregator(id, event, snapshot.aggregate),
        version = snapshot.version + 1)

}
