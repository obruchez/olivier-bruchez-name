package models

import scala.util.Try
import util._

case class PdfCv(binaryContent: BinaryContent) extends Cacheable

object PdfCv extends Fetchable {
  type C = PdfCv

  override val name = "PDF"
  override val sourceUrl = Configuration.url("url.cv.pdf").get

  override def fetch(): Try[PdfCv] = BinaryContent(sourceUrl).map(PdfCv(_))

  val DownloadFilename = sourceUrl.toString.split("/").last
}

case class WordCv(binaryContent: BinaryContent) extends Cacheable

object WordCv extends Fetchable {
  type C = WordCv

  override val name = "Word"
  override val sourceUrl = Configuration.url("url.cv.word").get

  override def fetch(): Try[WordCv] = BinaryContent(sourceUrl).map(WordCv(_))

  val DownloadFilename = sourceUrl.toString.split("/").last
}
