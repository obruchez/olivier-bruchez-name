package controllers

import actors.Cache
import models.{Courses, CourseCertificate}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

object CourseCertificatesController extends Controller with FileHelper {
  def courseCertificate(slug: String) = Action.async {
    Cache.courses map { courses =>
      courses.certificateFromSlug(slug) match {
        case Some(certificate) =>
          certificate.url.fileType match {
            case Markdown =>
              NotImplemented
            case _ =>
              // @todo implement this
              NotImplemented
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
