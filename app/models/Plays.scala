package models

import java.net.URL
import org.joda.time.Partial
import scala.util.Try
import scala.xml.{Node, XML}
import util._

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

object Play {
  def apply(rootNode: Node): Try[Play] = Try {
    Play(
      date = Parsing.dateFromString((rootNode \\ "date").text).get,
      location = (rootNode \\ "location").text.trim,
      author = (rootNode \\ "author").text.trim,
      name = (rootNode \\ "name").text.trim,
      director = (rootNode \\ "director").text.trim,
      adaptation = Option((rootNode \\ "adaptation").text.trim).filter(_.nonEmpty),
      translation = Option((rootNode \\ "translation").text.trim).filter(_.nonEmpty),
      actors = (rootNode \\ "actor").map(_.text.trim),
      rating = Parsing.ratingFromString((rootNode \\ "rating").text),
      comments = Parsing.commentsFromString((rootNode \\ "comments").text))
  }
}

case class Plays(override val introduction: Option[Introduction],
                 plays: Seq[Play]) extends Cacheable

object Plays extends Fetchable {
  type C = Plays

  override val name = "Plays"
  override val sourceUrl = Configuration.baseUrlWithFile("plays.xml").get

  override def fetch(): Try[Plays] = apply(sourceUrl)

  def apply(url: URL): Try[Plays] = for {
    xml <- Try(XML.load(url))
    plays <- apply(xml)
  } yield plays

  def apply(rootNode: Node): Try[Plays] = Try {
    val playsNode = (rootNode \\ "plays").head
    val introduction = Parsing.introductionFromNode(playsNode).get
    val playsSeq = (playsNode \\ "play").map(Play(_).get)

    Plays(introduction, playsSeq.map(play => play.copy(slug = ListItem.slug(play, playsSeq))))
  }
}
