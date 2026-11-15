package util

import java.net.{URI, URL}

object Url {
  // java.net.URL's constructor is deprecated in favour of URI#toURL, but URI is stricter and some
  // of the URLs found in the data files (spaces, accents) would be rejected.
  @annotation.nowarn("cat=deprecation")
  def parse(string: String): URL = new URL(string)

  def withSuffix(url: URL, suffix: String): URL = {
    val urlScheme = url.getProtocol
    val urlWithoutScheme = Strings.withoutPrefix(url.toString, urlScheme + ":")

    // Use URI to get correct RFC 3986 encoding
    val uri = new URI(urlScheme, urlWithoutScheme + suffix, null)
    parse(uri.toASCIIString)
  }
}
