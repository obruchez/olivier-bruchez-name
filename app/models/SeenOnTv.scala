package models

import util._

import java.net.URL
import scala.util._

case class SeenOnTv(override val introduction: Option[Introduction], content: HtmlContent)
    extends Cacheable

object SeenOnTv extends Fetchable {
  type C = SeenOnTv

  override val name = "Seen on TV"
  override val sourceUrl = Configuration.baseUrlWithFile("markdown/seen-on-tv.md").get
  override val icon = Some("fa-television")

  override def fetch(): Try[SeenOnTv] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[SeenOnTv] =
    for {
      markdownContent <- MarkdownContent(url)
      (introduction, content) <- markdownContent.toIntroductionAndMainContent
    } yield SeenOnTv(introduction, content)
}
