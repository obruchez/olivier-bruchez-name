package models

import models.ListItems._
import org.joda.time.Duration

import java.net.URL
import scala.concurrent.duration._
import scala.util.Try

case class Introduction(shortVersion: HtmlContent, fullVersion: HtmlContent)

object Introduction {
  def apply(string: String): Introduction =
    Introduction(
      shortVersion = HtmlContent.fromNonHtmlString(string),
      fullVersion = HtmlContent.fromNonHtmlString(string)
    )
}

trait Cacheable {
  // def sizeInBytes: Int
  def introduction: Option[Introduction] = None
  def listItems: Seq[ListItem] = Seq()
  def listItemUrlsFromListItemSlugs: Boolean = true
  def subFetchables: Seq[Fetchable] = Seq()

  def latestItems(fetchable: Fetchable, page: Page, count: Int): ListItems = {
    val latestItems = this.latestItems(fetchable, count)
    val latestItemsWithUrls =
      if (listItemUrlsFromListItemSlugs) latestItems.listItems.withUrls(page)
      else latestItems.listItems

    latestItems.copy(listItems = latestItemsWithUrls)
  }

  def latestItems(fetchable: Fetchable, count: Int): ListItems =
    ListItems(listItems.filterNot(_.next).take(count), fetchable)
}

trait Fetchable {
  type C <: Cacheable

  def name: String
  def sourceUrl: URL
  def icon: Option[String] = None
  def maximumAge: Duration = new Duration(15.minutes.toMillis)
  def fetch(): Try[C]

  /*
    Github seems to cache the data on the server-side (???), so the following have no effect...

      urlConnection.setUseCaches(false)
      urlConnection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
      urlConnection.setRequestProperty("Pragma", "no-cache")
      urlConnection.setRequestProperty("Expires", "0")

    For now, we're using the following workaround.
   */
  def sourceUrlWithNoCacheParameter: URL =
    new URL(sourceUrl.toString + "?_=" + System.currentTimeMillis())
}
