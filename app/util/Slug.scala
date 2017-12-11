package util

object Slug {
  def slugFromString(string: String): String =
    normalizedString(string).replaceAll(" ", "-")

  def withBaseUrl(baseUrl: String, slug: String): String =
    s"$baseUrl#$slug"

  private def normalizedString(string: String): String = {
    import java.text.Normalizer

    // http://stackoverflow.com/questions/3322152/java-getting-rid-of-accents-and-converting-them-to-regular-letters

    val nfdNormalizedString = Normalizer.normalize(string.toLowerCase.trim, Normalizer.Form.NFD)

    CombiningDiacriticalMarks
      .replaceAllIn(nfdNormalizedString, "")
      . // Replace accented characters by their non-accented equivalents
      replaceAll("\\.", "")
      . // Remove dots (don't replace them by spaces)
      replaceAll("[^a-zA-Z0-9 ]", " ")
      . // Keep only letters, digits and spaces
      replaceAll(" +", " ")
      . // Replace multiple spaces by single spaces
      trim // Final trim to remove leading and trailing spaces
  }

  private val CombiningDiacriticalMarks = """\p{InCombiningDiacriticalMarks}+""".r
}
