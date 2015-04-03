package controllers

object Breadcrumbs {
  case class Link(name: String, urlOption: Option[String]) {
    def isHome: Boolean = name == Sitemap.home.title
  }

  object Link {
    def apply(page: Page): Link = Link(name = page.title, urlOption = Option(page.url).filter(_.nonEmpty))
  }

  def links(page: Page): Seq[Link] = {
    def links(pageToTest: Page, acc: Seq[Link] = Seq()): Seq[Link] = {
      val currentLink = Link(pageToTest)

      if (pageToTest == page) {
        acc :+ currentLink.copy(urlOption = None)
      } else {
        val childAcc = if (pageToTest == Sitemap.root) acc else acc :+ currentLink
        (for {
          childPage <- pageToTest.children
          childLinks = links(pageToTest = childPage, acc = childAcc)
          if childLinks.nonEmpty
        } yield childLinks).headOption.getOrElse(Seq())
      }
    }

    withHome {
      links(pageToTest = Sitemap.root)
    }
  }

  private def withHome(links: Seq[Link]): Seq[Link] =
    if (links.headOption.exists(_.isHome)) links else Link(Sitemap.home) +: links
}
