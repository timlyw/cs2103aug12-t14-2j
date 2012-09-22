package mhs.src;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;

public class Database {

	private TaskRecordFile taskRecordFile;
	private GoogleCalendar googleCalendar;

	private Map<Integer, Task> taskList;

	public Database(String taskRecordFileName) throws IOException {
		initalizeDatabase(taskRecordFileName);
		// syncronize local and web databases
		syncronizeDatabases();
	}

	public Database() throws IOException {
		initalizeDatabase();
		// syncronize local and web databases
		syncronizeDatabases();
	}

	private void initalizeDatabase(String taskRecordFileName)
			throws IOException {
		taskRecordFile = new TaskRecordFile(taskRecordFileName);
		googleCalendar = new GoogleCalendar();

		taskList = taskRecordFile.loadTaskList();
	}

	private void initalizeDatabase() throws IOException {
		taskRecordFile = new TaskRecordFile();
		googleCalendar = new GoogleCalendar();

		taskList = taskRecordFile.loadTaskList();
	}

	private void syncronizeDatabases() {
		// TODO
	}

	/**
	 * Returns List of all tasks
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<Task> query() throws IOException {
		List<Task> queryTaskList = new LinkedList<Task>(taskList.values());
		return queryTaskList;
	}

	public Task query(int taskId) throws IOException {
		return taskList.get(taskId);
	}

	public List<Task> query(String taskName) {
		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			if (entry.getValue().getTaskName().toLowerCase()
					.contains(taskName.toLowerCase())) {
				queriedTaskRecordset.add(entry.getValue());
			}
		}
		return queriedTaskRecordset;
	}

	public List<Task> query(TaskCategory queryTaskCategory) {
		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			TaskCategory taskCategory = entry.getValue().getTaskCategory();
			if (taskCategory.compareTo(queryTaskCategory) == 0) {
				queriedTaskRecordset.add(entry.getValue());
			}
		}
		return queriedTaskRecordset;
	}

	/**
	 * Returns tasks that is within startTime or endTime inclusive
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public List<Task> query(DateTime startTime, DateTime endTime) {
		List<Task> queriedTaskRecordset = new LinkedList<Task>();

		Interval dateTimeInterval = new Interval(startTime,
				endTime.plusMillis(1));

		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			switch (entry.getValue().getTaskCategory()) {
			case TIMED:
				if (dateTimeInterval.contains(entry.getValue()
						.getStartDateTime())
						|| dateTimeInterval.contains(entry.getValue()
								.getEndDateTime())) {
					queriedTaskRecordset.add(entry.getValue());
				}
				break;
			case DEADLINE:
				if (dateTimeInterval
						.contains(entry.getValue().getEndDateTime())) {
					queriedTaskRecordset.add(entry.getValue());
				}
				break;
			case FLOATING:
			default:
				break;
			}
		}

		return queriedTaskRecordset;
	}

	public List<Task> query(String taskName, TaskCategory taskCategory,
			DateTime startTime, DateTime endTime) {
		List<Task> queriedTaskRecordset = new LinkedList<Task>();

		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			if (entry.getValue().getTaskName().toLowerCase()
					.contains(taskName.toLowerCase())) {
				queriedTaskRecordset.add(entry.getValue());
			}
		}
		return null;
	}

	public void add(Task task) throws IOException {
		int newTaskId = getNewTaskId();
		task.setTaskId(newTaskId);
		taskList.put(newTaskId, task);
		saveTaskRecordFile();
	}

	private void saveTaskRecordFile() throws IOException {
		taskRecordFile.saveTaskList(taskList);
	}

	public void delete(int taskId) throws IOException {
		// check if task exists
		if (taskList.containsKey(taskId)) {
			taskList.remove(taskId);
			saveTaskRecordFile();
		} else {
			throw new Error("Invalid Task");
		}
	}

	public void update(Task task) throws IOException {
		// check if task exists
		if (taskList.containsKey(task.getTaskId())) {
			taskList.put(task.getTaskId(), task);
			saveTaskRecordFile();
		} else {
			throw new Error("Invalid Task");
		}
	}

	public void clearDatabase() throws IOException {
		taskList.clear();
		saveTaskRecordFile();
	}

	private int getNewTaskId() {
		Set<Integer> taskKeySet = taskList.keySet();

		int getNewTaskId = 0;
		Iterator<Integer> iterator = taskKeySet.iterator();
		while (iterator.hasNext()) {
			getNewTaskId = iterator.next();
		}
		getNewTaskId++;
		return getNewTaskId;
	}

}