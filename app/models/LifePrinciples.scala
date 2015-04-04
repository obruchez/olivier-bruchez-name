package models

import java.net.URL
import scala.util._
import util.{Configuration, HtmlContent}

import scala.xml.{Elem, XML}

case class LifePrinciple(summary: HtmlContent, details: HtmlContent, slug: String)

case class LifePrinciples(override val introduction: Introduction,
                          lifePrinciples: Seq[LifePrinciple]) extends Cacheable {
  override val size = lifePrinciples.size
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
     val introduction = Lists.introductionFromNode(lifePrinciples).get

     val lifePrinciplesSeq = for {
       lifePrinciple <- lifePrinciples \\ "lifeprinciple"
       summaryAsMarkdown = lifePrinciple \@ "summary"
       detailsAsMarkdown = lifePrinciple.text
       slug = lifePrinciple \@ "slug"
     } yield {
         LifePrinciple(
           summary = HtmlContent.fromMarkdown(summaryAsMarkdown).get,
           details = HtmlContent.fromMarkdown(detailsAsMarkdown).get,
           slug = slug)
     }

    LifePrinciples(introduction, lifePrinciplesSeq)
  }
}
