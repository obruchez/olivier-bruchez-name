package org.bruchez.olivier.snippet

import org.bruchez.olivier.model._
import scala.xml.NodeSeq

object UserMenu {
  def render: NodeSeq = User.currentUser map { user =>
    <ul class="dropdown-menu">
      <li><a href="#">{user.niceName}</a></li>
      <li class="divider"></li>
      <li><a href={User.logoutUrl}>Logout</a></li>
    </ul>
  } openOr {
    <ul class="dropdown-menu">
      <li><a href={User.loginUrl}>Login</a></li>
    </ul>
  }
}
