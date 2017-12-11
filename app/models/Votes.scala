package models

import java.net.URL
import scala.util._
import util._

case class Votes(override val introduction: Option[Introduction], content: HtmlContent)
    extends Cacheable

object Votes extends Fetchable {
  type C = Votes

  override val name = "Votes"
  override val sourceUrl = Configuration.baseUrlWithFile("markdown/votes.md").get
  override val icon = Some("fa-bullhorn")

  override def fetch(): Try[Votes] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Votes] =
    for {
      markdownContent <- MarkdownContent(url)
      (introduction, content) <- markdownContent.toIntroductionAndMainContent
    } yield Votes(introduction, content)
}
