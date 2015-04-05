package models

import java.net.URL
import scala.io.{Codec, Source}
import scala.util._
import util.{Configuration, HtmlContent, Markdown}

case class SeenOnTv(override val introduction: Introduction, content: HtmlContent) extends Cacheable {
  override val size = 0
}

object SeenOnTv extends Fetchable {
  type C = SeenOnTv

  override val name = "Seen on TV"
  override val sourceUrl = Configuration.url("url.seenontv").get

  override def fetch(): Try[SeenOnTv] = apply(sourceUrl)

  def apply(url: URL): Try[SeenOnTv] = for {
    markdown <- Try(Source.fromURL(url)(Codec("UTF-8")).mkString)
    (introduction, content) <- Markdown.introductionAndContentFromMarkdown(markdown)
  } yield SeenOnTv(introduction, content)
}
