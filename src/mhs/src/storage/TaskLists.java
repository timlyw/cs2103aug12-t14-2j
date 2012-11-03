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
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

import org.joda.time.DateTime;
import org.joda.time.Interval;

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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		if (taskListToInitialize == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_TASK_LIST_TO_INITIALIZE));
		}

		initializeTaskLists(taskListToInitialize);

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Initialize task lists
	 * 
	 * @param taskListToInitialize
	 */
	private void initializeTaskLists(Map<Integer, Task> taskListToInitialize) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		assert(taskListToInitialize != null);
		
		createTaskLists();
		loadTaskListsFromTaskListToInitialize(taskListToInitialize);

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Create task lists - taskList - gCalTaskList
	 */
	private void createTaskLists() {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		taskList = new LinkedHashMap<Integer, Task>();
		gCalTaskList = new LinkedHashMap<String, Task>();

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Load task lists with tasks from taskListToInitialize
	 * 
	 * @param taskListToInitialize
	 */
	private void loadTaskListsFromTaskListToInitialize(
			Map<Integer, Task> taskListToInitialize) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		assert(taskListToInitialize != null);
		
		for (Map.Entry<Integer, Task> entry : taskListToInitialize.entrySet()) {
			Task taskToAdd = entry.getValue();
			taskList.put(taskToAdd.getTaskId(), taskToAdd);
			if (taskToAdd.getgCalTaskId() != null) {
				gCalTaskList.put(taskToAdd.getgCalTaskId(), taskToAdd);
			}
		}

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	// Task List task CRUD Methods

	/**
	 * Adds or updates task to task lists
	 * 
	 * @param taskToUpdateInTaskLists
	 */
	public void updateTaskInTaskLists(Task taskToUpdateInTaskLists) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

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

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Remove Task in task lists
	 * 
	 * @param taskToRemoveFromTaskLists
	 */
	public void removeTaskInTaskLists(Task taskToRemoveFromTaskLists) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		if (taskToRemoveFromTaskLists == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_TASK_TO_REMOVE_FROM_TASK_LISTS));
		}

		taskList.remove(taskToRemoveFromTaskLists.getTaskId());
		gCalTaskList.remove(taskToRemoveFromTaskLists.getgCalTaskId());

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		if (!containsTask(taskId)) {
			throw new TaskNotFoundException(
					EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		if (gCalTaskId == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_G_CAL_TASK_ID));
		}
		if (!containsSyncTask(gCalTaskId)) {
			throw new TaskNotFoundException(
					EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return gCalTaskList.get(gCalTaskId).clone();
	}

	/**
	 * Query all tasks from taskList
	 * 
	 * @param orderByStartDateTime
	 * @return list of all tasks
	 */
	public List<Task> getTasks(boolean orderByStartDateTime) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		getAllNonDeletedTasks(queriedTaskRecordset);
		sortTaskList(orderByStartDateTime, queriedTaskRecordset);

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return queriedTaskRecordset;
	}

	/**
	 * Get all non-deleted tasks from taskList
	 * 
	 * @param queriedTaskRecordset
	 */
	private void getAllNonDeletedTasks(List<Task> queriedTaskRecordset) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			if (entry.getValue().isDeleted()) {
				continue;
			}
			queriedTaskRecordset.add(entry.getValue().clone());
		}

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		if (taskName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_TASK_NAME));
		}

		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		getAllNonDeletedTasksWithMatchingTaskName(taskName,
				queriedTaskRecordset);
		sortTaskList(orderByStartDateTime, queriedTaskRecordset);

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			if (entry.getValue().isDeleted()) {
				continue;
			}
			if (entry.getValue().getTaskName().toLowerCase()
					.contains(taskName.toLowerCase())) {
				queriedTaskRecordset.add(entry.getValue().clone());
			}
		}
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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

		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		if (queryTaskCategory == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_QUERY_TASK_CATEGORY));
		}

		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		getAllNonDeletedTasksWithTaskCategory(queryTaskCategory,
				queriedTaskRecordset);
		sortTaskList(orderByStartDateTime, queriedTaskRecordset);

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			if (entry.getValue().isDeleted()) {
				continue;
			}

			TaskCategory taskCategory = entry.getValue().getTaskCategory();
			if (taskCategory.equals(queryTaskCategory)) {
				queriedTaskRecordset.add(entry.getValue().clone());
			}
		}
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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

		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		// Set interval for matched range (increase endtime by 1 ms to include)
		Interval dateTimeInterval = new Interval(startDateTime,
				endDateTime.plusMillis(1));
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

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
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

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

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

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

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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

		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

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
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
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
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		if (orderByStartDateTime) {
			orderTaskRecordSetByStartDateTime(queriedTaskRecordset);
		}
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Orders list of tasks by start DateTime
	 * 
	 * @param queriedTaskRecordset
	 */
	private void orderTaskRecordSetByStartDateTime(
			List<Task> queriedTaskRecordset) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		TaskStartDateTimeComparator taskStartDateTimeComparator = new TaskStartDateTimeComparator();
		Collections.sort(queriedTaskRecordset, taskStartDateTimeComparator);

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Clear all task lists
	 */
	public void clearTaskLists() {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		taskList.clear();
		gCalTaskList.clear();
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Checks if Task Lists contains Task
	 * 
	 * @param taskId
	 * @return true if task specified by taskId exists
	 */
	public boolean containsTask(int taskId) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		boolean containsTask = false;
		if (taskList.containsKey(taskId)) {
			logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
			containsTask = true;
		}
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return containsTask;
	}

	/**
	 * Checks if Task Lists contains Sync Task
	 * 
	 * @param gCalTaskId
	 * @return true if task specified by gCalTaskId exists
	 */
	public boolean containsSyncTask(String gCalTaskId) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		if (gCalTaskId == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_G_CAL_TASK_ID));
		}
		boolean containsSyncTask = false;
		if (gCalTaskList.containsKey(gCalTaskId)) {
			containsSyncTask = true;
		}
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return containsSyncTask;
	}

	/**
	 * Getter for task list with taskId as key
	 * 
	 * @return taskList
	 */
	public Map<Integer, Task> getTaskList() {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return taskList;
	}

	/**
	 * Getter for gCalTaskList with gCalTaskId as key
	 * 
	 * @return gCalTaskList
	 */
	public Map<String, Task> getGcalTaskList() {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return gCalTaskList;
	}

}
