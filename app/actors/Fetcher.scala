package actors

import models.Fetchable
import org.apache.pekko.actor.{Actor, ActorRef}
import play.api.Logging

import scala.util._

sealed trait FetcherMessage
case class Fetch(fetchable: Fetchable) extends FetcherMessage

class Fetcher(cache: ActorRef) extends Actor with Logging {
  def receive: Receive = { case Fetch(fetchable) =>
    logger.info(s"Fetch(${fetchable.name})...")

    fetch(fetchable)
  }

  private def fetch(fetchable: Fetchable): Unit = {
    fetchable.fetch() match {
      case Success(cacheable) =>
        cache ! SetCache(fetchable, cacheable)
        cacheable.subFetchables.foreach(Master.fetcherRouter ! Fetch(_))
      case Failure(throwable) =>
        logger.error(s"Could not fetch ${fetchable.name}", throwable)
    }
  }
}
