package models

import org.commonmark.Extension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.image.attributes.ImageAttributesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import util.FileType

import java.net.URL
import scala.io.{Codec, Source}
import scala.util._

case class MarkdownContent(lines: Seq[String]) extends FileContent(FileType.Markdown) {
  def withoutHeadingTitle: MarkdownContent = {
    val linesWithoutHeadingTitle = lines
      .dropWhile(_.trim.isEmpty)
      .dropWhile(_.startsWith(MarkdownContent.HeadingCharacter.toString))
      .dropWhile(_.trim.isEmpty)

    MarkdownContent(lines = linesWithoutHeadingTitle)
  }

  def toHtmlContent: Try[HtmlContent] = Try {
    val markdownString =
      lines.dropWhile(_.trim.isEmpty).reverse.dropWhile(_.trim.isEmpty).reverse.mkString("\n")
    val htmlString = MarkdownContent.renderer.render(MarkdownContent.parser.parse(markdownString))
    HtmlContent(htmlString).withNonBreakingSpaces.withoutRootParagraph
  }

  /** A whole document: paragraphs, headings, lists... (toHtmlContent is meant for short texts). */
  def toHtmlBlocks: Try[HtmlContent] = Try {
    val html = MarkdownContent.renderer.render(MarkdownContent.parser.parse(lines.mkString("\n")))
    HtmlContent(html).withNonBreakingSpaces
  }

  def toIntroductionAndMainContent: Try[(Option[Introduction], HtmlContent)] = Try {
    val withoutHeadingTitle = this.withoutHeadingTitle

    val indexOfContent = withoutHeadingTitle.lines.indexWhere(_.startsWith("## "))

    val (introductionLines, mainContentLines) =
      if (indexOfContent >= 0) {
        (
          withoutHeadingTitle.lines.take(indexOfContent).dropWhile(_.trim.isEmpty),
          withoutHeadingTitle.lines.drop(indexOfContent)
        )
      } else {
        (withoutHeadingTitle.lines, Seq())
      }

    val introductionHtmlOption =
      Some(introductionLines).filter(_.nonEmpty).map(MarkdownContent(_).toHtmlContent.get)
    val contentHtml =
      MarkdownContent(mainContentLines).toHtmlContent.get
        .withCssClassAddedToHeadings(heading = "h2", cssClass = MarkdownContent.HeadlineClass)

    (
      introductionHtmlOption.map { html =>
        Introduction(shortVersion = HtmlContent.fromNonHtmlString(""), fullVersion = html)
      },
      contentHtml
    )
  }
}

object MarkdownContent {
  def apply(string: String): MarkdownContent =
    MarkdownContent(string.split("\\r?\\n").toSeq)

  def apply(url: URL): Try[MarkdownContent] = Try {
    val markdownString = Source.fromURL(url)(using Codec("UTF-8")).mkString
    apply(markdownString)
  }

  // CommonMark, plus tables and image widths (![](image.jpg){width=400}). Raw HTML is let through: the
  // Markdown files are the site's own data.
  private val extensions =
    java.util.List.of[Extension](TablesExtension.create(), ImageAttributesExtension.create())
  private val parser = Parser.builder().extensions(extensions).build()
  private val renderer = HtmlRenderer.builder().extensions(extensions).build()

  val HeadingCharacter = '#'

  val HeadlineClass = "headline"
}
