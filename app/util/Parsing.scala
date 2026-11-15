package util

import java.net.URL
import models._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, NodeSeq}

object Parsing {
  def introductionFromNode(node: Node): Try[Option[Introduction]] = Try {
    val introductions = (node \\ "introduction").map { introductionNode =>
      MarkdownContent(introductionNode.text).toHtmlContent.get
    }

    val shortVersionOption = introductions.headOption
    val fullVersion = HtmlContent.fromHtmlString(introductions.map(_.htmlString).mkString(" "))

    shortVersionOption.map(shortVersion =>
      Introduction(shortVersion = shortVersion, fullVersion = fullVersion)
    )
  }

  def picturesFromNode(node: Node): Seq[Pictures] =
    (node \\ "pictures").map { pictures =>
      Pictures(
        title = Option(pictures.text.trim).filter(_.nonEmpty),
        url = Url.parse(pictures \@ "url")
      )
    }

  /** `directory` is the list's directory under data/images. */
  def imageFromNode(node: Node, directory: String): Option[String] =
    (node \ "image").headOption
      .map(_.text.trim)
      .filter(_.nonEmpty)
      // File names are stored with composed accents (NFC), whereas names pasted from the macOS
      // Finder come decomposed (NFD): "é" as "e" followed by a combining accent
      .map(file => java.text.Normalizer.normalize(file, java.text.Normalizer.Form.NFC))
      .map(file => s"$directory/$file")

  /** Where the image comes from (e.g. the photo in its Flickr album), if stated. */
  def imageUrlFromNode(node: Node): Option[String] =
    (node \ "image").headOption
      .map(image => (image \@ "url").trim)
      .filter(url => url.startsWith("https://") || url.startsWith("http://"))

  def commentsFromString(string: String): Option[HtmlContent] =
    Option(string).map(_.trim).filter(_.nonEmpty).map(HtmlContent(_))

  def commentsFromNodeChildren(nodeOption: Option[Node]): Option[HtmlContent] =
    nodeOption match {
      case None =>
        None
      case Some(node) =>
        commentsFromString(node.child.mkString)
    }

  def nonEmptyTexts(nodes: NodeSeq): Seq[String] =
    nodes.map(_.text.trim).filter(_.nonEmpty)

  def dateFromString(string: String): Try[Partial] =
    Date.partialFromYyyymmddString(string.trim)

  def ratingFromString(string: String): Option[Double] =
    Option(string).map(_.trim).filter(_.nonEmpty).map(_.toDouble - 1)

  def isTrue(string: String): Boolean =
    Set("on", "true", "y", "yes", "1").contains(string.trim.toLowerCase)
}
