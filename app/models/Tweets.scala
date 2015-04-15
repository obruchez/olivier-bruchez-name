package models

import org.joda.time.{LocalDateTime, ReadablePartial}
import scala.util.Try
import twitter.Twitter
import twitter.Twitter._
import twitter4j.Status
import util._

case class Tweet(override val date: ReadablePartial,
                 content: String,
                 reply: Boolean,
                 override val slug: String = "")
    extends ListItem(date, slug, s"$content")

object Tweet {
  def apply(status: Status): Tweet =
    Tweet(
      date = new LocalDateTime(status.getCreatedAt),
      content = status.getText,
      reply = status.isReply,
      slug = "") // @todo
}

case class Tweets(profile: String, tweets: Seq[Tweet]) extends Cacheable {
  override def introduction: Option[Introduction] = Some(Introduction(profile))
}

object Tweets extends Fetchable {
  type C = Tweets

  override val name = "Tweets"
  override val sourceUrl = Configuration.url("twitter.url").get

  override def fetch(): Try[Tweets] = Try {
    Tweets(
      profile = Twitter.userDescription,
      // @todo how many tweets do we really want to fetch?
      tweets = Twitter.latestStatuses(count = 1000).map(Tweet(_)))
  }
}
