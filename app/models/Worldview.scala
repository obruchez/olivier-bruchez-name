package models

import java.net.URL
import scala.util.Try
import scala.xml._
import util.HtmlContent

case class WorldviewPosition(summary: HtmlContent, details: HtmlContent, slug: String)

case class WorldviewCategory(description: HtmlContent, worldviewPositions: Seq[WorldviewPosition], slug: String)

case class Worldview(introduction: HtmlContent, worldviewCategories: Seq[WorldviewCategory], references: Seq[HtmlContent])

object Worldview {
  def apply(url: URL): Try[Worldview] = for {
     xml <- Try(XML.load(url))
     profile <- apply(xml)
   } yield profile

  def apply(elem: Elem): Try[Worldview] = Try {
     val worldview = (elem \\ "worldview").head
     val introduction = Lists.introductionFromNode(worldview).get

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
           summary = HtmlContent.fromMarkdown(summaryAsMarkdown).get,
           details = HtmlContent.fromMarkdown(detailsAsMarkdown).get,
           slug = positionSlug)

         WorldviewCategory(
           description = HtmlContent.fromMarkdown(descriptionAsMarkdown).get,
           worldviewPositions = worldviewPositions,
           slug = categorySlug)
     }

    val references = for {
      references <- worldview \\ "references"
      reference <- references \\ "reference"
      referenceAsMarkdown = reference.text
    } yield HtmlContent.fromMarkdown(referenceAsMarkdown).get

     Worldview(introduction, worldviewCategories, references)
   }
}
