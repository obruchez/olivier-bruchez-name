package models.lifelogging

import java.net.URL
import models._
import models.ListItems._
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{ Node, XML }
import util._

case class Crash(override val date: Partial,
                 manufacturer: String,
                 model: String,
                 comments: Option[HtmlContent],
                 override val itemSlug: Option[String] = None,
                 override val itemUrl: Option[String] = None)
    extends ListItem(date, HtmlContent.fromNonHtmlString(s"$manufacturer - $model"), itemSlug, itemUrl) {
  type T = Crash

  override def withSlug(slug: Option[String]): Crash = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Crash = copy(itemUrl = url)
}

object Crash {
  def apply(rootNode: Node): Try[Crash] = Try {
    Crash(
      date = Parsing.dateFromString((rootNode \\ "date").text).get,
      manufacturer = (rootNode \\ "manufacturer").text.trim,
      model = (rootNode \\ "model").text.trim,
      comments = Parsing.commentsFromString((rootNode \\ "comments").text))
  }
}

case class Crashes(override val introduction: Option[Introduction],
                   override val listItems: Seq[Crash]) extends Cacheable

object Crashes extends Fetchable {
  type C = Crashes

  override val name = "Crashes"
  override val sourceUrl = Configuration.baseUrlWithFile("crashes.xml").get
  override val icon = Some("fa-hdd-o")

  override def fetch(): Try[Crashes] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Crashes] = for {
    xml <- Try(XML.load(url))
    crashes <- apply(xml)
  } yield crashes

  def apply(rootNode: Node): Try[Crashes] = Try {
    val crashesNode = (rootNode \\ "crashes").head
    val introduction = Parsing.introductionFromNode(crashesNode).get
    val crashesSeq = (crashesNode \\ "crash").map(Crash(_).get)

    Crashes(introduction, crashesSeq.withSlugs)
  }
}
