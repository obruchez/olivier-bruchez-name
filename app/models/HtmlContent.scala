package models

import java.net.URL
import play.twirl.api.Html
import scala.io.{Codec, Source}
import scala.util.Try
import util.FileType

case class HtmlContent(htmlString: String) extends FileContent(FileType.Html) {
  def withoutRootParagraph: HtmlContent = {
    val trimmed = htmlString.trim

    if (trimmed.startsWith("<p>") && trimmed.endsWith("</p>"))
      copy(htmlString = trimmed.substring("<p>".length, trimmed.length - "</p>".length))
    else
      this
  }

  def withNonBreakingSpaces: HtmlContent = {
    val updatedString = HtmlContent.charactersWithNonBreakingSpaces.foldLeft(htmlString) {
      case (s, c) => s.replaceAll(s" \\$c", s"&nbsp;$c")
    }

    copy(htmlString = updatedString)
  }

  def withCssClassAddedToHeadings(heading: String, cssClass: String): HtmlContent = {
    val quotes = "\""
    val updatedString = htmlString.replaceAll(s"(<$heading>.*</$heading>)", s"<div class=$quotes$cssClass$quotes>$$1</div>")

    copy(htmlString = updatedString)
  }

  def html: Html =
    Html(htmlString)
}

object HtmlContent {
  def apply(url: URL): Try[HtmlContent] = Try {
    val markdownString = Source.fromURL(url)(Codec("UTF-8")).mkString
    apply(markdownString)
  }

  def fromHtmlString(string: String): HtmlContent =
    apply(string)

  def fromNonHtmlString(string: String): HtmlContent =
    apply(xml.Utility.escape(string))

  private val charactersWithNonBreakingSpaces = Seq(';', ':', '!', '?', '/', '–', '—', '»')
}
