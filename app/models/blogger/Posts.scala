package models.blogger

import blogger.Blogger
import controllers.routes
import models._
import org.joda.time.ReadablePartial
import org.jsoup.Jsoup
import scala.util.Try
import util._

case class Post(override val date: ReadablePartial,
                title: String,
                content: HtmlContent,
                relativePermalink: String,
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
      content = bloggerPost.content,
      //content = HtmlContent(cleanHtml(bloggerPost.content.htmlString)),
      relativePermalink = bloggerPost.relativePermalink,
      itemUrl = Some(routes.BlogPostsController.blogPost(bloggerPost.relativePermalink).url))

  protected def cleanHtml(html: String): String = {
    val doc = Jsoup.parse(html)

    //val cleaner = new org.jsoup.safety.Cleaner(org.jsoup.safety.Whitelist.relaxed())
    //val cleanDoc = cleaner.clean(doc)

    doc.outputSettings(doc.outputSettings().prettyPrint(false))

    val out = doc.outerHtml

    //println("=" * 80)
    //println(out)
    //println("=" * 80)

    // @todo
    out
  }
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
      //listItems = Blogger.latestPosts(count = 1).map(Post(_)))
  }
}
