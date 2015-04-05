package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Elem, XML}
import util.{Configuration, Parsing}

case class Hike(override val date: Partial,
                place: String,
                pictures: Seq[Pictures],
                override val slug: String = "") extends ListItem(date, slug)

case class Hikes(override val introduction: Introduction, hikes: Seq[Hike]) extends Cacheable {
  override val size = hikes.size
}

object Hikes extends Fetchable {
  type C = Hikes

  override val name = "Hikes"
  override val sourceUrl = Configuration.url("url.hikes").get

  override def fetch(): Try[Hikes] = apply(sourceUrl)

  def apply(url: URL): Try[Hikes] = for {
    xml <- Try(XML.load(url))
    hikes <- apply(xml)
  } yield hikes

  def apply(elem: Elem): Try[Hikes] = Try {
    val hikes = (elem \\ "hikes").head
    val introduction = Parsing.introductionFromNode(hikes).get

    val hikesSeq = for {
      hike <- hikes \\ "hike"
      dateString = (hike \\ "date").text
      place = (hike \\ "place").text
    } yield Hike(
      date = Parsing.dateFromString(dateString).get,
      place = place.trim,
      pictures = Parsing.picturesFromNode(hike))

    Hikes(introduction, hikesSeq.map(hike => hike.copy(slug = ListItem.slug(hike, hikesSeq))))
  }
}
