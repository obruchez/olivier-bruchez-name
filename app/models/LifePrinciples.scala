package models

import java.net.URL
import scala.util._
import util.{Configuration, HtmlContent}

// @todo implement parsing

case class LifePrinciples(override val introduction: HtmlContent) extends Cacheable {
  override val size = 0
}

object LifePrinciples extends Fetchable {
  type C = LifePrinciples

  override val name = "Life principles"
  override val sourceUrl = Configuration.url("url.lifeprinciples").get

  override def fetch(): Try[LifePrinciples] = apply(sourceUrl)

  def apply(url: URL): Try[LifePrinciples] = Failure(new NotImplementedError())
}
