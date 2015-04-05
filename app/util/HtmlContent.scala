package util

import com.github.rjeschke.txtmark._
import scala.util._

case class HtmlContent(string: String)

object HtmlContent {
  def fromMarkdown(markdown: String): Try[HtmlContent] = Try {
    HtmlContent(string = withoutRootParagraph {
      withNonBreakingSpaces {
        Processor.process(markdown)
      }})
  }

  private def withoutRootParagraph(html: String): String = {
    val trimmedHtml = html.trim

    if (trimmedHtml.startsWith("<p>") && trimmedHtml.endsWith("</p>"))
      trimmedHtml.substring("<p>".length, trimmedHtml.length - "</p>".length)
    else
      html
  }

  private def withNonBreakingSpaces(html: String): String =
    charactersWithNonBreakingSpaces.foldLeft(html) { case (s, c) => s.replaceAll(s" \\$c", s"&nbsp;$c") }

  private val charactersWithNonBreakingSpaces = Seq(';', ':', '!', '?', '/', '–', '—', '»')
}
