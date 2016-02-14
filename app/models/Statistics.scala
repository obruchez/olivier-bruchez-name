package models

case class Statistics(concerts: Concerts) {
  def mostSeenArtists(mainMusiciansOnly: Boolean): Seq[(Double, String)] = {
    val strings =
      concerts.listItems.filterNot(_.concertType == Soundcheck).flatMap { concert =>
          if (concert.musicians.size == 1) {
            concert.musicians.map(_.name)
          } else if (mainMusiciansOnly) {
            val mainMusicians = concert.musicians.filter(_.main).map(_.name)

            concert.band match {
              case Some(band) =>
                // No main musician + band available => return band instead
                if (mainMusicians.isEmpty) Seq(band) else Seq()
              case None =>
                mainMusicians
            }
          } else {
            concert.musicians.map(_.name)
          }
      }.filterNot(_ == UnknownArtist)

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
