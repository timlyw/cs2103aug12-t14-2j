//@author A0088015H

package mhs.src;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import mhs.src.userinterface.UiController;

/**
 * This class contains the main method for MHS It starts up the UiController
 * which displays the MHS interface to the user
 * 
 * @author John Wong
 * 
 */

public class Mhs {
	public static void main(String[] args) {

		URL remoteStorageServiceUrl;
		try {
			remoteStorageServiceUrl = new URL(
					"https://accounts.google.com/o/oauth2/auth?scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile&redirect_uri=urn:ietf:wg:oauth:2.0:oob&response_type=code&client_id=812741506391-h38jh0j4fv0ce1krdkiq0hfvt6n5amrf.apps.googleusercontent.com");
			URLConnection googleAuthConnection = remoteStorageServiceUrl
					.openConnection();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		UiController uiController = new UiController();
		uiController.showUserInterface();
	}
}
