package models

import java.net.URL
import scala.util._
import util.{Configuration, HtmlContent}

// @todo implement parsing

case class MoviesToWatch(override val introduction: HtmlContent) extends Cacheable {
  override val size = 0
}

object MoviesToWatch extends Fetchable {
  type C = MoviesToWatch

  override val name = "Movies to watch"
  override val sourceUrl = Configuration.url("url.moviestowatch").get

  override def fetch(): Try[MoviesToWatch] = apply(sourceUrl)

  def apply(url: URL): Try[MoviesToWatch] = Failure(new NotImplementedError())
}
