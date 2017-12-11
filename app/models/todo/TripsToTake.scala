package models.todo

import java.net.URL
import models._
import models.lifelogging.Trips
import scala.util._
import util._

case class TripsToTake(override val introduction: Option[Introduction], content: HtmlContent)
    extends Cacheable

object TripsToTake extends Fetchable {
  type C = TripsToTake

  override val name = "Trips to take"
  override val sourceUrl = Configuration.baseUrlWithFile("markdown/trips-to-take.md").get
  override val icon = Trips.icon

  override def fetch(): Try[TripsToTake] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[TripsToTake] =
    for {
      markdownContent <- MarkdownContent(url)
      (introduction, content) <- markdownContent.toIntroductionAndMainContent
    } yield TripsToTake(introduction, content)
}
