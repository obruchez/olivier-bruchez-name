package blogger

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{ GoogleAuthorizationCodeFlow, GoogleClientSecrets }
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.blogger.{ Blogger, BloggerScopes }
import java.util.Collections
import util.Configuration

object Test {
  private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
  private val jsonFactory = JacksonFactory.getDefaultInstance

  private val dataStorePath = Configuration.string("blogger.credentialpath").get
  private val dataStoreDirectory = new java.io.File(System.getProperty("user.home"), dataStorePath)
  private val dataStoreFactory = new FileDataStoreFactory(dataStoreDirectory)

  private def authorize(): Credential = {
    val clientId = Configuration.string("blogger.clientid").get
    val clientSecret = Configuration.string("blogger.clientsecret").get

    val clientDetails = (new GoogleClientSecrets.Details).setClientId(clientId).setClientSecret(clientSecret)
    val clientSecrets = (new GoogleClientSecrets).setInstalled(clientDetails)

    val flow = new GoogleAuthorizationCodeFlow.Builder(
      httpTransport,
      jsonFactory,
      clientSecrets,
      Collections.singleton(BloggerScopes.BLOGGER_READONLY)).setDataStoreFactory(dataStoreFactory).build()

    // allow redirect URI here: https://console.developers.google.com/apis/credentials?project=olivier-bruchez-org

    // https://stackoverflow.com/questions/21002827/google-oauth2-api-client-is-not-working-properly
    val localServerReceiver = new LocalServerReceiver.Builder().setPort(9089).build()

    new AuthorizationCodeInstalledApp(flow, localServerReceiver).authorize("single-user")
  }

  val credential = authorize()
  val applicationName = Configuration.string("blogger.applicationname").get
  val blogger = new Blogger.Builder(httpTransport, jsonFactory, credential).setApplicationName(applicationName).build()
}
