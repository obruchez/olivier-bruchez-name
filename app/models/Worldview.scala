package models

import java.net.URL
import scala.util.Try
import scala.xml._
import util._

case class WorldviewPosition(summary: HtmlContent, details: HtmlContent, slug: String)

case class WorldviewCategory(description: HtmlContent, worldviewPositions: Seq[WorldviewPosition], slug: String)

case class Worldview(override val introduction: Option[Introduction],
                     worldviewCategories: Seq[WorldviewCategory],
                     references: Seq[HtmlContent]) extends Cacheable

object Worldview extends Fetchable {
  type C = Worldview

  override val name = "Worldview"
  override val sourceUrl = Configuration.baseUrlWithFile("worldview.xml").get

  override def fetch(): Try[Worldview] = apply(sourceUrl)

  def apply(url: URL): Try[Worldview] = for {
     xml <- Try(XML.load(url))
     profile <- apply(xml)
   } yield profile

  def apply(elem: Elem): Try[Worldview] = Try {
     val worldview = (elem \\ "worldview").head
     val introduction = Parsing.introductionFromNode(worldview).get

     val worldviewCategories = for {
       category <- worldview \\ "category"
       descriptionAsMarkdown = category \@ "description"
       categorySlug = category \@ "slug"
     } yield {
       val worldviewPositions = for {
         position <- category \\ "position"
         summaryAsMarkdown = position \@ "summary"
         detailsAsMarkdown = position.text
         positionSlug = position \@ "slug"
       } yield WorldviewPosition(
         summary = MarkdownContent(summaryAsMarkdown).toHtmlContent.get,
         details = MarkdownContent(detailsAsMarkdown).toHtmlContent.get,
         slug = positionSlug)

       WorldviewCategory(
         description = MarkdownContent(descriptionAsMarkdown).toHtmlContent.get,
         worldviewPositions = worldviewPositions,
         slug = categorySlug)
     }

    val references = for {
      references <- worldview \\ "references"
      reference <- references \\ "reference"
      referenceAsMarkdown = reference.text
    } yield MarkdownContent(referenceAsMarkdown).toHtmlContent.get

     Worldview(introduction, worldviewCategories, references)
   }
}
