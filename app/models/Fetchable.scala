package models

import java.net.URL
import models.ListItems._
import org.joda.time.Duration
import scala.concurrent.duration._
import scala.util.Try

case class Introduction(shortVersion: HtmlContent, fullVersion: HtmlContent)

object Introduction {
  def apply(string: String): Introduction =
    Introduction(
      shortVersion = HtmlContent.fromNonHtmlString(string),
      fullVersion = HtmlContent.fromNonHtmlString(string))
}

trait Cacheable {
  //def sizeInBytes: Int
  def introduction: Option[Introduction] = None
  def listItems: Seq[ListItem] = Seq()
  def subFetchables: Seq[Fetchable] = Seq()

  def latestItems(fetchable: Fetchable, page: Page, count: Int): ListItems = {
    val latestItems = this.latestItems(fetchable, count)
    latestItems.copy(listItems = latestItems.listItems.withUrls(page))
  }

  def latestItems(fetchable: Fetchable, count: Int): ListItems =
    ListItems(listItems.take(count), fetchable)
}

trait Fetchable {
  type C <: Cacheable

  def name: String
  def sourceUrl: URL
  def icon: Option[String] = None
  def maximumAge: Duration = new Duration(15.minutes.toMillis)
  def fetch(): Try[C]
}
