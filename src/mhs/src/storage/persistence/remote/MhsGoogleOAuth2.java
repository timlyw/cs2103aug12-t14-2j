//@author A0087048X
package mhs.src.storage.persistence.remote;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.NoActiveCredentialException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.api.services.oauth2.model.Userinfo;

/**
 * MhsGoogleOAuth2
 * 
 * Google API Authentication via OAuth2 with refresh mechanism for MHS access.
 * Allows single active credential.
 * 
 * Functionality: Google Authenticator via browser<br>
 * Credential Storage and creator<br>
 * Instance Creator for:<br>
 * 1. Oauth2<br>
 * 2. httpTransport<br>
 * 3. jsonFactory<br>
 * 
 * @author Timothy Lim Yi Wen A0087048X
 * 
 */
public class MhsGoogleOAuth2 {

	private static final String GOOGLE_USER_INFO_EMAIL = "email";
	private static final String GOOGLE_USER_INFO_GIVEN_NAME = "given_name";

	// Configurables
	private static final String FILE_PATH_CREDENTIALS_OAUTH2 = ".credentials/oauth2.json";
	private static final String CLIENT_SECRET = "NO9b_OyMLmAGiYhCVyPrprpy";
	private static final String CLIENT_ID = "678151509235.apps.googleusercontent.com";
	private static final String SCOPE_GOOGLEAPIS_COM_AUTH_TASKS = "https://www.googleapis.com/auth/tasks";
	private static final String SCOPE_GOOGLEAPIS_COM_AUTH_CALENDAR = "https://www.googleapis.com/auth/calendar";
	private static final String SCOPE_GOOGLEAPIS_COM_AUTH_USER_INFO_PROFILE = "https://www.googleapis.com/auth/userinfo.profile";
	private static final String SCOPE_GOOGLEAPIS_COM_AUTH_USER_INFO_EMAIL = "https://www.googleapis.com/auth/userinfo.email";
	private static final List<String> OAUTH_2_SCOPES = Arrays.asList(
			SCOPE_GOOGLEAPIS_COM_AUTH_TASKS,
			SCOPE_GOOGLEAPIS_COM_AUTH_CALENDAR,
			SCOPE_GOOGLEAPIS_COM_AUTH_USER_INFO_PROFILE,
			SCOPE_GOOGLEAPIS_COM_AUTH_USER_INFO_EMAIL);

	// Constants
	private static final String GOOGLE_OAUTH2_DEFAULT_USER_ID = "cs2103mhs@gmail.com";
	private static final String GOOGLE_OAUTH2_APPLICATION_NAME = "MHS/0.5";
	private static final String GOOGLE_OAUTH2_APPROVAL_PROMPT_METHOD_FORCE = "force";
	private static final String GOOGLE_OAUTH2_ACCESS_TYPE_OFFLINE = "offline";

	// Messages
	private static final String EXCEPTION_MESSAGE_NO_VALID_CREDENTIAL = "No valid credential. Call authorizeCredentialAndStoreInCredentialStore to setup credential.";
	private static final String ERROR_MESSAGE_ERROR_AUDIENCE_DOES_NOT_MATCH_OUR_CLIENT_ID = "ERROR: audience does not match our client ID!";

	private static MhsGoogleOAuth2 instance = null;
	private static Oauth2 oAuth2;
	private static FileCredentialStore credentialStore;
	private static Credential credential;

	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	private static final Logger logger = MhsLogger.getLogger();

	private static String GOOGLE_CURRENT_OAUTH2_USER_ID = GOOGLE_OAUTH2_DEFAULT_USER_ID;

	/**
	 * Returns instance of MhsGoogleOAuth2
	 * 
	 * @return MhsGoogleOAuth2 instance
	 * @throws IOException
	 */
	public static MhsGoogleOAuth2 getInstance() throws IOException {
		if (instance == null) {
			instance = new MhsGoogleOAuth2();
		}
		return instance;
	}

	/**
	 * OAuth2 Constructor
	 * 
	 * Authorizes credential and sets up Oauth2 instance
	 * 
	 * @throws IOExceptionsyn
	 */
	public MhsGoogleOAuth2() throws IOException {
		logEnterMethod("OAuth2");
		setupCredentialStore();
		logExitMethod("OAuth2");
	}

	/**
	 * Setup User Id for Credential retrieval
	 * 
	 * @param userId
	 *            or null for default user
	 */
	public static void setupUserId(String userId) {
		logEnterMethod("setupUserId");
		if (userId != null) {
			GOOGLE_CURRENT_OAUTH2_USER_ID = userId;
		} else {
			GOOGLE_CURRENT_OAUTH2_USER_ID = GOOGLE_OAUTH2_DEFAULT_USER_ID;
		}
		logExitMethod("setupUserId");
	}

	/**
	 * Authorizes the installed application to access user's Google data
	 * 
	 * Prompts user to authenticate or refreshes AuthToken if refresh token
	 * exists
	 * 
	 * @return Credential
	 * @throws Exception
	 */
	private static Credential authorizeCredential() throws Exception {
		logEnterMethod("authorizeCredential");
		assert (HTTP_TRANSPORT != null);
		assert (JSON_FACTORY != null);
		assert (CLIENT_ID != null);
		assert (CLIENT_SECRET != null);
		assert (credentialStore != null);

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, CLIENT_ID, CLIENT_SECRET,
				OAUTH_2_SCOPES)
				.setAccessType(GOOGLE_OAUTH2_ACCESS_TYPE_OFFLINE)
				.setApprovalPrompt(GOOGLE_OAUTH2_APPROVAL_PROMPT_METHOD_FORCE)
				.setCredentialStore(credentialStore).build();

		Credential authorizationCodeInstalledApp = new AuthorizationCodeInstalledApp(
				flow, new LocalServerReceiver())
				.authorize(GOOGLE_CURRENT_OAUTH2_USER_ID);

		logExitMethod("authorizeCredential");
		return authorizationCodeInstalledApp;
	}

	/**
	 * Deletes current user credential
	 * 
	 * @param userId
	 * @throws IOException
	 */
	public static void deleteCurrentCredential() throws IOException {
		logEnterMethod("deleteCurrentCredential");
		credentialStore.delete(GOOGLE_CURRENT_OAUTH2_USER_ID, credential);
		credential = null;
		logExitMethod("deleteCurrentCredential");
	}

	/**
	 * Set up file credential store
	 * 
	 * @throws IOException
	 */
	private static void setupCredentialStore() throws IOException {
		logEnterMethod("setUpCredentialStore");
		File credentialStoreFile = new File(FILE_PATH_CREDENTIALS_OAUTH2);
		credentialStore = new FileCredentialStore(credentialStoreFile,
				JSON_FACTORY);
		logExitMethod("setUpCredentialStore");
	}

	/**
	 * Log user and token info after authentication
	 * 
	 * @throws IOException
	 */
	private static void logUserAndTokenInfo() throws IOException {
		logExitMethod("logUserAndTokenInfo");
		logger.log(Level.INFO, getTokenInfo(credential.getAccessToken()));
		logger.log(Level.INFO, getUserInfo());
		logExitMethod("logUserAndTokenInfo");
	}

	/**
	 * Builds Oauth2 instance
	 * 
	 * @throws NoActiveCredentialException
	 *             when no credential is active
	 */
	private static void buildOAuth2() throws NoActiveCredentialException {
		logEnterMethod("buildOAuth2");
		if (credential == null) {
			throw new NoActiveCredentialException(
					EXCEPTION_MESSAGE_NO_VALID_CREDENTIAL);
		}
		oAuth2 = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(GOOGLE_OAUTH2_APPLICATION_NAME).build();
		logExitMethod("buildOAuth2");
	}

	/**
	 * Authorize credential and stores in credential store for persistent use
	 * 
	 * @throws Exception
	 * @throws IOException
	 */
	public static void authorizeCredentialAndStoreInCredentialStore()
			throws Exception, IOException {
		logEnterMethod("authorizeCredentialAndStoreInCredentialStore");
		credential = authorizeCredential();
		credentialStore.store(GOOGLE_CURRENT_OAUTH2_USER_ID, credential);
		buildOAuth2();
		logUserAndTokenInfo();
		logExitMethod("authorizeCredentialAndStoreInCredentialStore");
	}

	/**
	 * Getter for getHttpTransport instance
	 * 
	 * @return
	 */
	public static HttpTransport getHttpTransport() {
		logEnterMethod("getHttpTransport");
		logExitMethod("getHttpTransport");
		return HTTP_TRANSPORT;
	}

	/**
	 * Getter for getJsonFactory instance
	 * 
	 * @return
	 */
	public static JsonFactory getJsonFactory() {
		logEnterMethod("getJsonFactory");
		logExitMethod("getJsonFactory");
		return JSON_FACTORY;
	}

	/**
	 * Getter for Oauth instance
	 * 
	 * @return Oauth
	 */
	public static Oauth2 getOauth() {
		logEnterMethod("getOauth");
		logExitMethod("getOauth");
		return oAuth2;
	}

	/**
	 * Getter for Credential instance
	 * 
	 * @return Credential
	 * @throws NoActiveCredentialException
	 */
	public static Credential getCredential() throws NoActiveCredentialException {
		logEnterMethod("Credential");
		if (credential == null) {
			throw new NoActiveCredentialException(
					EXCEPTION_MESSAGE_NO_VALID_CREDENTIAL);
		}
		logExitMethod("Credential");
		return credential;
	}

	/**
	 * Getter for access token
	 * 
	 * @return accessToken
	 * @throws NoActiveCredentialException
	 */
	public static String getAccessToken() throws NoActiveCredentialException {
		logEnterMethod("getAccessToken");
		logExitMethod("getAccessToken");
		if (credential == null) {
			throw new NoActiveCredentialException(
					EXCEPTION_MESSAGE_NO_VALID_CREDENTIAL);
		}
		return credential.getAccessToken();
	}

	private static String getTokenInfo(String accessToken) throws IOException {
		logEnterMethod("tokenInfo");
		Tokeninfo tokeninfo = oAuth2.tokeninfo().setAccessToken(accessToken)
				.execute();
		if (!tokeninfo.getAudience().equals(CLIENT_ID)) {
			return ERROR_MESSAGE_ERROR_AUDIENCE_DOES_NOT_MATCH_OUR_CLIENT_ID;
		}
		logExitMethod("tokenInfo");
		return tokeninfo.toPrettyString();
	}

	private static String getUserInfo() throws IOException {
		logEnterMethod("userInfo");
		Userinfo userinfo = oAuth2.userinfo().get().execute();
		logExitMethod("userInfo");
		return userinfo.toPrettyString();
	}

	/**
	 * Gets user email from authenticated info
	 * 
	 * @return user email or null if not available
	 * @throws IOException
	 */
	public static String getUserEmail() {
		logEnterMethod("getuserEmail");
		if (oAuth2 == null) {
			return null;
		}
		String userEmail = null;
		try {
			Userinfo userinfo = oAuth2.userinfo().get().execute();
			userEmail = userinfo.get(GOOGLE_USER_INFO_EMAIL).toString();
		} catch (IOException e) {
			return userEmail;
		}
		logExitMethod("getuserEmail");
		return userEmail;
	}

	/**
	 * Gets user name from authenticated info
	 * 
	 * @return user name from google account or null if not available
	 * @throws IOException
	 */
	public static String getUserName() {
		logEnterMethod("getuserName");
		if (oAuth2 == null) {
			return null;
		}
		String userName = null;
		try {
			Userinfo userinfo = oAuth2.userinfo().get().execute();
			userName = userinfo.get(GOOGLE_USER_INFO_GIVEN_NAME).toString();
		} catch (IOException e) {
			return userName;
		}
		logExitMethod("getuserName");
		return userName;
	}

	/**
	 * Checks if user is authenticated with Google Server
	 * 
	 * @return
	 */
	public static boolean isAuthenticated() {
		logEnterMethod("isAuthenticated");
		if (credential != null) {
			logExitMethod("isAuthenticated");
			return true;
		}
		logExitMethod("isAuthenticated");
		return false;
	}

	/**
	 * Logger methods
	 */

	/**
	 * Log trace for method entry
	 * 
	 * @param methodName
	 */
	static void logEnterMethod(String methodName) {
		logger.entering("OAuth2", methodName);
	}

	/**
	 * Log trace for method exit
	 * 
	 * @param methodName
	 */
	static void logExitMethod(String methodName) {
		logger.exiting("OAuth2", methodName);
	}

}
