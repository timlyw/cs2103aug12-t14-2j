package mhs.src;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.DateTime;

public class TaskRecordFile {

	private RandomAccessFile recordFile;
	private Map<String, Integer> recordFieldAttributes; // Field Name, Field
														// Length
	private static long RECORD_ENTRIES_START_POSITION;
	private static int RECORD_TOTAL_NO_ENTRIES;
	private static int RECORD_ENTRY_LENGTH = 0;

	private final static String DEFAULT_TASK_RECORD_FILENAME = "taskRecord.txt";
	private final static String REGEX_DOUBLE_LINE_LENGTH_100 = "(=)\\1*{100}";
	private final static String REGEX_WHITESPACE = "\\s+";

	public TaskRecordFile() throws IOException {
		initalizeRecordFile(DEFAULT_TASK_RECORD_FILENAME);
		initializeRecordParameters();
		countRecords();
	}

	public TaskRecordFile(String taskRecordFileName) throws IOException {
		initalizeRecordFile(taskRecordFileName);
		initializeRecordParameters();
		countRecords();
	}

	private void initalizeRecordFile(String recordFileName) {
		try {
			recordFile = new RandomAccessFile(recordFileName, "rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Read in Record File Parameters from first line
	 * 
	 * @throws IOException
	 */
	private void initializeRecordParameters() throws IOException {
		recordFieldAttributes = new LinkedHashMap<String, Integer>();
		// read in record attributes from record file
		String line;
		while ((line = recordFile.readLine()) != null) {
			if (line.matches(REGEX_DOUBLE_LINE_LENGTH_100)) {
				break;
			}
			String[] recordFieldAttribute = line.split("\\s+");
			int recordFieldLength = Integer.parseInt(recordFieldAttribute[1]);
			recordFieldAttributes.put(recordFieldAttribute[0],
					recordFieldLength);
			RECORD_ENTRY_LENGTH += (recordFieldLength);
		}
		RECORD_ENTRIES_START_POSITION = recordFile.getFilePointer();
	}

	/**
	 * Add new record to file by appending new line
	 * 
	 * @throws IOException
	 */
	public void addRecord(Task task) throws IOException {

		recordFile.seek(recordFile.length());

		// add fields for task
		for (Map.Entry<String, String> entry : task.getTaskProperties()
				.entrySet()) {

			if (entry.getKey() == "taskId") {

				// generate unique taskId
				int newTaskId = getNewTaskId();

				addFieldEntry(entry.getKey(), Integer.toString(newTaskId));
			} else {
				addFieldEntry(entry.getKey(), entry.getValue());
			}

		}
		RECORD_TOTAL_NO_ENTRIES++;
	}

	private int getNewTaskId() throws IOException {
		return countRecords() + 1;
	}

	private void addFieldEntry(String fieldNameToAdd, String fieldValueToAdd)
			throws IOException {

		int fieldLength = recordFieldAttributes.get(fieldNameToAdd);
		if (fieldValueToAdd.length() <= fieldLength) {
			recordFile.writeBytes(fieldValueToAdd);
		} else {
			recordFile.writeBytes(fieldValueToAdd.substring(0, fieldLength));
		}

		// Fill in remaining field length
		for (int i = 0; i < (fieldLength - fieldValueToAdd.length()); i++) {
			recordFile.writeByte('\0');
		}

		// add newline
		recordFile.writeBytes(String.format("%s", System.lineSeparator()));
	}

	/**
	 * Updates record with updated task
	 * 
	 * @throws IOException
	 */
	public void updateRecord(Task updatedTask) throws IOException {
		// seek record
		recordFile.seek(seekRecordPosition(updatedTask.getTaskId()));

		// overwrite record
		for (Map.Entry<String, String> entry : updatedTask.getTaskProperties()
				.entrySet()) {
			addFieldEntry(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Sets record row entry as deleted
	 * 
	 * @return
	 * 
	 * @throws IOException
	 */
	public boolean deleteRecord(int recordIndex) throws IOException {

		if (!recordExists(recordIndex)) {
			return false;
		}

		// seek record
		recordFile.seek(seekRecordPosition(recordIndex));

		// skip to isDeleted field
		for (Entry<String, Integer> it : recordFieldAttributes.entrySet()) {
			if (it.getKey() == "isDeleted") {
				// set delete flag
				addFieldEntry(it.getKey(), "true");
			} else {
				recordFile.readLine();
			}
		}
		return true;
	}

	/**
	 * Delete record from file
	 * 
	 * @throws IOException
	 */
	public void removeRecord(int recordIndex) throws IOException {
		// TODO
	}

	/**
	 * Fetch all records as tasks
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<Task> fetchTasks() throws IOException {
		List<Task> taskList = new LinkedList<Task>();

		for (int i = 0; i < RECORD_TOTAL_NO_ENTRIES; i++) {
			taskList.add(fetchTask(i));
		}
		return taskList;
	}

	/**
	 * Fetch specified records as tasks
	 * 
	 * @param startTaskId
	 * @param endTaskId
	 * @return
	 * @throws IOException
	 */
	public List<Task> fetchTasks(int startTaskId, int endTaskId)
			throws IOException {

		List<Task> taskList = new LinkedList<Task>();

		for (int i = startTaskId; i < endTaskId; i++) {
			taskList.add(fetchTask(i));
		}
		return taskList;
	}

	/**
	 * Fetch specified record as task
	 * 
	 * @return
	 * @throws IOException
	 */
	public Task fetchTask(int taskId) throws IOException {

		Map<String, String> recordRow = fetchRecord(taskId);

		Task task = new Task(Integer.parseInt(recordRow.get("taskId")),
				recordRow.get("taskName"), recordRow.get("taskCategory"),
				new DateTime(recordRow.get("startDateTime")), new DateTime(
						recordRow.get("endDateTime")), new DateTime(
						recordRow.get("taskCreated")), new DateTime(
						recordRow.get("taskUpdated")), new DateTime(
						recordRow.get("taskLastSync")),
				recordRow.get("gCalTaskId"), Boolean.parseBoolean(recordRow
						.get("isDone")), Boolean.parseBoolean(recordRow
						.get("isDeleted")));

		return task;
	}

	/**
	 * Fetches specified records from file
	 * 
	 * @param startIndex
	 * @param endIndex
	 * @return List<Map<String,String>> a list of field name, field value
	 *         key-value pairs
	 * @throws IOException
	 */
	private List<Map<String, String>> fetchRecords(int startIndex, int endIndex)
			throws IOException {
		List<Map<String, String>> recordSet = new LinkedList<Map<String, String>>();

		for (int i = startIndex; i < endIndex; i++) {
			recordSet.add(fetchRecord(i));
		}
		return recordSet;
	}

	/**
	 * Fetches a single record
	 * 
	 * @param recordIndex
	 * @return Map<String,String> field name, field value key-value pair
	 * @throws IOException
	 */
	private Map<String, String> fetchRecord(int recordIndex) throws IOException {

		Map<String, String> recordRow = new LinkedHashMap<String, String>();

		recordFile.seek(seekRecordPosition(recordIndex));

		// read in record entry fields
		for (Entry<String, Integer> recordFieldAttributeEntry : recordFieldAttributes
				.entrySet()) {

			String fieldValue = readRecordField(recordFieldAttributeEntry
					.getValue());
			recordRow.put(recordFieldAttributeEntry.getKey(), fieldValue);

		}

		return recordRow;
	}

	/**
	 * Reads record field
	 * 
	 * @param fieldLength
	 * @return
	 * @throws IOException
	 */
	private String readRecordField(int fieldLength) throws IOException {
		String field = recordFile.readLine().trim();
		// System.out.println(field);
		return field;
	}

	/*
	 * Random Access File Helpers
	 */
	/**
	 * Seek record position in file
	 * 
	 * @param recordIndex
	 * @return
	 * @throws IOException
	 */
	private long seekRecordPosition(int recordIndex) throws IOException {
		// TODO out of bounds
		recordFile.seek(RECORD_ENTRIES_START_POSITION);

		// skip records to go to start of record pos
		for (int i = 0; i < (recordIndex - 1) * recordFieldAttributes.size(); i++) {
			recordFile.readLine();
		}

		return recordFile.getFilePointer();
	}

	/**
	 * Count total number of records (including records with deleted flag)
	 * 
	 * @return
	 * @throws IOException
	 */
	private int countRecords() throws IOException {
		recordFile.seek(RECORD_ENTRIES_START_POSITION);

		RECORD_TOTAL_NO_ENTRIES = 0;

		int i = 0;
		while ((recordFile.readLine()) != null) {
			i++;
			if (i % recordFieldAttributes.size() == 0) {
				RECORD_TOTAL_NO_ENTRIES++;
			}
		}

		return RECORD_TOTAL_NO_ENTRIES;
	}

	/**
	 * Checks if record exists (within record range)
	 * 
	 * @param recordIndex
	 * @return
	 */
	private boolean recordExists(int recordIndex) {
		if (recordIndex < 0 || recordIndex > RECORD_TOTAL_NO_ENTRIES) {
			return false;
		}
		return true;
	}

	/*
	 * Debug methods
	 */
	public void printTaskTotalRecords() {
		System.out.printf("TOTAL RECORDS : %s \n", RECORD_TOTAL_NO_ENTRIES);
	}

	public void printRecords() throws IOException {

		List<Task> taskList = fetchTasks();

		printTaskTotalRecords();

		Iterator<Task> iterator = taskList.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next().getTaskName());
		}
	}

	public void printRecordFieldAttributes() {
		for (Entry<String, Integer> entry : recordFieldAttributes.entrySet()) {
			System.out.printf("%s %s\n", entry.getKey(), entry.getValue());
		}
	}
}