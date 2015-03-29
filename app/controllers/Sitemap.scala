package controllers

import java.net.URL
import play.api.mvc._
import util.Configuration

case class Page(name: String, url: String, sourceUrl: Option[URL] = None, children: Seq[Page] = Seq())

object Page {
  def apply(name: String, call: Call, sourceUrlPath: String): Page =
    Page(name, call.url, sourceUrl = Configuration.url(sourceUrlPath))

  def apply(name: String, call: Call): Page =
    Page(name, call.url, children = Seq())

  def apply(name: String, call: Call, children: Seq[Page]): Page =
    Page(name, call.url, children = children)
}

object Sitemap {
  val home = Page("Home", routes.Application.home())

  val profile = Page("About / profile", routes.Application.profile(), "url.profile")

  val books = Page("Books", routes.Application.books(), "url.books")
  val concerts = Page("Concerts", routes.Application.concerts(), "url.concerts")
  val crashes = Page("Crashes", routes.Application.crashes(), "url.crashes")
  val exhibitions = Page("Exhibitions", routes.Application.exhibitions(), "url.exhibitions")
  val hikes = Page("Hikes", routes.Application.hikes(), "url.hikes")
  val movies = Page("Movies", routes.Application.movies(), "url.movies")
  val plays = Page("Plays", routes.Application.plays(), "url.plays")
  val trips = Page("Trips", routes.Application.trips(), "url.trips")

  val lists = Page(
    "Lists / lifelogging",
    routes.Application.lists(),
    children = Seq(books, concerts, crashes, exhibitions, hikes, movies, plays, trips))


  val booksToRead = Page("Books to read", routes.Application.booksToRead(), "url.bookstoread")
  val moviesToWatch = Page("Movies to watch", routes.Application.moviesToWatch(), "url.moviestowatch")
  val seenOnTv = Page("Seen on TV", routes.Application.seenOnTv(), "url.seenontv")
  val tripsToTake = Page("Trips to take", routes.Application.tripsToTake(), "url.tripstotake")

  val coursera = Page("Coursera", routes.Application.coursera(), "url.coursera")
  val lifePrinciples = Page("Life principles", routes.Application.lifePrinciples(), "url.lifeprinciples")
  val votes = Page("Votes", routes.Application.votes(), "url.votes")
  val worldview = Page("Worldview", routes.Application.worldview(), "url.worldview")

  // url.cv.html
  // url.cv.pdf
  // url.cv.word

  val externalLinks = Page(
    "External links",
    url = "",
    children = Seq(
      Page("Blogger", "https://bruchez.blogspot.com/"),
      Page("Facebook", "https://www.facebook.com/obruchez"),
      Page("Flickr", "https://secure.flickr.com/photos/bruchez/sets"),
      Page("Github", "https://github.com/obruchez"),
      Page("KeithJarrett.org", "http://www.keithjarrett.org/"),
      Page("Last.fm", "http://www.last.fm/user/obruchez"),
      Page("LinkedIn", "https://www.linkedin.com/in/obruchez"),
      Page("Twitter", "https://twitter.com/obruchez"),
      Page("YouTube", "https://www.youtube.com/user/obruchez")))

  val pages = Seq(home, profile, worldview, lists, externalLinks)
}
