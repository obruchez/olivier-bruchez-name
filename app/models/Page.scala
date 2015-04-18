package models

import actors.Cache
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Call
import scala.concurrent.Future
import util.{Fetchable, Introduction}

case class Page(title: String,
                url: String,
                icon: Option[String] = None,
                fetchables: Seq[Fetchable] = Seq(),
                children: Seq[Page] = Seq())

object Page {
  def apply(fetchable: Fetchable, call: Call): Page =
    Page(title = fetchable.name, call.url, icon = fetchable.icon, fetchables = Seq(fetchable))

  def apply(title: String, call: Call, icon: String): Page =
    Page(title, call.url, icon = Some(icon), children = Seq())

  def apply(title: String, call: Call, icon: String, children: Seq[Page]): Page =
    Page(title, call.url, icon = Some(icon), children = children)

  def introductionsFromPages(pages: Seq[Page]): Future[Seq[(Page, Option[Introduction])]] = {
    val sequenceOfFutures = for (page <- pages) yield {
      // Take only first fetchable into account
      val introductionFuture = page.fetchables.headOption match {
        case Some(fetchable) => Cache.get(fetchable).map(cacheable => cacheable.introduction)
        case None => Future(None)
      }
      introductionFuture.map(page -> _)
    }

    Future.sequence(sequenceOfFutures)
  }
}
