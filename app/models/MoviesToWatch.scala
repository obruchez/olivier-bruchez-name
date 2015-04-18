package models

import java.net.URL
import scala.util._
import util._

case class MoviesToWatch(override val introduction: Option[Introduction],
                         content: HtmlContent) extends Cacheable

object MoviesToWatch extends Fetchable {
  type C = MoviesToWatch

  override val name = "Movies to watch"
  override val sourceUrl = Configuration.baseUrlWithFile("markdown/moviestowatch.md").get
  override val icon = Movies.icon

  override def fetch(): Try[MoviesToWatch] = apply(sourceUrl)

  def apply(url: URL): Try[MoviesToWatch] = for {
    markdownContent <- MarkdownContent(url)
    (introduction, content) <- markdownContent.toIntroductionAndMainContent
 } yield MoviesToWatch(introduction, content)
}
