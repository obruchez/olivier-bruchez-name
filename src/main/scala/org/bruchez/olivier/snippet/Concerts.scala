package org.bruchez.olivier.snippet

import net.liftweb.common._
import net.liftweb.sitemap._
import net.liftweb.util.Helpers._
import org.bruchez.olivier.model._

object Concerts extends Loggable {

  val ccList = Country.getAllList()

  lazy val menu = Menu.i("Concerts") / "concerts"

  def render = {
    "a" #> (ccList map { c =>
      "* [href]" #> ACountry.menu.calcHref(c) &
      "* *" #> (c.name)
    })
  }
}
