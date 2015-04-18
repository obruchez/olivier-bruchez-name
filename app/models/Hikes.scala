package models

import java.net.URL
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class Hike(override val date: Partial,
                place: String,
                pictures: Seq[Pictures],
                override val itemSlug: Option[String] = None,
                override val itemUrl: Option[String] = None)
    extends ListItem(date, s"$place", itemSlug, itemUrl) {
  type T = Hike

  override def withSlug(slug: Option[String]): Hike = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Hike = copy(itemUrl = url)
}

object Hike {
  def apply(rootNode: Node): Try[Hike] = Try {
    Hike(
      date = Parsing.dateFromString((rootNode \\ "date").text).get,
      place = (rootNode \\ "place").text.trim,
      pictures = Parsing.picturesFromNode(rootNode))
  }
}

case class Hikes(override val introduction: Option[Introduction],
                 override val listItems: Seq[Hike]) extends Cacheable

object Hikes extends Fetchable {
  type C = Hikes

  override val name = "Hikes"
  override val sourceUrl = Configuration.baseUrlWithFile("hikes.xml").get
  override val icon = Some("fa-sun-o") // @todo better icon

  override def fetch(): Try[Hikes] = apply(sourceUrl)

  def apply(url: URL): Try[Hikes] = for {
    xml <- Try(XML.load(url))
    hikes <- apply(xml)
  } yield hikes

  def apply(rootNode: Node): Try[Hikes] = Try {
    val hikesNode = (rootNode \\ "hikes").head
    val introduction = Parsing.introductionFromNode(hikesNode).get
    val hikesSeq = (hikesNode \\ "hike").map(Hike(_).get)

    Hikes(introduction, hikesSeq.withSlugs)
  }
}
