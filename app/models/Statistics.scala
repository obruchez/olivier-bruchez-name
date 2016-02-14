package models

case class Statistics(concerts: Concerts) {
  def mostSeenArtists(mainMusiciansOnly: Boolean): Seq[(Double, String)] = {
    val musicianFilter: (Musician) => Boolean = if (mainMusiciansOnly) _.main else _ => true

    val strings =
      concerts.listItems.flatMap { concert =>
        if (concert.musicians.size == 1) concert.musicians else concert.musicians.filter(musicianFilter)
      }.map(_.name).filterNot(_ == UnknownArtist)

    sortedValuesWithLabels(strings)
  }

  private def sortedValuesWithLabels(strings: Seq[String]): Seq[(Double, String)] =
    strings.groupBy(string => string).
      toSeq.
      map(kv => kv._2.size.toDouble -> kv._1).
      // Sort by count (highest first) and then by name (alphabetical order)
      sortBy(kv => (-kv._1, kv._2))

  private val UnknownArtist = "?"
}
