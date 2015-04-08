package controllers

import actors.Cache
import models.{Books, BookNotes}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import scala.concurrent.Future
import scala.util._
import util._

object BookNotesController extends Controller {
  def bookNotes(slug: String) = Action.async {
    for {
      books <- Cache.books
      result <- resultFromSlug(books, slug)
    } yield result
  }

  private def resultFromSlug(books: Books, slug: String): Future[Result] = books.notesFromSlug(slug) match {
    case Some(notes) =>
      val page = books.pageFromNotes(notes)

      Cache.fileContent(notes.fileSource) map {
        case markdownContent: MarkdownContent =>
          markdownContent.withoutHeadingTitle.toHtmlContent match {
            case Success(htmlContent) =>
              Ok(views.html.markdown(page, introduction = None, htmlContent))
            case Failure(throwable) =>
              Ok(views.html.error(page, throwable))
          }

        case binaryContent: BinaryContent =>
          Ok(binaryContent.content).as(binaryContent.fileType.mimeType)
        case _ =>
          NotImplemented
      }
    case None =>
      Future(NotFound)
  }

  implicit class BooksOps(books: Books) {
    def pageFromNotes(bookNotes: BookNotes): Page = {
      val book = books.books.find(_.notes.contains(bookNotes)).get

      Page(
        title = s"${book.author} - ${book.title} (${bookNotes.description.getOrElse(BookNotes.DefaultDescription)})",
        url = routes.BookNotesController.bookNotes(bookNotes.slug).url,
        icon = Sitemap.books.icon)
    }
  }
}
