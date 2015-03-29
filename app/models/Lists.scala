package models

import org.joda.time.Partial
import scala.util.Try
import util.Date

object Lists {
  def dateFromString(string: String): Try[Partial] =
    Date.partialFromYyyymmddString(string.trim)

  def ratingFromString(string: String): Option[Double] =
    Option(string).map(_.trim).filter(_.nonEmpty).map(_.toDouble - 1)

  def commentsFromString(string: String): Option[String] =
    Option(string).map(_.trim).filter(_.nonEmpty)

  def isTrue(string: String): Boolean =
    Set("on", "true", "y", "yes", "1").contains(string.trim.toLowerCase)

  /*
      <xsl:template name="comma-or-and">
        <xsl:choose>
            <xsl:when test="position()=last()-1">
                <xsl:choose>
                    <xsl:when test="last()=2">
                        <xsl:text> and </xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>, and </xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="position()!=last()">
                <xsl:text>, </xsl:text>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
   */

  def commaOrAnd(index: Int, totalCount: Int): String =
    if (index == totalCount - 2)
      if (totalCount == 2) " and " else ", and"
    else if (index < totalCount - 2)
      ", "
    else
      ""
}
