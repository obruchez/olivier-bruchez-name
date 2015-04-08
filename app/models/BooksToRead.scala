package models

import java.net.URL
import scala.util._
import util._

case class BooksToRead(override val introductionOption: Option[Introduction],
                       content: HtmlContent) extends Cacheable

object BooksToRead extends Fetchable {
  type C = BooksToRead

  override val name = "Books to read"
  override val sourceUrl = Configuration.url("url.bookstoread").get

  override def fetch(): Try[BooksToRead] = apply(sourceUrl)

  def apply(url: URL): Try[BooksToRead] = for {
    markdownContent <- MarkdownContent(url)
    (introductionOption, content) <- markdownContent.toIntroductionAndMainContent
  } yield BooksToRead(introductionOption, content)
}
