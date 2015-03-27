package controllers

import play.api.mvc._

object Home extends Controller {
  val PageTitle = "Home"

  def index = Action {
    Ok(views.html.index())
  }
}

