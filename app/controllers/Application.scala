package controllers

import actors.Cache
import models._
import models.ListItems._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import models.{ PdfCv, WordCv }
import scala.concurrent.Future

object Application extends Controller {
  // @todo remove this (test code)
  def test = Action {
    import scala.collection.JavaConversions._

    val x = blogger.Test.blogger
    // 3497105234617179295 = Olivier Bruchez's blog
    //val y = x.blogs().get("3497105234617179295")
    val z = x.posts().list("3497105234617179295").setMaxResults(500L)
    val zzz = z.execute()
    for (post <- zzz.getItems.iterator().toSeq) {
      println(s"title = ${post.getTitle} -> ${post.getUrl}")
      println(s"${post.getContent}")
    }

    Ok("Test")
  }

  def home = Action.async {
    val MaxItemCount = 5

    val allListItemsFuture = Future.sequence {
      for {
        page <- Sitemap.recentActivityPages
        if page.fetchables.size == 1
        fetchable <- page.fetchables
      } yield {
        Cache.get(fetchable).map(_.latestItems(fetchable, page, count = MaxItemCount)).map(_ -> page)
      }
    }

    for {
      allListItems <- allListItemsFuture
      recentActivityListItemsWithPages = allListItems.filter(_._1.listItems.nonEmpty).sortBy(_._1.fetchable.name)
      tweets <- Cache.get(Tweets)
    } yield {
      Ok(views.html.home(tweets, recentActivityListItemsWithPages))
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

  def worldview = Action.async { Cache.get(Worldview).map(worldview => Ok(views.html.worldview(worldview))) }

  def lifelogging = Action.async {
    Page.introductionsFromPages(Sitemap.lifelogging.children) map { pagesAndIntroductions =>
      Ok(views.html.menu(Sitemap.lifelogging, pagesAndIntroductions, groupSize = 3, colSize = 4))
    }
  }

  def books = Action.async { Cache.get(Books).map(books => Ok(views.html.books(books))) }

  def concerts =  Action.async { Cache.get(Concerts).map(concerts => Ok(views.html.concerts(concerts))) }

  def courses =  Action.async { Cache.get(Courses).map(courses => Ok(views.html.courses(courses))) }

  def crashes = Action.async { Cache.get(Crashes).map(crashes => Ok(views.html.crashes(crashes))) }

  def exhibitions = Action.async { Cache.get(Exhibitions).map(exhibitions => Ok(views.html.exhibitions(exhibitions))) }

  def hikes = Action.async { Cache.get(Hikes).map(hikes => Ok(views.html.hikes(hikes))) }

  def movies = Action.async { Cache.get(Movies).map(movies => Ok(views.html.movies(movies))) }

  def plays = Action.async { Cache.get(Plays).map(plays => Ok(views.html.plays(plays))) }

  def seenOnTv = Action.async {
    Cache.get(SeenOnTv) map { seenOnTv =>
      Ok(views.html.markdown(Sitemap.seenOnTv, seenOnTv.introduction, seenOnTv.content))
    }
  }

  def trips = Action.async { Cache.get(Trips).map(trips => Ok(views.html.trips(trips))) }

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
      Ok(views.html.markdown(Sitemap.moviesToWatch, moviesToWatch.introduction, moviesToWatch.content))
    }
  }

  def tripsToTake = Action.async {
    Cache.get(TripsToTake) map { tripsToTake =>
      Ok(views.html.markdown(Sitemap.tripsToTake, tripsToTake.introduction, tripsToTake.content))
    }
  }

  def toDo = Action.async {
    Page.introductionsFromPages(Sitemap.toDo.children) map { pagesAndIntroductions =>
      // Don't include introductions as long as they come from the raw Markdown pages
      val pagesWithoutIntroductions = pagesAndIntroductions.map(pai => (pai._1, None))
      Ok(views.html.menu(Sitemap.toDo, pagesWithoutIntroductions, groupSize = 3, colSize = 4))
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
      Ok(wordCv.binaryContent.content).as(wordCv.binaryContent.fileType.mimeType).withFilename(WordCv.DownloadFilename)
    }
  }

  def contacts = Action.async { Cache.get(Contacts).map(contacts => Ok(views.html.contacts(contacts))) }

  implicit class ResultOps(result: Result) {
    def withFilename(filename: String): Result =
      result.withHeaders(CONTENT_DISPOSITION -> s"""attachment; filename="$filename"""")
  }
}
