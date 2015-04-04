package models

import java.net.URL
import scala.io.{Codec, Source}
import scala.util._
import util.{Configuration, HtmlContent}

case class BooksToRead(override val introduction: Introduction, content: HtmlContent) extends Cacheable {
  override val size = 0
}

object BooksToRead extends Fetchable {
  type C = BooksToRead

  override val name = "Books to read"
  override val sourceUrl = Configuration.url("url.bookstoread").get

  override def fetch(): Try[BooksToRead] = apply(sourceUrl)

  def apply(url: URL): Try[BooksToRead] = for {
    markdown <- Try(Source.fromURL(url)(Codec("UTF-8")).mkString)
    (introduction, content) <- Parsing.introductionAndContentFromMarkdown(markdown)
  } yield BooksToRead(introduction, content)
}
