package controllers

import models._
import models.PageGroup._
import models.about.{LifePrinciples, Profile, Worldview}
import models.blog.Posts
import models.lists._
import models.todo.{BooksToRead, MoviesToWatch, TripsToTake}

object Sitemap {
  // Public name of the section that groups all the lists (formerly "Lifelogging")
  val ListsTitle = "Lists"

  val home = Page("Home", routes.Application.home.url)

  val profile = Page(Profile, routes.Application.profile)
  val lifePrinciples = Page(LifePrinciples, routes.Application.lifePrinciples)
  val worldview = Page(Worldview, routes.Application.worldview)

  val about = Page(
    "About",
    routes.Application.about,
    groupChildren = singlePageGroup(profile, lifePrinciples, worldview)
  )

  val articles = Page(Articles, routes.Application.articles)
  val ballets = Page(Ballets, routes.Application.ballets)
  val books = Page(Books, routes.Application.books)
  val comedyShows = Page(ComedyShows, routes.Application.comedyShows)
  val comics = Page(Comics, routes.Application.comics)
  val concerts = Page(Concerts, routes.Application.concerts)
  val courses = Page(Courses, routes.Application.courses)
  val crashes = Page(Crashes, routes.Application.crashes)
  val exhibitions = Page(Exhibitions, routes.Application.exhibitions)
  val hikes = Page(Hikes, routes.Application.hikes)
  val movies = Page(Movies, routes.Application.movies)
  val operas = Page(Operas, routes.Application.operas)
  val plays = Page(Plays, routes.Application.plays)
  val podcasts = Page(Podcasts, routes.Application.podcasts)
  val shows = Page(Shows, routes.Application.shows)
  val trips = Page(Trips, routes.Application.trips)

  val booksToRead = Page(BooksToRead, routes.Application.booksToRead)
  val moviesToWatch = Page(MoviesToWatch, routes.Application.moviesToWatch)
  val tripsToTake = Page(TripsToTake, routes.Application.tripsToTake)

  val statistics = Page("Statistics", routes.Application.statistics)

  val lists = Page(
    ListsTitle,
    routes.Application.lists,
    groupChildren = Seq(
      PageGroup(
        Seq(articles, books, comics, movies, podcasts, shows),
        title = Some("Read, watched, listened to")
      ),
      PageGroup(
        Seq(ballets, comedyShows, concerts, exhibitions, operas, plays),
        title = Some("Seen live")
      ),
      PageGroup(Seq(hikes, trips), title = Some("Outdoors and travel")),
      PageGroup(Seq(courses, crashes), title = Some("Everything else")),
      PageGroup(Seq(booksToRead, moviesToWatch, tripsToTake), title = Some("Still to do")),
      PageGroup(Seq(statistics), title = Some("In numbers"))
    )
  )

  val entombed = Page("Entombed", routes.Application.entombed)

  val projects = Page(
    "Projects",
    routes.Application.projects,
    groupChildren = singlePageGroup(entombed)
  )

  val votes = Page(Votes, routes.Application.votes)

  val pdfCv = Page(PdfCv, routes.Application.pdfCv)
  val wordCv = Page(WordCv, routes.Application.wordCv)

  val cv = Page(
    "CV / résumé",
    routes.Application.cv,
    groupChildren = singlePageGroup(pdfCv, wordCv)
  )

  // Shown under the title of pages that have no introduction of their own
  val pageNotes: Map[Page, String] = Map(
    entombed -> "A platform game written for the Atari ST in 1991, ported to Scala. Play it in your browser.",
    pdfCv -> "To read on screen or to print.",
    wordCv -> "The same CV as an editable document (downloads the file)."
  )

  val blog = Page("Blog", routes.Application.blogPosts)

  val contacts = Page(Contacts, routes.Application.contacts)

  val root = Page(
    title = "",
    url = "",
    groupChildren = singlePageGroup(home, about, blog, lists, votes, cv, projects, contacts)
  )

  val posts = Page(
    title = "Posts",
    url = Posts.sourceUrl.toString,
    fetchables = Seq(Posts),
    groupChildren = Seq()
  )

  val seenOnTv = Page(SeenOnTv, routes.Application.seenOnTv)

  val nonRootPages = Seq(posts, seenOnTv)

  /** What a single item of a list is called, and how its not-yet-dated items are introduced. */
  case class ListInfo(itemName: String, nextItemsTitle: Option[String] = None)

  val listInfos: Map[Page, ListInfo] = Map(
    articles -> ListInfo("Article", Some("Currently reading")),
    ballets -> ListInfo("Ballet", Some("Upcoming ballets")),
    books -> ListInfo("Book", Some("Currently reading")),
    comedyShows -> ListInfo("Comedy show", Some("Upcoming comedy shows")),
    comics -> ListInfo("Comic", Some("Currently reading")),
    concerts -> ListInfo("Concert", Some("Upcoming concerts")),
    courses -> ListInfo("Course", Some("Currently taking")),
    crashes -> ListInfo("Crash"),
    exhibitions -> ListInfo("Exhibition"),
    hikes -> ListInfo("Hike"),
    movies -> ListInfo("Movie", Some("Currently watching")),
    operas -> ListInfo("Opera", Some("Upcoming operas")),
    plays -> ListInfo("Play", Some("Upcoming plays")),
    podcasts -> ListInfo("Podcast", Some("Currently listening to")),
    shows -> ListInfo("TV show", Some("Currently watching")),
    trips -> ListInfo("Trip", Some("Upcoming trips"))
  )

  // Lists whose images are not upright covers or posters: photos (landscape), artworks (square)
  val coverShapes: Map[Page, String] =
    Map(hikes -> "has-photos", trips -> "has-photos", podcasts -> "has-squares")

  def nextItemsTitle(page: Page): String =
    listInfos.get(page).flatMap(_.nextItemsTitle).getOrElse("Next")

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

  /** True if `page` is `section` itself, one of its descendants, or a page nested under one. */
  def inSection(section: Page, page: Page): Boolean =
    if (section == home)
      page.url == home.url
    else
      section.url == page.url ||
      page.url.startsWith(section.url + "/") ||
      section.children.exists(inSection(_, page))

  def fetchables: Seq[Fetchable] =
    allPages.flatMap(_.fetchables).distinct

  def allPages: Seq[Page] = {
    def currentAndChildren(page: Page): Seq[Page] =
      page +: page.children.flatMap(currentAndChildren)

    (currentAndChildren(root) ++ nonRootPages).distinct
  }

  // Lists feeding the "Lately" section of the home page
  val recentActivityPages =
    Seq(
      articles,
      ballets,
      books,
      comedyShows,
      comics,
      concerts,
      exhibitions,
      hikes,
      movies,
      operas,
      plays,
      podcasts,
      trips,
      shows
    )

  // Lists whose items in progress (no date yet) are shown in the "Right now" section of the home page
  val rightNowPages = Seq(books, comics, movies, shows, courses)

  // Lists of events: an item without a date is something planned, not something in progress
  val eventPages = Seq(concerts, plays, operas, ballets, comedyShows, trips)

  // Lists whose items dated in the future are shown in the "Upcoming" section of the home page
  val upcomingPages = (recentActivityPages ++ eventPages :+ courses).distinct
}
