package controllers

import play.api.mvc._

object ExternalLinks extends Controller {
  def index = Action {
    Ok(views.html.menu("External links"))
  }
}
