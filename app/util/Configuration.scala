package util

import java.net.URL
import play.api.Play

object Configuration {
  def string(path: String): Option[String] =
    Play.current.configuration.getString(path)

  def url(path: String): Option[URL] =
    string(path).map(new URL(_))

  def urlWithFile(path: String, file: String): Option[URL] = url(path) map { baseUrl =>
    Url.withSuffix(new URL(Strings.withSuffix(baseUrl.toString, "/")), file)
  }

  def baseUrlWithFile(file: String): Option[URL] =
    urlWithFile("url.base", file)
}
