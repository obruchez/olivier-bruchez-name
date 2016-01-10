package models

import scala.util.Try
import util._

case class PdfCv(binaryContent: BinaryContent) extends Cacheable

object PdfCv extends Fetchable {
  type C = PdfCv

  override val name = "PDF"
  override val sourceUrl = Configuration.baseUrlWithFile("cv/ResumeOlivierBruchez.pdf").get
  override val icon = Some("fa-file-pdf-o")

  override def fetch(): Try[PdfCv] = BinaryContent(sourceUrlWithNoCacheParameter).map(PdfCv(_))

  val DownloadFilename = sourceUrl.toString.split("/").last
}

case class WordCv(binaryContent: BinaryContent) extends Cacheable

object WordCv extends Fetchable {
  type C = WordCv

  override val name = "Word"
  override val sourceUrl = Configuration.baseUrlWithFile("cv/ResumeOlivierBruchez.doc").get
  override val icon = Some("fa-file-word-o")

  override def fetch(): Try[WordCv] = BinaryContent(sourceUrlWithNoCacheParameter).map(WordCv(_))

  val DownloadFilename = sourceUrl.toString.split("/").last
}
