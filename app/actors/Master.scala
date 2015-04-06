package actors

import akka.actor.{ActorSystem, Props}
import models._
import util.Slug

object Master {
  lazy val system = ActorSystem("System")
  lazy val cache = system.actorOf(Props[Cache], name = "cache")
  lazy val fetchers = Fetchable.allFetchables map { fetchable =>
    val actorName = Slug.slugFromString(fetchable.name)
    system.actorOf(props = Props(new Fetcher(cache, fetchable)), name = s"$actorName-fetcher")
  }

  def start(): Unit = {
    fetchers.foreach(_ ! Fetch)
  }

  def stop(): Unit = {
    system.shutdown()
  }
}
