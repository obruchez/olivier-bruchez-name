package org.bruchez.olivier.snippet

import net.liftweb.http._
import org.bruchez.olivier.model.User
import xml.NodeSeq

object Home {
  //def render: NodeSeq = S.redirectTo("http://bruchez.blogspot.com/")
  def render: NodeSeq = <span>{"Hello, "+User.currentUser+", logged in = "+User.loggedIn_?}</span>
}
