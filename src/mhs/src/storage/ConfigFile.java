/**
 * Configuration File 
 * - Handles File I/O operation for persistent user configuration in json file.
 * - Supports Query/Set configuration parameters
 * 
 * @author timlyw
 */

package mhs.src.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

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
	private File configFile;
	private static final Logger logger = MhsLogger.getLogger();

	private Map<String, String> configParameters;

	private static String CONFIG_FILENAME;
	private static final String CHAR_ENCODING_UTF8 = "UTF-8";
	private final static String DEFAULT_CONFIG_FILENAME = "configFile.json";

	private static final String EXCEPTION_MESSAGE_NULL_PARAMETER = "%1$s cannot be null!";
	private static final String PARAMETER_VALUE = "value";
	private static final String PARAMETER_PARAMETER = "parameter";

	/**
	 * Default Constructor for ConfigFile
	 * 
	 * @throws IOException
	 */
	public ConfigFile() throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());
		CONFIG_FILENAME = DEFAULT_CONFIG_FILENAME;
		configParameters = new HashMap<String, String>();
		initializeGson();
		initializeConfigFile();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	/**
	 * Constructor for ConfigFile
	 * 
	 * @param configFileName
	 * @throws IOException
	 */
	public ConfigFile(String configFileName) throws IOException,
			IllegalArgumentException {
		logger.entering(getClass().getName(), this.getClass().getName());
		if (configFileName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, "taskRecordFileName"));
		}
		CONFIG_FILENAME = configFileName;
		configParameters = new HashMap<String, String>();
		initializeGson();
		initializeConfigFile();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void initializeGson() {
		logger.entering(getClass().getName(), this.getClass().getName());
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(DateTime.class,
				new DateTimeTypeConverter());
		gsonBuilder.registerTypeAdapter(Task.class, new TaskTypeConverter());
		gson = gsonBuilder.serializeNulls().create();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void initializeConfigFile() throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());
		configFile = new File(CONFIG_FILENAME);
		if (!configFile.exists()) {
			createNewJsonFile();
		}
		loadConfigFile();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void createNewJsonFile() throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());
		assert (configFile != null && configParameters != null);
		configFile.createNewFile();
		FileWriter fileWriter = new FileWriter(configFile);
		fileWriter.write(gson.toJson(configParameters));
		fileWriter.close();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	private void loadConfigFile() throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());
		assert (configFile != null);
		inputStream = new FileInputStream(configFile);
		jsonReader = new JsonReader(new InputStreamReader(inputStream,
				CHAR_ENCODING_UTF8));

		JsonParser parser = new JsonParser();
		JsonObject configJObject = parser.parse(jsonReader).getAsJsonObject();

		configParameters = gson.fromJson(configJObject,
				configParameters.getClass());

		jsonReader.close();
		inputStream.close();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	/**
	 * Saves configuration to json file
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());
		FileWriter fileWriter = new FileWriter(configFile);
		fileWriter.write(gson.toJson(configParameters));
		fileWriter.close();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	/**
	 * Checks whether configuration parameter exists
	 * 
	 * @param parameter
	 * @return boolean
	 */
	public boolean hasConfigParameter(String parameter) {
		logger.entering(getClass().getName(), this.getClass().getName());
		if (parameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_PARAMETER));
		}
		if (configParameters.get(parameter) == null) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return false;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return true;
	}

	/**
	 * Get Config Parameter
	 * 
	 * @param parameter
	 * @return parameter value
	 */
	public String getConfigParameter(String parameter) {
		logger.entering(getClass().getName(), this.getClass().getName());
		if (parameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_PARAMETER));
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return configParameters.get(parameter);
	}

	/**
	 * Set config parameter
	 * 
	 * @param parameter
	 * @param value
	 * @throws IOException
	 */
	public void setConfigParameter(String parameter, String value)
			throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());
		if (parameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_PARAMETER));
		}
		if (value == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_VALUE));
		}
		configParameters.put(parameter, value);
		save();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}
}