package controllers

import models.Page
import util.Strings

object Breadcrumbs {
  case class Link(name: String, url: Option[String]) {
    def isHome: Boolean = name == Sitemap.home.title
  }

  object Link {
    def apply(page: Page): Link = Link(name = page.title, url = Option(page.url).filter(_.nonEmpty))
  }

  def links(page: Page): Seq[Link] = shortenIfTooLong {
    withHome {
      val linksFromSitemap = this.linksFromSitemap(page, noUrlForLastLink = true)

      if (linksFromSitemap.nonEmpty)
        linksFromSitemap
      else
        linksFromUrl(page)
    }
  }

  private def linksFromSitemap(page: Page, noUrlForLastLink: Boolean): Seq[Link] = {
    def linksFromSitemap(pageToTest: Page, acc: Seq[Link] = Seq()): Seq[Link] = {
      val currentLink = Link(pageToTest)

      if (pageToTest.url == page.url) {
        acc :+ (if (noUrlForLastLink) currentLink.copy(url = None) else currentLink)
      } else {
        val childAcc = if (pageToTest == Sitemap.root) acc else acc :+ currentLink
        (for {
          childPage <- pageToTest.children
          childLinks = linksFromSitemap(pageToTest = childPage, acc = childAcc)
          if childLinks.nonEmpty
        } yield childLinks).headOption.getOrElse(Seq())
      }
    }

    linksFromSitemap(pageToTest = Sitemap.root)
  }

  private def linksFromUrl(page: Page): Seq[Link] = {
    def linksFromUrl(urlToTest: String): Seq[Link] =
      Sitemap.pageByUrl(urlToTest) match {
        case Some(sitemapPage) =>
          linksFromSitemap(sitemapPage, noUrlForLastLink = false)
        case None =>
          val slashIndex = urlToTest.lastIndexOf("/")
          if (slashIndex < 0)
            Seq()
          else
            linksFromUrl(urlToTest.substring(0, slashIndex))
      }

    linksFromUrl(page.url) :+ Link(page).copy(url = None)
  }

  private def withHome(links: Seq[Link]): Seq[Link] =
    if (links.headOption.exists(_.isHome)) links else Link(Sitemap.home) +: links

  private def shortenIfTooLong(links: Seq[Link]): Seq[Link] = {
    val totalLength = links.map(_.name.length).sum
    if (totalLength <= TotalMaxLength || links.isEmpty) {
      links
    } else {
      val lastLinkMaxLength = math.max(LastLinkMinLength, TotalMaxLength - links.init.map(_.name.length).sum)
      val lastLink = links.last
      val lastLinkName = Strings.truncatedWithDots(lastLink.name, maxSize = lastLinkMaxLength, onWordLimit = true)
      links.init :+ lastLink.copy(name = lastLinkName)
    }
  }

  private val TotalMaxLength = 50
  private val LastLinkMinLength = 10
}
