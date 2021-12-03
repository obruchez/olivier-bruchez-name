package controllers

import models._
import models.PageGroup._
import models.about.{LifePrinciples, Worldview, Profile}
import models.blogger.Posts
import models.lifelogging._
import models.todo.{TripsToTake, MoviesToWatch, BooksToRead}
import models.twitter.Tweets

object Sitemap {
  // @todo better icon for hikes: https://cdn2.iconfinder.com/data/icons/vacation-landmarks/512/12-512.png
  // @todo better icon for plays: https://cdn0.iconfinder.com/data/icons/huge-basic-icons-part-3/512/Theater_symbol.png

  val home = Page("Home", routes.Application.home.url, Some("fa-home"))

  val profile = Page(Profile, routes.Application.profile)
  val lifePrinciples = Page(LifePrinciples, routes.Application.lifePrinciples)
  val worldview = Page(Worldview, routes.Application.worldview)

  val about = Page("About",
                   routes.Application.about,
                   "fa-user",
                   groupChildren = singlePageGroup(profile, lifePrinciples, worldview))

  val articles = Page(Articles, routes.Application.articles)
  val books = Page(Books, routes.Application.books)
  val comics = Page(Comics, routes.Application.comics)
  val concerts = Page(Concerts, routes.Application.concerts)
  val courses = Page(Courses, routes.Application.courses)
  val crashes = Page(Crashes, routes.Application.crashes)
  val exhibitions = Page(Exhibitions, routes.Application.exhibitions)
  val hikes = Page(Hikes, routes.Application.hikes)
  val movies = Page(Movies, routes.Application.movies)
  val plays = Page(Plays, routes.Application.plays)
  val podcasts = Page(Podcasts, routes.Application.podcasts)
  val shows = Page(Shows, routes.Application.shows)
  val trips = Page(Trips, routes.Application.trips)

  val booksToRead = Page(BooksToRead, routes.Application.booksToRead)
  val moviesToWatch = Page(MoviesToWatch, routes.Application.moviesToWatch)
  val tripsToTake = Page(TripsToTake, routes.Application.tripsToTake)

  val statistics = Page("Statistics", routes.Application.statistics, "fa-line-chart")

  val lifelogging = Page(
    "Lifelogging",
    routes.Application.lifelogging,
    "fa-list",
    groupChildren = Seq(
      PageGroup(
        Seq(articles,
            books,
            comics,
            concerts,
            courses,
            crashes,
            exhibitions,
            hikes,
            movies,
            plays,
            podcasts,
            shows,
            trips)),
      PageGroup(Seq(booksToRead, moviesToWatch, tripsToTake)),
      PageGroup(Seq(statistics))
    )
  )

  val votes = Page(Votes, routes.Application.votes)

  val pdfCv = Page(PdfCv, routes.Application.pdfCv)
  val wordCv = Page(WordCv, routes.Application.wordCv)

  val cv = Page("CV / résumé",
                routes.Application.cv,
                "fa-file-text-o", // @todo better icon
                groupChildren = singlePageGroup(pdfCv, wordCv))

  val blog = Page("Blog", routes.Application.blogPosts, "fa-rss")

  val contacts = Page(Contacts, routes.Application.contacts)

  val root = Page(title = "",
                  url = "",
                  groupChildren =
                    singlePageGroup(home, about, blog, lifelogging, votes, cv, contacts))

  val posts = Page(title = "Posts",
                   url = Posts.sourceUrl.toString,
                   icon = Posts.icon,
                   fetchables = Seq(Posts),
                   groupChildren = Seq())

  val twitter = Page(title = "Twitter",
                     url = Tweets.sourceUrl.toString,
                     icon = Tweets.icon,
                     fetchables = Seq(Tweets),
                     groupChildren = Seq())

  val seenOnTv = Page(SeenOnTv, routes.Application.seenOnTv)

  val nonRootPages = Seq(posts, twitter, seenOnTv)

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

  def fetchables: Seq[Fetchable] =
    allPages.flatMap(_.fetchables).distinct

  def allPages: Seq[Page] = {
    def currentAndChildren(page: Page): Seq[Page] =
      page +: page.children.flatMap(currentAndChildren)

    (currentAndChildren(root) ++ nonRootPages).distinct
  }

  val recentActivityPages =
    Seq(articles, books, concerts, exhibitions, movies, plays, podcasts, trips, shows)
}
