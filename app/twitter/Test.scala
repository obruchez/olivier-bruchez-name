package twitter

import scala.collection.JavaConversions._
import twitter4j._
import twitter4j.conf.ConfigurationBuilder
import _root_.util.Configuration

object Test {
  def test(): Unit = {
    val configuration = new ConfigurationBuilder().
        setOAuthConsumerKey(Configuration.string("twitter.consumerkey").get).
        setOAuthConsumerSecret(Configuration.string("twitter.consumersecret").get).
        setOAuthAccessToken(Configuration.string("twitter.accesstoken").get).
        setOAuthAccessTokenSecret(Configuration.string("twitter.accesstokensecret").get).
        build()

    val twitter = new TwitterFactory(configuration).getInstance()

    val user = twitter.showUser(Configuration.string("twitter.username").get)
    val desc = user.getDescription

    println(s"desc = $desc")

    val statuses = twitter.getUserTimeline(new Paging(1, 200)).iterator().toSeq
    for {
      status <- statuses
      if status.getInReplyToStatusId < 0
    } {
      println(s"status -> ${status.getText}, getInReplyToStatusId = ${status.getInReplyToStatusId}")
    }
  }
}
