package models

import actors.Cache
import play.api.mvc.Call

import scala.concurrent.{ExecutionContext, Future}

case class Page(title: String,
                url: String,
                icon: Option[String] = None,
                fetchables: Seq[Fetchable] = Seq(),
                groupChildren: Seq[PageGroup] = Seq()) {
  def children: Seq[Page] = groupChildren.flatMap(_.pages)
}

case class PageGroup(pages: Seq[Page])

object Page {
  def apply(fetchable: Fetchable, call: Call): Page =
    Page(title = fetchable.name, call.url, icon = fetchable.icon, fetchables = Seq(fetchable))

  def apply(title: String, call: Call, icon: String): Page =
    Page(title, call.url, icon = Some(icon), groupChildren = Seq())

  def apply(title: String, call: Call, icon: String, groupChildren: Seq[PageGroup]): Page =
    Page(title, call.url, icon = Some(icon), groupChildren = groupChildren)

  def introductionsFromPages(pages: Seq[Page])(
      implicit ec: ExecutionContext): Future[Seq[(Page, Option[Introduction])]] = {
    val sequenceOfFutures = for (page <- pages) yield {
      // Take only first fetchable into account
      val introductionFuture = page.fetchables.headOption match {
        case Some(fetchable) => Cache.get(fetchable).map(cacheable => cacheable.introduction)
        case None            => Future(None)
      }
      introductionFuture.map(page -> _)
    }

    Future.sequence(sequenceOfFutures)
  }
}

object PageGroup {
  def singlePageGroup(pages: Page*): Seq[PageGroup] = Seq(PageGroup(pages = pages.toSeq))
}
