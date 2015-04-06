package actors

import akka.actor.{ActorRef, ActorSystem, Props}
import models._

object Master {
  lazy val system = ActorSystem("System")
  lazy val cache = system.actorOf(Props[Cache], name = "cache")
  lazy val fetchers = Fetchable.allFetchables map { fetchable =>
    val actorName =
      fetchable.name.toLowerCase.replaceAll("[^a-z]", " ").split(' ').map(_.trim).filter(_.nonEmpty).mkString("-")
    system.actorOf(props = Props(new Fetcher(cache, fetchable)), name = s"$actorName-fetcher")
  }

  def start(): Unit = {
    fetchers.foreach(_ ! Fetch)
  }

  def stop(): Unit = {
    system.shutdown()
  }
}
