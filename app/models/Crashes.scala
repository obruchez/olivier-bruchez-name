package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Elem, XML}
import util.HtmlContent

case class Crash(override val date: Partial,
                 manufacturer: String,
                 model: String,
                 comments: Option[HtmlContent]) extends ListItem(date)

case class Crashes(introduction: HtmlContent, crashes: Seq[Crash])

object Crashes {
  def apply(url: URL): Try[Crashes] = for {
    xml <- Try(XML.load(url))
    crashes <- apply(xml)
  } yield crashes

  def apply(elem: Elem): Try[Crashes] = Try {
    val crashes = (elem \\ "crashes").head
    val introduction = Lists.introductionFromNode(crashes).get

    val crashesSeq = for {
      crash <- crashes \\ "crash"
      dateString = (crash \\ "date").text
      manufacturer = (crash \\ "manufacturer").text
      model = (crash \\ "model").text
      comments = (crash \\ "comments").text
    } yield Crash(
      date = Lists.dateFromString(dateString).get,
      manufacturer = manufacturer.trim,
      model = model.trim,
      comments = Lists.commentsFromString(comments))

    Crashes(introduction, crashesSeq)
  }
}
