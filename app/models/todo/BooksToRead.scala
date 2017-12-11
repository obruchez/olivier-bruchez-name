package models.todo

import java.net.URL
import models._
import models.lifelogging.Books
import scala.util._
import util._

case class BooksToRead(override val introduction: Option[Introduction], content: HtmlContent)
    extends Cacheable

object BooksToRead extends Fetchable {
  type C = BooksToRead

  override val name = "Books to read"
  override val sourceUrl = Configuration.baseUrlWithFile("markdown/books-to-read.md").get
  override val icon = Books.icon

  override def fetch(): Try[BooksToRead] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[BooksToRead] =
    for {
      markdownContent <- MarkdownContent(url)
      (introduction, content) <- markdownContent.toIntroductionAndMainContent
    } yield BooksToRead(introduction, content)
}
