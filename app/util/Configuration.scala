package util

import java.net.URL

object Configuration {
  // Set once at startup by Startup, before anything is fetched
  @volatile var configuration: play.api.Configuration = scala.compiletime.uninitialized

  def string(path: String): Option[String] =
    configuration.getOptional[String](path)

  /** Google Analytics 4 measurement ID ("G-..."); no tracking when absent. */
  def analyticsMeasurementId: Option[String] =
    string("analytics.measurementid").map(_.trim).filter(_.nonEmpty)

  /** Disqus site name ("shortname"); no comments when absent. */
  def disqusShortname: Option[String] =
    string("disqus.shortname").map(_.trim).filter(_.nonEmpty)

  /** Address of the site, without a final slash: the absolute URLs that link previews need. */
  def siteUrl: String =
    string("site.url").map(_.trim.stripSuffix("/")).filter(_.nonEmpty).getOrElse(DefaultSiteUrl)

  private val DefaultSiteUrl = "https://olivier.bruchez.name"

  def url(path: String): Option[URL] =
    string(path).map(Url.parse)

  def urlWithFile(path: String, file: String): Option[URL] = url(path) map { baseUrl =>
    Url.withSuffix(Url.parse(Strings.withSuffix(baseUrl.toString, "/")), file)
  }

  def baseUrlWithFile(file: String): Option[URL] =
    urlWithFile("url.base", file)
}
