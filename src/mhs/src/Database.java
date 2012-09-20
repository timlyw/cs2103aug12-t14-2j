package mhs.src;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
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
	}

	private void createSortedTaskList() {
		Comparator<DateTime> TaskLatestCmp = new CompareTaskLatest();
		sortedTasks = new TreeMap<DateTime, Task>(TaskLatestCmp);

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

	public Task query(int taskId) throws IOException {
		// fetch record
		return taskRecordFile.fetchTask(taskId);
	}

	public List<Task> query(String taskName) {
		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		Iterator<Task> iterator = taskList.iterator();
		while (iterator.hasNext()) {
			if(iterator.next().getTaskName().contains(taskName)){
				queriedTaskRecordset.add(iterator.next());
			}		
		}		
		return queriedTaskRecordset;
	}

	public List<Task> query(TaskCategory taskCategory) {
		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		Iterator<Task> iterator = taskList.iterator();
		while (iterator.hasNext()) {
			if(iterator.next().getTaskCategory().equals(taskCategory)){
				queriedTaskRecordset.add(iterator.next());
			}		
		}		
		return queriedTaskRecordset;
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
			Task task = iterator.next();
			System.out.println(task.getTaskName());
			System.out.println(task.getStartDateTime().toString());
		}
	}

	public void printSortedTasks() {
		
		createSortedTaskList();
		
		for (Entry<DateTime, Task> entry : sortedTasks.entrySet()) {
			DateTime key = entry.getKey();
			Task value = entry.getValue();
			System.out.println(key + " => " + value.getTaskName());
		}

	}
}