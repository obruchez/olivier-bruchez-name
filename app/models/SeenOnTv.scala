package models

import java.net.URL
import scala.util._
import util.{Configuration, HtmlContent}

// @todo implement parsing

case class SeenOnTv(override val introduction: HtmlContent) extends Cacheable {
  override val size = 0
}

object SeenOnTv extends Fetchable {
  type C = SeenOnTv

  override val name = "Seen on TV"
  override val sourceUrl = Configuration.url("url.seenontv").get

  override def fetch(): Try[SeenOnTv] = apply(sourceUrl)

  def apply(url: URL): Try[SeenOnTv] = Failure(new NotImplementedError())
}
