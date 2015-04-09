package util

import java.net.URL
import scala.io.{Codec, Source}
import scala.util.Try

case class HtmlContent(string: String) extends FileContent(FileType.Html) {
  def withoutRootParagraph: HtmlContent = {
    val trimmed = string.trim

    if (trimmed.startsWith("<p>") && trimmed.endsWith("</p>"))
      copy(string = trimmed.substring("<p>".length, trimmed.length - "</p>".length))
    else
      this
  }

  def withNonBreakingSpaces: HtmlContent = {
    val updatedString = HtmlContent.charactersWithNonBreakingSpaces.foldLeft(string) {
      case (s, c) => s.replaceAll(s" \\$c", s"&nbsp;$c")
    }

    copy(string = updatedString)
  }

  def withCssClassAddedToHeadings(heading: String, cssClass: String): HtmlContent = {
    val quotes = "\""
    val updatedString = string.replaceAll(s"(<$heading>.*</$heading>)", s"<div class=$quotes$cssClass$quotes>$$1</div>")

    copy(string = updatedString)
  }
}

object HtmlContent {
  def apply(url: URL): Try[HtmlContent] = Try {
    val markdownString = Source.fromURL(url)(Codec("UTF-8")).mkString
    apply(markdownString)
  }

  private val charactersWithNonBreakingSpaces = Seq(';', ':', '!', '?', '/', '–', '—', '»')
}
