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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

	private Map<Integer, Task> taskList;

	public TaskRecordFile() throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());

		RECORD_FILE_NAME = DEFAULT_TASK_RECORD_FILENAME;
		initalizeRecordFile();

		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	public TaskRecordFile(String taskRecordFileName) throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());

		RECORD_FILE_NAME = taskRecordFileName;
		initalizeRecordFile();

		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void initalizeRecordFile() throws IOException {
		logger.exiting(getClass().getName(), this.getClass().getName());

		taskRecordFile = new File(RECORD_FILE_NAME);
		if (!taskRecordFile.exists()) {
			createNewJsonFile();
			loadTaskListFromFile();
		} else {
			loadTaskListFromFile();
		}

		taskRecordFile = new File(RECORD_FILE_NAME);
	}

	private void createNewJsonFile() throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());
		taskRecordFile.createNewFile();
		outputStream = new FileOutputStream(RECORD_FILE_NAME);
		jsonWriter = new JsonWriter(new OutputStreamWriter(outputStream,
				CHAR_ENCODING_UTF8));
		writeEmptyJsonArray();
		jsonWriter.close();
		outputStream.close();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void writeEmptyJsonArray() throws IOException {
		jsonWriter.setIndent("  ");
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

		inputStream = new FileInputStream(RECORD_FILE_NAME);
		jsonReader = new JsonReader(new InputStreamReader(inputStream,
				CHAR_ENCODING_UTF8));
		taskList = new LinkedHashMap<Integer, Task>();

		JsonParser parser = new JsonParser();
		JsonArray Jarray = parser.parse(jsonReader).getAsJsonArray();

		for (JsonElement obj : Jarray) {
			Task newTask = gson.fromJson(obj, Task.class);
			taskList.put(newTask.getTaskId(), newTask);
		}

		jsonReader.close();
		inputStream.close();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	/**
	 * Saves Task List to file
	 * 
	 * @param taskList
	 * @throws IOException
	 */
	public void saveTaskList(Map<Integer, Task> taskList) throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());
		outputStream = new FileOutputStream(RECORD_FILE_NAME);
		jsonWriter = new JsonWriter(new OutputStreamWriter(outputStream,
				CHAR_ENCODING_UTF8));

		jsonWriter.setIndent("  ");
		jsonWriter.beginArray();

		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			gson.toJson(entry.getValue(), entry.getValue().getClass(),
					jsonWriter);
		}

		jsonWriter.endArray();
		jsonWriter.close();
		outputStream.close();
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