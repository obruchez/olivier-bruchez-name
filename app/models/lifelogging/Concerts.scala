package models.lifelogging

import java.net.URL
import models._
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{ Node, XML }
import util._

case class Musician(name: String, instrument: Option[String], main: Boolean)

object Musician {
  def apply(rootNode: Node): Try[Musician] = Try {
   Musician(
     name = rootNode.text.trim,
     instrument = Option((rootNode \@ "instrument").trim).filter(_.nonEmpty),
     main = Parsing.isTrue(rootNode \@ "main"))
  }

  def musicians(bandOption: Option[String], musicians: Seq[Musician]): String =
    bandOption.map(_ + (if (musicians.nonEmpty) ": " else "")).getOrElse("") +
      (musicians.zipWithIndex map { case (musician, index) =>
        val separator = Concerts.commaOrAnd(index = index, totalCount = musicians.size)
        musician.name + separator
      }).mkString("")

  def musiciansSummary(bandOption: Option[String], musicians: Seq[Musician], eventOption: Option[String]): String =
    bandOption match {
      case Some(band) =>
        band
      case None =>
        val mainMusicians = musicians.filter(_.main)
        if (mainMusicians.isEmpty)
          eventOption.getOrElse(this.musicians(bandOption = None, musicians))
        else
          this.musicians(bandOption = None, mainMusicians)
    }
}

case class Concert(override val date: Partial,
                   location: String,
                   event: Option[String],
                   concertType: ConcertType,
                   band: Option[String],
                   musicians: Seq[Musician],
                   rating: Option[Double],
                   comments: Option[HtmlContent],
                   override val itemSlug: Option[String] = None,
                   override val itemUrl: Option[String] = None)
    extends ListItem(
      date,
      HtmlContent.fromNonHtmlString(s"${Musician.musiciansSummary(band, musicians, event)} - $location"),
      itemSlug,
      itemUrl) {
  type T = Concert

  override def withSlug(slug: Option[String]): Concert = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Concert = copy(itemUrl = url)
}

object Concert {
  def apply(rootNode: Node): Try[Concert] = Try {
    val musicans = (rootNode \\ "musician").map(Musician(_).get)

    Concert(
      date = Parsing.dateFromString((rootNode \\ "date").text).get,
      location = (rootNode \\ "location").text,
      concertType = ConcertType.fromString((rootNode \\ "type").text.trim).getOrElse(NormalConcert),
      event = Option((rootNode \\ "event").text.trim).filter(_.nonEmpty),
      band = Option((rootNode \\ "group").text.trim).filter(_.nonEmpty),
      musicians = musicans,
      rating = Parsing.ratingFromString((rootNode \\ "rating").text),
      comments = Parsing.commentsFromString((rootNode \\ "comments").text))
  }
}

case class Concerts(override val introduction: Option[Introduction],
                    override val listItems: Seq[Concert]) extends Cacheable

object Concerts extends Fetchable {
  type C = Concerts

  override val name = "Concerts"
  override val sourceUrl = Configuration.baseUrlWithFile("concerts.xml").get
  override val icon = Some("fa-music") // @todo better icon

  override def fetch(): Try[Concerts] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Concerts] = for {
    xml <- Try(XML.load(url))
    concerts <- apply(xml)
  } yield concerts

  def apply(rootNode: Node): Try[Concerts] = Try {
    val concertsNode = (rootNode \\ "concerts").head
    val introduction = Parsing.introductionFromNode(concertsNode).get
    val concertsSeq = (concertsNode \\ "concert").map(Concert(_).get)

    Concerts(introduction, concertsSeq.withSlugs)
  }

  def commaOrAnd(index: Int, totalCount: Int): String =
    if (index == totalCount - 2)
      if (totalCount == 2) " and " else ", and "
    else if (index < totalCount - 2)
      ", "
    else
      ""
}

sealed abstract class ConcertType(val id: String)

case object NormalConcert extends ConcertType("normal")
case object Aftershow extends ConcertType("aftershow")
case object Soundcheck extends ConcertType("soundcheck")

object ConcertType {
  private val concertTypes = Seq(NormalConcert, Aftershow, Soundcheck)

  def fromString(string: String): Option[ConcertType] = {
    val stringLC = string.toLowerCase
    concertTypes.find(_.id.toLowerCase == stringLC)
  }
}
