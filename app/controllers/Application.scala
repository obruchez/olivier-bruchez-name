package controllers

import actors.Cache
import models._
import models.about.{LifePrinciples, Worldview, Profile}
import models.blogger.Posts
import models.lifelogging._
import models.statistics.Statistics
import models.todo.{TripsToTake, MoviesToWatch, BooksToRead}
import models.twitter.Tweets
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import models.{PdfCv, WordCv}
import scala.concurrent.Future

object Application extends Controller {
  def home = Action.async {
    val MaxItemCount = 5

    val allListItemsFuture = Future.sequence {
      for {
        page <- Sitemap.recentActivityPages
        if page.fetchables.size == 1
        fetchable <- page.fetchables
      } yield {
        Cache
          .get(fetchable)
          .map(_.latestItems(fetchable, page, count = MaxItemCount))
          .map(_ -> page)
      }
    }

    for {
      allListItems <- allListItemsFuture
      recentActivityListItemsWithPages = allListItems
        .filter(_._1.listItems.nonEmpty)
        .sortBy(_._1.fetchable.name.toLowerCase)
      posts <- Cache.get(Posts)
      tweets <- Cache.get(Tweets)
    } yield {
      Ok(views.html.home(posts, tweets, recentActivityListItemsWithPages))
    }
  }

  def profile = Action.async { Cache.get(Profile).map(profile => Ok(views.html.profile(profile))) }

  def about = Action.async {
    Page.introductionsFromPages(Sitemap.about.children) map { pagesAndIntroductions =>
      Ok(views.html.menu(Sitemap.about, pagesAndIntroductions, groupSize = 3, colSize = 4))
    }
  }

  def lifePrinciples = Action.async {
    Cache.get(LifePrinciples).map(lifePrinciples => Ok(views.html.lifeprinciples(lifePrinciples)))
  }

  def worldview = Action.async {
    Cache.get(Worldview).map(worldview => Ok(views.html.worldview(worldview)))
  }

  def lifelogging = Action.async {
    Page.introductionsFromPages(Sitemap.lifelogging.children) map { pagesAndIntroductions =>
      Ok(views.html.menu(Sitemap.lifelogging, pagesAndIntroductions, groupSize = 3, colSize = 4))
    }
  }

  def articles = Action.async {
    Cache.get(Articles).map(articles => Ok(views.html.articles(articles)))
  }

  def books = Action.async { Cache.get(Books).map(books => Ok(views.html.books(books))) }

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

  def plays = Action.async { Cache.get(Plays).map(plays => Ok(views.html.plays(plays))) }

  def podcasts = Action.async {
    Cache.get(Podcasts).map(podcasts => Ok(views.html.podcasts(podcasts)))
  }

  def shows = Action.async { Cache.get(Shows).map(shows => Ok(views.html.shows(shows))) }

  def trips = Action.async { Cache.get(Trips).map(trips => Ok(views.html.trips(trips))) }

  def statistics = Action.async {
    for {
      books <- Cache.get(Books)
      concerts <- Cache.get(Concerts)
      exhibitions <- Cache.get(Exhibitions)
      movies <- Cache.get(Movies)
      plays <- Cache.get(Plays)
      podcasts <- Cache.get(Podcasts)
      statistics = Statistics(books, concerts, exhibitions, movies, plays, podcasts)
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
          .markdown(Sitemap.moviesToWatch, moviesToWatch.introduction, moviesToWatch.content))
    }
  }

  def tripsToTake = Action.async {
    Cache.get(TripsToTake) map { tripsToTake =>
      Ok(views.html.markdown(Sitemap.tripsToTake, tripsToTake.introduction, tripsToTake.content))
    }
  }

  def cv = Action.async {
    Page.introductionsFromPages(Sitemap.cv.children) map { pagesAndIntroductions =>
      Ok(views.html.menu(Sitemap.cv, pagesAndIntroductions, groupSize = 2, colSize = 6))
    }
  }

  def pdfCv = Action.async {
    Cache.get(PdfCv) map { pdfCv =>
      Ok(pdfCv.binaryContent.content).as(pdfCv.binaryContent.fileType.mimeType)
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

  def reload = Action {
    actors.Master.forceFetch()
    Redirect(routes.Application.home())
  }

  def blogPostComparison = Action.async {
    Cache.get(Posts) map { posts =>
      Ok(views.html.blogPostComparison(posts.listItems))
    }
  }

  implicit class ResultOps(result: Result) {
    def withFilename(filename: String): Result =
      result.withHeaders(CONTENT_DISPOSITION -> s"""attachment; filename="$filename"""")
  }
}
