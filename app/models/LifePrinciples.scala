package models

import java.net.URL
import scala.util._
import util._
import scala.xml.{Elem, XML}

case class LifePrinciple(summary: HtmlContent, details: HtmlContent, slug: String)

case class LifePrinciples(override val introductionOption: Option[Introduction],
                          lifePrinciples: Seq[LifePrinciple]) extends Cacheable {
  def indexFromColumnNumber(columnNumber: Int, columnCount: Int): Int =
    math.round((columnNumber.toDouble / columnCount.toDouble) * lifePrinciples.size).toInt
}

object LifePrinciples extends Fetchable {
  type C = LifePrinciples

  override val name = "Life principles"
  override val sourceUrl = Configuration.url("url.lifeprinciples").get

  override def fetch(): Try[LifePrinciples] = apply(sourceUrl)

  def apply(url: URL): Try[LifePrinciples] = for {
     xml <- Try(XML.load(url))
     lifePrinciples <- apply(xml)
   } yield lifePrinciples

  def apply(elem: Elem): Try[LifePrinciples] = Try {
     val lifePrinciples = (elem \\ "lifeprinciples").head
     val introductionOption = Parsing.introductionFromNode(lifePrinciples).get

     val lifePrinciplesSeq = for {
       lifePrinciple <- lifePrinciples \\ "lifeprinciple"
       summaryAsMarkdown = lifePrinciple \@ "summary"
       detailsAsMarkdown = lifePrinciple.text
       slug = lifePrinciple \@ "slug"
     } yield {
       LifePrinciple(
         summary = MarkdownContent(summaryAsMarkdown).toHtmlContent.get,
         details = MarkdownContent(detailsAsMarkdown).toHtmlContent.get,
         slug = slug)
     }

    LifePrinciples(introductionOption, lifePrinciplesSeq)
  }
}
