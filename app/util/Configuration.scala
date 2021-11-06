package util

import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigurationSetter @Inject()(configuration: play.api.Configuration) {
  Configuration.configuration = configuration
}

object Configuration {
  var configuration: play.api.Configuration = _

  def string(path: String): Option[String] =
    configuration.getOptional[String](path)

  def url(path: String): Option[URL] =
    string(path).map(new URL(_))

  def urlWithFile(path: String, file: String): Option[URL] = url(path) map { baseUrl =>
    Url.withSuffix(new URL(Strings.withSuffix(baseUrl.toString, "/")), file)
  }

  def baseUrlWithFile(file: String): Option[URL] =
    urlWithFile("url.base", file)
}
