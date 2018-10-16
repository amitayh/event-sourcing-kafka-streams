package org.amitayh.invoices.common.serde

import java.nio.ByteBuffer
import java.util.UUID

object UuidConverters {
  def toBytes(uuid: UUID): Array[Byte] =
    toByteBuffer(uuid).array

  def toByteBuffer(uuid: UUID): ByteBuffer = {
    val buffer = ByteBuffer.allocate(16)
    buffer.putLong(0, uuid.getMostSignificantBits)
    buffer.putLong(8, uuid.getLeastSignificantBits)
    buffer
  }

  def fromBytes(data: Array[Byte]): UUID =
    fromByteBuffer(ByteBuffer.wrap(data))

  def fromByteBuffer(buffer: ByteBuffer): UUID = {
    val mostSignificantBits = buffer.getLong(0)
    val leastSignificantBits = buffer.getLong(8)
    new UUID(mostSignificantBits, leastSignificantBits)
  }
}
