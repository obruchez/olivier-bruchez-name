package blogger

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{ GoogleAuthorizationCodeFlow, GoogleClientSecrets }
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.{MemoryDataStoreFactory, FileDataStoreFactory}
import com.google.api.services.blogger.{ Blogger, BloggerScopes }
import java.util.Collections
import util.Configuration

object Blogger {
  private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
  private val jsonFactory = JacksonFactory.getDefaultInstance

  val blogger = new Blogger(httpTransport, jsonFactory, null)
}
