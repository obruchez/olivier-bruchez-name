package util

sealed trait StarType
case object FullStar extends StarType
case object HalfStar extends StarType
case object EmptyStar extends StarType

object Rating {
  def starTypesForRating(rating: Double, maxStarCount: Int = 5): Seq[StarType] = {
    var remainingRating = rating

    for (star <- 1 to maxStarCount) yield {
      if (remainingRating >= 0.75) {
        remainingRating -= 1.0
        FullStar
      } else if (remainingRating >= 0.25) {
        remainingRating -= 0.5
        HalfStar
      } else {
        EmptyStar
      }
    }
  }
}
