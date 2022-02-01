package imdb

object Imdb {
  /*
  No official API. Official text dumps from IMDb not very complete.

  Idea:

  - HTTP request on regular IMDB site, e.g. http://www.imdb.com/find?q=La%20M%C3%A9moire%20Dans%20La%20Peau&s=tt
  - parse result => look for td elements with result_text class + parse URL + parse aka "..." title

  Then:

  - display UI elements to search IMDb on /movies page, but only in dev mode (play.api.Play.isDev(play.api.Play.current()))
  - display a select element with search results
  - add download link to download movies.xml file (modify it in memory using JSoup)
   */
}
