package models.lifelogging

import java.net.URL
import models._
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class Performer(name: String, role: Option[String])

object Performer {
  def apply(rootNode: Node): Try[Performer] = Try {
    Performer(
      name = rootNode.text.trim,
      role = Option((rootNode \@ "inst").trim).filter(_.nonEmpty)
    )
  }

  // Performers grouped by role, roles in order of first appearance
  def byRole(performers: Seq[Performer]): Seq[(Option[String], Seq[String])] =
    performers.map(_.role).distinct.map { role =>
      role -> performers.filter(_.role == role).map(_.name)
    }
}

case class Opera(
    override val date: Partial,
    location: String,
    name: String,
    composers: Seq[String],
    directors: Seq[String],
    performers: Seq[Performer],
    rating: Option[Double],
    comments: Option[HtmlContent],
    override val itemSlug: Option[String] = None,
    override val itemUrl: Option[String] = None,
    override val next: Boolean = false
) extends ListItem(date, HtmlContent.fromNonHtmlString(s"$name - $location"), itemSlug, itemUrl) {
  type T = Opera

  override def withNext(next: Boolean): Opera = copy(next = next)
  override def withSlug(slug: Option[String]): Opera = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Opera = copy(itemUrl = url)
}

object Opera {
  def apply(rootNode: Node): Try[Opera] = Try {
    Opera(
      date = Parsing.dateFromString((rootNode \ "date").text).get,
      location = (rootNode \ "location").text.trim,
      name = (rootNode \ "name").text.trim,
      composers = Parsing.nonEmptyTexts(rootNode \ "composer"),
      directors = Parsing.nonEmptyTexts(rootNode \ "director"),
      performers = (rootNode \ "musician").map(Performer(_).get),
      rating = Parsing.ratingFromString((rootNode \ "rating").text),
      comments = Parsing.commentsFromNodeChildren((rootNode \ "comments").headOption)
    )
  }
}

case class Operas(
    override val introduction: Option[Introduction],
    override val listItems: Seq[Opera]
) extends Cacheable

object Operas extends Fetchable {
  type C = Operas

  override val name = "Operas"
  override val sourceUrl = Configuration.baseUrlWithFile("operas.xml").get
  override val icon = Some("fa-microphone") // @todo better icon

  override def fetch(): Try[Operas] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Operas] =
    for {
      xml <- Try(XML.load(url))
      operas <- apply(xml)
    } yield operas

  def apply(rootNode: Node): Try[Operas] = Try {
    val operasNode = (rootNode \\ "operas").head
    val introduction = Parsing.introductionFromNode(operasNode).get
    val operasSeq = (operasNode \ "opera").map(Opera(_).get)

    Operas(introduction, operasSeq.withSlugs.withNextFlags)
  }
}
