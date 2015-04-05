package models

import java.net.URL
import scala.io.{Codec, Source}
import scala.util._
import util.{Configuration, HtmlContent, Markdown}

case class TripsToTake(override val introduction: Introduction, content: HtmlContent) extends Cacheable {
  override val size = 0
}

object TripsToTake extends Fetchable {
  type C = TripsToTake

  override val name = "Trips to take"
  override val sourceUrl = Configuration.url("url.tripstotake").get

  override def fetch(): Try[TripsToTake] = apply(sourceUrl)

  def apply(url: URL): Try[TripsToTake] = for {
     markdown <- Try(Source.fromURL(url)(Codec("UTF-8")).mkString)
     (introduction, content) <- Markdown.introductionAndContentFromMarkdown(markdown)
   } yield TripsToTake(introduction, content)
}
