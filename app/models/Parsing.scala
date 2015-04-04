package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.Node
import util._

object Parsing {
  def introductionFromNode(node: Node): Try[Introduction] = Try {
    val introductions = for {
      introductionNode <- node \\ "introduction"
      introductionAsMarkdown = introductionNode.text
      introductionAsHtmlTry = HtmlContent.fromMarkdown(introductionAsMarkdown)
    } yield introductionAsHtmlTry.get

    val shortVersion = introductions.head
    val fullVersion = HtmlContent(string = introductions.map(_.string).mkString(" "))

    Introduction(shortVersion = shortVersion, fullVersion = fullVersion)
  }

  def picturesFromNode(node: Node): Seq[Pictures] = for {
    pictures <- node \\ "pictures"
    title = pictures.text
    url = pictures \@ "url"
  } yield Pictures(
    title = Option(title.trim).filter(_.nonEmpty),
    url = new URL(url))

  def commentsFromString(string: String): Option[HtmlContent] =
    Option(string).map(_.trim).filter(_.nonEmpty).map(HtmlContent(_))

  def dateFromString(string: String): Try[Partial] =
    Date.partialFromYyyymmddString(string.trim)

  def ratingFromString(string: String): Option[Double] =
    Option(string).map(_.trim).filter(_.nonEmpty).map(_.toDouble - 1)

  def slugFromString(string: String): String =
    string.split('/').head.trim.toLowerCase.replaceAll(" ", "-")

  def isTrue(string: String): Boolean =
    Set("on", "true", "y", "yes", "1").contains(string.trim.toLowerCase)

  def introductionAndContentFromMarkdown(markdown: String): Try[(Introduction, HtmlContent)] = Try {
    val lines = markdown.split("\\r?\\n").toSeq

    val linesWithoutTitle = if (lines.head.startsWith("# ")) lines.drop(1) else lines

    val indexOfContent = linesWithoutTitle.indexWhere(_.startsWith("## "))

    val (introductionLines, contentLines) = if (indexOfContent >= 0)
      (linesWithoutTitle.take(indexOfContent), linesWithoutTitle.drop(indexOfContent))
    else
      (linesWithoutTitle, Seq())

    def htmlFromMarkdownLines(lines: Seq[String]): HtmlContent =
      HtmlContent.fromMarkdown(
        lines.dropWhile(_.trim.isEmpty).reverse.dropWhile(_.trim.isEmpty).reverse.mkString("\n")).get

    val introduction = htmlFromMarkdownLines(introductionLines)
    val content = htmlFromMarkdownLines(contentLines)

    (Introduction(shortVersion = introduction, fullVersion = introduction), content)
  }
}
