package controllers

import play.api.mvc._
import scala.util._

object Profile extends Controller {
  val PageTitle = "About / profile"

  def index = Action {
    models.Profile(new java.net.URL("https://raw.githubusercontent.com/obruchez/public-src/master/about.xml")) match {
      case Success(profile) =>
        Ok(views.html.profile(profile))
      case Failure(throwable) =>
        InternalServerError(views.html.error(PageTitle, throwable))
    }
  }
}

 /*import scala.io.Source
  val markdown = Source.fromURL("https://raw.githubusercontent.com/obruchez/public/master/Introspection/About.md").mkString

  import com.github.rjeschke.txtmark._
  val html = Processor.process(markdown)*/