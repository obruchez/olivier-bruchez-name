package util

import java.net.{URI, URL}

object Url {
  def withSuffix(url: URL, suffix: String): URL = {
    val urlScheme = url.getProtocol
    val urlWithoutScheme = Strings.withoutPrefix(url.toString, urlScheme + ":")

    // Use URI to get correct RFC 3986 encoding
    val uri = new URI(urlScheme, urlWithoutScheme + suffix, null)
    new URL(uri.toASCIIString)
  }
}
