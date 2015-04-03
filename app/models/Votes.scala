package models

import java.net.URL
import scala.util._
import util.{Configuration, HtmlContent}

// @todo implement parsing

case class Votes(override val introduction: HtmlContent) extends Cacheable {
  override val size = 0
}

object Votes extends Fetchable {
  type C = Votes

  override val name = "Votes"
  override val sourceUrl = Configuration.url("url.votes").get

  override def fetch(): Try[Votes] = apply(sourceUrl)

  def apply(url: URL): Try[Votes] = Failure(new NotImplementedError())
}
