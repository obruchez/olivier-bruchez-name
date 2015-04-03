package models

import java.net.URL
import scala.util._
import util.{Configuration, HtmlContent}

// @todo implement parsing

case class Coursera(override val introduction: HtmlContent) extends Cacheable {
  override val size = 0
}

object Coursera extends Fetchable {
  type C = Coursera

  override val name = "Coursera"
  override val sourceUrl = Configuration.url("url.coursera").get

  override def fetch(): Try[Coursera] = apply(sourceUrl)

  def apply(url: URL): Try[Coursera] = Failure(new NotImplementedError())
}
