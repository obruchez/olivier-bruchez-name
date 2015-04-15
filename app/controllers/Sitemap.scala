package controllers

import actors.Cache
import models._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import scala.concurrent.Future
import util.{Fetchable, Introduction}

case class Page(title: String,
                url: String,
                icon: Option[String] = None,
                fetchables: Seq[Fetchable] = Seq(),
                children: Seq[Page] = Seq())

object Page {
  def apply(fetchable: Fetchable, call: Call, icon: String): Page =
    Page(title = fetchable.name, call.url, icon = Some(icon), fetchables = Seq(fetchable))

  def apply(title: String, call: Call, icon: String): Page =
    Page(title, call.url, icon = Some(icon), children = Seq())

  def apply(title: String, call: Call, icon: String, children: Seq[Page]): Page =
    Page(title, call.url, icon = Some(icon), children = children)

  def introductionsFromPages(pages: Seq[Page]): Future[Seq[(Page, Option[Introduction])]] = {
    val sequenceOfFutures = for (page <- pages) yield {
      // Take only first fetchable into account
      val introductionFuture = page.fetchables.headOption match {
        case Some(fetchable) => Cache.get(fetchable).map(cacheable => cacheable.introduction)
        case None => Future(None)
      }
      introductionFuture.map(page -> _)
    }

    Future.sequence(sequenceOfFutures)
  }
}

object Sitemap {
  // @todo better icon for hikes: https://cdn2.iconfinder.com/data/icons/vacation-landmarks/512/12-512.png
  // @todo better icon for plays: https://cdn0.iconfinder.com/data/icons/huge-basic-icons-part-3/512/Theater_symbol.png

  val home = Page("Home", routes.Application.home().url, Some("fa-home"), fetchables = Seq(Tweets))

  val profile = Page(Profile, routes.Application.profile(), "fa-list")
  val lifePrinciples = Page(LifePrinciples, routes.Application.lifePrinciples(), "fa-compass")
  val worldview = Page(Worldview, routes.Application.worldview(), "fa-globe")

  val about = Page(
      "About",
      routes.Application.about(),
      "fa-user",
      children = Seq(profile, lifePrinciples, worldview))

  val books = Page(Books, routes.Application.books(), "fa-book")
  val concerts = Page(Concerts, routes.Application.concerts(), "fa-music") // @todo better icon
  val courses = Page(Courses, routes.Application.courses(), "fa-graduation-cap")
  val crashes = Page(Crashes, routes.Application.crashes(), "fa-hdd-o")
  val exhibitions = Page(Exhibitions, routes.Application.exhibitions(), "fa-picture-o") // @todo better icon
  val hikes = Page(Hikes, routes.Application.hikes(), "fa-sun-o") // @todo better icon
  val movies = Page(Movies, routes.Application.movies(), "fa-film")
  val plays = Page(Plays, routes.Application.plays(), "fa-ticket") // @todo better icon
  val trips = Page(Trips, routes.Application.trips(), "fa-suitcase")

  val seenOnTv = Page(SeenOnTv, routes.Application.seenOnTv(), "fa-desktop") // @todo better icon

  val booksToRead = Page(BooksToRead, routes.Application.booksToRead(), "fa-book")
  val moviesToWatch = Page(MoviesToWatch, routes.Application.moviesToWatch(), "fa-film")
  val tripsToTake = Page(TripsToTake, routes.Application.tripsToTake(), "fa-suitcase")

  val toDo = Page(
      "To-do",
      routes.Application.toDo(),
      "fa-check-square-o", // @todo better icon
      children = Seq(booksToRead, moviesToWatch, tripsToTake))

  val lifelogging = Page(
    "Lifelogging",
    routes.Application.lifelogging(),
    "fa-list",
    children = Seq(books, concerts, courses, crashes, exhibitions, hikes, movies, plays, trips, toDo))

  val votes = Page(Votes, routes.Application.votes(), "fa-bullhorn") // @todo find icon

  val pdfCv = Page(PdfCv, routes.Application.pdfCv(), "fa-file-pdf-o")
  val wordCv = Page(WordCv, routes.Application.wordCv(), "fa-file-word-o")

  val cv = Page(
    "CV / résumé",
    routes.Application.cv(),
    "fa-file-text-o", // @todo better icon
    children = Seq(pdfCv, wordCv))

  val contacts = Page(Contacts, routes.Application.contacts(), "fa-envelope-o")

  val root = Page(
    title = "",
    url = "",
    children = Seq(home, about, lifelogging, votes, cv, contacts))

  def pageByUrl(url: String): Option[Page] = {
    def pageByUrl(pageToTest: Page): Option[Page] =
      if (pageToTest.url == url)
        Some(pageToTest)
      else if (pageToTest.children.isEmpty)
        None
      else
        (for {
          childPage <- pageToTest.children
          childPageByUrl <- pageByUrl(pageToTest = childPage)
        } yield childPageByUrl).headOption

    pageByUrl(root)
  }

  def fetchables: Seq[Fetchable] = {
    def fetchable(page: Page): Seq[Fetchable] =
      page.fetchables ++ page.children.flatMap(fetchable)

    fetchable(root).distinct
  }
}
