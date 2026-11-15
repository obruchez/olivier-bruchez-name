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

  def url(path: String): Option[URL] =
    string(path).map(Url.parse)

  def urlWithFile(path: String, file: String): Option[URL] = url(path) map { baseUrl =>
    Url.withSuffix(Url.parse(Strings.withSuffix(baseUrl.toString, "/")), file)
  }

  def baseUrlWithFile(file: String): Option[URL] =
    urlWithFile("url.base", file)
}
