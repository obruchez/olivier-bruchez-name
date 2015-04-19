package models

import org.joda.time.{LocalDateTime, ReadablePartial}
import scala.util.Try
import twitter.Twitter
import twitter.Twitter._
import util._

case class Tweet(override val date: ReadablePartial,
                 content: String,
                 reply: Boolean,
                 override val itemSlug: Option[String] = None,
                 override val itemUrl: Option[String] = None)
    extends ListItem(date, HtmlContent.fromNonHtmlString(s"$content"), itemSlug, itemUrl) {
  type T = Tweet

  override def withSlug(slug: Option[String]): Tweet = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Tweet = copy(itemUrl = url)
}

object Tweet {
  def apply(status: ExtendedStatus): Tweet =
    Tweet(
      date = new LocalDateTime(status.status.getCreatedAt),
      content = status.status.getText,
      reply = status.isReply,
      itemUrl = Some(status.url.toString))
}

case class Tweets(profile: String, override val listItems: Seq[Tweet]) extends Cacheable {
  override def latestItems(fetchable: Fetchable, count: Int): ListItems =
    ListItems(listItems.filterNot(_.reply).take(count), fetchable)
}

object Tweets extends Fetchable {
  type C = Tweets

  override val name = "Tweets"
  override val sourceUrl = Configuration.url("twitter.url").get
  override val icon = Some("fa-twitter")

  override def fetch(): Try[Tweets] = Try {
    Tweets(
      profile = Twitter.userDescription,
      // @todo how many tweets do we really want to fetch?
      listItems = Twitter.latestStatuses(count = 1000).map(Tweet(_)))
  }
}
