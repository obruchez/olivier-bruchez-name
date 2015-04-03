package controllers

import actors.Cache
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import scala.util._

object Application extends Controller {
  def home = Action {
    Ok(views.html.home())
  }

  def profile = Action {
    models.Profile(Sitemap.profile.sourceUrl.get) match {
      case Success(profile) =>
        Ok(views.html.profile(profile))
      case Failure(throwable) =>
        InternalServerError(views.html.error(Sitemap.profile, throwable))
    }
  }

  def lists = Action {
    // @todo
    Ok(views.html.menu(Sitemap.lists))
  }

  def books = Action.async {
    gCache.books.map(books => Ok(views.html.books(books)))
  }

  def concerts = Action {
    models.Concerts(Sitemap.concerts.sourceUrl.get) match {
      case Success(concerts) =>
        Ok(views.html.concerts(concerts))
      case Failure(throwable) =>
        InternalServerError(views.html.error(Sitemap.concerts, throwable))
    }
  }

  def crashes = Action {
    models.Crashes(Sitemap.crashes.sourceUrl.get) match {
      case Success(crashes) =>
        Ok(views.html.crashes(crashes))
      case Failure(throwable) =>
        InternalServerError(views.html.error(Sitemap.crashes, throwable))
    }
  }

  def exhibitions = Action {
    models.Exhibitions(Sitemap.exhibitions.sourceUrl.get) match {
      case Success(exhibitions) =>
        Ok(views.html.exhibitions(exhibitions))
      case Failure(throwable) =>
        InternalServerError(views.html.error(Sitemap.exhibitions, throwable))
    }
  }

  def hikes = Action {
    models.Hikes(Sitemap.hikes.sourceUrl.get) match {
      case Success(hikes) =>
        Ok(views.html.hikes(hikes))
      case Failure(throwable) =>
        InternalServerError(views.html.error(Sitemap.hikes, throwable))
    }
  }

  def movies = Action {
    models.Movies(Sitemap.movies.sourceUrl.get) match {
       case Success(movies) =>
         Ok(views.html.movies(movies))
       case Failure(throwable) =>
         InternalServerError(views.html.error(Sitemap.movies, throwable))
     }
  }

  def plays = Action {
    models.Plays(Sitemap.plays.sourceUrl.get) match {
      case Success(plays) =>
        Ok(views.html.plays(plays))
      case Failure(throwable) =>
        InternalServerError(views.html.error(Sitemap.plays, throwable))
    }
  }

  def trips = Action {
    models.Trips(Sitemap.trips.sourceUrl.get) match {
      case Success(trips) =>
        Ok(views.html.trips(trips))
      case Failure(throwable) =>
        InternalServerError(views.html.error(Sitemap.trips, throwable))
    }
  }

  def booksToRead = Action {
    // @todo
    Ok(views.html.menu(Sitemap.booksToRead))
  }

  def moviesToWatch = Action {
    // @todo
    Ok(views.html.menu(Sitemap.moviesToWatch))
  }

  def seenOnTv = Action {
    // @todo
    Ok(views.html.menu(Sitemap.seenOnTv))
  }

  def tripsToTake = Action {
    // @todo
    Ok(views.html.menu(Sitemap.tripsToTake))
  }

  def coursera = Action {
    // @todo
    Ok(views.html.menu(Sitemap.coursera))
  }

  def lifePrinciples = Action {
    // @todo
    Ok(views.html.menu(Sitemap.lifePrinciples))
  }

  def votes = Action {
    // @todo
    Ok(views.html.menu(Sitemap.votes))
  }

  def worldview = Action {
    models.Worldview(Sitemap.worldview.sourceUrl.get) match {
      case Success(worldview) =>
        Ok(views.html.worldview(worldview))
      case Failure(throwable) =>
        InternalServerError(views.html.error(Sitemap.worldview, throwable))
    }
  }

  def externalLinks = Action {
    // @todo
    Ok(views.html.menu(Sitemap.externalLinks))
  }
}
