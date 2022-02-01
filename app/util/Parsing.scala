package util

import java.net.URL
import models._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.Node

object Parsing {
  def introductionFromNode(node: Node): Try[Option[Introduction]] = Try {
    val introductions = for {
      introductionNode <- node \\ "introduction"
      introductionAsMarkdown = introductionNode.text
      introductionAsHtmlTry = MarkdownContent(introductionAsMarkdown).toHtmlContent
    } yield introductionAsHtmlTry.get

    val shortVersionOption = introductions.headOption
    val fullVersion = HtmlContent.fromHtmlString(introductions.map(_.htmlString).mkString(" "))

    shortVersionOption.map(shortVersion =>
      Introduction(shortVersion = shortVersion, fullVersion = fullVersion)
    )
  }

  def picturesFromNode(node: Node): Seq[Pictures] =
    for {
      pictures <- node \\ "pictures"
      title = pictures.text
      url = pictures \@ "url"
    } yield Pictures(title = Option(title.trim).filter(_.nonEmpty), url = new URL(url))

  def commentsFromString(string: String): Option[HtmlContent] =
    Option(string).map(_.trim).filter(_.nonEmpty).map(HtmlContent(_))

  def commentsFromNodeChildren(nodeOption: Option[Node]): Option[HtmlContent] =
    nodeOption match {
      case None =>
        None
      case Some(node) =>
        commentsFromString(node.child.mkString)
    }

  def dateFromString(string: String): Try[Partial] =
    Date.partialFromYyyymmddString(string.trim)

  def ratingFromString(string: String): Option[Double] =
    Option(string).map(_.trim).filter(_.nonEmpty).map(_.toDouble - 1)

  def isTrue(string: String): Boolean =
    Set("on", "true", "y", "yes", "1").contains(string.trim.toLowerCase)
}
