package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Elem, XML}
import util.{Configuration, Parsing}

case class Trip(from: Partial,
                to: Partial,
                place: String,
                pictures: Seq[Pictures],
                override val slug: String = "") extends ListItem(from, slug)

case class Trips(override val introduction: Introduction, trips: Seq[Trip]) extends Cacheable

object Trips extends Fetchable {
  type C = Trips

  override val name = "Trips"
  override val sourceUrl = Configuration.url("url.trips").get

  override def fetch(): Try[Trips] = apply(sourceUrl)

  def apply(url: URL): Try[Trips] = for {
    xml <- Try(XML.load(url))
    trips <- apply(xml)
  } yield trips

  def apply(elem: Elem): Try[Trips] = Try {
    val trips = (elem \\ "trips").head
    val introduction = Parsing.introductionFromNode(trips).get

    val tripsSeq = for {
      trip <- trips \\ "trip"
      fromString = (trip \\ "from").text
      toString = (trip \\ "to").text
      place = (trip \\ "place").text
    } yield Trip(
      from = Parsing.dateFromString(fromString).get,
      to = Parsing.dateFromString(toString).get,
      place = place,
      pictures = Parsing.picturesFromNode(trip))

    Trips(introduction, tripsSeq.map(trip => trip.copy(slug = ListItem.slug(trip, tripsSeq))))
  }
}
