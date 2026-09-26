package models.lifelogging

import java.net.URL
import models._
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class ComedyShow(
    override val date: Partial,
    location: String,
    name: Option[String],
    group: Option[String],
    comedians: Seq[String],
    openingActs: Seq[String],
    authors: Seq[String],
    directors: Seq[String],
    rating: Option[Double],
    comments: Option[HtmlContent],
    override val itemSlug: Option[String] = None,
    override val itemUrl: Option[String] = None,
    override val next: Boolean = false
) extends ListItem(
      date,
      HtmlContent.fromNonHtmlString(
        s"${group.getOrElse(comedians.mkString(", "))} - $location"
      ),
      itemSlug,
      itemUrl
    ) {
  type T = ComedyShow

  override def withNext(next: Boolean): ComedyShow = copy(next = next)
  override def withSlug(slug: Option[String]): ComedyShow = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): ComedyShow = copy(itemUrl = url)
}

object ComedyShow {
  def apply(rootNode: Node): Try[ComedyShow] = Try {
    ComedyShow(
      date = Parsing.dateFromString((rootNode \ "date").text).get,
      location = (rootNode \ "location").text.trim,
      name = Option((rootNode \ "name").text.trim).filter(_.nonEmpty),
      group = Option((rootNode \ "group").text.trim).filter(_.nonEmpty),
      comedians = Parsing.nonEmptyTexts(rootNode \ "comedian"),
      openingActs = Parsing.nonEmptyTexts(rootNode \ "openingact"),
      authors = Parsing.nonEmptyTexts(rootNode \ "author"),
      directors = Parsing.nonEmptyTexts(rootNode \ "director"),
      rating = Parsing.ratingFromString((rootNode \ "rating").text),
      comments = Parsing.commentsFromNodeChildren((rootNode \ "comments").headOption)
    )
  }
}

case class ComedyShows(
    override val introduction: Option[Introduction],
    override val listItems: Seq[ComedyShow]
) extends Cacheable

object ComedyShows extends Fetchable {
  type C = ComedyShows

  override val name = "Comedy shows"
  override val sourceUrl = Configuration.baseUrlWithFile("comedyshows.xml").get
  override val icon = Some("fa-smile-o")

  override def fetch(): Try[ComedyShows] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[ComedyShows] =
    for {
      xml <- Try(XML.load(url))
      comedyShows <- apply(xml)
    } yield comedyShows

  def apply(rootNode: Node): Try[ComedyShows] = Try {
    val comedyShowsNode = (rootNode \\ "comedyshows").head
    val introduction = Parsing.introductionFromNode(comedyShowsNode).get
    val comedyShowsSeq = (comedyShowsNode \ "comedyshow").map(ComedyShow(_).get)

    ComedyShows(introduction, comedyShowsSeq.withSlugs.withNextFlags)
  }
}
