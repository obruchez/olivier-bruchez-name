package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Elem, XML}
import util._

case class BookNotes(description: Option[String], url: URL, slug: String) {
  def fileSource: FileSource =
    FileSource(name = description.getOrElse(BookNotes.DefaultDescription), sourceUrl = url)
}

object BookNotes {
  val DefaultDescription = "Notes"
}

case class Book(override val date: Partial,
                author: String,
                title: String,
                subtitle: Option[String],
                year: Int,
                rating: Option[Double],
                comments: Option[HtmlContent],
                url: URL,
                notes: Option[BookNotes],
                override val slug: String = "") extends ListItem(date, slug)

case class Books(override val introduction: Option[Introduction],
                 books: Seq[Book]) extends Cacheable {
  def notesFromSlug(slug: String): Option[BookNotes] =
    books.find(_.notes.exists(_.slug == slug)).flatMap(_.notes)

  override def subFetchables: Seq[FileSource] = for {
    book <- books
    notes <- book.notes
  } yield notes.fileSource
}

object Books extends Fetchable {
  type C = Books

  override val name = "Books"
  override val sourceUrl = Configuration.url("url.books").get

  override def fetch(): Try[Books] = apply(sourceUrl)

  def apply(url: URL): Try[Books] = for {
    xml <- Try(XML.load(url))
    books <- apply(xml)
  } yield books

  def apply(elem: Elem): Try[Books] = Try {
    val books = (elem \\ "books").head
    val introduction = Parsing.introductionFromNode(books).get

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
    } yield {
      val notesOption = for {
        notes <- (book \\ "notes").headOption
        notesUrl = notes.text.trim
        if notesUrl.nonEmpty
        notesDescription = notes \@ "description"
      } yield BookNotes(
        description = Option(notesDescription.trim).filter(_.nonEmpty),
        url = new URL(notesUrl),
        slug = Slug.slugFromString(s"$author $title"))

      Book(
        date = Parsing.dateFromString(dateString).get,
        author = author.trim,
        title = title.trim,
        subtitle = Option(subtitle.trim).filter(_.nonEmpty),
        year = yearString.trim.toInt,
        rating = Parsing.ratingFromString(ratingString),
        comments = Parsing.commentsFromString(comments),
        url = new URL(url),
        notesOption)}

    Books(introduction, booksSeq.map(book => book.copy(slug = ListItem.slug(book, booksSeq))))
  }
}
