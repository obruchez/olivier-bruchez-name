package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml._
import util.{Configuration, HtmlContent}

case class Musician(name: String, instrument: Option[String], leader: Boolean)

case class Concert(override val date: Partial,
                   location: String,
                   event: Option[String],
                   band: Option[String],
                   musicians: Seq[Musician],
                   rating: Option[Double],
                   comments: Option[HtmlContent],
                   override val slug: String = "") extends ListItem(date, slug)

case class Concerts(introduction: HtmlContent, concerts: Seq[Concert]) extends Cacheable {
  override val size = concerts.size
}

object Concerts extends Fetchable {
  type C = Concerts

  override val name = "Concerts"
  override val sourceUrl = Configuration.url("url.concerts").get

  override def fetch(): Try[Concerts] = apply(sourceUrl)

  def apply(url: URL): Try[Concerts] = for {
    xml <- Try(XML.load(url))
    concerts <- apply(xml)
  } yield concerts

  def apply(elem: Elem): Try[Concerts] = Try {
    val concerts = (elem \\ "concerts").head
    val introduction = Lists.introductionFromNode(concerts).get

    val concertsSeq = for {
      concert <- concerts \\ "concert"
      dateString = (concert \\ "date").text
      location = (concert \\ "location").text
      event = (concert \\ "event").text
      band = (concert \\ "group").text
      ratingString = (concert \\ "rating").text
      comments = (concert \\ "comments").text
    } yield {
      val musicans = for {
        musician <- concert \\ "musician"
        name = musician.text
        instrument = musician \@ "instrument"
        leader = musician \@ "leader"
      } yield Musician(
        name = name.trim,
        instrument = Option(instrument.trim).filter(_.nonEmpty),
        leader = Lists.isTrue(leader))

      Concert(date = Lists.dateFromString(dateString).get,
        location = location,
        event = Option(event.trim).filter(_.nonEmpty),
        band = Option(band.trim).filter(_.nonEmpty),
        musicians = musicans,
        rating = Lists.ratingFromString(ratingString),
        comments = Lists.commentsFromString(comments))
    }

    Concerts(introduction, concertsSeq.map(concert => concert.copy(slug = Lists.slug(concert, concertsSeq))))
  }
}
