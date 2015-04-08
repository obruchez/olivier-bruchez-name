package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Elem, XML}
import util._

case class Crash(override val date: Partial,
                 manufacturer: String,
                 model: String,
                 comments: Option[HtmlContent],
                 override val slug: String = "") extends ListItem(date, slug)

case class Crashes(override val introduction: Option[Introduction],
                   crashes: Seq[Crash]) extends Cacheable

object Crashes extends Fetchable {
  type C = Crashes

  override val name = "Crashes"
  override val sourceUrl = Configuration.url("url.crashes").get

  override def fetch(): Try[Crashes] = apply(sourceUrl)

  def apply(url: URL): Try[Crashes] = for {
    xml <- Try(XML.load(url))
    crashes <- apply(xml)
  } yield crashes

  def apply(elem: Elem): Try[Crashes] = Try {
    val crashes = (elem \\ "crashes").head
    val introduction = Parsing.introductionFromNode(crashes).get

    val crashesSeq = for {
      crash <- crashes \\ "crash"
      dateString = (crash \\ "date").text
      manufacturer = (crash \\ "manufacturer").text
      model = (crash \\ "model").text
      comments = (crash \\ "comments").text
    } yield Crash(
      date = Parsing.dateFromString(dateString).get,
      manufacturer = manufacturer.trim,
      model = model.trim,
      comments = Parsing.commentsFromString(comments))

    Crashes(introduction, crashesSeq.map(crash => crash.copy(slug = ListItem.slug(crash, crashesSeq))))
  }
}
