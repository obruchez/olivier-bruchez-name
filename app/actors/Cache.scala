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
case class GetCache[F <: Fetchable](fetchable: F) extends CacheMessage
case class SetCache[F <: Fetchable](fetchable: F, cacheable: F#C) extends CacheMessage

sealed trait CacheResultMessage
case class CacheResult[T <: Cacheable](cacheable: T)

class Cache extends Actor {
  private val cachedValues = mutable.Map[Fetchable, Cacheable]()
  private val subscribers = mutable.Map[Fetchable, Seq[ActorRef]]().withDefaultValue(Seq())

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

  def books: Future[Books] = get(Books)
  def concerts: Future[Concerts] = get(Concerts)
  def crashes: Future[Crashes] = get(Crashes)
  def exhibitions: Future[Exhibitions] = get(Exhibitions)
  def hikes: Future[Hikes] = get(Hikes)
  def movies: Future[Movies] = get(Movies)
  def plays: Future[Plays] = get(Plays)
  def profile: Future[Profile] = get(Profile)
  def trips: Future[Trips] = get(Trips)
  def worldview: Future[Worldview] = get(Worldview)

  private def get[F <: Fetchable](fetchable: F): Future[F#C] =
    ask(Master.cache, GetCache(fetchable)).mapTo[CacheResult[F#C]].map(_.cacheable)
}
