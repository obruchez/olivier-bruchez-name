package models

import java.net.URL
import scala.util._
import util.Configuration

// @todo implement parsing

case class TripsToTake(override val introduction: Introduction) extends Cacheable {
  override val size = 0
}

object TripsToTake extends Fetchable {
  type C = TripsToTake

  override val name = "Trips to take"
  override val sourceUrl = Configuration.url("url.tripstotake").get

  override def fetch(): Try[TripsToTake] = apply(sourceUrl)

  def apply(url: URL): Try[TripsToTake] = Failure(new NotImplementedError())
}
