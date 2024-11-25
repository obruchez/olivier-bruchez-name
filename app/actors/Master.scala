package actors

import controllers.Sitemap
import org.apache.pekko.actor._
import org.apache.pekko.routing.RoundRobinPool
import org.joda.time.DateTime
import play.api.Logging

import scala.concurrent.Future
import scala.concurrent.duration._

sealed trait MasterMessage
case class CheckCache(force: Boolean, reschedule: Boolean) extends MasterMessage

class Master extends Actor with Logging {
  import context._

  def receive: Receive = { case CheckCache(force, reschedule) =>
    logger.info(s"Checking cache...")

    // Retrieve caching times of all fetchables from Sitemap
    val sequenceOfFutures =
      Sitemap.fetchables.map(fetchable => Cache.cachingTime(fetchable).map(fetchable -> _))

    Future.sequence(sequenceOfFutures) foreach { fetchablesAndCachingTimes =>
      val currentTime = new DateTime()

      // Fetch all fetchables not in cache OR in cache for longer than the maximum cache age
      for {
        (fetchable, cachingTimeOption) <- fetchablesAndCachingTimes
        mustFetch = cachingTimeOption.fold(true) { cachingTime =>
          currentTime.getMillis - cachingTime.getMillis >= fetchable.maximumAge.getMillis
        }
        if mustFetch || force
      } {
        Master.fetcherRouter ! Fetch(fetchable)
      }
    }

    if (reschedule) {
      logger.info(s"Reschedule CheckCache (force = $force)")
      system.scheduler.scheduleOnce(
        Master.CheckPeriod,
        self,
        CheckCache(force = force, reschedule = true)
      )
    } else {
      logger.info(s"Do not reschedule CheckCache (force = $force)")
    }
  }
}

object Master {
  private val CheckPeriod = 60.seconds

  lazy val system: ActorSystem = ActorSystem("System")
  private lazy val master = system.actorOf(Props[Master](), name = "master")
  lazy val cache: ActorRef = system.actorOf(Props[Cache](), name = "cache")
  lazy val fetcherRouter: ActorRef =
    system.actorOf(RoundRobinPool(10).props(Props(new Fetcher(cache))), "fetcher-pool")

  // @todo add hook for call to stop()
  start()

  def start(): Unit = {
    master ! CheckCache(force = false, reschedule = true)
  }

  def stop(): Unit = {
    system.terminate()
  }

  def forceFetch(): Unit = {
    master ! CheckCache(force = true, reschedule = false)
  }
}
