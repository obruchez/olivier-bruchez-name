package models.lifelogging

import java.net.URL
import models._
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class BalletPiece(name: String, choreographers: Seq[String], composers: Seq[String])

object BalletPiece {
  def apply(rootNode: Node): Try[BalletPiece] = Try {
    BalletPiece(
      name = (rootNode \ "name").text.trim,
      choreographers = Parsing.nonEmptyTexts(rootNode \ "choreographer"),
      composers = Parsing.nonEmptyTexts(rootNode \ "composer")
    )
  }
}

case class Ballet(
    override val date: Partial,
    location: String,
    company: Option[String],
    pieces: Seq[BalletPiece],
    dancers: Seq[String],
    rating: Option[Double],
    comments: Option[HtmlContent],
    override val itemSlug: Option[String] = None,
    override val itemUrl: Option[String] = None,
    override val next: Boolean = false
) extends ListItem(
      date,
      HtmlContent.fromNonHtmlString(s"${Ballet.summary(company, pieces)} - $location"),
      itemSlug,
      itemUrl
    ) {
  type T = Ballet

  override def withNext(next: Boolean): Ballet = copy(next = next)
  override def withSlug(slug: Option[String]): Ballet = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Ballet = copy(itemUrl = url)
}

object Ballet {
  def apply(rootNode: Node): Try[Ballet] = Try {
    Ballet(
      date = Parsing.dateFromString((rootNode \ "date").text).get,
      location = (rootNode \ "location").text.trim,
      company = Option((rootNode \ "company").text.trim).filter(_.nonEmpty),
      pieces = (rootNode \ "piece").map(BalletPiece(_).get),
      dancers = Parsing.nonEmptyTexts(rootNode \ "dancer"),
      rating = Parsing.ratingFromString((rootNode \ "rating").text),
      comments = Parsing.commentsFromNodeChildren((rootNode \ "comments").headOption)
    )
  }

  // Single piece => piece name, mixed program => company name
  def summary(company: Option[String], pieces: Seq[BalletPiece]): String =
    pieces match {
      case Seq(piece) => piece.name
      case _          => company.getOrElse(pieces.map(_.name).mkString(" / "))
    }
}

case class Ballets(
    override val introduction: Option[Introduction],
    override val listItems: Seq[Ballet]
) extends Cacheable

object Ballets extends Fetchable {
  type C = Ballets

  override val name = "Ballets"
  override val sourceUrl = Configuration.baseUrlWithFile("ballets.xml").get
  override val icon = Some("fa-child") // @todo better icon

  override def fetch(): Try[Ballets] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Ballets] =
    for {
      xml <- Try(XML.load(url))
      ballets <- apply(xml)
    } yield ballets

  def apply(rootNode: Node): Try[Ballets] = Try {
    val balletsNode = (rootNode \\ "ballets").head
    val introduction = Parsing.introductionFromNode(balletsNode).get
    val balletsSeq = (balletsNode \ "ballet").map(Ballet(_).get)

    Ballets(introduction, balletsSeq.withSlugs.withNextFlags)
  }
}
