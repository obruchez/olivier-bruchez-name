package util

import java.net.URL
import org.joda.time.Duration
import scala.concurrent.duration._
import scala.util.Try

case class Introduction(shortVersion: HtmlContent, fullVersion: HtmlContent)

object Introduction {
  def apply(string: String): Introduction = {
    val htmlString = xml.Utility.escape(string)
    Introduction(shortVersion = HtmlContent(htmlString), fullVersion = HtmlContent(htmlString))
  }
}

trait Cacheable {
  //def sizeInBytes: Int
  def introduction: Option[Introduction] = None
  def subFetchables: Seq[Fetchable] = Seq()
}

trait Fetchable {
  type C <: Cacheable

  def name: String
  def sourceUrl: URL
  def maximumAge: Duration = new Duration(15.minutes.toMillis)
  def fetch(): Try[C]
}
