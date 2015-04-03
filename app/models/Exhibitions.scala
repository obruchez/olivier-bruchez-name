package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Elem, XML}
import util.{Configuration, HtmlContent}

case class Exhibition(override val date: Partial,
                      name: String,
                      museum: String,
                      rating: Option[Double],
                      comments: Option[HtmlContent],
                      override val slug: String = "") extends ListItem(date, slug)

case class Exhibitions(introduction: HtmlContent, exhibitions: Seq[Exhibition]) extends Cacheable {
  override val size = exhibitions.size
}

object Exhibitions extends Fetchable {
  type C = Exhibitions

  override val name = "Exhibitions"
  override val sourceUrl = Configuration.url("url.exhibitions").get

  override def fetch(): Try[Exhibitions] = apply(sourceUrl)

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

    Exhibitions(
      introduction,
      exhibitionsSeq.map(exhibition => exhibition.copy(slug = Lists.slug(exhibition, exhibitionsSeq))))
  }
}
