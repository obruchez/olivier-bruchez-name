package models

object Header {
  sealed trait MenuItem

  case class MenuPage(page: Page) extends MenuItem
  case object MenuSeparator extends MenuItem

  def menuItems(page: Page): Seq[MenuItem] = {
    val lastPageGroupIndex = page.groupChildren.size - 1

    for {
      (pageGroup, pageGroupIndex) <- page.groupChildren.zipWithIndex
      lastPageGroup = pageGroupIndex == lastPageGroupIndex
      menuPages = pageGroup.pages.map(MenuPage)
      menuSeparators = if (lastPageGroup) Seq() else Seq(MenuSeparator)
      menuItems = menuPages ++ menuSeparators
      menuItem <- menuItems
    } yield menuItem
  }
}
