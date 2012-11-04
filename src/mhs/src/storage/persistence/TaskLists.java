//@author A0087048X

package mhs.src.storage.persistence;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;
import mhs.src.util.TaskNotFoundException;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * TaskLists
 * 
 * Abstracts operations on the taskLists containing different views of tasks:
 * 1. taskList - by taskId
 * 2. gCalTaskList - by gCalTaskId
 * 
 * Functionality
 * - logic for CRUD on tasks in all task lists
 * - getters for taskLists
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */
public class TaskLists {

	private Map<Integer, Task> taskList; // primary task list with index as key
	private Map<String, Task> gCalTaskList; // task list with gCalId as key

	private static final Logger logger = MhsLogger.getLogger();

	private static final String EXCEPTION_MESSAGE_NULL_PARAMETER = "%1$s cannot be null!";

	private static final String PARAMETER_TASK_LIST_TO_INITIALIZE = "taskListToInitialize";
	private static final String PARAMETER_TASK_TO_UPDATE_IN_TASK_LISTS = "taskToUpdateInTaskLists";
	private static final String PARAMETER_TASK_TO_REMOVE_FROM_TASK_LISTS = "taskToRemoveFromTaskLists";
	private static final String PARAMETER_QUERY_TASK_CATEGORY = "queryTaskCategory";
	private static final String PARAMETER_G_CAL_TASK_ID = "gCalTaskId";
	private static final String PARAMETER_TASK_NAME = "taskName";
	private static final String PARAMETER_START_AND_END_DATE_TIMES = "start and end date times";
	private static final String EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST = "Task does not exist!";

	/**
	 * Constructor for TaskLists
	 * 
	 * @param taskListToInitialize
	 */
	public TaskLists(Map<Integer, Task> taskListToInitialize) {
		logEnterMethod("TaskLists");

		if (taskListToInitialize == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_TASK_LIST_TO_INITIALIZE));
		}

		initializeTaskLists(taskListToInitialize);

		logExitMethod("TaskLists");
	}

	/**
	 * Initialize task lists
	 * 
	 * @param taskListToInitialize
	 */
	private void initializeTaskLists(Map<Integer, Task> taskListToInitialize) {
		logEnterMethod("initializeTaskLists");
		assert (taskListToInitialize != null);

		createTaskLists();
		loadTaskListsFromTaskListToInitialize(taskListToInitialize);

		logExitMethod("initializeTaskLists");
	}

	/**
	 * Create task lists 
	 * - taskList 
	 * - gCalTaskList
	 */
	private void createTaskLists() {
		logExitMethod("createTaskLists");

		taskList = new LinkedHashMap<Integer, Task>();
		gCalTaskList = new LinkedHashMap<String, Task>();

		logExitMethod("createTaskLists");
	}

	/**
	 * Load task lists with tasks from taskListToInitialize
	 * 
	 * @param taskListToInitialize
	 */
	private void loadTaskListsFromTaskListToInitialize(
			Map<Integer, Task> taskListToInitialize) {
		logEnterMethod("loadTaskListsFromTaskListToInitialize");
		assert (taskListToInitialize != null);

		for (Map.Entry<Integer, Task> entry : taskListToInitialize.entrySet()) {
			Task taskToAdd = entry.getValue();
			taskList.put(taskToAdd.getTaskId(), taskToAdd);
			if (taskToAdd.getgCalTaskId() != null) {
				gCalTaskList.put(taskToAdd.getgCalTaskId(), taskToAdd);
			}
		}
		logExitMethod("loadTaskListsFromTaskListToInitialize");
	}

	// Task List task CRUD Methods

	/**
	 * Adds or updates task to task lists
	 * 
	 * @param taskToUpdateInTaskLists
	 */
	public void updateTaskInTaskLists(Task taskToUpdateInTaskLists) {
		logEnterMethod("updateTaskInTaskLists");

		if (taskToUpdateInTaskLists == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_TASK_TO_UPDATE_IN_TASK_LISTS));
		}

		taskList.put(taskToUpdateInTaskLists.getTaskId(),
				taskToUpdateInTaskLists);

		if (taskToUpdateInTaskLists.getgCalTaskId() != null) {
			gCalTaskList.put(taskToUpdateInTaskLists.getgCalTaskId(),
					taskToUpdateInTaskLists);
		}
		logExitMethod("updateTaskInTaskLists");
	}

	/**
	 * Remove Task in task lists
	 * 
	 * @param taskToRemoveFromTaskLists
	 */
	public void removeTaskInTaskLists(Task taskToRemoveFromTaskLists) {
		logEnterMethod("removeTaskInTaskLists");

		if (taskToRemoveFromTaskLists == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_TASK_TO_REMOVE_FROM_TASK_LISTS));
		}

		taskList.remove(taskToRemoveFromTaskLists.getTaskId());
		gCalTaskList.remove(taskToRemoveFromTaskLists.getgCalTaskId());

		logExitMethod("removeTaskInTaskLists");
	}

	// Task Query Methods

	/**
	 * Gets task by taskId
	 * 
	 * @param taskId
	 * @return task
	 * @throws TaskNotFoundException
	 */
	public Task getTask(int taskId) throws TaskNotFoundException {
		logEnterMethod("getTask");

		if (!containsTask(taskId)) {
			throw new TaskNotFoundException(
					EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		logExitMethod("getTask");
		return taskList.get(taskId).clone();
	}

	/**
	 * Gets sync task by gCalTaskId
	 * 
	 * @param gCalTaskId
	 * @return task with gCalTaskId
	 * @throws TaskNotFoundException
	 */
	public Task getSyncTask(String gCalTaskId) throws TaskNotFoundException {
		logEnterMethod("getSyncTask");

		if (gCalTaskId == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_G_CAL_TASK_ID));
		}
		if (!containsSyncTask(gCalTaskId)) {
			throw new TaskNotFoundException(
					EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		logExitMethod("getSyncTask");
		return gCalTaskList.get(gCalTaskId).clone();
	}

	/**
	 * Query all tasks from taskList
	 * 
	 * @param orderByStartDateTime
	 * @return list of all tasks
	 */
	public List<Task> getTasks(boolean orderByStartDateTime) {
		logEnterMethod("getTasks");

		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		getAllNonDeletedTasks(queriedTaskRecordset);
		sortTaskList(orderByStartDateTime, queriedTaskRecordset);

		logExitMethod("getTasks");
		return queriedTaskRecordset;
	}

	/**
	 * Get all non-deleted tasks from taskList
	 * 
	 * @param queriedTaskRecordset
	 */
	private void getAllNonDeletedTasks(List<Task> queriedTaskRecordset) {
		logEnterMethod("getAllNonDeletedTasks");

		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			if (entry.getValue().isDeleted()) {
				continue;
			}
			queriedTaskRecordset.add(entry.getValue().clone());
		}

		logExitMethod("getAllNonDeletedTasks");
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
		logEnterMethod("getTasks");
		if (taskName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_TASK_NAME));
		}

		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		getAllNonDeletedTasksWithMatchingTaskName(taskName,
				queriedTaskRecordset);
		sortTaskList(orderByStartDateTime, queriedTaskRecordset);

		logExitMethod("getTasks");
		return queriedTaskRecordset;
	}

	/**
	 * Get all non-deleted tasks with matching task name
	 * 
	 * @param taskName
	 * @param queriedTaskRecordset
	 */
	private void getAllNonDeletedTasksWithMatchingTaskName(String taskName,
			List<Task> queriedTaskRecordset) {
		logEnterMethod("getAllNonDeletedTasksWithMatchingTaskName");
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			if (entry.getValue().isDeleted()) {
				continue;
			}
			if (entry.getValue().getTaskName().toLowerCase()
					.contains(taskName.toLowerCase())) {
				queriedTaskRecordset.add(entry.getValue().clone());
			}
		}
		logExitMethod("getAllNonDeletedTasksWithMatchingTaskName");
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
		logEnterMethod("getTasks");
		if (queryTaskCategory == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_QUERY_TASK_CATEGORY));
		}

		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		getAllNonDeletedTasksWithTaskCategory(queryTaskCategory,
				queriedTaskRecordset);
		sortTaskList(orderByStartDateTime, queriedTaskRecordset);

		logExitMethod("getTasks");
		return queriedTaskRecordset;
	}

	/**
	 * Gets non deleted tasks and adds them to queriedTaskRecordset
	 * 
	 * @param queryTaskCategory
	 * @param queriedTaskRecordset
	 */
	private void getAllNonDeletedTasksWithTaskCategory(
			TaskCategory queryTaskCategory, List<Task> queriedTaskRecordset) {
		logEnterMethod("getAllNonDeletedTasksWithTaskCategory");

		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			if (entry.getValue().isDeleted()) {
				continue;
			}

			TaskCategory taskCategory = entry.getValue().getTaskCategory();
			if (taskCategory.equals(queryTaskCategory)) {
				queriedTaskRecordset.add(entry.getValue().clone());
			}
		}
		logExitMethod("getAllNonDeletedTasksWithTaskCategory");
	}

	/**
	 * Returns tasks that is within startTime or endTime inclusive (exclusive of
	 * deleted tasks)
	 * 
	 * @param startDateTime
	 * @param endDateTime
	 * @param orderByStartDateTime
	 * @return list of matched tasks
	 */
	public List<Task> getTasks(DateTime startDateTime, DateTime endDateTime,
			boolean orderByStartDateTime) {
		logEnterMethod("getTasks");
		if (startDateTime == null | endDateTime == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_START_AND_END_DATE_TIMES));
		}

		List<Task> queriedTaskRecordset = new LinkedList<Task>();

		Interval dateTimeInterval = getDateTimeInterval(startDateTime,
				endDateTime);
		getAllTasksWithinInterval(queriedTaskRecordset, dateTimeInterval);
		sortTaskList(orderByStartDateTime, queriedTaskRecordset);

		logExitMethod("getTasks");
		return queriedTaskRecordset;
	}

	/**
	 * Get DateTime interval from two DateTimes inclusive
	 * 
	 * @param startDateTime
	 * @param endDateTime
	 * @return dateTimeInterval
	 */
	private Interval getDateTimeInterval(DateTime startDateTime,
			DateTime endDateTime) {
		logEnterMethod("getDateTimeInterval");
		// Set interval for matched range (increase endtime by 1 ms to include)
		Interval dateTimeInterval = new Interval(startDateTime,
				endDateTime.plusMillis(1));
		logExitMethod("getDateTimeInterval");
		return dateTimeInterval;
	}

	/**
	 * Gets tasks within DateTime interval and adds them to queriedTaskRecordset
	 * 
	 * @param queriedTaskRecordset
	 * @param dateTimeInterval
	 */
	private void getAllTasksWithinInterval(List<Task> queriedTaskRecordset,
			Interval dateTimeInterval) {
		logEnterMethod("getAllTasksWithinInterval");
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			Task taskEntry = entry.getValue();
			if (taskEntry.isDeleted()) {
				continue;
			}

			switch (taskEntry.getTaskCategory()) {
			case TIMED:
				if (isWithinInterval(dateTimeInterval,
						taskEntry.getStartDateTime(),
						taskEntry.getEndDateTime())) {
					queriedTaskRecordset.add(entry.getValue().clone());
				}
				break;
			case DEADLINE:
				if (isWithinInterval(dateTimeInterval,
						taskEntry.getEndDateTime(), taskEntry.getEndDateTime())) {
					queriedTaskRecordset.add(taskEntry.clone());
				}
				break;
			case FLOATING:
			default:
				break;
			}
		}
		logExitMethod("getAllTasksWithinInterval");
	}

	/**
	 * Checks if startDateTime and endDateTime are within a dateTime interval
	 * 
	 * @param dateTimeInterval
	 * @param startDateTime
	 * @param endDateTime
	 * @return true if start and end DateTime are with dateTimeInterval
	 */
	private boolean isWithinInterval(Interval dateTimeInterval,
			DateTime startDateTime, DateTime endDateTime) {
		logEnterMethod("isWithinInterval");
		logExitMethod("isWithinInterval");
		return dateTimeInterval.contains(startDateTime)
				|| dateTimeInterval.contains(endDateTime);
	}

	/**
	 * Returns task that matches any of the specified parameters (exclusive of
	 * deleted tasks)
	 * 
	 * @param taskName
	 * @param startDateTime
	 * @param endDateTime
	 * @param orderByStartDateTime
	 * @return list of matched tasks
	 */
	public List<Task> getTasks(String taskName, DateTime startDateTime,
			DateTime endDateTime, boolean orderByStartDateTime) {
		logEnterMethod("getTasks");
		if (taskName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_TASK_NAME));
		}

		if (startDateTime == null | endDateTime == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_START_AND_END_DATE_TIMES));
		}

		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		getTasksMatchingParameters(taskName, startDateTime, endDateTime,
				queriedTaskRecordset);
		sortTaskList(orderByStartDateTime, queriedTaskRecordset);

		logExitMethod("getTasks");
		return queriedTaskRecordset;
	}

	/**
	 * Gets tasks matching any of the specified parameters and adds them to
	 * queriedTaskRecordset
	 * 
	 * @param taskName
	 * @param startDateTime
	 * @param endDateTime
	 * @param queriedTaskRecordset
	 */
	private void getTasksMatchingParameters(String taskName,
			DateTime startDateTime, DateTime endDateTime,
			List<Task> queriedTaskRecordset) {
		logEnterMethod("getTasksMatchingParameters");
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {

			Task taskEntry = entry.getValue();
			if (taskEntry.isDeleted()) {
				continue;
			}

			Interval dateTimeInterval = getDateTimeInterval(startDateTime,
					endDateTime);

			if (taskEntry.getTaskName().contains(taskName)) {
				queriedTaskRecordset.add(taskEntry.clone());
				continue;
			}

			if (dateTimeInterval.contains(taskEntry.getStartDateTime())
					|| dateTimeInterval.contains(taskEntry.getEndDateTime())) {
				queriedTaskRecordset.add(taskEntry.clone());
				continue;
			}
		}
		logExitMethod("getTasksMatchingParameters");
	}

	/**
	 * Returns task that matches any of the specified parameters (exclusive of
	 * deleted tasks)
	 * 
	 * @param taskName
	 * @param taskCategory
	 * @param startDateTime
	 * @param endDateTime
	 * @param orderByStartDateTime
	 * @return list of matched tasks
	 */
	public List<Task> getTasks(String taskName, TaskCategory taskCategory,
			DateTime startDateTime, DateTime endDateTime,
			boolean orderByStartDateTime) {
		logEnterMethod("getTasks");

		if (taskName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_TASK_NAME));
		}

		if (startDateTime == null | endDateTime == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_START_AND_END_DATE_TIMES));
		}

		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		getTasksMatchingParameters(taskName, taskCategory, startDateTime,
				endDateTime, queriedTaskRecordset);
		sortTaskList(orderByStartDateTime, queriedTaskRecordset);

		logExitMethod("getTasks");
		return queriedTaskRecordset;
	}

	/**
	 * Gets tasks matching any of the specified parameters and adds them to
	 * queriedTaskRecordset
	 * 
	 * @param taskName
	 * @param taskCategory
	 * @param startDateTime
	 * @param endDateTime
	 * @param queriedTaskRecordset
	 */
	private void getTasksMatchingParameters(String taskName,
			TaskCategory taskCategory, DateTime startDateTime,
			DateTime endDateTime, List<Task> queriedTaskRecordset) {
		logEnterMethod("getTasksMatchingParameters");

		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			Task taskEntry = entry.getValue();
			if (taskEntry.isDeleted()) {
				continue;
			}

			Interval dateTimeInterval = getDateTimeInterval(startDateTime,
					endDateTime);

			if (taskEntry.getTaskCategory().equals(taskCategory)) {
				queriedTaskRecordset.add(taskEntry.clone());
				continue;
			}
			if (taskEntry.getTaskName().contains(taskName)) {
				queriedTaskRecordset.add(taskEntry.clone());
				continue;
			}
			if (dateTimeInterval.contains(taskEntry.getStartDateTime())
					|| dateTimeInterval.contains(taskEntry.getEndDateTime())) {
				queriedTaskRecordset.add(taskEntry.clone());
				continue;
			}
		}
		logExitMethod("getTasksMatchingParameters");
	}

	/**
	 * Sort task list by startDateTime if orderByStartDateTime is true,
	 * otherwise, normal ordering by taskId is used
	 * 
	 * @param orderByStartDateTime
	 * @param queriedTaskRecordset
	 */
	private void sortTaskList(boolean orderByStartDateTime,
			List<Task> queriedTaskRecordset) {
		logEnterMethod("sortTaskList");
		if (orderByStartDateTime) {
			orderTaskRecordSetByStartDateTime(queriedTaskRecordset);
		}
		logExitMethod("sortTaskList");
	}

	/**
	 * Orders list of tasks by start DateTime
	 * 
	 * @param queriedTaskRecordset
	 */
	private void orderTaskRecordSetByStartDateTime(
			List<Task> queriedTaskRecordset) {
		logEnterMethod("orderTaskRecordSetByStartDateTime");
		TaskStartDateTimeComparator taskStartDateTimeComparator = new TaskStartDateTimeComparator();
		Collections.sort(queriedTaskRecordset, taskStartDateTimeComparator);
		logExitMethod("orderTaskRecordSetByStartDateTime");
	}

	/**
	 * Clear all task lists
	 */
	public void clearTaskLists() {
		logEnterMethod("clearTaskLists");
		taskList.clear();
		gCalTaskList.clear();
		logExitMethod("clearTaskLists");
	}

	/**
	 * Checks if Task Lists contains Task
	 * 
	 * @param taskId
	 * @return true if task specified by taskId exists
	 */
	public boolean containsTask(int taskId) {
		logEnterMethod("containsTask");
		boolean containsTask = false;
		if (taskList.containsKey(taskId)) {
			logger.exiting(getClass().getName(),
					new Exception().getStackTrace()[0].getMethodName());
			containsTask = true;
		}
		logExitMethod("containsTask");
		return containsTask;
	}

	/**
	 * Checks if Task Lists contains Sync Task
	 * 
	 * @param gCalTaskId
	 * @return true if task specified by gCalTaskId exists
	 */
	public boolean containsSyncTask(String gCalTaskId) {
		logEnterMethod("containsSyncTask");
		if (gCalTaskId == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_G_CAL_TASK_ID));
		}
		boolean containsSyncTask = false;
		if (gCalTaskList.containsKey(gCalTaskId)) {
			containsSyncTask = true;
		}
		logExitMethod("containsSyncTask");
		return containsSyncTask;
	}

	/**
	 * Getter for task list with taskId as key
	 * 
	 * @return taskList
	 */
	public Map<Integer, Task> getTaskList() {
		logEnterMethod("getTaskList");
		logExitMethod("getTaskList");
		return taskList;
	}

	/**
	 * Getter for gCalTaskList with gCalTaskId as key
	 * 
	 * @return gCalTaskList
	 */
	public Map<String, Task> getGcalTaskList() {
		logEnterMethod("getGcalTaskList");
		logExitMethod("getGcalTaskList");
		return gCalTaskList;
	}

	private void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}

	private void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}
}
