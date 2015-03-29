package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml._

case class Musician(name: String, instrument: Option[String], leader: Boolean)

case class Concert(date: Partial,
                   location: String,
                   event: Option[String],
                   band: Option[String],
                   musicians: Seq[Musician],
                   rating: Option[Double],
                   comments: Option[String])

case class Concerts(introduction: String, concerts: Seq[Concert])

object Concerts {
  def apply(url: URL): Try[Concerts] = for {
    xml <- Try(XML.load(url))
    concerts <- apply(xml)
  } yield concerts

  /*
    <concert>
      <date>2015.01.28</date>
      <location>Cave du Bleu Lézard, Lausanne, Switzerland</location>
      <group>Mary's Private Eyes</group>
      <musician inst="bass">Adrian Held</musician>
      <musician inst="guitar">Patrick Macheret</musician>
      <musician inst="drums">Nathalie Winiger</musician>
      <musician inst="vocals">Sarah Artacho</musician>
      <rating>3.5</rating>
      <comments/>
    </concert>
   */

  def apply(elem: Elem): Try[Concerts] = Try {
    val concerts = (elem \\ "concerts").head
    val introduction = (concerts \\ "introduction").head.text

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

    Concerts(introduction, concertsSeq)
  }
}
