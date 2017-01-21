package models.lifelogging

import java.net.URL
import models._
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{ Node, XML }
import util.Date._
import util._

case class BookNotes(description: Option[String], url: URL, slug: String) {
  def fileSource: FileSource =
    FileSource(name = description.getOrElse(BookNotes.DefaultDescription), sourceUrl = url)
}

object BookNotes {
  val DefaultDescription = "Notes"

  def apply(rootNode: Node, author: String, title: String): Try[Option[BookNotes]] = Try {
    Option(rootNode.text.trim).filter(_.nonEmpty) map { url =>
      BookNotes(
        description = Option((rootNode \@ "description").trim).filter(_.nonEmpty),
        url = Configuration.urlWithFile("url.booknotes", url).get,
        slug = Slug.slugFromString(s"$author $title"))
    }
  }
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
                override val itemSlug: Option[String] = None,
                override val itemUrl: Option[String] = None,
                override val next: Boolean = false)
    extends ListItem(date, HtmlContent.fromNonHtmlString(s"$author - $title"), itemSlug, itemUrl) {
  type T = Book

  override def withNext(next: Boolean): Book = copy(next = next)
  override def withSlug(slug: Option[String]): Book = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Book = copy(itemUrl = url)
}

object Book {
  def apply(rootNode: Node): Try[Book] = Try {
    val author = (rootNode \\ "author").text.trim
    val title = (rootNode \\ "title").text.trim

    val notesOption = (rootNode \\ "notes").headOption.flatMap(BookNotes(_, author, title).get)

    Book(
      date = Parsing.dateFromString((rootNode \\ "date").text).get,
      author = author,
      title = title,
      subtitle = Option((rootNode \\ "subtitle").text.trim).filter(_.nonEmpty),
      year = (rootNode \\ "publishingyear").text.trim.toInt,
      rating = Parsing.ratingFromString((rootNode \\ "rating").text),
      comments = Parsing.commentsFromString((rootNode \\ "comments").text),
      url = new URL((rootNode \\ "url").text.trim),
      notesOption)
  }
}

case class Books(override val introduction: Option[Introduction],
                 override val listItems: Seq[Book]) extends Cacheable {
  def notesFromSlug(slug: String): Option[BookNotes] =
    listItems.find(_.notes.exists(_.slug == slug)).flatMap(_.notes)

  override def subFetchables: Seq[FileSource] = for {
    book <- listItems
    notes <- book.notes
  } yield notes.fileSource
}

object Books extends Fetchable {
  type C = Books

  override val name = "Books"
  override val sourceUrl = Configuration.baseUrlWithFile("books.xml").get
  override val icon = Some("fa-book")

  override def fetch(): Try[Books] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Books] = for {
    xml <- Try(XML.load(url))
    books <- apply(xml)
  } yield books

  def apply(rootNode: Node): Try[Books] = Try {
    val booksNode = (rootNode \\ "books").head
    val introduction = Parsing.introductionFromNode(booksNode).get
    val booksSeq = (booksNode \\ "book").map(Book(_).get)

    Books(introduction, booksSeq.withSlugs.withNextFlags)
  }
}
