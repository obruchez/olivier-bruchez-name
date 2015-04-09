package actors

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import models._
import play.api.Logger
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import util._

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
          // @todo ask fetcher to fetch cacheable
          subscribers(fetchable) = subscribers(fetchable) :+ sender
      }

    case SetCache(fetchable, cacheable) =>
      Logger.trace(s"Caching ${fetchable.name}...")

      cachedValues(fetchable) = cacheable
      subscribers(fetchable).foreach(_ ! CacheResult(cacheable))
      subscribers(fetchable) = Seq()
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
    ask(Master.cache, GetCache(fetchable)).mapTo[CacheResult[F#C]].map(_.cacheable)
}
