package controllers

import play.api.mvc._
import scala.util._
import util.Configuration

object Profile extends Controller {
  val PageTitle = "About / profile"

  def index = Action {
    models.Profile(url) match {
      case Success(profile) =>
        Ok(views.html.profile(profile))
      case Failure(throwable) =>
        InternalServerError(views.html.error(PageTitle, throwable))
    }
  }

  private val url = Configuration.url("url.profile").get
}
