package blogger

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.blogger.{Blogger => BloggerService}
import models.HtmlContent
import org.joda.time.LocalDateTime
import util.{Configuration, Strings}

object Blogger {
  private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
  private val jsonFactory = GsonFactory.getDefaultInstance

  private val blogger = new BloggerService(httpTransport, jsonFactory, null)

  private val blogId = Configuration.string("blogger.blogid").get

  private val apiKey = Configuration.string("blogger.apikey").get

  case class BloggerPost(title: String,
                         url: String,
                         publicationDate: LocalDateTime,
                         content: HtmlContent,
                         labels: Seq[String]) {
    val relativePermalink = permalinkFromUrl(url)
  }

  def latestPosts(count: Int): Seq[BloggerPost] = {
    import scala.jdk.CollectionConverters._

    val posts =
      blogger.posts().list(blogId).setMaxResults(count.toLong).setKey(apiKey).execute().getItems

    for (post <- posts.iterator().asScala.toSeq)
      yield
        BloggerPost(
          title = post.getTitle,
          url = post.getUrl,
          publicationDate = new LocalDateTime(DateTime.parseRfc3339(post.getPublished).getValue),
          content = HtmlContent(post.getContent),
          labels = Option(post.getLabels).map(_.asScala.toSeq).getOrElse(Seq())
        )
  }

  protected def permalinkFromUrl(url: String): String = {
    val noHttp = Strings.withoutPrefix(url, "http://")
    val noHttps = Strings.withoutPrefix(noHttp, "https://")
    val noExtension = Strings.withoutSuffix(noHttps, ".html")
    val urlParts = noExtension.split("/")
    if (urlParts.size > 1) urlParts.drop(1).mkString("/") else url
  }
}
