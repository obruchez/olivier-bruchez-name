package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Elem, XML}
import util.HtmlContent

case class Pictures(title: Option[String], url: URL)

case class Trip(from: Partial,
                to: Partial,
                place: String,
                pictures: Seq[Pictures]) extends ListItem(from)

case class Trips(introduction: HtmlContent, trips: Seq[Trip])

object Trips {
  def apply(url: URL): Try[Trips] = for {
    xml <- Try(XML.load(url))
    trips <- apply(xml)
  } yield trips

  def apply(elem: Elem): Try[Trips] = Try {
    val trips = (elem \\ "trips").head
    val introduction = Lists.introductionFromNode(trips).get

    val tripsSeq = for {
      trip <- trips \\ "trip"
      fromString = (trip \\ "from").text
      toString = (trip \\ "to").text
      place = (trip \\ "place").text
    } yield {
      val pictures = for {
        pictures <- trip \\ "pictures"
        title = pictures.text
        url = pictures \@ "url"
      } yield Pictures(
        title = Option(title.trim).filter(_.nonEmpty),
        url = new URL(url))

      Trip(
        from = Lists.dateFromString(fromString).get,
        to = Lists.dateFromString(toString).get,
        place = place,
        pictures = pictures)
    }

    Trips(introduction, tripsSeq)
  }
}
