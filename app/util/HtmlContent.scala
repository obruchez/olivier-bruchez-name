package util

case class HtmlContent(string: String) {
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
  private val charactersWithNonBreakingSpaces = Seq(';', ':', '!', '?', '/', '–', '—', '»')
}
