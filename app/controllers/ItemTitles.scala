package controllers

import models.{HtmlContent, ListItem}
import util.Date._

// On the home page ("Upcoming", "Lately") and in the index of the lists, every entry is a title
// and its date, written the same way for all lists
object ItemTitles {
  def title(item: ListItem): HtmlContent =
    item match {
      case trip: models.lists.Trip => HtmlContent.fromNonHtmlString(trip.place)
      case _                       => item.shortRepresentation
    }

  def date(item: ListItem): Option[String] =
    item match {
      case _ if item.noDate                                                                  => None
      case trip: models.lists.Trip if trip.to.isoString.length == 10 && trip.to != trip.from =>
        val (from, to) = (trip.from, trip.to)
        val sameYear = from.isoString.take(4) == to.isoString.take(4)
        val sameMonth = sameYear && from.isoString.take(7) == to.isoString.take(7)
        Some(
          if (sameMonth)
            s"${from.shortEnglishString(withYear = false)}-${to.isoString.drop(8).toInt}, ${to.isoString.take(4)}"
          else
            s"${from.shortEnglishString(withYear = !sameYear)} - ${to.shortEnglishString(withYear = true)}"
        )
      case _ => Some(item.date.shortEnglishString(withYear = true))
    }
}
