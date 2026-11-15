package controllers

import models.Page

// One pictogram per list (drawn on a 24x24 grid, stroked with the current colour)
object ListIcons {
  private val paths: Map[Page, String] = Map(
    Sitemap.articles -> """<path d="M6 3h9l4 4v14H6z"/><path d="M9 11h7M9 14.5h7M9 18h4"/>""",
    Sitemap.ballets -> """<path d="M9 3v4c0 1 1.5 2 3 2s3-1 3-2V3"/><path d="M9.5 9L3 16c3 1.5 6 2 9 2s6-.5 9-2L14.5 9"/><path d="M8 17.5l1.5-5M12 18v-5.5M16 17.5l-1.5-5"/>""",
    Sitemap.books -> """<path d="M12 6c-2-1.5-5-2-8-1.5v13c3-.5 6 0 8 1.5 2-1.5 5-2 8-1.5v-13c-3-.5-6 0-8 1.5z"/><path d="M12 6v13"/>""",
    Sitemap.comedyShows -> """<rect x="9" y="3" width="6" height="11" rx="3"/><path d="M5.5 11a6.5 6.5 0 0 0 13 0M12 17.5V21M8.5 21h7"/>""",
    Sitemap.comics -> """<path d="M4 5h16v11h-9l-4 4v-4H4z"/>""",
    Sitemap.concerts -> """<path d="M9 18V5l10-2v13"/><circle cx="6.5" cy="18" r="2.5"/><circle cx="16.5" cy="16" r="2.5"/>""",
    Sitemap.courses -> """<path d="M2 9l10-5 10 5-10 5z"/><path d="M6 11.5V16c1.5 1.5 3.5 2.5 6 2.5s4.5-1 6-2.5v-4.5M22 9v6"/>""",
    Sitemap.exhibitions -> """<rect x="3" y="4" width="18" height="16" rx="1"/><path d="M6 17l4.5-5 3 3 2-2 2.5 4"/><circle cx="15.5" cy="8.5" r="1.3"/>""",
    Sitemap.hikes -> """<path d="M2 20L9 7l4 7 2.5-4L22 20z"/>""",
    Sitemap.movies -> """<rect x="3" y="4" width="18" height="16" rx="1.5"/><path d="M7.5 4v16M16.5 4v16M3 9h4.5M3 15h4.5M16.5 9H21M16.5 15H21"/>""",
    Sitemap.operas -> """<path d="M6 14a6 6 0 0 1 12 0v3H6z"/><path d="M6.5 11.5C3.5 10.5 3 7 4 4c.8 2.8 2 4 4 5M17.5 11.5c3-1 3.5-4.5 2.5-7.5-.8 2.8-2 4-4 5"/><path d="M12 8v9"/>""",
    Sitemap.plays -> """<path d="M5 4h14v7a7 7 0 0 1-14 0z"/><path d="M8.5 9h2M13.5 9h2M9 13c1.5 1.8 4.5 1.8 6 0"/>""",
    Sitemap.podcasts -> """<path d="M4 15v-3a8 8 0 0 1 16 0v3"/><rect x="3" y="14" width="4.5" height="6.5" rx="1.5"/><rect x="16.5" y="14" width="4.5" height="6.5" rx="1.5"/>""",
    Sitemap.shows -> """<rect x="3" y="7" width="18" height="12" rx="1.5"/><path d="M8 3l4 4 4-4M8 22h8"/>""",
    Sitemap.trips -> """<rect x="4" y="8" width="16" height="12" rx="1.5"/><path d="M9 8V5h6v3M9 8v12M15 8v12"/>"""
  )

  def svg(page: Page): String =
    paths
      .get(page)
      .fold("")(path =>
        s"""<svg class="list-icon" viewBox="0 0 24 24" aria-hidden="true">$path</svg>"""
      )

  /** The same pictogram as a symbol that entries without a cover refer to (`<use
    * href="#list-icon">`).
    */
  def symbol(page: Page): String =
    s"""<svg hidden><symbol id="list-icon" viewBox="0 0 24 24">${paths.getOrElse(
        page,
        ""
      )}</symbol></svg>"""
}
