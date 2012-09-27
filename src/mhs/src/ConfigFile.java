package mhs.src;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class ConfigFile {

	private Gson gson;
	private JsonReader jsonReader;
	private InputStream inputStream;

	private static String CONFIG_FILENAME;
	private final static String DEFAULT_CONFIG_FILENAME = "configFile.json";

	Map<String, String> configParameters;

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
		configParameters = new HashMap<String, String>();
		loadConfigFile();
	}

	private void initializeGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(DateTime.class,
				new DateTimeTypeConverter());
		gsonBuilder.registerTypeAdapter(Task.class, new TaskTypeConverter());
		gson = gsonBuilder.serializeNulls().create();
	}

	@SuppressWarnings("unchecked")
	private void loadConfigFile() throws IOException {
		inputStream = new FileInputStream(CONFIG_FILENAME);
		jsonReader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));

		JsonParser parser = new JsonParser();
		JsonObject configJObject = parser.parse(jsonReader).getAsJsonObject();

		configParameters = gson.fromJson(configJObject,
				configParameters.getClass());

		jsonReader.close();
		inputStream.close();
	}

	public void save() throws IOException {
		FileWriter fileWriter = new FileWriter(CONFIG_FILENAME);
		fileWriter.write(gson.toJson(configParameters));
		fileWriter.close();
	}

	public boolean hasConfigParameter(String parameter) {
		if (configParameters.get(parameter) == null) {
			return false;
		}
		return true;
	}

	/**
	 * Get Config Parameter - userGoogleUserEmail - googleAuthToken
	 * 
	 * @param parameter
	 * @return
	 */
	public String getConfigParameter(String parameter) {
		return configParameters.get(parameter);
	}

	/**
	 * Set config parameter
	 * 
	 * @param parameter
	 * @param value
	 * @return
	 * @throws IOException
	 */
	public void setConfigParameter(String parameter, String value)
			throws IOException {
		configParameters.put(parameter, value);
		save();
	}
}