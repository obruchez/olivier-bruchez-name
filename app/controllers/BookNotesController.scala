package controllers

import actors.Cache
import models._
import models.lifelogging.{BookNotes, Books}
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util._

class BookNotesController @Inject()(implicit ec: ExecutionContext,
                                    val controllerComponents: ControllerComponents)
    extends BaseController {
  def bookNotes(slug: String) = Action.async {
    for {
      books <- Cache.get(Books)
      result <- resultFromSlug(books, slug)
    } yield result
  }

  private def resultFromSlug(books: Books, slug: String): Future[Result] =
    books.notesFromSlug(slug) match {
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
      val book = books.listItems.find(_.notes.contains(bookNotes)).get
      val bookWithSameTitleCount = books.listItems.count(_.title == book.title)
      val pageTitle =
        if (bookWithSameTitleCount > 1)
          s"${book.author} - ${book.title} (${bookNotes.description.getOrElse(BookNotes.DefaultDescription)})"
        else
          s"${book.title} (${bookNotes.description.getOrElse(BookNotes.DefaultDescription)})"

      Page(title = pageTitle,
           url = routes.BookNotesController.bookNotes(bookNotes.slug).url,
           icon = Sitemap.books.icon,
           fetchables = Seq(bookNotes.fileSource))
    }
  }
}
