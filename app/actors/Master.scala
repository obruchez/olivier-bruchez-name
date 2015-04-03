package actors

import akka.actor.{ActorRef, ActorSystem, Props}
import models._
import scala.concurrent.duration._

object Master {
  lazy val system = ActorSystem("System")
  lazy val cache = system.actorOf(Props[Cache], name = "cache")
  lazy val fetchers = Map(Books -> Books.fetcher)

  def start(): Unit = {
    import system.dispatcher
    fetchers.map(_._2).foreach(fetcher => system.scheduler.scheduleOnce(0.second, fetcher, Fetch))
  }

  def stop(): Unit = {
    system.shutdown()
  }

  private implicit class FetchableOps[T <: Cacheable](fetchable: Fetchable[T]) {
    def fetcher: ActorRef = system.actorOf(Props(new Fetcher(cache, fetchable)), name = s"fetcher-${fetchable.name}")
  }
}
