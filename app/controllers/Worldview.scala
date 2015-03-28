package controllers

import play.api.mvc._
import scala.util._
import util.Configuration

object Worldview extends Controller {
  val PageTitle = "Worldview"

  def index = Action {
    models.Worldview(url) match {
      case Success(worldview) =>
        Ok(views.html.worldview(worldview))
      case Failure(throwable) =>
        InternalServerError(views.html.error(PageTitle, throwable))
    }
  }

  private val url = Configuration.url("url.worldview").get
}
