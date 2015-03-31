package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Elem, XML}
import util.HtmlContent

case class Hike(override val date: Partial,
                place: String,
                pictures: Seq[Pictures]) extends ListItem(date)

case class Hikes(introduction: HtmlContent, hikes: Seq[Hike])

object Hikes {
  def apply(url: URL): Try[Hikes] = for {
    xml <- Try(XML.load(url))
    hikes <- apply(xml)
  } yield hikes

  def apply(elem: Elem): Try[Hikes] = Try {
    val hikes = (elem \\ "hikes").head
    val introduction = Lists.introductionFromNode(hikes).get

    val hikesSeq = for {
      hike <- hikes \\ "hike"
      dateString = (hike \\ "date").text
      place = (hike \\ "place").text
    } yield Hike(
      date = Lists.dateFromString(dateString).get,
      place = place.trim,
      pictures = Lists.picturesFromNode(hike))

    Hikes(introduction, hikesSeq)
  }
}
