package twitter

import scala.collection.JavaConversions._
import twitter4j._
import twitter4j.conf.ConfigurationBuilder
import _root_.util.Configuration

object Twitter {
  val configuration = new ConfigurationBuilder().
      setOAuthConsumerKey(Configuration.string("twitter.consumerkey").get).
      setOAuthConsumerSecret(Configuration.string("twitter.consumersecret").get).
      setOAuthAccessToken(Configuration.string("twitter.accesstoken").get).
      setOAuthAccessTokenSecret(Configuration.string("twitter.accesstokensecret").get).
      build()

  val twitter = new TwitterFactory(configuration).getInstance()

  val user = twitter.showUser(Configuration.string("twitter.username").get)

  def userDescription: String =
    user.getDescription

  def latestStatuses(count: Int): Seq[Status] = {
    val pageCount = (count.toDouble / MaxStatusCountPerPaging.toDouble).ceil.round.toInt
    (for {
      page <- 1 to pageCount
      status <- twitter.getUserTimeline(new Paging(page, MaxStatusCountPerPaging)).iterator().toSeq
    } yield status).take(count)
  }

  private val MaxStatusCountPerPaging = 200

  implicit class StatusOps(status: Status) {
    def isReply: Boolean = status.getInReplyToStatusId >= 0
  }
}
