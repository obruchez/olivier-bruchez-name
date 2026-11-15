package actors

import models._
import org.apache.pekko.actor._
import org.apache.pekko.pattern._
import org.apache.pekko.util.Timeout
import org.joda.time.DateTime
import play.api.Logging

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

sealed trait CacheMessage
case class GetCache(fetchable: Fetchable) extends CacheMessage
case class SetCache(fetchable: Fetchable, cacheable: Cacheable) extends CacheMessage
case class GetCachingTime(fetchable: Fetchable) extends CacheMessage

sealed trait CacheResultMessage
case class CacheResult(fetchable: Fetchable, cacheable: Cacheable) extends CacheResultMessage
case class CachingTimeResult(fetchable: Fetchable, cachingTime: Option[DateTime])
    extends CacheResultMessage

class Cache extends Actor with Logging {
  private case class CachedValue(cacheable: Cacheable, cachingTime: DateTime)

  private val cachedValues = mutable.Map[Fetchable, CachedValue]()
  private val subscribers = mutable.Map[Fetchable, Seq[ActorRef]]().withDefaultValue(Seq())

  def receive: Receive = {
    case GetCache(fetchable) =>
      logger.debug(s"GetCache(${fetchable.name})")

      cachedValues.get(fetchable) match {
        case Some(cachedValue) =>
          sender() ! CacheResult(fetchable, cachedValue.cacheable)
        case None =>
          subscribers(fetchable) = subscribers(fetchable) :+ sender()
      }

    case SetCache(fetchable, cacheable) =>
      logger.info(s"SetCache(${fetchable.name})")

      cachedValues(fetchable) = CachedValue(cacheable, cachingTime = new DateTime())
      subscribers(fetchable).foreach(_ ! CacheResult(fetchable, cacheable))
      subscribers(fetchable) = Seq()

    case GetCachingTime(fetchable) =>
      logger.debug(s"GetCachingTime(${fetchable.name})")

      sender() ! CachingTimeResult(
        fetchable,
        cachingTime = cachedValues.get(fetchable).map(_.cachingTime)
      )
  }
}

object Cache {
  private given Timeout = Timeout(30.seconds)
  private given ExecutionContext = Master.system.dispatcher

  def fileContent(fileSource: FileSource): Future[FileContent] = get(fileSource)

  // The cache only ever stores, for a given fetchable, what that fetchable's fetch() returned
  def get(fetchable: Fetchable): Future[fetchable.C] =
    ask(Master.cache, GetCache(fetchable))
      .mapTo[CacheResult]
      .map(_.cacheable.asInstanceOf[fetchable.C])

  def cachingTime(fetchable: Fetchable): Future[Option[DateTime]] =
    ask(Master.cache, GetCachingTime(fetchable)).mapTo[CachingTimeResult].map(_.cachingTime)
}
