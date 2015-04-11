package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class Musician(name: String, instrument: Option[String], leader: Boolean)

object Musician {
  def apply(rootNode: Node): Try[Musician] = Try {
   Musician(
     name = rootNode.text.trim,
     instrument = Option((rootNode \@ "instrument").trim).filter(_.nonEmpty),
     leader = Parsing.isTrue(rootNode \@ "leader"))
  }
}

case class Concert(override val date: Partial,
                   location: String,
                   event: Option[String],
                   band: Option[String],
                   musicians: Seq[Musician],
                   rating: Option[Double],
                   comments: Option[HtmlContent],
                   override val slug: String = "") extends ListItem(date, slug)

object Concert {
  def apply(rootNode: Node): Try[Concert] = Try {
    val musicans = (rootNode \\ "musician").map(Musician(_).get)

    Concert(
      date = Parsing.dateFromString((rootNode \\ "date").text).get,
      location = (rootNode \\ "location").text,
      event = Option((rootNode \\ "event").text.trim).filter(_.nonEmpty),
      band = Option((rootNode \\ "group").text.trim).filter(_.nonEmpty),
      musicians = musicans,
      rating = Parsing.ratingFromString((rootNode \\ "rating").text),
      comments = Parsing.commentsFromString((rootNode \\ "comments").text))
  }
}

case class Concerts(override val introduction: Option[Introduction],
                    concerts: Seq[Concert]) extends Cacheable

object Concerts extends Fetchable {
  type C = Concerts

  override val name = "Concerts"
  override val sourceUrl = Configuration.url("url.concerts").get

  override def fetch(): Try[Concerts] = apply(sourceUrl)

  def apply(url: URL): Try[Concerts] = for {
    xml <- Try(XML.load(url))
    concerts <- apply(xml)
  } yield concerts

  def apply(rootNode: Node): Try[Concerts] = Try {
    val concertsNode = (rootNode \\ "concerts").head
    val introduction = Parsing.introductionFromNode(concertsNode).get
    val concertsSeq = (concertsNode \\ "concert").map(Concert(_).get)

    Concerts(introduction, concertsSeq.map(concert => concert.copy(slug = ListItem.slug(concert, concertsSeq))))
  }

  def commaOrAnd(index: Int, totalCount: Int): String =
    if (index == totalCount - 2)
      if (totalCount == 2) " and " else ", and"
    else if (index < totalCount - 2)
      ", "
    else
      ""
}
