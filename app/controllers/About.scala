package controllers

import play.api.mvc._

object About extends Controller {
  def index = Action {
    import scala.io.Source

    val markdown = Source.fromURL("https://raw.githubusercontent.com/obruchez/public/master/Introspection/About.md").mkString

    import com.github.rjeschke.txtmark._

    val html = Processor.process(markdown)

    Ok(views.html.html("About", html))
  }
}
