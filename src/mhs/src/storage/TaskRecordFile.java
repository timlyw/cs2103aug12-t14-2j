/**
 * Task Record File 
 * 
 * - Handles File I/O operations for tasks in json file
 *  
 * @author timlyw
 */

package mhs.src.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TaskRecordFile {

	private static final String JSON_INDENT = "  ";
	private JsonWriter jsonWriter;
	private JsonReader jsonReader;
	private InputStream inputStream;
	private OutputStream outputStream;
	private File taskRecordFile;

	private static Gson gson = MhsGson.getInstance();
	private static final Logger logger = MhsLogger.getLogger();

	private static String RECORD_FILE_NAME;

	private static final String CHAR_ENCODING_UTF8 = "UTF-8";
	private static final String DEFAULT_TASK_RECORD_FILENAME = "taskRecordFile.json";
	private static final String EXCEPTION_MESSAGE_NULL_PARAMETER = "%1$s cannot be null!";

	private Map<Integer, Task> taskList;

	/**
	 * TaskRecordFile Default Constructor
	 * 
	 * @throws IOException
	 */
	public TaskRecordFile() throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());

		RECORD_FILE_NAME = DEFAULT_TASK_RECORD_FILENAME;
		initializeTaskList();
		initalizeRecordFile();

		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	/**
	 * TaskRecordFile Constructor
	 * 
	 * @param taskRecordFileName
	 * @throws IOException
	 */
	public TaskRecordFile(String taskRecordFileName) throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());

		if (taskRecordFileName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, "taskRecordFileName"));
		}

		RECORD_FILE_NAME = taskRecordFileName;
		initializeTaskList();
		initalizeRecordFile();

		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	/**
	 * Initialize task list
	 */
	private void initializeTaskList() {
		taskList = new LinkedHashMap<Integer, Task>();
	}

	/**
	 * Initialize record file
	 * 
	 * @throws IOException
	 */
	private void initalizeRecordFile() throws IOException {
		logger.exiting(getClass().getName(), this.getClass().getName());

		taskRecordFile = new File(RECORD_FILE_NAME);

		if (!taskRecordFile.exists()) {
			createNewJsonFile();
			loadTaskListFromFile();
		} else {
			loadTaskListFromFile();
		}

	}

	/**
	 * Creates new Json file
	 * 
	 * @throws IOException
	 */
	private void createNewJsonFile() throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());

		assert (taskRecordFile != null);

		taskRecordFile.createNewFile();
		openJsonOutputStream();
		writeEmptyJsonArray();
		closeJsonOutputStream();

		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	/**
	 * Opens json output stream for writing
	 * 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private void openJsonOutputStream() throws FileNotFoundException,
			UnsupportedEncodingException {
		outputStream = new FileOutputStream(RECORD_FILE_NAME);
		jsonWriter = new JsonWriter(new OutputStreamWriter(outputStream,
				CHAR_ENCODING_UTF8));
	}

	/**
	 * Closes json output stream
	 * 
	 * @throws IOException
	 */
	private void closeJsonOutputStream() throws IOException {
		jsonWriter.close();
		outputStream.close();
	}

	/**
	 * Writes empty json array
	 * 
	 * @throws IOException
	 */
	private void writeEmptyJsonArray() throws IOException {
		jsonWriter.setIndent(JSON_INDENT);
		jsonWriter.beginArray();
		jsonWriter.endArray();
	}

	/**
	 * Load task list with tasks from file
	 * 
	 * @throws IOException
	 */
	public void loadTaskListFromFile() throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());

		openJsonInputStream();
		loadTaskListFromJarray();
		closeJsonInputStream();

		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	/**
	 * Loads Jarray from json reader
	 */
	private void loadTaskListFromJarray() {
		JsonParser parser = new JsonParser();
		JsonArray Jarray = parser.parse(jsonReader).getAsJsonArray();

		for (JsonElement obj : Jarray) {
			Task newTask = gson.fromJson(obj, Task.class);
			taskList.put(newTask.getTaskId(), newTask);
		}
	}

	/**
	 * Open json input stream
	 * 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private void openJsonInputStream() throws FileNotFoundException,
			UnsupportedEncodingException {
		inputStream = new FileInputStream(RECORD_FILE_NAME);
		jsonReader = new JsonReader(new InputStreamReader(inputStream,
				CHAR_ENCODING_UTF8));
	}

	/**
	 * Close json input stream
	 * 
	 * @throws IOException
	 */
	private void closeJsonInputStream() throws IOException {
		jsonReader.close();
		inputStream.close();
	}

	/**
	 * Saves Task List to file
	 * 
	 * @param taskList
	 * @throws IOException
	 */
	public void saveTaskList(Map<Integer, Task> taskList) throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());
		openJsonOutputStream();

		jsonWriter.setIndent(JSON_INDENT);
		jsonWriter.beginArray();

		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			gson.toJson(entry.getValue(), entry.getValue().getClass(),
					jsonWriter);
		}

		jsonWriter.endArray();
		closeJsonOutputStream();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	/**
	 * Getter for Task List
	 * 
	 * @return Task List with taskId as key
	 */
	public Map<Integer, Task> getTaskList() {
		logger.entering(getClass().getName(), this.getClass().getName());
		logger.exiting(getClass().getName(), this.getClass().getName());
		return taskList;
	}
}