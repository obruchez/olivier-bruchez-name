package models

import com.github.rjeschke.txtmark._
import java.net.URL
import scala.collection.mutable
import scala.io.Source
import scala.util.Try
import util.HtmlContent

case class WorldviewPosition(summary: HtmlContent, details: HtmlContent)

case class WorldviewCategory(description: HtmlContent, worldviewPositions: Seq[WorldviewPosition])

case class Worldview(worldviewCategories: Seq[WorldviewCategory], references: Seq[HtmlContent])

object Worldview {
  def apply(url: URL): Try[Worldview] = for {
    markdown <- Try(Source.fromURL(url).mkString)
    worldview <- apply(markdown)
  } yield worldview

  // @todo use XML source instead...?

  def apply(markdown: String): Try[Worldview] = Try {
    //val html = Processor.process(markdown)

    val nonEmptyLines = markdown.split("\\r?\\n").toSeq.map(_.trim).filter(_.nonEmpty)

    val linesGroupedByHeaders: Seq[(String, Seq[String])] =
      linesByH2Headers(nonEmptyLines) filterNot { case (h2Header, _) =>
        H2HeadersToIgnore.contains(h2Header.toLowerCase)
      }

    val references: Seq[String] =
      linesGroupedByHeaders.find(_._1.toLowerCase == ReferencesH2Header).map(_._2).getOrElse(Seq())

    println(s"references = $references")

    Worldview(Seq(), Seq())
  }

  private def linesByH2Headers(lines: Seq[String]): Seq[(String, Seq[String])] =
    linesByHeaders(lines, headerLevel = 2)

  private def linesByH5Headers(lines: Seq[String]): Seq[(String, Seq[String])] =
    linesByHeaders(lines, headerLevel = 5)

  private def linesByHeaders(lines: Seq[String], headerLevel: Int): Seq[(String, Seq[String])] = {
    val headerPrefix = "#" * headerLevel + " "

    val result = mutable.Buffer[(String, Seq[String])]()
    var header = ""
    var headerLines = Seq[String]()

    def accumulate(headerLineOption: Option[String]): Unit = {
      result.append((header, headerLines))
      headerLineOption foreach { headerLine =>
        header = headerLine.substring(headerPrefix.length)
        headerLines = Seq()
      }
    }

    for (line <- lines) {
      if (line.startsWith(headerPrefix)) {
        accumulate(headerLineOption = Some(line))
      } else {
        headerLines = headerLines :+ line
      }
    }

    accumulate(headerLineOption = None)

    result.toSeq
  }

  private val ThemesToAddH2Header = "thèmes à ajouter"
  private val IdeasToAddH2Header = "idées à ajouter"
  private val H2HeadersToIgnore = Set(ThemesToAddH2Header, IdeasToAddH2Header)

  private val ReferencesH2Header = "références générales"
}

/*
## Explication du monde ("Quelle est la nature de notre univers ? Comment fonctionne-t-il ?")

##### Il existe une réalité matérielle objective, unique, dans laquelle nous existons et avons nos expériences subjectives.

J'adhère donc à un point de vue [réaliste](http://fr.wikipedia.org/wiki/R%C3%A9alisme_%28philosophie%29) et [matérialiste](http://fr.wikipedia.org/wiki/Mat%C3%A9rialisme) (ou, pour être un peu plus précis, [physicaliste](http://fr.wikipedia.org/wiki/Physicalisme) et [moniste](http://fr.wikipedia.org/wiki/Monisme)). J'adhère également et en particulier à la position que [Paul Thagard](https://en.wikipedia.org/wiki/Paul_Thagard) appelle le [réalisme constructif](http://philoscience.over-blog.com/article-the-brain-and-the-meaning-of-life-par-pa-51282235.html). Ce point de vue détermine à lui seul toute une série de concepts en lesquels je ne crois pas, car, de manière générale, pour moi, il n'existe rien en dehors de cette réalité matérielle dont il est question. Je ne crois par exemple pas en Dieu ([athéisme](http://fr.wikipedia.org/wiki/Ath%C3%A9isme)). De même, je ne crois pas en l'existence de l'âme (absence de [dualisme](https://fr.wikipedia.org/wiki/Dualisme_(philosophie_de_l%27esprit))) ; je crois que la conscience est une propriété émergente de notre cerveau (ce qui, en soi, n'explique rien et demeure, pour l'instant, un "mystère"). Je ne crois toutefois pas forcément que l'être humain pourra un jour comprendre l'entier de la réalité dans laquelle il évolue. En tout cas pas en se contentant de son seul cerveau. Je pense en effet que les ordinateurs et l'intelligence artificielle joueront un rôle fondamental dans la compréhension de l'univers.

##### L'univers dans lequel nous vivons est déterministe.
...

## Références générales

* [ebruchez's world view](https://github.com/ebruchez/public/blob/master/Wordview.md)
* [World Views: From fragmentation to integration](http://www.vub.ac.be/CLEA/pub/books/worldviews.pdf)
* [World view development](https://en.wikipedia.org/wiki/World_view#Development)

## Thèmes à ajouter

 * armée, guerre
 * armes (port d'armes, etc.)
 * drogues (légalisation, cannabis, etc)
 * droits des enfants (éducation, travail, etc.)
 * droits des femmes (avortement, [prostitution](http://danielmiessler.com/blog/current-view-prostitution/), etc.)
 * droits des homosexuels (mariage, adoption, etc.)
 * état (lien état / individu, lien état / économie, etc.)
 * finance
 * justice (prison, réhabilitation, etc.)
 * liberté d'expression, de la presse, de blasphémer (cf Charlie Hebdo)
 * politique (démocratie, systèmes politiques, etc.)
 * santé (assurances, assistance au suicide, etc.)
 * vin, audiophilie (effet placebo et snobisme)
 * voir aussi ["What do philosophers believe?"](http://philpapers.org/archive/BOUWDP)

## A faire / idées à ajouter
...
 */