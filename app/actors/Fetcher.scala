package actors

import akka.actor.{Actor, ActorRef}
import models.Fetchable
import play.api.Logger
import scala.util._

sealed trait FetcherMessage
case class Fetch(fetchable: Fetchable) extends FetcherMessage

class Fetcher(cache: ActorRef) extends Actor {
  def receive = {
    case Fetch(fetchable) =>
      Logger.trace(s"Fetch(${fetchable.name})...")

      fetch(fetchable)
  }

  private def fetch(fetchable: Fetchable): Unit = {
    fetchable.fetch() match {
      case Success(cacheable) =>
        cache ! SetCache(fetchable, cacheable)
        cacheable.subFetchables.foreach(Master.fetcherRouter ! Fetch(_))
      case Failure(throwable) =>
        Logger.error(s"Could not fetch ${fetchable.name}", throwable)
    }
  }
}
