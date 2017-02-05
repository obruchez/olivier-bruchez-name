package models.lifelogging

import java.net.URL
import models._
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{ Node, XML }
import util._

case class Comic(override val date: Partial,
                 writer: Option[String],
                 artist: Option[String],
                 colorist: Option[String],
                 series: Option[String],
                 seriesUrl: Option[URL],
                 volume: String,
                 volumeNumber: Option[Int],
                 volumeUrl: Option[URL],
                 comments: Option[HtmlContent],
                 override val itemSlug: Option[String] = None,
                 override val itemUrl: Option[String] = None,
                 override val next: Boolean = false)
  extends ListItem(
    date,
    HtmlContent.fromNonHtmlString(s"${Comics.authorSummary(writer, artist)} - $volume"),
    itemSlug,
    itemUrl) {
  type T = Comic

  override def withNext(next: Boolean): Comic = copy(next = next)
  override def withSlug(slug: Option[String]): Comic = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Comic = copy(itemUrl = url)
}

object Comic {
  def apply(rootNode: Node): Try[Comic] = Try {
    val seriesNode = rootNode \\ "series"
    val volumeNode = rootNode \\ "volume"

    Comic(
      date = Parsing.dateFromString((rootNode \\ "date").text).get,
      writer = Option((rootNode \\ "writer").text.trim).filter(_.nonEmpty),
      artist = Option((rootNode \\ "artist").text.trim).filter(_.nonEmpty),
      colorist = Option((rootNode \\ "colorist").text.trim).filter(_.nonEmpty),
      series = Option(seriesNode.text.trim).filter(_.nonEmpty),
      seriesUrl = Option((seriesNode \@ "url").trim).filter(_.nonEmpty).map(new URL(_)),
      volume = volumeNode.text.trim,
      volumeNumber = Option((volumeNode \@ "number").trim).filter(_.nonEmpty).map(_.toInt),
      volumeUrl = Option((volumeNode \@ "url").trim).filter(_.nonEmpty).map(new URL(_)),
      comments = Parsing.commentsFromNodeChildren((rootNode \\ "comments").headOption))
  }
}

case class Comics(override val introduction: Option[Introduction],
                  override val listItems: Seq[Comic]) extends Cacheable

object Comics extends Fetchable {
  type C = Comics

  override val name = "Comics"
  override val sourceUrl = Configuration.baseUrlWithFile("comics.xml").get
  override val icon = Some("fa-comment-o")

  override def fetch(): Try[Comics] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Comics] = for {
    xml <- Try(XML.load(url))
    comics <- apply(xml)
  } yield comics

  def apply(rootNode: Node): Try[Comics] = Try {
    val comicsNode = (rootNode \\ "comics").head
    val introduction = Parsing.introductionFromNode(comicsNode).get
    val comicsSeq = (comicsNode \\ "comic").map(Comic(_).get)

    Comics(introduction, comicsSeq.withSlugs.withNextFlags.withNextFlags)
  }

  def authorSummary(writer: Option[String], artist: Option[String]): String = {
    val authors = Seq(writer, artist).flatten.distinct

    if (authors.isEmpty)
      ""
    else
      authors.mkString(" & ")
  }
}
