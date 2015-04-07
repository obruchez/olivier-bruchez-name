package models

import java.net.URL
import scala.io.{Codec, Source}
import scala.util._
import util.{Configuration, HtmlContent, Markdown}

case class Votes(override val introduction: Introduction, content: HtmlContent) extends Cacheable

object Votes extends Fetchable {
  type C = Votes

  override val name = "Votes"
  override val sourceUrl = Configuration.url("url.votes").get

  override def fetch(): Try[Votes] = apply(sourceUrl)

  def apply(url: URL): Try[Votes] = for {
    markdown <- Try(Source.fromURL(url)(Codec("UTF-8")).mkString)
    (introduction, content) <- Markdown.introductionAndContentFromMarkdown(markdown)
  } yield Votes(introduction, content)
}
