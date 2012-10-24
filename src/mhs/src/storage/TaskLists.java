/**
 * TaskLists 
 * 
 * Abstracts operations on the taskLists containing different views of tasks:
 * - taskList - by taskId
 * - gCalTaskList - by gCalTaskId
 * 
 * Functionality
 * - logic for CRUD on tasks in all task lists
 * - getters for taskLists
 */
package mhs.src.storage;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Interval;

public class TaskLists {

	private Map<Integer, Task> taskList; // primary task list with index as key
	private Map<String, Task> gCalTaskList; // task list with gCalId as key

	public TaskLists(Map<Integer, Task> taskListToInitialize) {
		initializeTaskLists(taskListToInitialize);
	}

	private void initializeTaskLists(Map<Integer, Task> taskListToInitialize) {

		taskList = new LinkedHashMap<Integer, Task>();
		gCalTaskList = new LinkedHashMap<String, Task>();

		if (taskListToInitialize == null) {
			return;
		}
		for (Map.Entry<Integer, Task> entry : taskListToInitialize.entrySet()) {
			Task taskToAdd = entry.getValue();
			taskList.put(taskToAdd.getTaskId(), taskToAdd);
			if (taskToAdd.getgCalTaskId() != null) {
				gCalTaskList.put(taskToAdd.getgCalTaskId(), taskToAdd);
			}
		}
	}

	// Task List task CRUD Methods
	/**
	 * Adds or updates task to task lists
	 * 
	 * @param taskToUpdateInTaskLists
	 */
	public void updateTaskInTaskLists(Task taskToUpdateInTaskLists) {

		if (taskToUpdateInTaskLists == null) {
			return;
		}

		taskList.put(taskToUpdateInTaskLists.getTaskId(),
				taskToUpdateInTaskLists);

		if (taskToUpdateInTaskLists.getgCalTaskId() != null) {
			gCalTaskList.put(taskToUpdateInTaskLists.getgCalTaskId(),
					taskToUpdateInTaskLists);
		}
	}

	public void removeTaskInTaskLists(Task taskToRemoveFromTaskLists) {
		taskList.remove(taskToRemoveFromTaskLists.getTaskId());
		gCalTaskList.remove(taskToRemoveFromTaskLists.getgCalTaskId());
	}

	// Task Query Methods
	public Task getTask(int taskId) {
		return taskList.get(taskId).clone();
	}

	public Task getSyncTask(String gCalTaskId) {
		return gCalTaskList.get(gCalTaskId).clone();
	}

	/**
	 * Query all tasks from taskList
	 * 
	 * @param orderByStartDateTime
	 * @return list of all tasks
	 */
	public List<Task> getTasks(boolean orderByStartDateTime) {
		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {

			if (entry.getValue().isDeleted()) {
				continue;
			}

			queriedTaskRecordset.add(entry.getValue().clone());
		}

		if (orderByStartDateTime) {
			orderTaskRecordSetByStartDateTime(queriedTaskRecordset);
		}

		return queriedTaskRecordset;
	}

	/**
	 * Return tasks with matching taskName, case-insensitive substring search
	 * (exclusive of deleted tasks)
	 * 
	 * @param taskName
	 * @param orderByStartDateTime
	 * @return list of matched tasks
	 */
	public List<Task> getTasks(String taskName, boolean orderByStartDateTime) {
		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			if (entry.getValue().isDeleted()) {
				continue;
			}
			if (entry.getValue().getTaskName().toLowerCase()
					.contains(taskName.toLowerCase())) {
				queriedTaskRecordset.add(entry.getValue().clone());
			}
		}

		if (orderByStartDateTime) {
			orderTaskRecordSetByStartDateTime(queriedTaskRecordset);
		}

		return queriedTaskRecordset;
	}

	/**
	 * Returns tasks that match specified TaskCategory (exclusive of deleted
	 * tasks)
	 * 
	 * @param queryTaskCategory
	 * @param orderByStartDateTime
	 * @return list of matched tasks
	 */
	public List<Task> getTasks(TaskCategory queryTaskCategory,
			boolean orderByStartDateTime) {
		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {

			if (entry.getValue().isDeleted()) {
				continue;
			}

			TaskCategory taskCategory = entry.getValue().getTaskCategory();
			if (taskCategory.equals(queryTaskCategory)) {
				queriedTaskRecordset.add(entry.getValue().clone());
			}
		}

		if (orderByStartDateTime) {
			orderTaskRecordSetByStartDateTime(queriedTaskRecordset);
		}

		return queriedTaskRecordset;
	}

	/**
	 * Returns tasks that is within startTime or endTime inclusive (exclusive of
	 * deleted tasks)
	 * 
	 * @param startTime
	 * @param endTime
	 * @param orderByStartDateTime
	 * @return list of matched tasks
	 */
	public List<Task> getTasks(DateTime startTime, DateTime endTime,
			boolean orderByStartDateTime) {
		List<Task> queriedTaskRecordset = new LinkedList<Task>();

		// Set interval for matched range (increase endtime by 1 ms to include)
		Interval dateTimeInterval = new Interval(startTime,
				endTime.plusMillis(1));

		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {

			if (entry.getValue().isDeleted()) {
				continue;
			}

			switch (entry.getValue().getTaskCategory()) {
			case TIMED:
				if (dateTimeInterval.contains(entry.getValue()
						.getStartDateTime())
						|| dateTimeInterval.contains(entry.getValue()
								.getEndDateTime())) {
					if (!entry.getValue().isDeleted()) {
						queriedTaskRecordset.add(entry.getValue().clone());
					}
				}
				break;
			case DEADLINE:
				if (dateTimeInterval
						.contains(entry.getValue().getEndDateTime())) {
					if (!entry.getValue().isDeleted()) {
						queriedTaskRecordset.add(entry.getValue().clone());
					}
				}
				break;
			case FLOATING:
			default:
				break;
			}
		}

		if (orderByStartDateTime) {
			orderTaskRecordSetByStartDateTime(queriedTaskRecordset);
		}

		return queriedTaskRecordset;
	}

	/**
	 * Returns task that matches any of the specified parameters (exclusive of
	 * deleted tasks)
	 * 
	 * @param queriedTaskName
	 * @param queriedStartTime
	 * @param queriedEndTime
	 * @param orderByStartDateTime
	 * @return list of matched tasks
	 */
	public List<Task> getTasks(String queriedTaskName,
			DateTime queriedStartTime, DateTime queriedEndTime,
			boolean orderByStartDateTime) {

		List<Task> queriedTaskRecordset = new LinkedList<Task>();

		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {

			if (entry.getValue().isDeleted()) {
				continue;
			}

			Task taskEntry = entry.getValue();
			// Set interval for matched range (increase endtime by 1 ms to
			// include)
			Interval dateTimeInterval = new Interval(queriedStartTime,
					queriedEndTime.plusMillis(1));

			if (taskEntry.getTaskName().contains(queriedTaskName)) {
				queriedTaskRecordset.add(entry.getValue().clone());
				continue;
			}
			if (dateTimeInterval.contains(taskEntry.getStartDateTime())
					|| dateTimeInterval.contains(taskEntry.getEndDateTime())) {
				queriedTaskRecordset.add(entry.getValue().clone());
				continue;
			}
		}

		if (orderByStartDateTime) {
			orderTaskRecordSetByStartDateTime(queriedTaskRecordset);
		}

		return queriedTaskRecordset;
	}

	/**
	 * Returns task that matches any of the specified parameters (exclusive of
	 * deleted tasks)
	 * 
	 * @param queriedTaskName
	 * @param queriedTaskCategory
	 * @param queriedStartTime
	 * @param queriedEndTime
	 * @param orderByStartDateTime
	 * @return list of matched tasks
	 */
	public List<Task> getTasks(String queriedTaskName,
			TaskCategory queriedTaskCategory, DateTime queriedStartTime,
			DateTime queriedEndTime, boolean orderByStartDateTime) {

		List<Task> queriedTaskRecordset = new LinkedList<Task>();

		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {

			if (entry.getValue().isDeleted()) {
				continue;
			}

			Task taskEntry = entry.getValue();
			// Set interval for matched range (increase endtime by 1 ms to
			// include)
			Interval dateTimeInterval = new Interval(queriedStartTime,
					queriedEndTime.plusMillis(1));

			if (taskEntry.getTaskCategory().equals(queriedTaskCategory)) {
				queriedTaskRecordset.add(entry.getValue().clone());
				continue;
			}
			if (taskEntry.getTaskName().contains(queriedTaskName)) {
				queriedTaskRecordset.add(entry.getValue().clone());
				continue;
			}
			if (dateTimeInterval.contains(taskEntry.getStartDateTime())
					|| dateTimeInterval.contains(taskEntry.getEndDateTime())) {
				queriedTaskRecordset.add(entry.getValue().clone());
				continue;
			}
		}

		if (orderByStartDateTime) {
			orderTaskRecordSetByStartDateTime(queriedTaskRecordset);
		}

		return queriedTaskRecordset;
	}

	private void orderTaskRecordSetByStartDateTime(
			List<Task> queriedTaskRecordset) {
		TaskStartDateTimeComparator taskStartDateTimeComparator = new TaskStartDateTimeComparator();
		Collections.sort(queriedTaskRecordset, taskStartDateTimeComparator);
	}

	public void clearTaskLists() {
		taskList.clear();
		gCalTaskList.clear();
	}

	public boolean containsTask(int taskId) {
		if (taskList.containsKey(taskId)) {
			return true;
		}
		return false;
	}

	public boolean containsSyncTask(String gCalTaskId) {
		if (gCalTaskList.containsKey(gCalTaskId)) {
			return true;
		}
		return false;
	}

	// Getters and Setters

	public Map<Integer, Task> getTaskList() {
		return taskList;
	}

	public Map<String, Task> getGcalTaskList() {
		return gCalTaskList;
	}

}
