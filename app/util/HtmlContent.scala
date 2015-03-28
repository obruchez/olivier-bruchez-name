package util

import com.github.rjeschke.txtmark._
import scala.util._

case class HtmlContent(string: String)

object HtmlContent {
  def fromMarkdown(markdown: String): Try[HtmlContent] = Try {
    HtmlContent(string = withoutRootParagraph(Processor.process(markdown)))
  }

  private def withoutRootParagraph(html: String): String = {
    val trimmedHtml = html.trim

    if (trimmedHtml.startsWith("<p>") && trimmedHtml.endsWith("</p>"))
      trimmedHtml.substring("<p>".length, trimmedHtml.length - "</p>".length)
    else
      html
  }
}
