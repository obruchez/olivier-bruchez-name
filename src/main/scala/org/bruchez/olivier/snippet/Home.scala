package org.bruchez.olivier.snippet

import net.liftweb.http._
import xml.NodeSeq

object Home {
  def render: NodeSeq = {
    println("*** Home")
    S.redirectTo("http://bruchez.blogspot.com/")
  }
}
