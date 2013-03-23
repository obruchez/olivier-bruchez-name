package org.bruchez.olivier.model

import net.liftmodules.openid.{MetaOpenIDProtoUser, OpenIDProtoUser}
import net.liftweb.mapper.{LongKeyedMapper, LongKeyedMetaMapper}
import net.liftweb.common.Full
import org.bruchez.olivier.login.OpenIdVendor

class User extends LongKeyedMapper[User] with OpenIDProtoUser[User] {
  def getSingleton = User

  override def niceName: String = (firstName.is, lastName.is, email.is) match {
    case (f, l, e) if f.length > 1 && l.length > 1 => f+" "+l+" ("+e+")"
    case (f, _, e) if f.length > 1 => f+" ("+e+")"
    case (_, l, e) if l.length > 1 => l+" ("+e+")"
    case (_, _, e) => e
  }
}

object User extends User with MetaOpenIDProtoUser[User] with LongKeyedMetaMapper[User] {
  override lazy val dbTableName = "users"
  override lazy val openIDVendor = OpenIdVendor
  override lazy val loginPath = List("login")
  override lazy val logoutPath = List("logout")
  override lazy val screenWrap = Full(<lift:surround with="login_template" at="content"><lift:bind /></lift:surround>)
  override lazy val loginXhtml =  <lift:embed what="login" />
  override lazy val fieldOrder = List(id, firstName, lastName, email, locale, timezone, password)
  override lazy val skipEmailValidation = true

  def loginUrl: String = "/"+loginPath.mkString("/")
  def logoutUrl: String = "/"+logoutPath.mkString("/")
}
