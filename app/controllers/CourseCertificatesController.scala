package controllers

import actors.Cache
import models.{Courses, CourseCertificate}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import scala.util._
import util._

object CourseCertificatesController extends Controller {
  def courseCertificate(slug: String) = Action.async {
    Cache.courses map { courses =>
      courses.certificateFromSlug(slug) match {
        case Some(certificate) =>
          // We expect only binary file types (PDF, mainly)
          BinaryContent(certificate.url) match {
            case Success(binaryContent) =>
              Ok(binaryContent.content).as(binaryContent.fileType.mimeType)
            case Failure(throwable) =>
              Ok(views.html.error(courses.pageFromCertificate(certificate), throwable))
          }
        case None =>
          NotFound
      }
    }
  }

  implicit class CoursesOps(courses: Courses) {
    def pageFromCertificate(courseCertificate: CourseCertificate): Page = {
      val course = courses.courses.find(_.certificate.contains(courseCertificate)).get

      Page(
        title = course.name,
        url = routes.CourseCertificatesController.courseCertificate(courseCertificate.slug).url,
        icon = Sitemap.courses.icon)
    }
  }
}
