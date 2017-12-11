package models.blogger

import blogger.Blogger
import controllers.routes
import models._
import org.joda.time.ReadablePartial
import org.jsoup.Jsoup
import org.jsoup.nodes._
import org.jsoup.safety._
import scala.util.Try
import util._

case class Post(override val date: ReadablePartial,
                title: String,
                content: HtmlContent,
                relativePermalink: String,
                originalUrl: String,
                override val itemSlug: Option[String] = None,
                override val itemUrl: Option[String] = None,
                override val next: Boolean = false)
    extends ListItem(date, HtmlContent.fromNonHtmlString(title), itemSlug, itemUrl) {
  type T = Post

  override def withNext(next: Boolean): Post = copy(next = next)
  override def withSlug(slug: Option[String]): Post = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Post = copy(itemUrl = url)
}

object Post {
  def apply(bloggerPost: Blogger.BloggerPost): Post =
    Post(
      date = bloggerPost.publicationDate,
      title = bloggerPost.title,
      //content = bloggerPost.content,
      content = HtmlContent(cleanHtml(bloggerPost.content.htmlString)),
      relativePermalink = bloggerPost.relativePermalink,
      originalUrl = bloggerPost.url,
      itemUrl = Some(routes.BlogPostsController.blogPost(bloggerPost.relativePermalink).url)
    )

  protected def cleanHtml(html: String): String = {
    val doc = Jsoup.parse(html)

    val whitelist = Whitelist.relaxed()
    val cleaner = new Cleaner(whitelist)
    val cleanDoc = cleaner.clean(doc)

    import scala.collection.JavaConversions._

    for {
      element <- cleanDoc.select("div")
    } {
      if (divWithSingleBr(element)) {
        element.remove()
      } else {
        element.tagName("p")
      }
      //println(s"element.children().size = ${element.children().size} -> ${element.children().map(_.tagName()).mkString(", ")}")
    }

    /*
    @todo replace spaces with &nbsp; before "?", "!", ":", etc.
    @todo add "blog post cover image" class to first image in post (this class must have float: right style) and put it
    outside of a/p elements
    @todo instead of replacing div with p elements, remove br elements, etc., look for all paragraphs by searching for
    element children that contain HTML/XML text, as well as, possibly, other elemenrs (i, a, etc.)

    see https://jsoup.org/cookbook/extracting-data/selector-syntax for JSoup select syntax
     */

    cleanDoc.outputSettings(doc.outputSettings().prettyPrint(false))

    val out = cleanDoc.outerHtml

    //println("=" * 80)
    //println(out)
    //println("=" * 80)

    out
  }

  private def divWithSingleBr(element: Element): Boolean =
    element.tagName == "div" && element.children.size == 1 && {
      val divSingleChild = element.children.first

      divSingleChild.tagName == "br" && divSingleChild.children.size == 0
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
