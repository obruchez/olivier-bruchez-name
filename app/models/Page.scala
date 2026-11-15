package models

import actors.Cache
import play.api.mvc.Call

import scala.concurrent.{ExecutionContext, Future}

case class Page(
    title: String,
    url: String,
    fetchables: Seq[Fetchable] = Seq(),
    groupChildren: Seq[PageGroup] = Seq()
) {
  def children: Seq[Page] = groupChildren.flatMap(_.pages)
}

case class PageGroup(pages: Seq[Page], title: Option[String] = None)

object Page {
  def apply(fetchable: Fetchable, call: Call): Page =
    Page(title = fetchable.name, call.url, fetchables = Seq(fetchable))

  def apply(title: String, call: Call): Page =
    Page(title, call.url)

  def apply(title: String, call: Call, groupChildren: Seq[PageGroup]): Page =
    Page(title, call.url, groupChildren = groupChildren)

  /** Introduction and number of (past) list items of each page, from its first fetchable. */
  def summariesFromPages(
      pages: Seq[Page]
  )(implicit ec: ExecutionContext): Future[Map[Page, PageSummary]] =
    Future
      .sequence(pages.map { page =>
        val summaryFuture = page.fetchables.headOption match {
          case Some(fetchable) =>
            Cache.get(fetchable).map { cacheable =>
              val pastItems = cacheable.listItems.filterNot(_.next)
              PageSummary(cacheable.introduction, pastItems.size, pastItems.headOption)
            }
          case None =>
            Future.successful(PageSummary(None, 0, None))
        }
        summaryFuture.map(page -> _)
      })
      .map(_.toMap)
}

case class PageSummary(
    introduction: Option[Introduction],
    itemCount: Int,
    latestItem: Option[ListItem]
)

object PageGroup {
  def singlePageGroup(pages: Page*): Seq[PageGroup] = Seq(PageGroup(pages = pages.toSeq))
}
