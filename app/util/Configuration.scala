package util

import java.net.URL
import play.api.Play

object Configuration {
  def url(path: String): Option[URL] =
    Play.current.configuration.getString(path).map(new URL(_))
}
