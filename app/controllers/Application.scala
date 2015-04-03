package controllers

import actors.Cache
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

object Application extends Controller {
  def home = Action {
    Ok(views.html.home())
  }

  def profile = Action.async { Cache.profile.map(profile => Ok(views.html.profile(profile))) }

  def lifelogging = Action { Ok(views.html.lifelogging()) }

  def books = Action.async { Cache.books.map(books => Ok(views.html.books(books))) }

  def concerts =  Action.async { Cache.concerts.map(concerts => Ok(views.html.concerts(concerts))) }

  def crashes = Action.async { Cache.crashes.map(crashes => Ok(views.html.crashes(crashes))) }

  def exhibitions = Action.async { Cache.exhibitions.map(exhibitions => Ok(views.html.exhibitions(exhibitions))) }

  def hikes = Action.async { Cache.hikes.map(hikes => Ok(views.html.hikes(hikes))) }

  def movies = Action.async { Cache.movies.map(movies => Ok(views.html.movies(movies))) }

  def plays = Action.async { Cache.plays.map(plays => Ok(views.html.plays(plays))) }

  def trips = Action.async { Cache.trips.map(trips => Ok(views.html.trips(trips))) }

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

  def worldview = Action.async { Cache.worldview.map(worldview => Ok(views.html.worldview(worldview))) }

  def externalLinks = Action { Ok(views.html.externallinks()) }
}
