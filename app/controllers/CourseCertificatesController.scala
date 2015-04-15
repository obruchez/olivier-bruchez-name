package controllers

import actors.Cache
import models.{Courses, CourseCertificate}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import scala.concurrent.Future
import util._

object CourseCertificatesController extends Controller {
  def courseCertificate(slug: String) = Action.async {
      for {
        courses <- Cache.get(Courses)
        result <- resultFromSlug(courses, slug)
      } yield result
    }

    private def resultFromSlug(courses: Courses, slug: String): Future[Result] = courses.certificateFromSlug(slug) match {
      case Some(certificate) =>
        val page = courses.pageFromCertificate(certificate)

        Cache.fileContent(certificate.fileSource) map {
          case binaryContent: BinaryContent =>
            // We expect only binary file types (PDF, mainly)
            Ok(binaryContent.content).as(binaryContent.fileType.mimeType)
          case _ =>
            NotImplemented
        }
      case None =>
        Future(NotFound)
    }

  implicit class CoursesOps(courses: Courses) {
    def pageFromCertificate(courseCertificate: CourseCertificate): Page = {
      val course = courses.courses.find(_.certificate.contains(courseCertificate)).get

      Page(
        title = course.name,
        url = routes.CourseCertificatesController.courseCertificate(courseCertificate.slug).url,
        icon = Sitemap.courses.icon,
        fetchables = Seq(courseCertificate.fileSource))
    }
  }
}
