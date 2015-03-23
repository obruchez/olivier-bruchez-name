package controllers

import java.util.Calendar

object Footer {
  def copyrightYears: String = {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    if (currentYear == StartYear) s"$StartYear" else s"$StartYear-$currentYear"
  }

  private val StartYear = 2015
}
