package models

import java.net.URL
import scala.io.{Codec, Source}
import scala.util._
import util.{Configuration, HtmlContent}

case class MoviesToWatch(override val introduction: Introduction, content: HtmlContent) extends Cacheable {
  override val size = 0
}

object MoviesToWatch extends Fetchable {
  type C = MoviesToWatch

  override val name = "Movies to watch"
  override val sourceUrl = Configuration.url("url.moviestowatch").get

  override def fetch(): Try[MoviesToWatch] = apply(sourceUrl)

  def apply(url: URL): Try[MoviesToWatch] = for {
   markdown <- Try(Source.fromURL(url)(Codec("UTF-8")).mkString)
   (introduction, content) <- Parsing.introductionAndContentFromMarkdown(markdown)
 } yield MoviesToWatch(introduction, content)
}
