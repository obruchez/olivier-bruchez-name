package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Elem, XML}
import util.{Configuration, HtmlContent}

case class Play(override val date: Partial,
                location: String,
                author: String,
                name: String,
                director: String,
                adaptation: Option[String],
                translation: Option[String],
                actors: Seq[String],
                rating: Option[Double],
                comments: Option[HtmlContent],
                override val slug: String = "") extends ListItem(date, slug)

case class Plays(override val introduction: Introduction, plays: Seq[Play]) extends Cacheable {
  override val size = plays.size
}

object Plays extends Fetchable {
  type C = Plays

  override val name = "Plays"
  override val sourceUrl = Configuration.url("url.plays").get

  override def fetch(): Try[Plays] = apply(sourceUrl)

  def apply(url: URL): Try[Plays] = for {
    xml <- Try(XML.load(url))
    plays <- apply(xml)
  } yield plays

  def apply(elem: Elem): Try[Plays] = Try {
    val plays = (elem \\ "plays").head
    val introduction = Lists.introductionFromNode(plays).get

    val playsSeq = for {
      play <- plays \\ "play"
      dateString = (play \\ "date").text
      location = (play \\ "location").text
      author = (play \\ "author").text
      name = (play \\ "name").text
      director = (play \\ "director").text
      adaptation = (play \\ "adaptation").text
      translation = (play \\ "translation").text
      actors = (play \\ "actor").map(_.text.trim)
      ratingString = (play \\ "rating").text
      comments = (play \\ "comments").text
      url = (play \\ "url").text
    } yield Play(
      date = Lists.dateFromString(dateString).get,
      location = location.trim,
      author = author.trim,
      name = name.trim,
      director = director.trim,
      adaptation = Option(adaptation.trim).filter(_.nonEmpty),
      translation = Option(translation.trim).filter(_.nonEmpty),
      actors = actors,
      rating = Lists.ratingFromString(ratingString),
      comments = Lists.commentsFromString(comments))

    Plays(introduction, playsSeq.map(play => play.copy(slug = Lists.slug(play, playsSeq))))
  }
}
