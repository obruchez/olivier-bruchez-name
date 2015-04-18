package models

import java.net.URL
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class Exhibition(override val date: Partial,
                      name: String,
                      museum: String,
                      rating: Option[Double],
                      comments: Option[HtmlContent],
                      override val itemSlug: Option[String] = None,
                      override val itemUrl: Option[String] = None)
    extends ListItem(date, s"$museum - $name", itemSlug, itemUrl) {
  type T = Exhibition

  override def withSlug(slug: Option[String]): Exhibition = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Exhibition = copy(itemUrl = url)
}

object Exhibition {
  def apply(rootNode: Node): Try[Exhibition] = Try {
    Exhibition(
       date = Parsing.dateFromString((rootNode \\ "date").text).get,
       name = (rootNode \\ "name").text.trim,
       museum = (rootNode \\ "museum").text.trim,
       rating = Parsing.ratingFromString((rootNode \\ "rating").text),
       comments = Parsing.commentsFromString((rootNode \\ "comments").text))
  }
}

case class Exhibitions(override val introduction: Option[Introduction],
                       override val listItems: Seq[Exhibition]) extends Cacheable

object Exhibitions extends Fetchable {
  type C = Exhibitions

  override val name = "Exhibitions"
  override val sourceUrl = Configuration.baseUrlWithFile("exhibitions.xml").get
  override val icon = Some("fa-picture-o") // @todo better icon

  override def fetch(): Try[Exhibitions] = apply(sourceUrl)

  def apply(url: URL): Try[Exhibitions] = for {
    xml <- Try(XML.load(url))
    exhibitions <- apply(xml)
  } yield exhibitions

  def apply(rootNode: Node): Try[Exhibitions] = Try {
    val exhibitionsNode = (rootNode \\ "exhibitions").head
    val introduction = Parsing.introductionFromNode(exhibitionsNode).get
    val exhibitionsSeq = (exhibitionsNode \\ "exhibition").map(Exhibition(_).get)

    Exhibitions(introduction, exhibitionsSeq.withSlugs)
  }
}
