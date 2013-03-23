package org.bruchez.olivier.login

import net.liftmodules.openid._
import net.liftweb.common._
import org.openid4java.discovery.DiscoveryInformation
import org.openid4java.message.AuthRequest

object OpenIdVendor extends SimpleOpenIDVendor {
  def addExtension(discoveryInformation: DiscoveryInformation, authReq: AuthRequest) {
    WellKnownEndpoints.findEndpoint(discoveryInformation) map { discoveredEndpoint =>
      import WellKnownAttributes._
      val attributes = List(Email, FullName, FirstName, LastName)
      discoveredEndpoint.makeAttributeExtension(attributes) foreach { messageExtension =>
        authReq.addExtension(messageExtension)
      }
    }
  }

  override def createAConsumer = new OpenIDConsumer[UserType] {
    beforeAuth = Full(addExtension _)
  }
}
