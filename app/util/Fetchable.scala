package util

import java.net.URL

import models._

import scala.concurrent.duration._
import scala.util.Try

case class Introduction(shortVersion: HtmlContent, fullVersion: HtmlContent)

trait Cacheable {
  //def sizeInBytes: Int
  def introduction: Option[Introduction] = None
  def subFetchables: Seq[Fetchable] = Seq()
}

trait Fetchable {
  type C <: Cacheable

  def name: String
  def sourceUrl: URL
  def fetchPeriod: FiniteDuration = 60.seconds
  def fetch(): Try[C]
}

object Fetchable {
  // @todo retrieve this list via Sitemapg
  val allFetchables = Seq(
    Books, BooksToRead, Concerts, Courses, Crashes, Exhibitions, Hikes, LifePrinciples,
    Movies, MoviesToWatch, Plays, Profile, SeenOnTv, Trips, TripsToTake, Votes, Worldview,
    PdfCv, WordCv)
}