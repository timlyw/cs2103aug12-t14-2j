package mhs.src;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.joda.time.DateTime;

public class Database {

	TaskRecordFile taskRecordFile;
	GoogleCalendar googleCalendar;

	List<Task> taskList;
	TreeMap<DateTime, Task> sortedTasks;

	public Database() throws IOException {
		initalizeDatabase();
		// syncronize local and web databases
		syncronizeDatabases();
	}

	private void initalizeDatabase() throws IOException {
		taskRecordFile = new TaskRecordFile();
		googleCalendar = new GoogleCalendar();

		taskList = taskRecordFile.fetchTasks();
		sortedTasks = new TreeMap<DateTime, Task>();

		createSortedTaskList();
		
	}

	private void createSortedTaskList() {
		Iterator<Task> iterator = taskList.iterator();
		while (iterator.hasNext()) {
			Task task = iterator.next();
			sortedTasks
			.put(task.getStartDateTime(), task);
		}
	}

	private void syncronizeDatabases() {
		// TODO
	}

	public List<Task> query(int taskId) {
		// TODO
		// fetch records
		// perform query operations on recordset
		// return list of tasks
		return null;
	}

	public List<Task> query(String taskName) {
		// TODO
		// fetch records
		// perform query operations on recordset
		// return list of tasks
		return null;
	}

	public List<Task> query(TaskCategory taskCategory) {
		// TODO
		// fetch records
		// perform query operations on recordset
		// return list of tasks
		return null;
	}

	public List<Task> query(String taskName, TaskCategory taskCategory,
			DateTime startTime, DateTime endTime) {
		// TODO
		// fetch records
		// perform query operations on recordset
		// return list of tasks
		return null;
	}

	public void add(Task task) throws IOException {
		taskRecordFile.addRecord(task);
	}

	public void delete(int taskId) throws IOException {
		// check if task exists
		taskRecordFile.deleteRecord(taskId);
	}

	public void update(Task task) throws IOException {
		taskRecordFile.updateRecord(task);
	}

	/*
	 * Debug Helpers
	 */
	public void printTasks() {
		Iterator<Task> iterator = taskList.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next().getTaskName());
		}
	}

	public void printSortedTasks() {

		for (Entry<DateTime, Task> entry : sortedTasks.entrySet()) {
			DateTime key = entry.getKey();
			Task value = entry.getValue();
			System.out.println(key + " => " + value.getTaskName());
		}

	}
}