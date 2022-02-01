package models

import util.FileType

import java.net.URL
import scala.io.{Codec, Source}
import scala.util.Try

case class BinaryContent(content: Array[Byte], override val fileType: FileType)
    extends FileContent(fileType)

object BinaryContent {
  def apply(url: URL): Try[BinaryContent] = Try {
    val content = Source.fromURL(url)(Codec.ISO8859).map(_.toByte).toArray
    val fileType = FileType.fileTypeFromUrl(url)

    BinaryContent(content, fileType)
  }
}
