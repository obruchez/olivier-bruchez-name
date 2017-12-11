package models.lifelogging

import java.net.URL
import models._
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class Podcast(override val date: Partial,
                   name: String,
                   url: Option[URL],
                   episodeName: String,
                   episodeUrl: Option[URL],
                   episodeNumber: Option[Int],
                   comments: Option[HtmlContent],
                   override val itemSlug: Option[String] = None,
                   override val itemUrl: Option[String] = None,
                   override val next: Boolean = false)
    extends ListItem(date,
                     HtmlContent.fromNonHtmlString(s"$name - $episodeName"),
                     itemSlug,
                     itemUrl) {
  type T = Podcast

  override def withNext(next: Boolean): Podcast = copy(next = next)
  override def withSlug(slug: Option[String]): Podcast = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Podcast = copy(itemUrl = url)
}

object Podcast {
  def apply(rootNode: Node): Try[Podcast] = Try {
    val nameNode = rootNode \\ "name"
    val episodeNode = rootNode \\ "episode"

    Podcast(
      date = Parsing.dateFromString((rootNode \\ "date").text).get,
      name = nameNode.text.trim,
      url = Option((nameNode \@ "url").trim).filter(_.nonEmpty).map(new URL(_)),
      episodeName = episodeNode.text.trim,
      episodeUrl = Option((episodeNode \@ "url").trim).filter(_.nonEmpty).map(new URL(_)),
      episodeNumber = Option((episodeNode \@ "number").trim).filter(_.nonEmpty).map(_.toInt),
      comments = Parsing.commentsFromNodeChildren((rootNode \\ "comments").headOption)
    )
  }
}

case class Podcasts(override val introduction: Option[Introduction],
                    override val listItems: Seq[Podcast])
    extends Cacheable

object Podcasts extends Fetchable {
  type C = Podcasts

  override val name = "Podcasts"
  override val sourceUrl = Configuration.baseUrlWithFile("podcasts.xml").get
  override val icon = Some("fa-podcast")

  override def fetch(): Try[Podcasts] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Podcasts] =
    for {
      xml <- Try(XML.load(url))
      podcasts <- apply(xml)
    } yield podcasts

  def apply(rootNode: Node): Try[Podcasts] = Try {
    val podcastsNode = (rootNode \\ "podcasts").head
    val introduction = Parsing.introductionFromNode(podcastsNode).get
    val podcastsSeq = (podcastsNode \\ "podcast").map(Podcast(_).get)

    Podcasts(introduction, podcastsSeq.withSlugs.withNextFlags.withNextFlags)
  }
}
