package models.blog

import controllers.routes
import models._
import org.joda.time.Partial
import util._
import util.Date._

import java.net.URL
import scala.util.Try
import scala.xml.{Node, XML}

/** A blog post: its metadata comes from the index (blog.xml), its text from a Markdown file.
  *
  * @param path
  *   year/month/name, as in the post's URL and in the name of its Markdown file
  * @param cover
  *   image that goes with the post, as a path under blog/images
  * @param originalUrl
  *   where the post was first published (Blogger)
  * @param feedId
  *   identifier of the post in the feed, when it is not its address: the one it had in Blogger's
  *   feed
  */
case class Post(
    override val date: Partial,
    title: String,
    path: String,
    language: String,
    labels: Seq[String],
    cover: Option[String],
    originalUrl: Option[String],
    feedId: Option[String],
    override val itemSlug: Option[String] = None,
    override val itemUrl: Option[String] = None,
    override val next: Boolean = false
) extends ListItem(date, HtmlContent.fromNonHtmlString(title), itemSlug, itemUrl) {
  type T = Post

  override def withNext(next: Boolean): Post = copy(next = next)
  override def withSlug(slug: Option[String]): Post = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Post = copy(itemUrl = url)

  def url: String = routes.BlogPostsController.blogPost(path).url

  def page: Page = Page(title, url)

  def fileSource: FileSource =
    FileSource(name = title, sourceUrl = Configuration.baseUrlWithFile(s"blog/$path.md").get)
}

object Post {
  def apply(node: Node): Try[Post] = Try {
    val path = (node \ "path").text.trim

    Post(
      date = Parsing.dateFromString((node \ "date").text).get,
      title = HtmlContent.textWithNonBreakingSpaces((node \ "title").text.trim),
      path = path,
      language = Option((node \ "language").text.trim).filter(_.nonEmpty).getOrElse("fr"),
      labels = (node \ "label").map(_.text.trim).filter(_.nonEmpty),
      cover = Option((node \ "image").text.trim).filter(_.nonEmpty),
      originalUrl = Option((node \ "original").text.trim).filter(_.nonEmpty),
      feedId = Option((node \ "id").text.trim).filter(_.nonEmpty),
      itemUrl = Some(routes.BlogPostsController.blogPost(path).url)
    )
  }
}

case class Posts(override val listItems: Seq[Post]) extends Cacheable {
  override val listItemUrlsFromListItemSlugs = false

  def postFromPath(path: String): Option[Post] = listItems.find(_.path == path)

  // The texts of the posts are fetched and cached along with the index
  override def subFetchables: Seq[FileSource] = listItems.map(_.fileSource)
}

object Posts extends Fetchable {
  type C = Posts

  override val name = "Posts"
  override val sourceUrl: URL = Configuration.baseUrlWithFile("blog.xml").get

  override def fetch(): Try[Posts] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Posts] =
    for {
      xml <- Try(XML.load(url))
      posts <- apply(xml)
    } yield posts

  // Most recent first, whatever the order in the file
  def apply(rootNode: Node): Try[Posts] = Try {
    Posts((rootNode \\ "post").map(Post(_).get).sortBy(_.date.yyyymmddString).reverse)
  }
}
