package controllers

import controllers.Application._
import play.api._
import play.api.mvc._

object Lists {
  def index = Action {
    Ok(views.html.lists())
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
