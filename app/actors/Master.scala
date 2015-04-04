package actors

import akka.actor.{ActorRef, ActorSystem, Props}
import models._

object Master {
  lazy val system = ActorSystem("System")
  lazy val cache = system.actorOf(Props[Cache], name = "cache")
  lazy val fetchers = Map(
    Books -> Books.fetcher,
    BooksToRead -> BooksToRead.fetcher,
    Concerts -> Concerts.fetcher,
    Courses -> Courses.fetcher,
    Crashes -> Crashes.fetcher,
    Exhibitions -> Exhibitions.fetcher,
    Hikes -> Hikes.fetcher,
    LifePrinciples -> LifePrinciples.fetcher,
    Movies -> Movies.fetcher,
    MoviesToWatch -> MoviesToWatch.fetcher,
    Plays -> Plays.fetcher,
    Profile -> Profile.fetcher,
    Trips -> Trips.fetcher,
    TripsToTake -> TripsToTake.fetcher,
    Worldview -> Worldview.fetcher)

  def start(): Unit = {
    fetchers.map(_._2).foreach(_ ! Fetch)
  }

  def stop(): Unit = {
    system.shutdown()
  }

  private implicit class FetchableOps(fetchable: Fetchable) {
    def fetcher: ActorRef = system.actorOf(
      props = Props(new Fetcher(cache, fetchable)),
      name = s"fetcher-$actorName")

    def actorName: String =
      fetchable.name.
        toLowerCase.
        replaceAll("[^a-z]", " ").
        split(' ').
        map(_.trim).
        filter(_.nonEmpty).
        mkString("-")
  }
}
