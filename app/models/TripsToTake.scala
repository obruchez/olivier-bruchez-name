package models

import java.net.URL
import scala.util._
import util._

case class TripsToTake(override val introductionOption: Option[Introduction],
                       content: HtmlContent) extends Cacheable

object TripsToTake extends Fetchable {
  type C = TripsToTake

  override val name = "Trips to take"
  override val sourceUrl = Configuration.url("url.tripstotake").get

  override def fetch(): Try[TripsToTake] = apply(sourceUrl)

  def apply(url: URL): Try[TripsToTake] = for {
    markdownContent <- MarkdownContent(url)
    (introductionOption, content) <- markdownContent.toIntroductionAndMainContent
   } yield TripsToTake(introductionOption, content)
}
