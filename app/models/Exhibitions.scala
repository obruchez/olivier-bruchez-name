package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class Exhibition(override val date: Partial,
                      name: String,
                      museum: String,
                      rating: Option[Double],
                      comments: Option[HtmlContent],
                      override val slug: String = "")
    extends ListItem(date, slug, s"$museum - $name")

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
                       exhibitions: Seq[Exhibition]) extends Cacheable

object Exhibitions extends Fetchable {
  type C = Exhibitions

  override val name = "Exhibitions"
  override val sourceUrl = Configuration.baseUrlWithFile("exhibitions.xml").get

  override def fetch(): Try[Exhibitions] = apply(sourceUrl)

  def apply(url: URL): Try[Exhibitions] = for {
    xml <- Try(XML.load(url))
    exhibitions <- apply(xml)
  } yield exhibitions

  def apply(rootNode: Node): Try[Exhibitions] = Try {
    val exhibitionsNode = (rootNode \\ "exhibitions").head
    val introduction = Parsing.introductionFromNode(exhibitionsNode).get
    val exhibitionsSeq = (exhibitionsNode \\ "exhibition").map(Exhibition(_).get)

    Exhibitions(
      introduction,
      exhibitionsSeq.map(exhibition => exhibition.copy(slug = ListItem.slug(exhibition, exhibitionsSeq))))
  }
}
