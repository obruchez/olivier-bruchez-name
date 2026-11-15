package models

import util._

import java.net.URL
import scala.util._

case class Votes(override val introduction: Option[Introduction], content: HtmlContent)
    extends Cacheable

object Votes extends Fetchable {
  type C = Votes

  override val name = "Votes"
  override val sourceUrl = Configuration.baseUrlWithFile("markdown/votes.md").get

  override def fetch(): Try[Votes] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Votes] =
    for {
      markdownContent <- MarkdownContent(url)
      (introduction, content) <- markdownContent.toIntroductionAndMainContent
    } yield Votes(introduction, withMarkedAnswers(content))

  // "Oui. Résultat : non (54.79%)." right after a question: my vote and the outcome, marked up so
  // that they can be told apart from the question at a glance
  private val Answer =
    """<p>(Oui|Non|\?)\. Résultat(?:&nbsp;| ):(?:&nbsp;| )(\S+) \(([^)]*)\)\.?</p>""".r

  private def withMarkedAnswers(content: HtmlContent): HtmlContent = {
    val updatedString = Answer.replaceAllIn(
      content.htmlString,
      m => {
        val (vote, cssClass) = m.group(1) match {
          case "Oui" => ("Oui", "vote-yes")
          case "Non" => ("Non", "vote-no")
          case _     => ("?", "vote-unknown")
        }

        val result = m.group(2) match {
          case "?"     => """<span class="vote-result">Résultat à venir</span>"""
          case outcome =>
            s"""<span class="vote-result">Résultat&nbsp;: $outcome (${m.group(3)})</span>"""
        }

        scala.util.matching.Regex.quoteReplacement(
          s"""<p class="vote"><span class="vote-mine $cssClass">$vote</span> $result</p>"""
        )
      }
    )

    content.copy(htmlString = updatedString)
  }
}
