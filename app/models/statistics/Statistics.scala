package models.statistics

import models.lifelogging.{ Concerts, Soundcheck }

case class Statistics(concerts: Concerts) {
  def mostSeenArtists(mainMusiciansOnly: Boolean): Seq[(Int, String)] = {
    val strings =
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
      }.filterNot(_ == UnknownArtist)

    sortedValuesWithLabels(strings)
  }

  def mostVisitedConcertVenues(): Seq[(Int, String)] = {
    import util.Date._

    val normalizedName = Map("Casino Barrière, Montreux, Switzerland" -> "Casino, Montreux, Switzerland")

    // Count a venue at most once for a given day
    val concertVenues =
      concerts.
        listItems.
        map(c => (c.location, c.date.yyyymmddString)).
        distinct.
        map(_._1).
        filterNot(_.isEmpty).
        map(location => normalizedName.getOrElse(location, location))

    sortedValuesWithLabels(concertVenues)
  }

  private def sortedValuesWithLabels(strings: Seq[String]): Seq[(Int, String)] =
    strings.groupBy(string => string).
      toSeq.
      map(kv => kv._2.size -> kv._1).
      // Sort by count (highest first) and then by name (alphabetical order)
      sortBy(kv => (-kv._1, kv._2))

  private val UnknownArtist = "?"
}
