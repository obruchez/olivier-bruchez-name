package controllers

case class Page(name: String, url: String, children: Seq[Page] = Seq())

object Sitemap {
  val lists = Seq(
        Page("Books", routes.Lists.books().url),
        Page("Concerts", routes.Lists.concerts().url),
        Page("Crashes", routes.Lists.crashes().url),
        Page("Exhibitions", routes.Lists.exhibitions().url),
        Page("Hikes", routes.Lists.hikes().url),
        Page("Movies", routes.Lists.movies().url),
        Page("Plays", routes.Lists.plays().url),
        Page("Trips", routes.Lists.trips().url))

  val externalLinks = Seq(
    Page("Blogger", "https://bruchez.blogspot.com/"),
    Page("Facebook", "https://www.facebook.com/obruchez"),
    Page("Flickr", "https://secure.flickr.com/photos/bruchez/sets"),
    Page("Github", "https://github.com/obruchez"),
    Page("KeithJarrett.org", "http://www.keithjarrett.org/"),
    Page("Last.fm", "http://www.last.fm/user/obruchez"),
    Page("LinkedIn", "https://www.linkedin.com/in/obruchez"),
    Page("Twitter", "https://twitter.com/obruchez"),
    Page("YouTube", "https://www.youtube.com/user/obruchez"))

  val pages = Seq(
    Page(controllers.Home.PageTitle, routes.Home.index().url),
    Page(controllers.Profile.PageTitle, routes.Profile.index().url),
    Page(controllers.Lists.PageTitle, routes.Lists.index().url, lists),
    Page(controllers.ExternalLinks.PageTitle, "", externalLinks))
}
