package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

case class WorldviewPosition(summary: HtmlContent,
                             details: HtmlContent,
                             dateAdded: Partial,
                             override val itemSlug: Option[String] = None,
                             override val itemUrl: Option[String] = None)
    extends ListItem(dateAdded, s"$summary", itemSlug, itemUrl) {
  type T = WorldviewPosition

  override def withSlug(slug: Option[String]): WorldviewPosition = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): WorldviewPosition = copy(itemUrl = url)
}

object WorldviewPosition {
  def apply(rootNode: Node): Try[WorldviewPosition] = Try {
    WorldviewPosition(
      summary = MarkdownContent(rootNode \@ "summary").toHtmlContent.get,
      details = MarkdownContent(rootNode.text).toHtmlContent.get,
      dateAdded = Parsing.dateFromString((rootNode \@ "added").trim).get,
      itemSlug = Some((rootNode \@ "slug").trim))
  }
}

case class WorldviewCategory(description: HtmlContent, worldviewPositions: Seq[WorldviewPosition], slug: String)

object WorldviewCategory {
  def apply(rootNode: Node): Try[WorldviewCategory] = Try {
    val worldviewPositionsSeq = (rootNode \\ "position").map(WorldviewPosition(_).get)

    WorldviewCategory(
      description = MarkdownContent(rootNode \@ "description").toHtmlContent.get,
      worldviewPositions = worldviewPositionsSeq,
      slug = (rootNode \@ "slug").trim)
  }
}

case class Worldview(override val introduction: Option[Introduction],
                     worldviewCategories: Seq[WorldviewCategory],
                     references: Seq[HtmlContent]) extends Cacheable

object Worldview extends Fetchable {
  type C = Worldview

  override val name = "Worldview"
  override val sourceUrl = Configuration.baseUrlWithFile("worldview.xml").get
  override val icon = Some("fa-globe")

  override def fetch(): Try[Worldview] = apply(sourceUrl)

  def apply(url: URL): Try[Worldview] = for {
     xml <- Try(XML.load(url))
     profile <- apply(xml)
   } yield profile

  def apply(rootNode: Node): Try[Worldview] = Try {
    val worldviewNode = (rootNode \\ "worldview").head
    val introduction = Parsing.introductionFromNode(worldviewNode).get

    val worldviewCategories = (worldviewNode \\ "category").map(WorldviewCategory(_).get)

    val references = for {
      references <- worldviewNode \\ "references"
      reference <- references \\ "reference"
      referenceAsMarkdown = reference.text
    } yield MarkdownContent(referenceAsMarkdown).toHtmlContent.get

     Worldview(introduction, worldviewCategories, references)
   }
}
