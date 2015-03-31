package models

import java.net.URL
import java.util.Locale
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Elem, XML}
import util.HtmlContent

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
                 url: Option[URL]) extends ListItem(date)

case class Movies(introduction: HtmlContent, movies: Seq[Movie])

object Movies {
  def apply(url: URL): Try[Movies] = for {
    xml <- Try(XML.load(url))
    movies <- apply(xml)
  } yield movies

  def apply(elem: Elem): Try[Movies] = Try {
    val movies = (elem \\ "movies").head
    val introduction = Lists.introductionFromNode(movies).get

    val moviesSeq = for {
      movie <- movies \\ "movie"
      dateString = (movie \\ "date").text
      theaterNode = movie \\ "theater"
      theater = theaterNode.text
      home = theaterNode \@ "home"
      director = (movie \\ "director").text
      version = (movie \\ "version").text
      ratingString = (movie \\ "rating").text
      comments = (movie \\ "comments").text
      url = (movie \\ "url").text
    } yield {
        val titles = for {
          title <- movie \\ "title"
          titleString = title.text
          language = title \@ "language"
        } yield titleString.trim -> Option(language.trim).filter(_.nonEmpty)

        val mainTitle = titles.find(_._2.isEmpty).map(_._1).get
        val otherTitles = titles.filter(_._2.nonEmpty) map {
          case (titleString, languageOption) => Title(titleString, new Locale(languageOption.get))
        }

        Movie(
          date = Lists.dateFromString(dateString).get,
          theater = if (Lists.isTrue(home)) Right(Home) else Left(theater),
          director = director.trim,
          title = mainTitle,
          otherTitles = otherTitles,
          version = Option(version.trim).filter(_.nonEmpty).map(new Locale(_)),
          rating = Lists.ratingFromString(ratingString),
          comments = Lists.commentsFromString(comments),
          url = Option(url.trim).filter(_.nonEmpty).map(new URL(_)))
      }

    Movies(introduction, moviesSeq)
  }
}
