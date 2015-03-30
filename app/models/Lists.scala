package models

import org.joda.time.Partial
import scala.util.Try
import scala.xml.Node
import util._

abstract class ListItem(val date: Partial)

object Lists {
  def introductionFromNode(node: Node): Try[HtmlContent] =
    HtmlContent.fromMarkdown((node \\ "introduction").head.text)

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
}
