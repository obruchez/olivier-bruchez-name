package models.lifelogging

import java.net.URL
import models._
import models.ListItems._
import org.joda.time.Partial
import scala.util._
import scala.xml.{Node, XML}
import util._

case class CourseCertificate(description: Option[String], url: URL, slug: String) {
  def fileSource: FileSource =
    FileSource(name = description.getOrElse(BookNotes.DefaultDescription), sourceUrl = url)
}

object CourseCertificate {
  val DefaultDescription = "Certificate"

  def apply(rootNode: Node, name: String): Try[Option[CourseCertificate]] = Try {
    Option(rootNode.text.trim).filter(_.nonEmpty) map { url =>
      CourseCertificate(
        description = Option((rootNode \@ "description").trim).filter(_.nonEmpty),
        url = Configuration.urlWithFile("url.coursecertificates", url).get,
        slug = Slug.slugFromString(name)
      )
    }
  }
}

case class Course(
    override val date: Partial,
    provider: String,
    name: String,
    instructor: String,
    url: URL,
    certificate: Option[CourseCertificate],
    override val itemSlug: Option[String] = None,
    override val itemUrl: Option[String] = None,
    override val next: Boolean = false
) extends ListItem(date, HtmlContent.fromNonHtmlString(s"$name"), itemSlug, itemUrl) {
  type T = Course

  override def withNext(next: Boolean): Course = copy(next = next)
  override def withSlug(slug: Option[String]): Course = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Course = copy(itemUrl = url)
}

object Course {
  def apply(rootNode: Node): Try[Course] = Try {
    val name = (rootNode \\ "name").text.trim

    val certificateOption =
      (rootNode \\ "certificate").headOption.flatMap(CourseCertificate(_, name).get)

    Course(
      date = Parsing.dateFromString((rootNode \\ "date").text).get,
      provider = (rootNode \\ "provider").text.trim,
      name = name,
      instructor = (rootNode \\ "instructor").text.trim,
      url = new URL((rootNode \\ "url").text.trim),
      certificate = certificateOption
    )
  }
}

case class Courses(
    override val introduction: Option[Introduction],
    override val listItems: Seq[Course]
) extends Cacheable {
  def certificateFromSlug(slug: String): Option[CourseCertificate] =
    listItems.find(_.certificate.exists(_.slug == slug)).flatMap(_.certificate)

  override def subFetchables: Seq[FileSource] =
    for {
      course <- listItems
      certificate <- course.certificate
    } yield certificate.fileSource
}

object Courses extends Fetchable {
  type C = Courses

  override val name = "Courses"
  override val sourceUrl = Configuration.baseUrlWithFile("courses.xml").get
  override val icon = Some("fa-graduation-cap")

  override def fetch(): Try[Courses] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Courses] =
    for {
      xml <- Try(XML.load(url))
      courses <- apply(xml)
    } yield courses

  def apply(rootNode: Node): Try[Courses] = Try {
    val coursesNode = (rootNode \\ "courses").head
    val introduction = Parsing.introductionFromNode(coursesNode).get
    val coursesSeq = (coursesNode \\ "course").map(Course(_).get)

    Courses(introduction, coursesSeq.withSlugs.withNextFlags)
  }
}
