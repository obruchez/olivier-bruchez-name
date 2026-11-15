package models

import org.joda.time.ReadablePartial
import util.Date._
import util.Slug

abstract class ListItem(
    val date: ReadablePartial,
    val shortRepresentation: HtmlContent,
    val itemSlug: Option[String],
    val itemUrl: Option[String],
    val next: Boolean = false
) {
  type T <: ListItem

  def noDate: Boolean = date.emptyDate

  /** Planned for a known day in the future (a booked concert, a trip): kept out of the list itself
    * and announced on the home page.
    */
  def upcoming: Boolean = next && date.isoString.length == 10

  /** Cover/poster, as a path relative to the images directory, e.g. "books/Le Horla.jpg". */
  def image: Option[String] = None

  /** Page the cover links to, e.g. the photo in its Flickr album. */
  def imageUrl: Option[String] = None

  def withNext(next: Boolean): T
  def withSlug(slug: Option[String]): T
  def withUrl(url: Option[String]): T
}

object ListItem {
  def slug(listItem: ListItem, allListItems: Seq[ListItem]): String = {
    val candidateSlug = listItem.date.yyyymmddString

    val listItemsWithSameDate = allListItems.reverse.filter(_.date.yyyymmddString == candidateSlug)

    if (listItemsWithSameDate.size == 1) {
      candidateSlug
    } else {
      candidateSlug + "-" + (listItemsWithSameDate.indexOf(listItem) + 1)
    }
  }
}

case class ListItems(listItems: Seq[ListItem], fetchable: Fetchable) {
  def withUrls(page: Page): ListItems = copy(listItems = ListItems.withUrls(listItems)(page))
}

object ListItems {
  extension [L <: ListItem { type T = L }](listItems: Seq[L]) {
    def withNextFlags: Seq[L] = {
      val headItemWithNoDateCount =
        listItems.takeWhile(li => li.date.emptyDate || li.date.futureDate).size

      listItems.zipWithIndex.map { case (listItem, index) =>
        listItem.withNext(next = index < headItemWithNoDateCount)
      }
    }

    def withSlugs: Seq[L] =
      listItems.map(listItem => listItem.withSlug(Some(ListItem.slug(listItem, listItems))))
  }

  extension (listItems: Seq[ListItem]) {
    def withUrls(page: Page): Seq[ListItem] =
      listItems.map { listItem =>
        listItem.withUrl(listItem.itemSlug.map(itemSlug => Slug.withBaseUrl(page.url, itemSlug)))
      }
  }
}
