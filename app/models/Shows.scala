package models

import java.net.URL
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class Show(override val date: Partial,
                name: String,
                url: URL,
                series: Option[String],
                seriesUrl: Option[URL],
                override val itemSlug: Option[String] = None,
                override val itemUrl: Option[String] = None)
    extends ListItem(date, HtmlContent.fromNonHtmlString(name + series.map(s => s" ($s)").getOrElse("")), itemSlug, itemUrl) {
  type T = Show

  override def withSlug(slug: Option[String]): Show = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Show = copy(itemUrl = url)
}

object Show {
  def apply(rootNode: Node): Try[Show] = Try {
    val nameNode = rootNode \\ "name"
    val seriesNode = rootNode \\ "series"

    Show(
      date = Parsing.dateFromString((rootNode \\ "date").text).get,
      name = nameNode.text.trim,
      url = new URL((nameNode \@ "url").trim),
      series = Option(seriesNode.text.trim).filter(_.nonEmpty),
      seriesUrl = Option((rootNode \@ "url").trim).filter(_.nonEmpty).map(new URL(_)))
  }
}

case class Shows(override val introduction: Option[Introduction],
                  override val listItems: Seq[Show]) extends Cacheable

object Shows extends Fetchable {
  type C = Shows

  override val name = "TV shows"
  override val sourceUrl = Configuration.baseUrlWithFile("shows.xml").get
  // @todo replace with fa-television when Font Awesome updated
  override val icon = Some("fa-desktop")

  override def fetch(): Try[Shows] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Shows] = for {
    xml <- Try(XML.load(url))
    shows <- apply(xml)
  } yield shows

  def apply(rootNode: Node): Try[Shows] = Try {
    val showsNode = (rootNode \\ "shows").head
    val introduction = Parsing.introductionFromNode(showsNode).get
    val showsSeq = (showsNode \\ "show").map(Show(_).get)

    Shows(introduction, showsSeq.withSlugs)
  }
}
