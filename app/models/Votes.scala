package models

import java.net.URL
import scala.util._
import util._

case class Votes(override val introductionOption: Option[Introduction],
                 content: HtmlContent) extends Cacheable

object Votes extends Fetchable {
  type C = Votes

  override val name = "Votes"
  override val sourceUrl = Configuration.url("url.votes").get

  override def fetch(): Try[Votes] = apply(sourceUrl)

  def apply(url: URL): Try[Votes] = for {
    markdownContent <- MarkdownContent(url)
    (introductionOption, content) <- markdownContent.toIntroductionAndMainContent
  } yield Votes(introductionOption, content)
}
