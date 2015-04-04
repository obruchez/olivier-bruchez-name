package models

import java.net.URL
import scala.util._
import util.Configuration

// @todo implement parsing

case class BooksToRead(override val introduction: Introduction) extends Cacheable {
  override val size = 0
}

object BooksToRead extends Fetchable {
  type C = BooksToRead

  override val name = "Books to read"
  override val sourceUrl = Configuration.url("url.bookstoread").get

  override def fetch(): Try[BooksToRead] = apply(sourceUrl)

  def apply(url: URL): Try[BooksToRead] = Failure(new NotImplementedError())
}
