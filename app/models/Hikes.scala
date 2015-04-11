package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class Hike(override val date: Partial,
                place: String,
                pictures: Seq[Pictures],
                override val slug: String = "") extends ListItem(date, slug)

object Hike {
  def apply(rootNode: Node): Try[Hike] = Try {
    Hike(
      date = Parsing.dateFromString((rootNode \\ "date").text).get,
      place = (rootNode \\ "place").text.trim,
      pictures = Parsing.picturesFromNode(rootNode))
  }
}

case class Hikes(override val introduction: Option[Introduction],
                 hikes: Seq[Hike]) extends Cacheable

object Hikes extends Fetchable {
  type C = Hikes

  override val name = "Hikes"
  override val sourceUrl = Configuration.baseUrlWithFile("hikes.xml").get

  override def fetch(): Try[Hikes] = apply(sourceUrl)

  def apply(url: URL): Try[Hikes] = for {
    xml <- Try(XML.load(url))
    hikes <- apply(xml)
  } yield hikes

  def apply(rootNode: Node): Try[Hikes] = Try {
    val hikesNode = (rootNode \\ "hikes").head
    val introduction = Parsing.introductionFromNode(hikesNode).get
    val hikesSeq = (hikesNode \\ "hike").map(Hike(_).get)

    Hikes(introduction, hikesSeq.map(hike => hike.copy(slug = ListItem.slug(hike, hikesSeq))))
  }
}
