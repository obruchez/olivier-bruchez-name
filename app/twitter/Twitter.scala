package twitter

import java.net.URL
import scala.collection.JavaConversions._
import twitter4j._
import twitter4j.conf.ConfigurationBuilder
import _root_.util.Configuration

object Twitter {
  case class ExtendedStatus(status: Status, username: String) {
    def isReply: Boolean = status.getInReplyToStatusId >= 0

    // @todo Isn't there a better way to do this?
    def url: URL = new URL(s"https://twitter.com/$username/status/${status.getId}")
  }

  private val configuration = new ConfigurationBuilder().
      setOAuthConsumerKey(Configuration.string("twitter.consumerkey").get).
      setOAuthConsumerSecret(Configuration.string("twitter.consumersecret").get).
      setOAuthAccessToken(Configuration.string("twitter.accesstoken").get).
      setOAuthAccessTokenSecret(Configuration.string("twitter.accesstokensecret").get).
      build()

  private val twitter = new TwitterFactory(configuration).getInstance()

  private val user = twitter.showUser(Configuration.string("twitter.username").get)

  val userDescription: String = user.getDescription

  val smallProfileImageUrl = new URL(user.getBiggerProfileImageURLHttps)

  val originalProfileImageUrl = new URL(user.getOriginalProfileImageURLHttps)

  def latestStatuses(count: Int): Seq[ExtendedStatus] = {
    val pageCount = (count.toDouble / MaxStatusCountPerPaging.toDouble).ceil.round.toInt
    (for {
      page <- 1 to pageCount
      status <- twitter.getUserTimeline(new Paging(page, MaxStatusCountPerPaging)).iterator().toSeq
    } yield ExtendedStatus(status, username = user.getScreenName)).take(count)
  }

  private val MaxStatusCountPerPaging = 200
}
