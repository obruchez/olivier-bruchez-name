package actors

import akka.actor.{Actor, ActorRef}
import play.api.Logger
import scala.util._
import util.Fetchable

sealed trait FetcherMessage
case object Fetch extends FetcherMessage

class Fetcher(cache: ActorRef, fetchable: Fetchable) extends Actor {
  import context._

  def receive = {
    case Fetch =>
      Logger.trace(s"Fetching ${fetchable.name}...")

      fetch(fetchable)

      system.scheduler.scheduleOnce(fetchable.fetchPeriod, self, Fetch)
  }

  private def fetch(fetchable: Fetchable): Unit = {
    fetchable.fetch() match {
      case Success(cacheable) =>
        cache ! SetCache(fetchable, cacheable)
        cacheable.subFetchables.foreach(fetch)
      case Failure(throwable) =>
        Logger.error(s"Could not fetch ${fetchable.name}", throwable)
    }
  }
}
