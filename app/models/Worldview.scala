package models

import java.net.URL
import scala.util.Try
import scala.xml._
import util.HtmlContent

case class WorldviewPosition(summary: HtmlContent, details: HtmlContent)

case class WorldviewCategory(description: HtmlContent, worldviewPositions: Seq[WorldviewPosition])

case class Worldview(worldviewCategories: Seq[WorldviewCategory], references: Seq[HtmlContent])

object Worldview {
  def apply(url: URL): Try[Worldview] = for {
     xml <- Try(XML.load(url))
     profile <- apply(xml)
   } yield profile

  def apply(elem: Elem): Try[Worldview] = Try {
     val worldview = (elem \\ "worldview").head

     val worldviewCategories = for {
       category <- worldview \\ "category"
       descriptionAsMarkdown = category \@ "description"
     } yield {
         val worldviewPositions = for {
           position <- category \\ "position"
           summaryAsMarkdown = position \@ "summary"
           detailsAsMarkdown = position.text
         } yield WorldviewPosition(
           summary = HtmlContent.fromMarkdown(summaryAsMarkdown).get,
           details = HtmlContent.fromMarkdown(detailsAsMarkdown).get)

         WorldviewCategory(description = HtmlContent.fromMarkdown(descriptionAsMarkdown).get, worldviewPositions)
     }

    val references = for {
      references <- worldview \\ "references"
      reference <- references \\ "reference"
      referenceAsMarkdown = reference.text
    } yield HtmlContent.fromMarkdown(referenceAsMarkdown).get

     Worldview(worldviewCategories, references)
   }
}
