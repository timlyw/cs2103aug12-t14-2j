//@author A0087048X

package mhs.src.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

/**
 * ConfigFile
 * 
 * Handles File I/O operations for persistent user configuration in json file.<br>
 * - Supports Query, Set configuration parameters<br>
 * 
 * @author Timothy Lim Yi Wen A0087048X
 * 
 */

public class ConfigFile {

	private JsonReader jsonReader;
	private InputStream inputStream;
	private File configFile;
	private static final Logger logger = MhsLogger.getLogger();
	private static Gson gson = MhsGson.getInstance();

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
		logEnterMethod("ConfigFile");
		CONFIG_FILENAME = DEFAULT_CONFIG_FILENAME;
		initializeConfigParameters();
		initializeConfigFile();
		logExitMethod("ConfigFile");
	}

	/**
	 * Constructor for ConfigFile
	 * 
	 * @param configFileName
	 * @throws IOException
	 */
	public ConfigFile(String configFileName) throws IOException,
			IllegalArgumentException {
		logEnterMethod("ConfigFile");
		if (configFileName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, "taskRecordFileName"));
		}
		CONFIG_FILENAME = configFileName;
		initializeConfigParameters();
		initializeConfigFile();
		logExitMethod("ConfigFile");
	}

	/**
	 * Initialize config file
	 * 
	 * @throws IOException
	 */
	private void initializeConfigFile() throws IOException {
		logEnterMethod("initializeConfigFile");
		configFile = new File(CONFIG_FILENAME);
		if (!configFile.exists()) {
			createNewJsonFile();
		}
		loadConfigFile();
		logExitMethod("initializeConfigFile");
	}

	/**
	 * Initialize config parameters
	 */
	private void initializeConfigParameters() {
		logEnterMethod("initializeConfigParameters");
		configParameters = new HashMap<String, String>();
		logExitMethod("initializeConfigParameters");
	}

	/**
	 * Create new json file
	 * 
	 * @throws IOException
	 */
	private void createNewJsonFile() throws IOException {
		logEnterMethod("createNewJsonFile");
		assert (configFile != null && configParameters != null);

		configFile.createNewFile();
		FileWriter fileWriter = new FileWriter(configFile);
		fileWriter.write(gson.toJson(configParameters));
		fileWriter.close();

		logExitMethod("createNewJsonFile");
	}

	/**
	 * Load Config File
	 * 
	 * @throws IOException
	 */
	private void loadConfigFile() throws IOException {
		logEnterMethod("loadConfigFile");
		assert (configFile != null);

		openJsonInputStream();
		loadConfigParametersFromJobject();
		closeJsonInputStream();

		logExitMethod("loadConfigFile");
	}

	/**
	 * Load config parameters from jobject
	 */
	@SuppressWarnings("unchecked")
	private void loadConfigParametersFromJobject() {
		logEnterMethod("loadConfigParametersFromJobject");
		assert (gson != null);

		JsonParser parser = new JsonParser();
		JsonObject configJObject = null;

		try {
			configJObject = parser.parse(jsonReader).getAsJsonObject();
		} catch (JsonSyntaxException | java.lang.IllegalStateException e) {
			// File corrupted
			logger.log(Level.INFO, "Json file corrupted.");
		} finally {
			if (configJObject != null) {
				configParameters = gson.fromJson(configJObject,
						configParameters.getClass());
			}
		}
		logExitMethod("loadConfigParametersFromJobject");
	}

	/**
	 * Open json input stream for reading
	 * 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private void openJsonInputStream() throws FileNotFoundException,
			UnsupportedEncodingException {
		logEnterMethod("openJsonInputStream");
		assert (configFile != null);
		inputStream = new FileInputStream(configFile);
		jsonReader = new JsonReader(new InputStreamReader(inputStream,
				CHAR_ENCODING_UTF8));
		logExitMethod("openJsonInputStream");
	}

	/**
	 * Close json input stream
	 * 
	 * @throws IOException
	 */
	private void closeJsonInputStream() throws IOException {
		logEnterMethod("closeJsonInputStream");
		assert (jsonReader != null && inputStream != null);
		jsonReader.close();
		inputStream.close();
		logExitMethod("closeJsonInputStream");
	}

	/**
	 * Saves configuration to json file
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
		logEnterMethod("save");

		FileWriter fileWriter = new FileWriter(configFile);
		fileWriter.write(gson.toJson(configParameters));
		fileWriter.close();

		logExitMethod("save");
	}

	/**
	 * Checks whether configuration parameter exists
	 * 
	 * @param parameter
	 * @return boolean
	 */
	public boolean hasConfigParameter(String parameter) {
		logEnterMethod("hasConfigParameter");

		if (parameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_PARAMETER));
		}
		if (configParameters.get(parameter) == null) {
			logger.exiting(getClass().getName(),
					new Exception().getStackTrace()[0].getMethodName());
			return false;
		}

		logExitMethod("hasConfigParameter");
		return true;
	}

	/**
	 * Checks whether configuration parameter exists and is not empty
	 * 
	 * @param parameter
	 * @return boolean
	 */
	public boolean hasNonEmptyConfigParameter(String parameter) {
		logEnterMethod("hasNonEmptyConfigParameter");

		if (parameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_PARAMETER));
		}

		boolean hasNonEmptyConfigParameter = false;

		if (configParameters.get(parameter) == null) {
			hasNonEmptyConfigParameter = false;
		} else if (!configParameters.get(parameter).isEmpty()) {
			hasNonEmptyConfigParameter = true;
		}

		logExitMethod("hasNonEmptyConfigParameter");
		return hasNonEmptyConfigParameter;
	}

	/**
	 * Get Config Parameter
	 * 
	 * @param parameter
	 * @return parameter value
	 */
	public String getConfigParameter(String parameter) {
		logEnterMethod("getConfigParameter");
		if (parameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_PARAMETER));
		}
		logExitMethod("getConfigParameter");
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
		logEnterMethod("setConfigParameter");
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
		logExitMethod("setConfigParameter");
	}

	/**
	 * Remove config parameter
	 * 
	 * @param parameter
	 * @param value
	 * @throws IOException
	 */
	public void removeConfigParameter(String parameter) throws IOException {
		logEnterMethod("removeConfigParameter");
		if (parameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_PARAMETER));
		}
		configParameters.remove(parameter);
		save();
		logExitMethod("removeConfigParameter");
	}

	/**
	 * Log Trace Entry Method
	 * 
	 * @param methodName
	 */
	private void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}

	/**
	 * Log Trace Exit Method
	 * 
	 * @param methodName
	 */

	private void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}
}