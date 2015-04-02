package models

import java.net.URL

import org.joda.time.Partial
import scala.util.Try
import scala.xml.Node
import util._
import util.Date._

abstract class ListItem(val date: Partial, val slug: String)

object Lists {
  def introductionFromNode(node: Node): Try[HtmlContent] =
    HtmlContent.fromMarkdown((node \\ "introduction").head.text)

  def picturesFromNode(node: Node): Seq[Pictures] = for {
    pictures <- node \\ "pictures"
    title = pictures.text
    url = pictures \@ "url"
  } yield Pictures(
    title = Option(title.trim).filter(_.nonEmpty),
    url = new URL(url))

  def dateFromString(string: String): Try[Partial] =
    Date.partialFromYyyymmddString(string.trim)

  def ratingFromString(string: String): Option[Double] =
    Option(string).map(_.trim).filter(_.nonEmpty).map(_.toDouble - 1)

  def commentsFromString(string: String): Option[HtmlContent] =
    Option(string).map(_.trim).filter(_.nonEmpty).map(HtmlContent(_))

  def isTrue(string: String): Boolean =
    Set("on", "true", "y", "yes", "1").contains(string.trim.toLowerCase)

  def commaOrAnd(index: Int, totalCount: Int): String =
    if (index == totalCount - 2)
      if (totalCount == 2) " and " else ", and"
    else if (index < totalCount - 2)
      ", "
    else
      ""

  def slug(listItem: ListItem, allListItems: Seq[ListItem]): String = {
    val candidateSlug = listItem.date.yyyymmddString

    val listItemsWithSameDate = allListItems.reverse.filter(_.date.yyyymmddString == candidateSlug)

    if (listItemsWithSameDate.size == 1)
      candidateSlug
    else
      candidateSlug + "-" + (listItemsWithSameDate.indexOf(listItem) + 1)
  }

  def slugFromString(string: String): String =
    string.split('/').head.trim.toLowerCase.replaceAll(" ", "-")
}
