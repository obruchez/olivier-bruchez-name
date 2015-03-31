package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Elem, XML}
import util.HtmlContent

case class Exhibition(override val date: Partial,
                      name: String,
                      museum: String,
                      rating: Option[Double],
                      comments: Option[HtmlContent]) extends ListItem(date)

case class Exhibitions(introduction: HtmlContent, exhibitions: Seq[Exhibition])

object Exhibitions {
  def apply(url: URL): Try[Exhibitions] = for {
    xml <- Try(XML.load(url))
    exhibitions <- apply(xml)
  } yield exhibitions

  def apply(elem: Elem): Try[Exhibitions] = Try {
    val exhibitions = (elem \\ "exhibitions").head
    val introduction = Lists.introductionFromNode(exhibitions).get

    val exhibitionsSeq = for {
      exhibition <- exhibitions \\ "exhibition"
      dateString = (exhibition \\ "date").text
      name = (exhibition \\ "name").text
      museum = (exhibition \\ "museum").text
      ratingString = (exhibition \\ "rating").text
      comments = (exhibition \\ "comments").text
    } yield Exhibition(
      date = Lists.dateFromString(dateString).get,
      name = name.trim,
      museum = museum.trim,
      rating = Lists.ratingFromString(ratingString),
      comments = Lists.commentsFromString(comments))

    Exhibitions(introduction, exhibitionsSeq)
  }
}
