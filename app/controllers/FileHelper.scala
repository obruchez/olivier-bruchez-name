package controllers

import java.net.URL
import play.api.mvc._

sealed trait FileType
case object Markdown extends FileType
case object Pdf extends FileType
case object Other extends FileType

trait FileHelper {
  this: Controller =>

  implicit class UrlOps(url: URL) {
    def fileType: FileType = {
      val urlLowerCase = url.toString.toLowerCase
      if (urlLowerCase.endsWith(".md"))
        Markdown
      else if (urlLowerCase.endsWith(".pdf"))
        Pdf
      else
        Other
    }
  }
}
