package models

import org.joda.time.ReadablePartial
import util.Date._

abstract class ListItem(val date: ReadablePartial, val slug: String)

object ListItem {
  def slug(listItem: ListItem, allListItems: Seq[ListItem]): String = {
    val candidateSlug = listItem.date.yyyymmddString

    val listItemsWithSameDate = allListItems.reverse.filter(_.date.yyyymmddString == candidateSlug)

    if (listItemsWithSameDate.size == 1)
      candidateSlug
    else
      candidateSlug + "-" + (listItemsWithSameDate.indexOf(listItem) + 1)
  }
}
