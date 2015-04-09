package util

import java.net.URL

sealed abstract class FileType(val mimeType: String)

object FileType {
  case object Markdown extends FileType("text/x-markdown")
  case object Pdf extends FileType("application/pdf")
  case object Word extends FileType("application/msword")
  case object Html extends FileType("text/html")
  case object Other extends FileType("application/octet-stream")

  def fileTypeFromFilename(filename: String): FileType = {
    val lowerCaseFilename = filename.toLowerCase.trim
    if (lowerCaseFilename.endsWith(".md"))
      Markdown
    else if (lowerCaseFilename.endsWith(".pdf"))
      Pdf
    else if (lowerCaseFilename.endsWith(".doc"))
      Word
    else if (lowerCaseFilename.endsWith(".html"))
      Html
    else
      Other
  }

  def fileTypeFromUrl(url: URL): FileType =
    fileTypeFromFilename(url.toString)
}
