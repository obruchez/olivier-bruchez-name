package models

import java.net.URL
import org.joda.time.Partial
import scala.util._
import util.{Configuration, Parsing, Slug}
import scala.xml.{Elem, XML}

case class CourseCertificate(description: Option[String], url: URL, slug: String)

case class Course(override val date: Partial,
                  provider: String,
                  name: String,
                  instructor: String,
                  url: URL,
                  certificate: Option[CourseCertificate],
                  override val slug: String = "") extends ListItem(date, slug)

case class Courses(override val introduction: Introduction, courses: Seq[Course]) extends Cacheable {
  override val size = courses.size
}

object Courses extends Fetchable {
  type C = Courses

  override val name = "Courses"
  override val sourceUrl = Configuration.url("url.courses").get

  override def fetch(): Try[Courses] = apply(sourceUrl)

  def apply(url: URL): Try[Courses] = for {
    xml <- Try(XML.load(url))
    courses <- apply(xml)
  } yield courses

  def apply(elem: Elem): Try[Courses] = Try {
    val courses = (elem \\ "courses").head
    val introduction = Parsing.introductionFromNode(courses).get

    val coursesSeq = for {
      course <- courses \\ "course"
      dateString = (course \\ "date").text
      provider = (course \\ "provider").text
      name = (course \\ "name").text
      instructor = (course \\ "instructor").text
      url = (course \\ "url").text
    } yield {
      val certificateOption = for {
        certificate <- (course \\ "certificate").headOption
        certificateUrl = certificate.text.trim
        if certificateUrl.nonEmpty
        certificateDescription = certificate \@ "description"
      } yield CourseCertificate(
        description = Option(certificateDescription.trim).filter(_.nonEmpty),
        url = new URL(certificateUrl),
        slug = Slug.slugFromString(name))

      Course(
        date = Parsing.dateFromString(dateString).get,
        provider = provider.trim,
        name = name.trim,
        instructor = instructor.trim,
        url = new URL(url.trim),
        certificate = certificateOption)
    }

    Courses(
      introduction,
      coursesSeq.map(course => course.copy(slug = ListItem.slug(course, coursesSeq))))
  }
}
