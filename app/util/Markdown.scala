package util

import models.Introduction

import scala.util.Try

object Markdown {
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

    (Introduction(shortVersion = introduction, fullVersion = introduction), withHeadlineHeaders(content))
  }

  def withHeadlineHeaders(htmlContent: HtmlContent, header: String = "h2"): HtmlContent = {
    val updated = htmlContent.string.replaceAll(s"(<$header>.*</$header>)", "<div class=\"headline\">$1</div>")
    HtmlContent(updated)
  }
}
