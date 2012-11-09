package mhs.src.storage.persistence.remote;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.api.services.oauth2.model.Userinfo;

/**
 * OAuth2
 * 
 * OAuth2 Authenticator
 * 
 * - Google API Authentication via OAuth2 - Credential
 * 
 * @author timlyw@google.com (Your Name Here)
 * 
 */
public class OAuth2 {

  private static final String CLIENT_SECRET = "7it1jgxKg8RVFc0f8YuBsU5j";
  private static final String CLIENT_ID = "975927934512.apps.googleusercontent.com";

  /** Global instance of the HTTP transport. */
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  /** OAuth 2.0 scopes. */
  private static final List<String> SCOPES = Arrays.asList(
      "https://www.googleapis.com/auth/userinfo.profile",
      "https://www.googleapis.com/auth/userinfo.email", "https://www.googleapis.com/auth/tasks",
      "https://www.googleapis.com/auth/calendar");

  private static Oauth2 oauth2;
  private static GoogleClientSecrets clientSecrets;
  static FileCredentialStore credentialStore;
  static GoogleAuthorizationCodeFlow flow;
  static Credential credential;

  /** Authorizes the installed application to access user's protected data. */
  private static Credential authorize() throws Exception {
    // set up file credential store
    FileCredentialStore credentialStore =
        new FileCredentialStore(new File(".credentials/oauth2.json"), JSON_FACTORY);
    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, CLIENT_ID,
            CLIENT_SECRET, SCOPES).setAccessType("offline").setApprovalPrompt("force").build();

    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }

  public OAuth2() {
    try {
      try {
        // authorization
        credential = authorize();
        // set up global Oauth2 instance
        oauth2 =
            new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(
                "Google-OAuth2Sample/1.0").build();
        // run commands
        tokenInfo(credential.getAccessToken());
        userInfo();
        // success!
        return;
      } catch (IOException e) {
        System.err.println(e.getMessage());
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }


  public static HttpTransport getHttpTransport() {
    return HTTP_TRANSPORT;
  }

  public static JsonFactory getJsonFactory() {
    return JSON_FACTORY;
  }

  public static Credential getCredential() {
    return credential;
  }

  public static String getAccessToken() {
    return credential.getAccessToken();
  }

  private static void tokenInfo(String accessToken) throws IOException {
    header("Validating a token");
    Tokeninfo tokeninfo = oauth2.tokeninfo().setAccessToken(accessToken).execute();
    System.out.println(tokeninfo.toPrettyString());
    if (!tokeninfo.getAudience().equals(CLIENT_ID)) {
      System.err.println("ERROR: audience does not match our client ID!");
    }
  }

  private static void userInfo() throws IOException {
    header("Obtaining User Profile Information");
    Userinfo userinfo = oauth2.userinfo().get().execute();
    System.out.println(userinfo.toPrettyString());
  }

  static void header(String name) {
    System.out.println();
    System.out.println("================== " + name + " ==================");
    System.out.println();
  }
}
