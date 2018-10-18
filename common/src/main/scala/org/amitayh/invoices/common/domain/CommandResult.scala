package org.amitayh.invoices.common.domain

import java.time.Instant
import java.util.UUID

case class CommandResult(originId: UUID,
                         commandId: UUID,
                         outcome: CommandResult.Outcome)

object CommandResult {
  sealed trait Outcome

  case class Success(events: Vector[Event],
                     oldSnapshot: InvoiceSnapshot,
                     newSnapshot: InvoiceSnapshot) extends Outcome {

    def update(timestamp: Instant,
               commandId: UUID,
               payload: Event.Payload): Success = {
      val event = Event(nextVersion, timestamp, commandId, payload)
      val snapshot = SnapshotReducer.handle(newSnapshot, event)
      copy(events = events :+ event, newSnapshot = snapshot)
    }

    private def nextVersion: Int =
      oldSnapshot.version + events.length + 1

  }

  object Success {
    def apply(snapshot: InvoiceSnapshot): Success =
      Success(Vector.empty, snapshot, snapshot)
  }

  case class Failure(cause: InvoiceError) extends Outcome
}
