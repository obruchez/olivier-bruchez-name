package util

object Strings {
  def rightTrim(string: String): String =
    string.replaceAll("\\s+$", "")

  def truncatedWithDots(string: String, maxSize: Int, onWordLimit: Boolean): String = {
    val dots = "..."
    val dotsLength = dots.length

    def truncatedAnywhere(string: String): String = rightTrim(string.substring(0, (maxSize - dotsLength).max(0))) + dots

    def truncatedOnWordLimit(string: String): String = {
      @annotation.tailrec
      def wordCount(remainingWords: List[String]): Int = {
        val joinedWords = remainingWords.mkString(" ")
        if (joinedWords.length + dotsLength <= maxSize)
          remainingWords.size
        else if (remainingWords.size > 1)
          wordCount(remainingWords.init)
        else
          0
      }

      val words = string.split(' ').toList

      // What is the maximum number of words we can keep to get a string of length maxSize (including the dots)?
      val wordCountForInputString = wordCount(words)

      if (wordCountForInputString == 0)
        truncatedAnywhere(string) // Doesn't work with whole words => truncate in the middle of the first word
      else
        words.take(wordCountForInputString).mkString(" ") + dots // Remove some words and add dots
    }

    if (string.length > maxSize)
      if (onWordLimit) truncatedOnWordLimit(string) else truncatedAnywhere(string)
    else
      string
  }

  def withoutPrefix(string: String, prefix: String): String =
    if (string.startsWith(prefix)) string.substring(prefix.length) else string

  def withoutSuffix(string: String, suffix: String): String =
    if (string.endsWith(suffix)) string.substring(0, string.length - suffix.length) else string

  def withSuffix(string: String, suffix: String): String =
    if (!string.endsWith(suffix)) string + suffix else string
}
