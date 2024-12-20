package controllers

import actors.Cache
import models._
import models.lifelogging.{CourseCertificate, Courses}
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CourseCertificatesController @Inject() (implicit
    ec: ExecutionContext,
    val controllerComponents: ControllerComponents
) extends BaseController {
  def courseCertificate(slug: String) = Action.async {
    for {
      courses <- Cache.get(Courses)
      result <- resultFromSlug(courses, slug)
    } yield result
  }

  private def resultFromSlug(courses: Courses, slug: String): Future[Result] =
    courses.certificateFromSlug(slug) match {
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
      val course = courses.listItems.find(_.certificate.contains(courseCertificate)).get

      Page(
        title = course.name,
        url = routes.CourseCertificatesController.courseCertificate(courseCertificate.slug).url,
        icon = Sitemap.courses.icon,
        fetchables = Seq(courseCertificate.fileSource)
      )
    }
  }
}
