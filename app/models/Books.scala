package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Elem, XML}
import util.HtmlContent

case class Book(override val date: Partial,
                author: String,
                title: String,
                subtitle: Option[String],
                year: Int,
                rating: Option[Double],
                comments: Option[HtmlContent],
                url: URL,
                override val slug: String = "") extends ListItem(date, slug)

case class Books(introduction: HtmlContent, books: Seq[Book])

object Books {
  def apply(url: URL): Try[Books] = for {
    xml <- Try(XML.load(url))
    books <- apply(xml)
  } yield books

  def apply(elem: Elem): Try[Books] = Try {
    val books = (elem \\ "books").head
    val introduction = Lists.introductionFromNode(books).get

    val booksSeq = for {
      book <- books \\ "book"
      dateString = (book \\ "date").text
      author = (book \\ "author").text
      title = (book \\ "title").text
      subtitle = (book \\ "subtitle").text
      yearString = (book \\ "publishingyear").text
      ratingString = (book \\ "rating").text
      comments = (book \\ "comments").text
      url = (book \\ "url").text
    } yield Book(
      date = Lists.dateFromString(dateString).get,
      author = author.trim,
      title = title.trim,
      subtitle = Option(subtitle.trim).filter(_.nonEmpty),
      year = yearString.trim.toInt,
      rating = Lists.ratingFromString(ratingString),
      comments = Lists.commentsFromString(comments),
      url = new URL(url))

    Books(introduction, booksSeq.map(book => book.copy(slug = Lists.slug(book, booksSeq))))
  }
}
