package blogger

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.blogger.{ Blogger => BloggerService }
import org.joda.time.LocalDateTime
import util.Configuration

object Blogger {
  private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
  private val jsonFactory = JacksonFactory.getDefaultInstance

  private val blogger = new BloggerService(httpTransport, jsonFactory, null)

  private val blogId = Configuration.string("blogger.blogid").get

  private val apiKey = Configuration.string("blogger.apikey").get

  case class BloggerPost(title: String, url: String, publicationDate: LocalDateTime)

  def latestPosts(count: Int): Seq[BloggerPost] = {
    import scala.collection.JavaConversions._

    val posts = blogger.posts().list(blogId).setMaxResults(count.toLong).setKey(apiKey).execute().getItems

    for (post <- posts.iterator().toSeq)
      yield BloggerPost(
        title = post.getTitle,
        url = post.getUrl,
        publicationDate = new LocalDateTime(post.getPublished.getValue))
  }
}
