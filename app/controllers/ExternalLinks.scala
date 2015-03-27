package controllers

import play.api.mvc._

object ExternalLinks extends Controller {
  val PageTitle = "External links"

  def index = Action {
    Ok(views.html.menu("External links"))
  }
}
