package models

import java.net.URL
import scala.util._
import util._

case class SeenOnTv(override val introduction: Option[Introduction],
                    content: HtmlContent) extends Cacheable

object SeenOnTv extends Fetchable {
  type C = SeenOnTv

  override val name = "Seen on TV"
  override val sourceUrl = Configuration.baseUrlWithFile("markdown/seenontv.md").get

  override def fetch(): Try[SeenOnTv] = apply(sourceUrl)

  def apply(url: URL): Try[SeenOnTv] = for {
    markdownContent <- MarkdownContent(url)
    (introduction, content) <- markdownContent.toIntroductionAndMainContent
  } yield SeenOnTv(introduction, content)
}
