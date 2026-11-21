package controllers

import actors.Cache
import models.about.{LifePrinciples, Profile, Worldview}
import models.blog.Posts
import models.ListItems._
import models.lists._
import util.Date._
import models.statistics.Statistics
import models.todo.{BooksToRead, MoviesToWatch, TripsToTake}
import models._
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

object Application

class Application @Inject() (implicit
    ec: ExecutionContext,
    val controllerComponents: ControllerComponents
) extends BaseController {
  private def cacheables(pages: Seq[Page]): Future[Seq[(Page, Cacheable)]] =
    Future.sequence(
      for {
        page <- pages
        fetchable <- page.fetchables.headOption
      } yield Cache.get(fetchable).map(page -> _)
    )

  // Latest items of all the lists, most recent first. Lists that grow fast (podcasts, movies) would
  // otherwise crowd out the others, hence the limit per list.
  private def lately(count: Int, maxPerList: Int): Future[Seq[(Page, ListItem)]] =
    cacheables(Sitemap.recentActivityPages).map { recentActivity =>
      recentActivity
        .flatMap { case (page, cacheable) =>
          cacheable.listItems.filterNot(_.next).take(maxPerList).withUrls(page).map(page -> _)
        }
        .sortBy(_._2.date.yyyymmddString)(using Ordering[String].reverse)
        .take(count)
    }

  def home = Action.async {
    for {
      // The latest entry of each list, unless the list has been left alone for a year
      latest <- lately(count = Int.MaxValue, maxPerList = 1)
      rightNowActivity <- cacheables(Sitemap.rightNowPages)
      upcomingActivity <- cacheables(Sitemap.upcomingPages)
      posts <- Cache.get(Posts)
    } yield {
      val rightNow =
        rightNowActivity
          .map { case (page, cacheable) =>
            page -> cacheable.listItems.filter(item => item.next && !item.upcoming).reverse
          }
          .filter(_._2.nonEmpty)

      // What is planned, soonest first; events without a date yet come last
      val upcoming =
        upcomingActivity
          .flatMap { case (page, cacheable) =>
            cacheable.listItems
              .filter(item => item.upcoming || (item.next && Sitemap.eventPages.contains(page)))
              .map(page -> _)
          }
          .sortBy { case (_, item) => if (item.noDate) "9999" else item.date.yyyymmddString }
          .map { case (page, item) => (page, ItemTitles.title(item), ItemTitles.date(item)) }

      val oneYearAgo = java.time.LocalDate.now.minusYears(1).toString
      val lately =
        latest
          .filter(_._2.date.yyyymmddString >= oneYearAgo)
          .map { case (page, item) => (page, item, ItemTitles.title(item), ItemTitles.date(item)) }

      Ok(views.html.home(rightNow, upcoming, lately, posts.listItems.take(5)))
    }
  }

  // Atom feed of the same selection as the "Lately" section of the home page, only longer
  def feed = Action.async { (request: Request[AnyContent]) =>
    lately(count = 50, maxPerList = 10).map { items =>
      def atomDate(item: ListItem): String = {
        val iso = item.date.isoString
        (if (iso.length == 10) iso else (iso + "-01-01").take(10)) + "T12:00:00Z"
      }

      val xml =
        <feed xmlns="http://www.w3.org/2005/Atom">
          <title>Olivier Bruchez: lately</title>
          <id>{absoluteUrl(request, "/")}</id>
          <link href={absoluteUrl(request, "/")}/>
          <link rel="self" href={absoluteUrl(request, routes.Application.feed.url)}/>
          <author><name>Olivier Bruchez</name></author>
          <updated>{
          items.headOption.fold("1970-01-01T00:00:00Z")(item => atomDate(item._2))
        }</updated>
          {
          items.map { case (page, item) =>
            val url = absoluteUrl(request, item.itemUrl.getOrElse(page.url))
            val kind = Sitemap.listInfos.get(page).fold(page.title)(_.itemName)
            <entry>
              <title>{
              s"$kind: ${org.jsoup.Jsoup.parse(item.shortRepresentation.htmlString).text()}"
            }</title>
              <id>{url}</id>
              <link href={url}/>
              <updated>{atomDate(item)}</updated>
              <category term={page.title}/>
            </entry>
          }
        }
        </feed>

      Ok("""<?xml version="1.0" encoding="UTF-8"?>""" + "\n" + xml.toString)
        .as("application/atom+xml; charset=utf-8")
    }
  }

  def profile = Action.async { Cache.get(Profile).map(profile => Ok(views.html.profile(profile))) }

  def about = Action.async {
    Page.summariesFromPages(Sitemap.about.children).map { summaries =>
      Ok(views.html.menu(Sitemap.about, summaries))
    }
  }

  def lifePrinciples = Action.async {
    Cache.get(LifePrinciples).map(lifePrinciples => Ok(views.html.lifeprinciples(lifePrinciples)))
  }

  def worldview = Action.async {
    Cache.get(Worldview).map(worldview => Ok(views.html.worldview(worldview)))
  }

  def lists = Action.async {
    Page.summariesFromPages(Sitemap.lists.children).map { summaries =>
      Ok(views.html.menu(Sitemap.lists, summaries))
    }
  }

  // The lists used to live under "Lifelogging"
  def lifelogging = Action { MovedPermanently(routes.Application.lists.url) }

  def articles = Action.async {
    Cache.get(Articles).map(articles => Ok(views.html.articles(articles)))
  }

  def ballets = Action.async { Cache.get(Ballets).map(ballets => Ok(views.html.ballets(ballets))) }

  def books = Action.async { Cache.get(Books).map(books => Ok(views.html.books(books))) }

  def comedyShows = Action.async {
    Cache.get(ComedyShows).map(comedyShows => Ok(views.html.comedyshows(comedyShows)))
  }

  def comics = Action.async { Cache.get(Comics).map(comics => Ok(views.html.comics(comics))) }

  def concerts = Action.async {
    Cache.get(Concerts).map(concerts => Ok(views.html.concerts(concerts)))
  }

  def courses = Action.async { Cache.get(Courses).map(courses => Ok(views.html.courses(courses))) }

  def crashes = Action.async { Cache.get(Crashes).map(crashes => Ok(views.html.crashes(crashes))) }

  def exhibitions = Action.async {
    Cache.get(Exhibitions).map(exhibitions => Ok(views.html.exhibitions(exhibitions)))
  }

  def hikes = Action.async { Cache.get(Hikes).map(hikes => Ok(views.html.hikes(hikes))) }

  def movies = Action.async { Cache.get(Movies).map(movies => Ok(views.html.movies(movies))) }

  def operas = Action.async { Cache.get(Operas).map(operas => Ok(views.html.operas(operas))) }

  def plays = Action.async { Cache.get(Plays).map(plays => Ok(views.html.plays(plays))) }

  def podcasts = Action.async {
    Cache.get(Podcasts).map(podcasts => Ok(views.html.podcasts(podcasts)))
  }

  def shows = Action.async { Cache.get(Shows).map(shows => Ok(views.html.shows(shows))) }

  def trips = Action.async { Cache.get(Trips).map(trips => Ok(views.html.trips(trips))) }

  def statistics = Action.async {
    for {
      ballets <- Cache.get(Ballets)
      books <- Cache.get(Books)
      comedyShows <- Cache.get(ComedyShows)
      concerts <- Cache.get(Concerts)
      exhibitions <- Cache.get(Exhibitions)
      movies <- Cache.get(Movies)
      operas <- Cache.get(Operas)
      plays <- Cache.get(Plays)
      podcasts <- Cache.get(Podcasts)
      statistics = Statistics(
        ballets,
        books,
        comedyShows,
        concerts,
        exhibitions,
        movies,
        operas,
        plays,
        podcasts
      )
    } yield Ok(views.html.statistics(statistics))
  }

  def seenOnTv = Action.async {
    Cache.get(SeenOnTv) map { seenOnTv =>
      Ok(views.html.markdown(Sitemap.seenOnTv, seenOnTv.introduction, seenOnTv.content))
    }
  }

  def votes = Action.async {
    Cache.get(Votes) map { votes =>
      Ok(views.html.markdown(Sitemap.votes, votes.introduction, votes.content))
    }
  }

  def booksToRead = Action.async {
    Cache.get(BooksToRead) map { booksToRead =>
      Ok(views.html.markdown(Sitemap.booksToRead, booksToRead.introduction, booksToRead.content))
    }
  }

  def moviesToWatch = Action.async {
    Cache.get(MoviesToWatch) map { moviesToWatch =>
      Ok(
        views.html
          .markdown(Sitemap.moviesToWatch, moviesToWatch.introduction, moviesToWatch.content)
      )
    }
  }

  def tripsToTake = Action.async {
    Cache.get(TripsToTake) map { tripsToTake =>
      Ok(views.html.markdown(Sitemap.tripsToTake, tripsToTake.introduction, tripsToTake.content))
    }
  }

  // There used to be a page with just the two links
  def projects = Action { Ok(views.html.menu(Sitemap.projects, Map.empty)) }

  def entombed = Action { Ok(views.html.entombed()) }

  def cv = Action.async {
    for {
      pdfCv <- Cache.get(PdfCv)
      wordCv <- Cache.get(WordCv)
    } yield Ok(
      views.html.cv(
        pdfSize = pdfCv.binaryContent.content.length,
        wordSize = wordCv.binaryContent.content.length
      )
    )
  }

  def pdfCv = Action.async {
    Cache.get(PdfCv) map { pdfCv =>
      // "inline": shown by the browser, with a proper name if the visitor then saves it
      Ok(pdfCv.binaryContent.content)
        .as(pdfCv.binaryContent.fileType.mimeType)
        .withHeaders(CONTENT_DISPOSITION -> s"""inline; filename="${PdfCv.DownloadFilename}"""")
    }
  }

  def wordCv = Action.async {
    Cache.get(WordCv) map { wordCv =>
      Ok(wordCv.binaryContent.content)
        .as(wordCv.binaryContent.fileType.mimeType)
        .withFilename(WordCv.DownloadFilename)
    }
  }

  def blogPosts = Action.async {
    Cache.get(Posts) map { posts =>
      Ok(views.html.blogPosts(Sitemap.blog, posts.listItems))
    }
  }

  def contacts = Action.async {
    Cache.get(Contacts).map(contacts => Ok(views.html.contacts(contacts)))
  }

  // Forces a refresh of all the data; only enabled when reload.token is configured
  def reload(token: Option[String]) = Action {
    util.Configuration.string("reload.token").filter(_.nonEmpty) match {
      case Some(expected) if token.contains(expected) =>
        actors.Master.forceFetch()
        Redirect(routes.Application.home)
      case _ =>
        NotFound
    }
  }

  def robots = Action { (request: Request[AnyContent]) =>
    Ok(
      s"User-agent: *\nAllow: /\nDisallow: /reload\n\nSitemap: ${absoluteUrl(request, "/sitemap.xml")}\n"
    )
  }

  def sitemapXml = Action.async { (request: Request[AnyContent]) =>
    Cache.get(Posts).map { posts =>
      val pageUrls = Sitemap.root.children.flatMap(page => page +: page.children).map(_.url)
      val urls = (pageUrls ++ posts.listItems.map(_.url)).distinct
      val xml =
        <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
          {urls.map(url => <url><loc>{absoluteUrl(request, url)}</loc></url>)}
        </urlset>

      Ok("""<?xml version="1.0" encoding="UTF-8"?>""" + "\n" + xml.toString).as("application/xml")
    }
  }

  private def absoluteUrl(request: RequestHeader, path: String): String =
    s"https://${request.host}$path"

  implicit class ResultOps(result: Result) {
    def withFilename(filename: String): Result =
      result.withHeaders(CONTENT_DISPOSITION -> s"""attachment; filename="$filename"""")
  }
}
