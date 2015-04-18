package models

import java.net.URL
import scala.util._
import util._

abstract class FileContent(val fileType: FileType) extends Cacheable

case class FileSource(override val name: String, override val sourceUrl: URL) extends Fetchable {
  type C = FileContent

  override def fetch(): Try[FileContent] = {
    FileType.fileTypeFromUrl(sourceUrl) match {
      case FileType.Html =>
        HtmlContent(sourceUrl)
      case FileType.Markdown =>
        MarkdownContent(sourceUrl)
      case _ =>
        BinaryContent(sourceUrl)
    }
  }
}
