package models.lifelogging

import java.net.URL
import java.util.Locale
import models._
import models.ListItems._
import org.joda.time.Partial
import util._
import scala.util.Try
import scala.xml.{ Node, XML }

sealed abstract class SpecialLocation(val description: String)
case object Home extends SpecialLocation("Home")

case class Title(title: String, locale: Locale)

case class Movie(override val date: Partial,
                 theater: Either[String, SpecialLocation],
                 director: String,
                 title: String,
                 otherTitles: Seq[Title],
                 version: Option[Locale],
                 rating: Option[Double],
                 comments: Option[HtmlContent],
                 url: Option[URL],
                 override val itemSlug: Option[String] = None,
                 override val itemUrl: Option[String] = None,
                 override val next: Boolean = false)
    extends ListItem(date, HtmlContent.fromNonHtmlString(s"$director - $title"), itemSlug, itemUrl) {
  type T = Movie

  override def withNext(next: Boolean): Movie = copy(next = next)
  override def withSlug(slug: Option[String]): Movie = copy(itemSlug = slug)
  override def withUrl(url: Option[String]): Movie = copy(itemUrl = url)
}

object Movie {
  def apply(rootNode: Node): Try[Movie] = Try {
    val theaterNode = rootNode \\ "theater"

    val titles = for {
      title <- rootNode \\ "title"
      titleString = title.text
      language = title \@ "language"
    } yield titleString.trim -> Option(language.trim).filter(_.nonEmpty)

    val mainTitle = titles.find(_._2.isEmpty).map(_._1).get
    val otherTitles = titles.filter(_._2.nonEmpty) map {
      case (titleString, languageOption) => Title(titleString, new Locale(languageOption.get))
    }

    Movie(
      date = Parsing.dateFromString((rootNode \\ "date").text).get,
      theater = if (Parsing.isTrue(theaterNode \@ "home")) Right(Home) else Left(theaterNode.text),
      director = (rootNode \\ "director").text.trim,
      title = mainTitle,
      otherTitles = otherTitles,
      version = Option((rootNode \\ "version").text.trim).filter(_.nonEmpty).map(new Locale(_)),
      rating = Parsing.ratingFromString((rootNode \\ "rating").text),
      comments = Parsing.commentsFromNodeChildren((rootNode \\ "comments").headOption),
      url = Option((rootNode \\ "url").text.trim).filter(_.nonEmpty).map(new URL(_)))
  }
}

case class Movies(override val introduction: Option[Introduction],
                  override val listItems: Seq[Movie]) extends Cacheable

object Movies extends Fetchable {
  type C = Movies

  override val name = "Movies"
  override val sourceUrl = Configuration.baseUrlWithFile("movies.xml").get
  override val icon = Some("fa-film")

  override def fetch(): Try[Movies] = apply(sourceUrlWithNoCacheParameter)

  def apply(url: URL): Try[Movies] = for {
    xml <- Try(XML.load(url))
    movies <- apply(xml)
  } yield movies

  def apply(rootNode: Node): Try[Movies] = Try {
    val moviesNode = (rootNode \\ "movies").head
    val introduction = Parsing.introductionFromNode(moviesNode).get
    val moviesSeq = (moviesNode \\ "movie").map(Movie(_).get)

    Movies(introduction, moviesSeq.withSlugs.withNextFlags)
  }
}
