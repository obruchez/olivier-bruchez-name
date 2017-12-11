package models.statistics

import models.ListItem
import models.lifelogging._
import org.joda.time.{DateTimeFieldType, Partial}
import scala.util.Try
import util.Date._

case class MinAvgMax(min: Double, avg: Double, max: Double)

object MinAvgMax {
  def apply(values: Seq[Double]): MinAvgMax =
    MinAvgMax(min = values.min, avg = values.sum / values.size.toDouble, max = values.max)
}

case class Statistics(books: Books,
                      concerts: Concerts,
                      exhibitions: Exhibitions,
                      movies: Movies,
                      plays: Plays,
                      podcasts: Podcasts) {
  def mostReadBookAuthors: Seq[(Int, String)] = {
    val authors = books.listItems.map(_.author)
    sortedValuesWithLabels(personsFromRawList(authors))
  }

  def bookRatingsDistribution: Seq[(String, Int)] =
    ratingsDistribution(books.listItems.flatMap(_.rating))

  def bookRatingsEvolution: Seq[(String, MinAvgMax)] =
    ratingsEvolution(books.listItems.flatMap(i => i.rating.map(r => (i.date, r))))

  def bookYearlyCounts: Seq[(String, Int)] =
    yearlyCounts(books.listItems)

  def mostSeenConcertArtists(mainMusiciansOnly: Boolean): Seq[(Int, String)] = {
    val artists =
      concerts.listItems.filterNot(_.concertType == Soundcheck).flatMap { concert =>
        if (concert.musicians.size == 1) {
          concert.musicians.map(_.name)
        } else if (mainMusiciansOnly) {
          val mainMusicians = concert.musicians.filter(_.main).map(_.name)

          concert.band match {
            case Some(band) if mainMusicians.isEmpty =>
              // No main musician + band available => return band instead
              Seq(band)
            case _ =>
              mainMusicians
          }
        } else {
          concert.musicians.map(_.name)
        }
      }

    sortedValuesWithLabels(personsFromRawList(artists))
  }

  def mostVisitedConcertVenues: Seq[(Int, String)] = {
    import util.Date._

    val normalizedName = Map(
      "Casino Barrière, Montreux, Switzerland" -> "Casino, Montreux, Switzerland")

    // Count a venue at most once for a given day
    val concertVenues =
      concerts.listItems
        .map(c => (c.location, c.date.yyyymmddString))
        .distinct
        .map(_._1)
        .filterNot(_.isEmpty)
        .map(location => normalizedName.getOrElse(location, location))

    sortedValuesWithLabels(concertVenues)
  }

  def concertRatingsDistribution: Seq[(String, Int)] =
    ratingsDistribution(concerts.listItems.flatMap(_.rating))

  def concertRatingsEvolution: Seq[(String, MinAvgMax)] =
    ratingsEvolution(concerts.listItems.flatMap(i => i.rating.map(r => (i.date, r))))

  def concertYearlyCounts: Seq[(String, Int)] =
    yearlyCounts(concerts.listItems)

  def mostSeenMovieDirectors: Seq[(Int, String)] = {
    val directors = movies.listItems.map(_.director)
    sortedValuesWithLabels(personsFromRawList(directors))
  }

  def mostSeenMovies: Seq[(Int, String)] = {
    val movieTitles = movies.listItems.map(_.title)
    sortedValuesWithLabels(movieTitles)
  }

  def movieRatingsDistribution: Seq[(String, Int)] =
    ratingsDistribution(movies.listItems.flatMap(_.rating))

  def movieRatingsEvolution: Seq[(String, MinAvgMax)] =
    ratingsEvolution(movies.listItems.flatMap(i => i.rating.map(r => (i.date, r))))

  def movieYearlyCounts: Seq[(String, Int)] =
    yearlyCounts(movies.listItems)

  def mostSeenPlayAuthors: Seq[(Int, String)] = {
    val authors = plays.listItems.map(_.author)
    sortedValuesWithLabels(personsFromRawList(authors))
  }

  def mostSeenPlayDirectors: Seq[(Int, String)] = {
    val directors = plays.listItems.map(_.director)
    sortedValuesWithLabels(personsFromRawList(directors))
  }

  def mostSeenPlayActors: Seq[(Int, String)] = {
    val actors = plays.listItems.flatMap(_.actors)
    sortedValuesWithLabels(personsFromRawList(actors))
  }

  def mostVisitedPlayTheatre: Seq[(Int, String)] = {
    val theatres = plays.listItems.map(_.location)
    sortedValuesWithLabels(theatres)
  }

  def playRatingsDistribution: Seq[(String, Int)] =
    ratingsDistribution(plays.listItems.flatMap(_.rating))

  def playRatingsEvolution: Seq[(String, MinAvgMax)] =
    ratingsEvolution(plays.listItems.flatMap(i => i.rating.map(r => (i.date, r))))

  def playYearlyCounts: Seq[(String, Int)] =
    yearlyCounts(plays.listItems)

  def mostListenedToPodcasts: Seq[(Int, String)] = {
    val podcastNames = podcasts.listItems.map(_.name)
    sortedValuesWithLabels(podcastNames)
  }

  def podcastYearlyCounts: Seq[(String, Int)] =
    yearlyCounts(podcasts.listItems)

  private def sortedValuesWithLabels(strings: Seq[String]): Seq[(Int, String)] =
    strings
      .groupBy(string => string)
      .toSeq
      .map(kv => kv._2.size -> kv._1)
      .
      // Sort by count (highest first) and then by name (alphabetical order)
      sortBy(kv => (-kv._1, kv._2))

  private def personsFromRawList(rawList: Seq[String]): Seq[String] =
    rawList.flatMap(_.split("[,&]")).map(_.trim).filterNot(PersonsToIgnore.contains)

  private val PersonsToIgnore = Set("", "?", "etc.")

  private def ratingsDistribution(ratings: Seq[Double]): Seq[(String, Int)] =
    for {
      rating <- 0.0 to (5.0, 0.25)
      count = ratings.count(_ == rating)
    } yield f"$rating%1.2f" -> count

  private def ratingsEvolution(datesAndRatings: Seq[(Partial, Double)]): Seq[(String, MinAvgMax)] =
    for {
      (yearOption, datesAndRatings) <- datesAndRatings
        .groupBy(dar => Try(dar._1.get(DateTimeFieldType.year())).toOption)
        .toSeq
        .sortBy(_._1)
      year <- yearOption
      minAvgMax = MinAvgMax(datesAndRatings.map(_._2))
    } yield (year.toString, minAvgMax)

  protected def yearlyCounts(listItems: Seq[ListItem]): Seq[(String, Int)] = {
    val yearsAndCounts =
      for {
        (yearOption, items) <- listItems.groupBy(li => li.date.year).toSeq
        year <- yearOption
      } yield (year, items.size)

    yearsAndCounts.sortBy(_._1).map { case (year, count) => (year.toString, count) }
  }
}
