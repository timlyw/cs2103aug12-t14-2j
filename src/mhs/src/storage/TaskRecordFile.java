/**
 * Task Record File 
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

import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TaskRecordFile {

	private static final String CHAR_ENCODING_UTF8 = "UTF-8";
	private Gson gson;
	private JsonWriter jsonWriter;
	private JsonReader jsonReader;
	private InputStream inputStream;
	private OutputStream outputStream;
	private File taskRecordFile;

	private static String RECORD_FILE_NAME;
	private final static String DEFAULT_TASK_RECORD_FILENAME = "taskRecordFile.json";

	private Map<Integer, Task> taskList;

	public TaskRecordFile() throws IOException {
		RECORD_FILE_NAME = DEFAULT_TASK_RECORD_FILENAME;
		initializeGson();
		initalizeRecordFile();
	}

	public TaskRecordFile(String taskRecordFileName) throws IOException {
		RECORD_FILE_NAME = taskRecordFileName;
		initializeGson();
		initalizeRecordFile();
	}

	private void initalizeRecordFile() throws IOException {
		taskRecordFile = new File(RECORD_FILE_NAME);
		if (!taskRecordFile.exists()) {
			createNewJsonFile();
			loadAllTaskLists();
		} else {
			loadAllTaskLists();
		}
	}

	private void createNewJsonFile() throws IOException {
		taskRecordFile.createNewFile();
		outputStream = new FileOutputStream(RECORD_FILE_NAME);
		jsonWriter = new JsonWriter(new OutputStreamWriter(outputStream,
				CHAR_ENCODING_UTF8));
		jsonWriter.setIndent("  ");
		jsonWriter.beginArray();
		jsonWriter.endArray();
		jsonWriter.close();
		outputStream.close();
	}

	private void initializeGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(DateTime.class,
				new DateTimeTypeConverter());
		gsonBuilder.registerTypeAdapter(Task.class, new TaskTypeConverter());
		gson = gsonBuilder.serializeNulls().create();

	}

	/**
	 * Loads tasks into taskList and gCalTaskList
	 * 
	 * @throws IOException
	 */
	public void loadAllTaskLists() throws IOException {

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
	}

	public void loadTaskList() throws IOException {

		inputStream = new FileInputStream(RECORD_FILE_NAME);
		jsonReader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
		taskList = new LinkedHashMap<Integer, Task>();

		JsonParser parser = new JsonParser();
		JsonArray Jarray = parser.parse(jsonReader).getAsJsonArray();

		for (JsonElement obj : Jarray) {
			Task newTask = gson.fromJson(obj, Task.class);
			taskList.put(newTask.getTaskId(), newTask);
		}

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

	}

	/**
	 * Getter for Task List
	 * 
	 * @return Task List with taskId as key
	 */
	public Map<Integer, Task> getTaskList() {
		return taskList;
	}
}