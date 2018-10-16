package org.amitayh.invoices.projector

import cats.effect.IO
import com.pusher.rest
import com.pusher.rest.data.Result

trait Pusher[F[_]] {
  def trigger[T](channel: String, eventName: String, data: T): F[Result]
}

object Pusher {
  def apply[F[_]](implicit F: Pusher[F]): Pusher[F] = F

  implicit val pusherIO: Pusher[IO] = new Pusher[IO] {
    private val pusher: rest.Pusher = {
      val appId = sys.env("PUSHER_APP_ID")
      val key = sys.env("PUSHER_KEY")
      val secret = sys.env("PUSHER_SECRET")
      val pusher = new rest.Pusher(appId, key, secret)
      pusher.setEncrypted(true)
      pusher.setCluster("eu")
      pusher
    }

    override def trigger[T](channel: String, eventName: String, data: T): IO[Result] =
      IO(pusher.trigger(channel, eventName, data))
  }
}
