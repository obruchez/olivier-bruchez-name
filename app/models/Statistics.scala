package models

case class Statistics(concerts: Concerts) {
  def mostSeenArtists(leadersOnly: Boolean): Seq[(Double, String)] = {
    val musicianFilter: (Musician) => Boolean = if (leadersOnly) _.leader else _ => true

    val strings =
      concerts.listItems.flatMap(_.musicians.filter(musicianFilter).map(_.name)).filterNot(_ == UnknownArtist)

    sortedValuesWithLabels(strings)
  }

  private def sortedValuesWithLabels(strings: Seq[String]): Seq[(Double, String)] =
    strings.groupBy(string => string).
      toSeq.
      map(kv => kv._2.size.toDouble -> kv._1).
      sortBy(_._1).
      reverse

  private val UnknownArtist = "?"
}
