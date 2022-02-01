package models.lifelogging

import java.net.URL
import models._
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class Play(
    override val date: Partial,
    location: String,
    author: String,
    name: String,
    director: String,
    adaptation: Option[String],
    translation: Option[String],
    actors: Seq[String],
    rating: Option[Double],
    comments: Option[HtmlContent],
    override val itemSlug: Option[String] = None,
    override val itemUrl: Option[String] = None,
    override val next: Boolean = false
) extends ListItem(date, HtmlContent.fromNonHtmlString(s"$name - $location"), itemSlug, itemUrl) {
  type T = Play

  override def withNext(next: Boolean): Play = copy(next = next)
  override def withSlug(slug: Option[String]): Play = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Play = copy(itemUrl = url)
}

object Play {
  def apply(rootNode: Node): Try[Play] = Try {
    Play(
      date = Parsing.dateFromString((rootNode \\ "date").text).get,
      location = (rootNode \\ "location").text.trim,
      author = (rootNode \\ "author").text.trim,
      name = (rootNode \\ "name").text.trim,
      director = (rootNode \\ "director").text.trim,
      adaptation = Option((rootNode \\ "adaptation").text.trim).filter(_.nonEmpty),
      translation = Option((rootNode \\ "translation").text.trim).filter(_.nonEmpty),
      actors = (rootNode \\ "actor").map(_.text.trim),
      rating = Parsing.ratingFromString((rootNode \\ "rating").text),
      comments = Parsing.commentsFromNodeChildren((rootNode \\ "comments").headOption)
    )
  }
}

case class Plays(override val introduction: Option[Introduction], override val listItems: Seq[Play])
    extends Cacheable

object Plays extends Fetchable {
  type C = Plays

  override val name = "Plays"
  override val sourceUrl = Configuration.baseUrlWithFile("plays.xml").get
  override val icon = Some("fa-ticket") // @todo better icon

  override def fetch(): Try[Plays] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Plays] =
    for {
      xml <- Try(XML.load(url))
      plays <- apply(xml)
    } yield plays

  def apply(rootNode: Node): Try[Plays] = Try {
    val playsNode = (rootNode \\ "plays").head
    val introduction = Parsing.introductionFromNode(playsNode).get
    val playsSeq = (playsNode \\ "play").map(Play(_).get)

    Plays(introduction, playsSeq.withSlugs.withNextFlags)
  }
}
