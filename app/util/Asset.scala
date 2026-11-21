package util

import controllers.routes

import java.security.MessageDigest
import scala.collection.concurrent.TrieMap
import scala.util.Using

object Asset {
  private val fingerprints = TrieMap[String, Option[String]]()

  /** Address of a script or style sheet, followed by a fingerprint of its content. Browsers keep
    * these files for a year (play.assets.cache in application.conf): without the fingerprint, a
    * visitor would go on using the previous ones after a new version of the site is deployed.
    */
  def url(file: String): String = {
    val address = routes.Assets.at(file).url
    fingerprints
      .getOrElseUpdate(file, fingerprint(file))
      .fold(address)(print => s"$address?v=$print")
  }

  private def fingerprint(file: String): Option[String] =
    Option(getClass.getResourceAsStream(s"/public/$file")).flatMap { stream =>
      Using(stream) { in =>
        MessageDigest
          .getInstance("SHA-1")
          .digest(in.readAllBytes())
          .take(5)
          .map("%02x".format(_))
          .mkString
      }.toOption
    }
}
