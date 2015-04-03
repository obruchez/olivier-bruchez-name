package actors

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import models._
import play.api.Logger
import scala.collection.mutable
import scala.concurrent._
import scala.concurrent.duration._

sealed trait CacheMessage
case class GetCache[T <: Cacheable](fetchable: Fetchable[T]) extends CacheMessage
case class SetCache[T <: Cacheable](fetchable: Fetchable[T], cacheable: T) extends CacheMessage

sealed trait CacheResultMessage
case class CacheResult[T <: Cacheable](cacheable: T)

class Cache extends Actor {
  private val cachedValues = mutable.Map[Fetchable[_], Cacheable]()
  private val subscribers = mutable.Map[Fetchable[_], Seq[ActorRef]]().withDefaultValue(Seq())

  def receive = {
    case GetCache(fetchable) =>
      cachedValues.get(fetchable) match {
        case Some(cacheable) =>
          sender ! CacheResult(cacheable)
        case None =>
          subscribers(fetchable) = subscribers(fetchable) :+ sender
      }

    case SetCache(fetchable, cacheable) =>
      Logger.trace(s"Caching ${fetchable.name} (${cacheable.size})...")

      cachedValues(fetchable) = cacheable
      subscribers(fetchable).foreach(_ ! CacheResult(cacheable))
      subscribers(fetchable) = Seq()
  }
}

object Cache {
  private implicit val timeout = Timeout(30.seconds)
  private implicit val dispatcher = Master.system.dispatcher

  def books: Future[Books] =
    ask(Master.cache, GetCache(Books)).mapTo[CacheResult[Books]].map(_.cacheable)
}
