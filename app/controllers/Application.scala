package controllers

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
        InternalServerError(views.html.error(Sitemap.profile.name, throwable))
    }
  }

  def lists = Action {
    // @todo
    Ok(views.html.menu(Sitemap.lists.name))
  }

  def books = Action {
    models.Books(Sitemap.books.sourceUrl.get) match {
      case Success(books) =>
        Ok(views.html.books(books))
      case Failure(throwable) =>
        InternalServerError(views.html.error(Sitemap.books.name, throwable))
    }
  }

  def concerts = Action {
    models.Concerts(Sitemap.concerts.sourceUrl.get) match {
      case Success(concerts) =>
        Ok(views.html.concerts(concerts))
      case Failure(throwable) =>
        InternalServerError(views.html.error(Sitemap.concerts.name, throwable))
    }
  }

  def crashes = Action {
    // @todo
    Ok(views.html.home())
  }

  def exhibitions = Action {
    // @todo
    Ok(views.html.home())
  }

  def hikes = Action {
    // @todo
    Ok(views.html.home())
  }

  def movies = Action {
    models.Movies(Sitemap.movies.sourceUrl.get) match {
       case Success(movies) =>
         Ok(views.html.movies(movies))
       case Failure(throwable) =>
         InternalServerError(views.html.error(Sitemap.movies.name, throwable))
     }
  }

  def plays = Action {
    models.Plays(Sitemap.plays.sourceUrl.get) match {
      case Success(plays) =>
        Ok(views.html.plays(plays))
      case Failure(throwable) =>
        InternalServerError(views.html.error(Sitemap.plays.name, throwable))
    }
  }

  def trips = Action {
    models.Trips(Sitemap.trips.sourceUrl.get) match {
      case Success(trips) =>
        Ok(views.html.trips(trips))
      case Failure(throwable) =>
        InternalServerError(views.html.error(Sitemap.trips.name, throwable))
    }
  }

  def booksToRead = Action {
    // @todo
    Ok(views.html.menu(Sitemap.booksToRead.name))
  }

  def moviesToWatch = Action {
    // @todo
    Ok(views.html.menu(Sitemap.moviesToWatch.name))
  }

  def seenOnTv = Action {
    // @todo
    Ok(views.html.menu(Sitemap.seenOnTv.name))
  }

  def tripsToTake = Action {
    // @todo
    Ok(views.html.menu(Sitemap.tripsToTake.name))
  }

  def coursera = Action {
    // @todo
    Ok(views.html.menu(Sitemap.coursera.name))
  }

  def lifePrinciples = Action {
    // @todo
    Ok(views.html.menu(Sitemap.lifePrinciples.name))
  }

  def votes = Action {
    // @todo
    Ok(views.html.menu(Sitemap.votes.name))
  }

  def worldview = Action {
    models.Worldview(Sitemap.worldview.sourceUrl.get) match {
      case Success(worldview) =>
        Ok(views.html.worldview(worldview))
      case Failure(throwable) =>
        InternalServerError(views.html.error(Sitemap.worldview.name, throwable))
    }
  }

  def externalLinks = Action {
    // @todo
    Ok(views.html.menu(Sitemap.externalLinks.name))
  }
}
