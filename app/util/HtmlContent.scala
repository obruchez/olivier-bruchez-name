package util

import com.github.rjeschke.txtmark._
import scala.util._

case class HtmlContent(string: String) {
  def withoutRootParagraph: HtmlContent = {
    val trimmed = string.trim

    if (trimmed.startsWith("<p>") && trimmed.endsWith("</p>"))
      copy(string = trimmed.substring("<p>".length, trimmed.length - "</p>".length))
    else
      this
  }

  def withNonBreakingSpaces: HtmlContent = copy(
    string = HtmlContent.charactersWithNonBreakingSpaces.foldLeft(string) {
      case (s, c) => s.replaceAll(s" \\$c", s"&nbsp;$c")
    })
}

object HtmlContent {
  def fromMarkdown(markdown: String): Try[HtmlContent] = Try {
    HtmlContent(Processor.process(markdown)).withNonBreakingSpaces.withoutRootParagraph
  }

  private val charactersWithNonBreakingSpaces = Seq(';', ':', '!', '?', '/', '–', '—', '»')
}
