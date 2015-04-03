package actors

import akka.actor.{Actor, ActorRef}
import models._
import play.api.Logger
import scala.util._

sealed trait FetcherMessage
case object Fetch extends FetcherMessage

class Fetcher[T <: Cacheable](cache: ActorRef, fetchable: Fetchable[T]) extends Actor {
  import context._

  def receive = {
    case Fetch =>
      Logger.trace(s"Fetching ${fetchable.name}...")

      fetchable.fetch() match {
        case Success(cacheable) =>
          cache ! SetCache(fetchable, cacheable)
        case Failure(throwable) =>
          Logger.error(s"Could not fetch ${fetchable.name}", throwable)
      }

      system.scheduler.scheduleOnce(fetchable.fetchPeriod, self, Fetch)
  }
}
