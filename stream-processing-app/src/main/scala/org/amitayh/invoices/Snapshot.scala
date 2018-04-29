package org.amitayh.invoices

import java.util.UUID

import org.amitayh.invoices.domain.{InvoiceError, VersionMismatch}
import org.apache.kafka.streams.kstream.{Aggregator, Initializer}

case class Snapshot[T](aggregate: T, version: Int) {
  def validateVersion(expectedVersion: Option[Int]): Either[InvoiceError, T] =
    if (expectedVersion.forall(_ == version)) Right(aggregate)
    else Left(VersionMismatch(version, expectedVersion))

  def next(nextAggregate: T => T): Snapshot[T] =
    Snapshot(nextAggregate(aggregate), version + 1)
}

object Snapshot {
  val InitialVersion = 0

  def initial[T](aggregate: T): Snapshot[T] =
    Snapshot(aggregate, InitialVersion)
}

class SnapshotReducer[Aggregate, Event](aggregateReducer: Reducer[Aggregate, Event])
  extends Reducer[Snapshot[Aggregate], Event] {

  override val initializer: Initializer[Snapshot[Aggregate]] = () =>
    Snapshot.initial(aggregateReducer.initializer())

  override val aggregator: Aggregator[UUID, Event, Snapshot[Aggregate]] =
    (id: UUID, event: Event, snapshot: Snapshot[Aggregate]) =>
      snapshot.next(aggregateReducer.aggregator(id, event, _))
}
