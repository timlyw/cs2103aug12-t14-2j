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

import mhs.src.common.MhsLogger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		CONFIG_FILENAME = DEFAULT_CONFIG_FILENAME;
		initializeConfigParameters();
		initializeConfigFile();

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Constructor for ConfigFile
	 * 
	 * @param configFileName
	 * @throws IOException
	 */
	public ConfigFile(String configFileName) throws IOException,
			IllegalArgumentException {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		if (configFileName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, "taskRecordFileName"));
		}
		CONFIG_FILENAME = configFileName;
		initializeConfigParameters();
		initializeConfigFile();
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Initialize config file
	 * 
	 * @throws IOException
	 */
	private void initializeConfigFile() throws IOException {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		configFile = new File(CONFIG_FILENAME);
		if (!configFile.exists()) {
			createNewJsonFile();
		}
		loadConfigFile();
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Initialize config parameters
	 */
	private void initializeConfigParameters() {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		configParameters = new HashMap<String, String>();
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Create new json file
	 * 
	 * @throws IOException
	 */
	private void createNewJsonFile() throws IOException {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		assert (configFile != null && configParameters != null);

		configFile.createNewFile();
		FileWriter fileWriter = new FileWriter(configFile);
		fileWriter.write(gson.toJson(configParameters));
		fileWriter.close();

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Load Config File
	 * 
	 * @throws IOException
	 */
	private void loadConfigFile() throws IOException {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		assert (configFile != null);

		openJsonInputStream();
		loadConfigParametersFromJobject();
		closeJsonInputStream();

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Load config parameters from jobject
	 */
	@SuppressWarnings("unchecked")
	private void loadConfigParametersFromJobject() {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		assert (gson != null);

		JsonParser parser = new JsonParser();
		JsonObject configJObject = null;
		
		try {
			configJObject = parser.parse(jsonReader)
					.getAsJsonObject();
		} catch (JsonSyntaxException | java.lang.IllegalStateException e) {
			// File corrupted
			logger.log(Level.INFO, "Json file corrupted.");
		} finally{
			if(configJObject != null){
				configParameters = gson.fromJson(configJObject,
						configParameters.getClass());
			}
		}

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Open json input stream for reading
	 * 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private void openJsonInputStream() throws FileNotFoundException,
			UnsupportedEncodingException {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		assert (configFile != null);

		inputStream = new FileInputStream(configFile);
		jsonReader = new JsonReader(new InputStreamReader(inputStream,
				CHAR_ENCODING_UTF8));

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Close json input stream
	 * 
	 * @throws IOException
	 */
	private void closeJsonInputStream() throws IOException {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		assert (jsonReader != null && inputStream != null);

		jsonReader.close();
		inputStream.close();
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Saves configuration to json file
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		FileWriter fileWriter = new FileWriter(configFile);
		fileWriter.write(gson.toJson(configParameters));
		fileWriter.close();

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Checks whether configuration parameter exists
	 * 
	 * @param parameter
	 * @return boolean
	 */
	public boolean hasConfigParameter(String parameter) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		if (parameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_PARAMETER));
		}
		if (configParameters.get(parameter) == null) {
			logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
			return false;
		}

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return true;
	}

	/**
	 * Checks whether configuration parameter exists and is not empty
	 * 
	 * @param parameter
	 * @return boolean
	 */
	public boolean hasNonEmptyConfigParameter(String parameter) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		if (parameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_PARAMETER));
		}

		boolean hasNonEmptyConfigParameter = false;

		if (configParameters.get(parameter) == null) {
			hasNonEmptyConfigParameter = false;
		} else {
			if (!configParameters.get(parameter).isEmpty()) {
				hasNonEmptyConfigParameter = true;
			}
		}

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return hasNonEmptyConfigParameter;
	}

	/**
	 * Get Config Parameter
	 * 
	 * @param parameter
	 * @return parameter value
	 */
	public String getConfigParameter(String parameter) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		if (parameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_PARAMETER));
		}

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

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
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Remove config parameter
	 * 
	 * @param parameter
	 * @param value
	 * @throws IOException
	 */
	public void removeConfigParameter(String parameter) throws IOException {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		if (parameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_PARAMETER));
		}

		configParameters.remove(parameter);
		save();
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}
}