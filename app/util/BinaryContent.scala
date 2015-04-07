package util

import java.net.URL
import scala.io.{Codec, Source}
import scala.util.Try

case class BinaryContent(content: Array[Byte], fileType: FileType)

object BinaryContent {
  def apply(url: URL): Try[BinaryContent] = Try {
    val content = Source.fromURL(url)(Codec.ISO8859).map(_.toByte).toArray
    val fileType = FileType.fileTypeFromUrl(url)

    BinaryContent(content, fileType)
  }
}
