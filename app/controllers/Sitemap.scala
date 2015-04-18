package controllers

import models._

object Sitemap {
  // @todo better icon for hikes: https://cdn2.iconfinder.com/data/icons/vacation-landmarks/512/12-512.png
  // @todo better icon for plays: https://cdn0.iconfinder.com/data/icons/huge-basic-icons-part-3/512/Theater_symbol.png

  val home = Page("Home", routes.Application.home().url, Some("fa-home"), fetchables = Seq(Tweets))

  val profile = Page(Profile, routes.Application.profile())
  val lifePrinciples = Page(LifePrinciples, routes.Application.lifePrinciples())
  val worldview = Page(Worldview, routes.Application.worldview())

  val about = Page(
      "About",
      routes.Application.about(),
      "fa-user",
      children = Seq(profile, lifePrinciples, worldview))

  val books = Page(Books, routes.Application.books())
  val concerts = Page(Concerts, routes.Application.concerts())
  val courses = Page(Courses, routes.Application.courses())
  val crashes = Page(Crashes, routes.Application.crashes())
  val exhibitions = Page(Exhibitions, routes.Application.exhibitions())
  val hikes = Page(Hikes, routes.Application.hikes())
  val movies = Page(Movies, routes.Application.movies())
  val plays = Page(Plays, routes.Application.plays())
  val trips = Page(Trips, routes.Application.trips())

  val seenOnTv = Page(SeenOnTv, routes.Application.seenOnTv())

  val booksToRead = Page(BooksToRead, routes.Application.booksToRead())
  val moviesToWatch = Page(MoviesToWatch, routes.Application.moviesToWatch())
  val tripsToTake = Page(TripsToTake, routes.Application.tripsToTake())

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

  val votes = Page(Votes, routes.Application.votes())

  val pdfCv = Page(PdfCv, routes.Application.pdfCv())
  val wordCv = Page(WordCv, routes.Application.wordCv())

  val cv = Page(
    "CV / résumé",
    routes.Application.cv(),
    "fa-file-text-o", // @todo better icon
    children = Seq(pdfCv, wordCv))

  val contacts = Page(Contacts, routes.Application.contacts())

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
