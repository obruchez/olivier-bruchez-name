package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class Crash(override val date: Partial,
                 manufacturer: String,
                 model: String,
                 comments: Option[HtmlContent],
                 override val slug: String = "")
    extends ListItem(date, slug, s"$manufacturer - $model")

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
                   crashes: Seq[Crash]) extends Cacheable

object Crashes extends Fetchable {
  type C = Crashes

  override val name = "Crashes"
  override val sourceUrl = Configuration.baseUrlWithFile("crashes.xml").get

  override def fetch(): Try[Crashes] = apply(sourceUrl)

  def apply(url: URL): Try[Crashes] = for {
    xml <- Try(XML.load(url))
    crashes <- apply(xml)
  } yield crashes

  def apply(rootNode: Node): Try[Crashes] = Try {
    val crashesNode = (rootNode \\ "crashes").head
    val introduction = Parsing.introductionFromNode(crashesNode).get
    val crashesSeq = (crashesNode \\ "crash").map(Crash(_).get)

    Crashes(introduction, crashesSeq.map(crash => crash.copy(slug = ListItem.slug(crash, crashesSeq))))
  }
}
