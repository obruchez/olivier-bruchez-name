package models.lifelogging

import java.net.URL
import models._
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class Show(
    override val date: Partial,
    name: String,
    url: URL,
    series: Option[String],
    seriesUrl: Option[URL],
    seriesType: Option[SeriesType],
    comments: Option[HtmlContent],
    override val itemSlug: Option[String] = None,
    override val itemUrl: Option[String] = None,
    override val next: Boolean = false
) extends ListItem(
      date,
      HtmlContent.fromNonHtmlString(name + SeriesType.fullTitleSuffix(series, seriesType)),
      itemSlug,
      itemUrl
    ) {
  type T = Show

  override def withNext(next: Boolean): Show = copy(next = next)
  override def withSlug(slug: Option[String]): Show = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Show = copy(itemUrl = url)

  val standAloneSeriesString =
    (for {
      series <- this.series
      seriesType = this.seriesType.getOrElse(CustomSeries)
    } yield seriesType.standAloneString(series)).getOrElse("-")
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
      seriesUrl = Option((seriesNode \@ "url").trim).filter(_.nonEmpty).map(new URL(_)),
      seriesType = SeriesType.fromString((seriesNode \@ "type").trim),
      comments = Parsing.commentsFromNodeChildren((rootNode \\ "comments").headOption)
    )
  }
}

case class Shows(override val introduction: Option[Introduction], override val listItems: Seq[Show])
    extends Cacheable

object Shows extends Fetchable {
  type C = Shows

  override val name = "TV shows"
  override val sourceUrl = Configuration.baseUrlWithFile("shows.xml").get
  override val icon = Some("fa-television")

  override def fetch(): Try[Shows] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Shows] =
    for {
      xml <- Try(XML.load(url))
      shows <- apply(xml)
    } yield shows

  def apply(rootNode: Node): Try[Shows] = Try {
    val showsNode = (rootNode \\ "shows").head
    val introduction = Parsing.introductionFromNode(showsNode).get
    val showsSeq = (showsNode \\ "show").map(Show(_).get)

    Shows(introduction, showsSeq.withSlugs.withNextFlags)
  }
}

sealed abstract class SeriesType(val id: String) {
  def fullTitleSuffix(seriesValue: String): String
  def standAloneString(seriesValue: String): String
}

case object CustomSeries extends SeriesType("custom") {
  override def fullTitleSuffix(seriesValue: String): String =
    s" ($seriesValue)"
  override def standAloneString(seriesValue: String): String =
    seriesValue
}
case object Season extends SeriesType("season") {
  override def fullTitleSuffix(seriesValue: String): String =
    s" (season $seriesValue)"
  override def standAloneString(seriesValue: String): String =
    s"Season $seriesValue"
}
case object Series extends SeriesType("series") {
  override def fullTitleSuffix(seriesValue: String): String =
    s" (series $seriesValue)"
  override def standAloneString(seriesValue: String): String =
    s"Series $seriesValue"
}

object SeriesType {
  private val seriesTypes = Seq(CustomSeries, Season, Series)

  def fromString(string: String): Option[SeriesType] = {
    val stringLC = string.toLowerCase
    seriesTypes.find(_.id.toLowerCase == stringLC)
  }

  def fullTitleSuffix(seriesOption: Option[String], seriesTypeOption: Option[SeriesType]): String =
    (for {
      series <- seriesOption
      seriesType = seriesTypeOption.getOrElse(CustomSeries)
    } yield seriesType.fullTitleSuffix(series)).getOrElse("")
}
