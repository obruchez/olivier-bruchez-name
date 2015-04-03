package controllers

import java.net.URL
import models._
import play.api.mvc._
import util.Configuration

case class Page(title: String,
                url: String,
                icon: Option[String] = None,
                sourceUrl: Option[URL] = None,
                children: Seq[Page] = Seq())

object Page {
  def apply(fetchable: Fetchable, call: Call, icon: String): Page =
    Page(title = fetchable.name, call.url, icon = Some(icon), sourceUrl = Some(fetchable.sourceUrl))

  def apply(title: String, call: Call, icon: String, sourceUrlPath: String): Page =
    Page(title, call.url, icon = Some(icon), sourceUrl = Configuration.url(sourceUrlPath))

  def apply(title: String, call: Call, icon: String): Page =
    Page(title, call.url, icon = Some(icon), children = Seq())

  def apply(title: String, call: Call, icon: String, children: Seq[Page]): Page =
    Page(title, call.url, icon = Some(icon), children = children)
}

object Sitemap {
  // @todo better icon for hikes: https://cdn2.iconfinder.com/data/icons/vacation-landmarks/512/12-512.png
  // @todo better icon for plays: https://cdn0.iconfinder.com/data/icons/huge-basic-icons-part-3/512/Theater_symbol.png

  val home = Page("Home", routes.Application.home(), "fa-home")

  val profile = Page(Profile, routes.Application.profile(), "fa-user")

  val books = Page(Books, routes.Application.books(), "fa-book")
  val concerts = Page(Concerts, routes.Application.concerts(), "fa-music") // @todo better icon
  val crashes = Page(Crashes, routes.Application.crashes(), "fa-hdd-o") // @todo better icon
  val exhibitions = Page(Exhibitions, routes.Application.exhibitions(), "fa-university") // @todo better icon
  val hikes = Page(Hikes, routes.Application.hikes(), "fa-sun-o") // @todo better icon
  val movies = Page(Movies, routes.Application.movies(), "fa-film")
  val plays = Page(Plays, routes.Application.plays(), "fa-ticket") // @todo better icon
  val trips = Page(Trips, routes.Application.trips(), "fa-suitcase")

  val lifelogging = Page(
    "Lifelogging",
    routes.Application.lifelogging(),
    "fa-list",
    children = Seq(books, concerts, crashes, exhibitions, hikes, movies, plays, trips))

  val booksToRead = Page("Books to read", routes.Application.booksToRead(), "fa-book", "url.bookstoread")
  val moviesToWatch = Page("Movies to watch", routes.Application.moviesToWatch(), "fa-film", "url.moviestowatch")
  val seenOnTv = Page("Seen on TV", routes.Application.seenOnTv(),"fa-desktop", "url.seenontv") // @todo better icon
  val tripsToTake = Page("Trips to take", routes.Application.tripsToTake(), "fa-suitcase", "url.tripstotake")

  val coursera = Page("Coursera", routes.Application.coursera(), "", "url.coursera") // @todo find icon
  val lifePrinciples = Page("Life principles", routes.Application.lifePrinciples(), "", "url.lifeprinciples") // @todo find icon
  val votes = Page("Votes", routes.Application.votes(), "", "url.votes") // @todo find icon
  val worldview = Page(Worldview, routes.Application.worldview(), "fa-globe")

  // url.cv.html
  // url.cv.pdf
  // url.cv.word

  val externalLinks = Page(
    "External links",
    routes.Application.externalLinks(),
    "fa-link",
    children = Seq(
      Page("Blogger", "https://bruchez.blogspot.com/", Some("fa-rss-square")), // @todo better icon
      Page("Facebook", "https://www.facebook.com/obruchez", Some("fa-facebook-square")),
      Page("Flickr", "https://secure.flickr.com/photos/bruchez/sets", Some("fa-flickr")),
      Page("Github", "https://github.com/obruchez", Some("fa-github-square")),
      Page("KeithJarrett.org", "http://www.keithjarrett.org/", Some("fa-caret-square-o-right")), // @todo better icon
      Page("Last.fm", "http://www.last.fm/user/obruchez", Some("fa-lastfm-square")),
      Page("LinkedIn", "https://www.linkedin.com/in/obruchez", Some("fa-linkedin-square")),
      Page("Twitter", "https://twitter.com/obruchez", Some("fa-twitter-square")),
      Page("YouTube", "https://www.youtube.com/user/obruchez", Some("fa-youtube-square"))))

  val root = Page(title = "", url = "", children = Seq(home, profile, worldview, lifelogging, externalLinks))
}
