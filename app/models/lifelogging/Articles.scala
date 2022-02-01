package models.lifelogging

import java.net.URL
import models._
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class Article(
    override val date: Partial,
    title: String,
    subtitle: Option[String],
    url: Option[URL],
    comments: Option[HtmlContent],
    override val itemSlug: Option[String] = None,
    override val itemUrl: Option[String] = None,
    override val next: Boolean = false
) extends ListItem(date, HtmlContent.fromNonHtmlString(s"$title"), itemSlug, itemUrl) {
  type T = Article

  override def withNext(next: Boolean): Article = copy(next = next)
  override def withSlug(slug: Option[String]): Article = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Article = copy(itemUrl = url)
}

object Article {
  def apply(rootNode: Node): Try[Article] = Try {
    val titleNode = rootNode \\ "title"

    Article(
      date = Parsing.dateFromString((rootNode \\ "date").text).get,
      title = titleNode.text.trim,
      subtitle = Option((rootNode \\ "subtitle").text.trim).filter(_.nonEmpty),
      url = Option((titleNode \@ "url").trim).filter(_.nonEmpty).map(new URL(_)),
      comments = Parsing.commentsFromNodeChildren((rootNode \\ "comments").headOption)
    )
  }
}

case class Articles(
    override val introduction: Option[Introduction],
    override val listItems: Seq[Article]
) extends Cacheable

object Articles extends Fetchable {
  type C = Articles

  override val name = "Articles"
  override val sourceUrl = Configuration.baseUrlWithFile("articles.xml").get
  override val icon = Some("fa-newspaper-o")

  override def fetch(): Try[Articles] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Articles] =
    for {
      xml <- Try(XML.load(url))
      articles <- apply(xml)
    } yield articles

  def apply(rootNode: Node): Try[Articles] = Try {
    val articlesNode = (rootNode \\ "articles").head
    val introduction = Parsing.introductionFromNode(articlesNode).get
    val articlesSeq = (articlesNode \\ "article").map(Article(_).get)

    Articles(introduction, articlesSeq.withSlugs.withNextFlags.withNextFlags)
  }
}
