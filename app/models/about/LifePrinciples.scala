package models.about

import java.net.URL
import models._
import scala.util._
import scala.xml.{ Node, XML }
import util._

case class LifePrinciple(summary: HtmlContent, details: HtmlContent, slug: String)

object LifePrinciple {
  def apply(rootNode: Node): Try[LifePrinciple] = Try {
    LifePrinciple(
      summary = MarkdownContent(rootNode \@ "summary").toHtmlContent.get,
      details = MarkdownContent(rootNode.text).toHtmlContent.get,
      slug = rootNode \@ "slug")
  }
}

case class LifePrinciples(override val introduction: Option[Introduction],
                          lifePrinciples: Seq[LifePrinciple]) extends Cacheable {
  def indexFromColumnNumber(columnNumber: Int, columnCount: Int): Int =
    math.round((columnNumber.toDouble / columnCount.toDouble) * lifePrinciples.size).toInt
}

object LifePrinciples extends Fetchable {
  type C = LifePrinciples

  override val name = "Life principles"
  override val sourceUrl = Configuration.baseUrlWithFile("lifeprinciples.xml").get
  override val icon = Some("fa-compass")

  override def fetch(): Try[LifePrinciples] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[LifePrinciples] = for {
     xml <- Try(XML.load(url))
     lifePrinciples <- apply(xml)
   } yield lifePrinciples

  def apply(rootNode: Node): Try[LifePrinciples] = Try {
    val lifePrinciplesNode = (rootNode \\ "lifeprinciples").head
    val introduction = Parsing.introductionFromNode(lifePrinciplesNode).get
    val lifePrinciplesSeq = (lifePrinciplesNode \\ "lifeprinciple").map(LifePrinciple(_).get)

    LifePrinciples(introduction, lifePrinciplesSeq)
  }
}
