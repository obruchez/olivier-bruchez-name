package models

import util._

import java.net.URL
import scala.util._

abstract class FileContent(val fileType: FileType) extends Cacheable

case class FileSource(override val name: String, override val sourceUrl: URL) extends Fetchable {
  type C = FileContent

  // A file source is a key of the cache. java.net.URL compares and hashes the IP address its host
  // resolves to, which changes from one moment to the next for a host with several addresses (GitHub):
  // the same file would then not be found in the cache. Compare the address as text instead.
  private def key: (String, String) = (name, sourceUrl.toString)

  override def equals(other: Any): Boolean = other match {
    case that: FileSource => key == that.key
    case _                => false
  }

  override def hashCode: Int = key.hashCode

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
