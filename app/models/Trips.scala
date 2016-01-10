package models

import java.net.URL
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._
import util.Date._

case class Trip(from: Partial,
                to: Partial,
                place: String,
                pictures: Seq[Pictures],
                override val itemSlug: Option[String] = None,
                override val itemUrl: Option[String] = None)
    extends ListItem(
      date = from,
      HtmlContent.fromNonHtmlString(s"${from.yyyymmddString}-${to.yyyymmddString}: $place"),
      itemSlug,
      itemUrl) {
  type T = Trip

  override def withSlug(slug: Option[String]): Trip = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Trip = copy(itemUrl = url)
}

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
                 override val listItems: Seq[Trip]) extends Cacheable

object Trips extends Fetchable {
  type C = Trips

  override val name = "Trips"
  override val sourceUrl = Configuration.baseUrlWithFile("trips.xml").get
  override val icon = Some("fa-suitcase")

  override def fetch(): Try[Trips] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Trips] = for {
    xml <- Try(XML.load(url))
    trips <- apply(xml)
  } yield trips

  def apply(rootNode: Node): Try[Trips] = Try {
    val tripsNode = (rootNode \\ "trips").head
    val introduction = Parsing.introductionFromNode(tripsNode).get
    val tripsSeq = (tripsNode \\ "trip").map(Trip(_).get)

    Trips(introduction, tripsSeq.withSlugs)
  }
}
