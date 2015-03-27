package controllers

import play.api.mvc._

object Lists extends Controller {
  val PageTitle = "Lists"

  def index = Action {
    Ok(views.html.menu("Lists"))
  }

  def books = Action {
    Ok(views.html.list())
  }

  def concerts = Action {
    Ok(views.html.list())
  }

  def crashes = Action {
    Ok(views.html.list())
  }

  def exhibitions = Action {
    Ok(views.html.list())
  }

  def hikes = Action {
    Ok(views.html.list())
  }

  def movies = Action {
    Ok(views.html.list())
  }

  def plays = Action {
    Ok(views.html.list())
  }

  def trips = Action {
    Ok(views.html.list())
  }
}
