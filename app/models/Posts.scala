package models

import blogger.Blogger
import org.joda.time.ReadablePartial
import scala.util.Try
import util._

case class Post(override val date: ReadablePartial,
                title: String,
                override val itemSlug: Option[String] = None,
                override val itemUrl: Option[String] = None)
    extends ListItem(date, HtmlContent.fromNonHtmlString(title), itemSlug, itemUrl) {
  type T = Post

  override def withSlug(slug: Option[String]): Post = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Post = copy(itemUrl = url)
}

object Post {
  def apply(bloggerPost: Blogger.BloggerPost): Post =
    Post(
      date = bloggerPost.publicationDate,
      title = bloggerPost.title,
      itemUrl = Some(bloggerPost.url))
}

case class Posts(override val listItems: Seq[Post]) extends Cacheable {
  override val listItemUrlsFromListItemSlugs = false
}

object Posts extends Fetchable {
  type C = Posts

  override val name = "Posts"
  override val sourceUrl = Configuration.url("blogger.url").get
  override val icon = Some("fa-rss")

  override def fetch(): Try[Posts] = Try {
    Posts(
      // @todo how many posts do we really want to fetch? (500 is actually the maximum allowed)
      listItems = Blogger.latestPosts(count = 500).map(Post(_)))
  }
}
