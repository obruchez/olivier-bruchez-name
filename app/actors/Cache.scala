package actors

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import models._
import org.joda.time.DateTime
import play.api.Logger
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import util._

sealed trait CacheMessage
case class GetCache[F <: Fetchable](fetchable: F) extends CacheMessage
case class SetCache[F <: Fetchable](fetchable: F, cacheable: F#C) extends CacheMessage
case class GetCachingTime[F <: Fetchable](fetchable: F) extends CacheMessage

sealed trait CacheResultMessage
case class CacheResult[F <: Fetchable](fetchable: F, cacheable: F#C) extends CacheResultMessage
case class CachingTimeResult[F <: Fetchable](fetchable: F, cachingTime: Option[DateTime]) extends CacheResultMessage

class Cache extends Actor {
  case class CachedValue(cacheable: Cacheable, cachingTime: DateTime)

  private val cachedValues = mutable.Map[Fetchable, CachedValue]()
  private val subscribers = mutable.Map[Fetchable, Seq[ActorRef]]().withDefaultValue(Seq())

  def receive = {
    case GetCache(fetchable) =>
      Logger.trace(s"GetCache(${fetchable.name})")

      cachedValues.get(fetchable) match {
        case Some(cachedValue) =>
          sender ! CacheResult(fetchable, cachedValue.cacheable.asInstanceOf[fetchable.C])
        case None =>
          subscribers(fetchable) = subscribers(fetchable) :+ sender
      }

    case SetCache(fetchable, cacheable) =>
      Logger.trace(s"SetCache(${fetchable.name})")

      cachedValues(fetchable) = CachedValue(cacheable, cachingTime = new DateTime())
      subscribers(fetchable).foreach(_ ! CacheResult(fetchable, cacheable))
      subscribers(fetchable) = Seq()

    case GetCachingTime(fetchable) =>
      Logger.trace(s"GetCachingTime(${fetchable.name})")

      sender ! CachingTimeResult(fetchable, cachingTime = cachedValues.get(fetchable).map(_.cachingTime))
  }
}

object Cache {
  private implicit val timeout = Timeout(30.seconds)
  private implicit val dispatcher = Master.system.dispatcher

  def books: Future[Books] = get(Books)
  def booksToRead: Future[BooksToRead] = get(BooksToRead)
  def concerts: Future[Concerts] = get(Concerts)
  def courses: Future[Courses] = get(Courses)
  def crashes: Future[Crashes] = get(Crashes)
  def exhibitions: Future[Exhibitions] = get(Exhibitions)
  def hikes: Future[Hikes] = get(Hikes)
  def lifePrinciples: Future[LifePrinciples] = get(LifePrinciples)
  def movies: Future[Movies] = get(Movies)
  def moviesToWatch: Future[MoviesToWatch] = get(MoviesToWatch)
  def plays: Future[Plays] = get(Plays)
  def profile: Future[Profile] = get(Profile)
  def seenOnTv: Future[SeenOnTv] = get(SeenOnTv)
  def trips: Future[Trips] = get(Trips)
  def tripsToTake: Future[TripsToTake] = get(TripsToTake)
  def votes: Future[Votes] = get(Votes)
  def worldview: Future[Worldview] = get(Worldview)

  def fileContent(fileSource: FileSource): Future[FileContent] = get(fileSource)

  def get[F <: Fetchable](fetchable: F): Future[F#C] =
    ask(Master.cache, GetCache(fetchable)).mapTo[CacheResult[F]].map(_.cacheable)

  def cachingTime[F <: Fetchable](fetchable: F): Future[Option[DateTime]] =
    ask(Master.cache, GetCachingTime(fetchable)).mapTo[CachingTimeResult[F]].map(_.cachingTime)
}
