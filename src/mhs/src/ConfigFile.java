package mhs.src;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ConfigFile {

	private Gson gson;
	private JsonWriter jsonWriter;
	private JsonReader jsonReader;
	private InputStream inputStream;
	private OutputStream outputStream;

	private static String CONFIG_FILENAME;
	private final static String DEFAULT_CONFIG_FILENAME = "configFile.json";

	private static String googleAuthToken;
	private static String userGoogleUserEmail;

	public ConfigFile(String configFileName) throws IOException {
		CONFIG_FILENAME = configFileName;
		initializeConfigFile();
	}

	public ConfigFile() throws IOException {
		CONFIG_FILENAME = DEFAULT_CONFIG_FILENAME;
		initializeConfigFile();
	}

	private void initializeConfigFile() throws IOException {
		initializeGson();
		loadConfigFile();
	}

	private void initializeGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(DateTime.class,
				new DateTimeTypeConverter());
		gsonBuilder.registerTypeAdapter(Task.class, new TaskTypeConverter());
		gson = gsonBuilder.create();
	}

	private void loadConfigFile() throws IOException {
		inputStream = new FileInputStream(CONFIG_FILENAME);
		jsonReader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));

		JsonParser parser = new JsonParser();
		JsonObject configJObject = parser.parse(jsonReader).getAsJsonObject();

		// TODO load config file params

		jsonReader.close();
		inputStream.close();
	}

	public void save() throws IOException {
		outputStream = new FileOutputStream(CONFIG_FILENAME);
		jsonWriter = new JsonWriter(new OutputStreamWriter(outputStream,
				"UTF-8"));

		jsonWriter.beginObject();

		// TODO save config file params

		jsonWriter.endObject();
		jsonWriter.close();
		outputStream.close();
	}

	public static String getGoogleAuthToken() {
		return googleAuthToken;
	}

	public static void setGoogleAuthToken(String googleAuthToken) {
		ConfigFile.googleAuthToken = googleAuthToken;
	}

	public static String getUserGoogleUserEmail() {
		return userGoogleUserEmail;
	}

	public static void setUserGoogleUserEmail(String userGoogleUserEmail) {
		ConfigFile.userGoogleUserEmail = userGoogleUserEmail;
	}
}