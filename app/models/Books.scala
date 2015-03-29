package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Elem, XML}
import util.Date

case class Book(date: Partial,
                author: String,
                title: String,
                subtitle: Option[String],
                year: Int,
                rating: Option[Double],
                comments: Option[String],
                url: URL)

case class Books(introduction: String, books: Seq[Book])

object Books {
  def apply(url: URL): Try[Books] = for {
    xml <- Try(XML.load(url))
    books <- apply(xml)
  } yield books

  def apply(elem: Elem): Try[Books] = Try {
    val books = (elem \\ "books").head
    val introduction = (books \\ "introduction").head.text

    val booksSeq = for {
      book <- books \\ "book"
      dateString = (book \\ "date").text.trim
      author = (book \\ "author").text.trim
      title = (book \\ "title").text.trim
      subtitle = (book \\ "subtitle").text.trim
      yearString = (book \\ "publishingyear").text.trim
      ratingString = (book \\ "rating").text.trim
      comments = (book \\ "comments").text.trim
      url = (book \\ "url").text.trim
    } yield Book(
      date = Date.partialFromYyyymmddString(dateString).get,
      author = author,
      title = title,
      subtitle = Option(subtitle).filter(_.nonEmpty),
      year = yearString.toInt,
      rating = Option(ratingString).filter(_.nonEmpty).map(_.toDouble - 1),
      comments = Option(comments).filter(_.nonEmpty),
      url = new URL(url))

    Books(introduction, booksSeq)
  }
}
