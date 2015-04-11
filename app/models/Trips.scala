package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class Trip(from: Partial,
                to: Partial,
                place: String,
                pictures: Seq[Pictures],
                override val slug: String = "") extends ListItem(from, slug)

object Trip {
  def apply(rootNode: Node): Try[Trip] = Try {
    Trip(
      from = Parsing.dateFromString((rootNode \\ "from").text).get,
      to = Parsing.dateFromString((rootNode \\ "to").text).get,
      place = (rootNode \\ "place").text,
      pictures = Parsing.picturesFromNode(rootNode))
  }
}

case class Trips(override val introduction: Option[Introduction],
                 trips: Seq[Trip]) extends Cacheable

object Trips extends Fetchable {
  type C = Trips

  override val name = "Trips"
  override val sourceUrl = Configuration.url("url.trips").get

  override def fetch(): Try[Trips] = apply(sourceUrl)

  def apply(url: URL): Try[Trips] = for {
    xml <- Try(XML.load(url))
    trips <- apply(xml)
  } yield trips

  def apply(rootNode: Node): Try[Trips] = Try {
    val tripsNode = (rootNode \\ "trips").head
    val introduction = Parsing.introductionFromNode(tripsNode).get
    val tripsSeq = (tripsNode \\ "trip").map(Trip(_).get)

    Trips(introduction, tripsSeq.map(trip => trip.copy(slug = ListItem.slug(trip, tripsSeq))))
  }
}
