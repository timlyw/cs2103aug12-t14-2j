package mhs.src;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class Database {

	private Syncronize syncronize;
	private static GoogleCalendar googleCalendar;
	private static TaskRecordFile taskRecordFile;
	private static ConfigFile configFile;

	// Data Views
	// contains Task objects references

	private static Map<Integer, Task> taskList; // primary task list with index
												// as key
	private static Map<String, Task> gCalTaskList; // task list with gCalId as
													// key

	private boolean isRemoteSyncEnabled = true;

	private static final String EXCEPTION_MESSAGE_INVALID_TASK_FORMAT = "Invalid Task Format";
	private static final String EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST = "Task does not exist";

	private static class Syncronize {

		/**
		 * Syncronizes Databases
		 * 
		 * @throws Exception
		 * 
		 * @throws IOException
		 */
		public void syncronizeDatabases() throws Exception {
			pullSync();
			pushSync();
		}

		/**
		 * Pushes local Deadline and Timed tasks to remote Syncs deleted local
		 * tasks, new tasks and updated existing tasks
		 * 
		 * @throws IOException
		 * @throws ServiceException
		 */
		private void pushSync() throws IOException, ServiceException {
			// push sync tasks from local to google calendar
			for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
				pushSyncTask(entry.getValue());
			}
		}

		/**
		 * Push sync new or existing new tasks
		 * 
		 * @param localTask
		 * @throws IOException
		 * @throws ServiceException
		 */
		private void pushSyncTask(Task localTask) throws IOException,
				ServiceException {

			// skip floating tasks
			if (localTask.getTaskCategory().equals(TaskCategory.FLOATING)) {
				return;
			}

			// remove deleted task
			if (localTask.isDeleted()) {
				System.out.println("Removing deleted synced task");
				googleCalendar.deleteEvent(localTask.getgCalTaskId());
				return;
			}

			// add unsynced tasks
			if (isUnsyncedTask(localTask)) {
				System.out.println("Pushing new sync task");
				pushSyncNewTask(localTask);
			} else {
				// add updated tasks
				if (localTask.getTaskUpdated().isAfter(
						localTask.getTaskLastSync())) {
					System.out.println("Pushing updated task");
					pushSyncExistingTask(localTask);
				}
			}
		}

		/**
		 * Push task that is currently not synced. Call pushSyncTask to sync
		 * tasks instead as it contains sync validation logic.
		 * 
		 * @param localTask
		 * @throws IOException
		 * @throws ServiceException
		 */
		private void pushSyncNewTask(Task localTask) throws IOException,
				ServiceException {

			// adds event to google calendar
			CalendarEventEntry addedGCalEvent = googleCalendar.createEvent(
					localTask.getTaskName(), localTask.getStartDateTime()
							.toString(), localTask.getEndDateTime().toString());

			// update local task sync details
			localTask.setgCalTaskId(addedGCalEvent.getIcalUID());
			localTask.setTaskLastSync(new DateTime(addedGCalEvent.getUpdated()
					.toString()));

			updateTaskLists(localTask);
		}

		/**
		 * Push existing synced task Call pushSyncTask to sync task as it
		 * handles sync logic Call pushSyncTask to sync tasks instead as it
		 * contains sync validation logic.
		 * 
		 * @param localTask
		 * @throws IOException
		 * @throws ServiceException
		 */
		private void pushSyncExistingTask(Task localTask) throws IOException,
				ServiceException {

			// update remote task
			CalendarEventEntry updatedGCalEvent = googleCalendar.updateEvent(
					localTask.getgCalTaskId(), localTask.getTaskName(),
					localTask.getStartDateTime().toString(), localTask
							.getEndDateTime().toString());

			if (updatedGCalEvent == null) {
				return;
			}

			// Update local task sync details
			DateTime syncDateTime = setSyncTime(updatedGCalEvent);
			localTask.setTaskLastSync(syncDateTime);

			updateTaskLists(localTask);
		}

		/**
		 * Pull Sync remote tasks to local
		 * 
		 * @throws Exception
		 */
		private void pullSync() throws Exception {
			List<CalendarEventEntry> googleCalendarEvents = googleCalendar
					.getEventList();
			Iterator<CalendarEventEntry> iterator = googleCalendarEvents
					.iterator();

			// pull sync remote tasks
			while (iterator.hasNext()) {
				CalendarEventEntry gCalEntry = iterator.next();
				pullSyncTask(gCalEntry);
			}
		}

		/**
		 * Pull sync new or existing task from remote
		 * 
		 * @param gCalEntry
		 * @throws Exception
		 */
		private void pullSyncTask(CalendarEventEntry gCalEntry)
				throws Exception {

			if (gCalTaskList.containsKey(gCalEntry.getIcalUID())) {

				Task localTask = gCalTaskList.get(gCalEntry.getIcalUID());

				// pull sync deleted events
				System.out.println("Deleting cancelled task");
				if (gCalEntry.getStatus().getValue().contains("canceled")) {

					// delete local task
					if (taskExists(gCalEntry.getIcalUID())) {
						deleteTask(gCalTaskList.get(gCalEntry.getIcalUID()));
					}
					return;
				}

				// pull sync newer task
				if (localTask.getTaskLastSync().isBefore(
						new DateTime(gCalEntry.getUpdated().getValue()))) {
					pullSyncExistingTask(gCalEntry, localTask);
				}

			} else {
				pullSyncNewTask(gCalEntry);
			}
		}

		/**
		 * Creates new synced task from remote. Call pullSyncTask as it contains
		 * sync validation logic.
		 * 
		 * @param gCalEntry
		 * @throws Exception
		 */
		private void pullSyncNewTask(CalendarEventEntry gCalEntry)
				throws Exception {

			// pull new remote task
			System.out.println("pulling new event");

			// pull sync deleted events
			if (gCalEntry.getStatus().getValue().contains("canceled")) {
				System.out.println("Deleting cancelled task");
				if (taskExists(gCalEntry.getIcalUID())) {
					deleteTask(gCalTaskList.get(gCalEntry.getIcalUID()));
				}
				return;
			}

			DateTime syncDateTime = setSyncTime(gCalEntry);

			// add task from google calendar entry
			if (gCalEntry.getTimes().get(0).getStartTime()
					.equals(gCalEntry.getTimes().get(0).getEndTime())) {
				// create new deadline task
				Task newTask = new DeadlineTask(getNewTaskId(), gCalEntry,
						syncDateTime);
				updateTaskLists(newTask);
			} else {
				// create new timed task
				Task newTask = new TimedTask(getNewTaskId(), gCalEntry,
						syncDateTime);
				updateTaskLists(newTask);
			}
		}

		/**
		 * Syncs existing local task with updated remote task Call pullSyncTask
		 * as it contains sync validation logic.
		 * 
		 * @param gCalEntry
		 * @param localTaskEntry
		 */
		private void pullSyncExistingTask(CalendarEventEntry gCalEntry,
				Task localTaskEntry) {

			System.out.println("pulling newer event : "
					+ localTaskEntry.getTaskName());

			DateTime syncDateTime = setSyncTime(gCalEntry);

			// update local task
			localTaskEntry.setTaskName(gCalEntry.getTitle().getPlainText());
			localTaskEntry.setStartDateTime(new DateTime(gCalEntry.getTimes()
					.get(0).getStartTime().toString()));
			localTaskEntry.setEndDateTime(new DateTime(gCalEntry.getTimes()
					.get(0).getEndTime().toString()));
			localTaskEntry.setTaskLastSync(syncDateTime);
			localTaskEntry.setTaskUpdated(syncDateTime);

			updateTaskLists(localTaskEntry);
		}

		/**
		 * Sets sync time for google calendar entry
		 * 
		 * @param gCalEntry
		 * @return sync datetime for updating local task
		 */
		private DateTime setSyncTime(CalendarEventEntry gCalEntry) {
			DateTime syncDateTime = new DateTime();
			if (gCalEntry.getUpdated() == null) {
				gCalEntry.setUpdated(com.google.gdata.data.DateTime.now());
			}
			syncDateTime = new DateTime(gCalEntry.getUpdated().toString());
			return syncDateTime;
		}
	}

	/**
	 * Database default constructor
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	public Database() throws IOException, ServiceException {
		initalizeDatabase();
		syncronizeDatabases();
	}

	/**
	 * Database constructor
	 * 
	 * @param taskRecordFileName
	 * @throws IOException
	 * @throws ServiceException
	 */
	public Database(String taskRecordFileName, boolean disableSyncronize)
			throws IOException, ServiceException {
		initalizeDatabase(taskRecordFileName);
		// syncronize local and web databases
		if (disableSyncronize) {
			isRemoteSyncEnabled = false;
		} else {
			syncronizeDatabases();
		}
	}

	/**
	 * Initialize database with specified taskRecordFile
	 * 
	 * @param taskRecordFileName
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void initalizeDatabase(String taskRecordFileName)
			throws IOException {

		try {
			configFile = new ConfigFile();
			taskRecordFile = new TaskRecordFile(taskRecordFileName);
			taskList = taskRecordFile.getTaskList();
			gCalTaskList = taskRecordFile.getGCalTaskList();

			syncronize = new Syncronize();
			initializeGoogleCalendarService();
		} catch (ServiceException e) {
			isRemoteSyncEnabled = false;
		}
	}

	/**
	 * Initialize database
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void initalizeDatabase() throws IOException {

		try {
			configFile = new ConfigFile();

			taskRecordFile = new TaskRecordFile();
			taskList = taskRecordFile.getTaskList();
			gCalTaskList = taskRecordFile.getGCalTaskList();

			syncronize = new Syncronize();
			initializeGoogleCalendarService();
		} catch (ServiceException e) {
			e.printStackTrace();
			isRemoteSyncEnabled = false;
		}

	}

	/**
	 * Initializes Google Calendar Service with saved access token
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void initializeGoogleCalendarService() throws IOException,
			ServiceException {
		if (configFile.hasConfigParameter("GOOGLE_AUTH_TOKEN")
				&& !configFile.getConfigParameter("GOOGLE_AUTH_TOKEN")
						.isEmpty()) {
			googleCalendar = new GoogleCalendar(
					configFile.getConfigParameter("GOOGLE_AUTH_TOKEN"));
			saveGoogleAuthToken();
		}
	}

	/**
	 * Logs in user google account with user details
	 * 
	 * @param userName
	 * @param userPassword
	 * @throws AuthenticationException
	 * @throws IOException
	 */
	public void authenticateUserGoogleAccount(String userName,
			String userPassword) throws AuthenticationException, IOException {
		googleCalendar.initializeCalendarService(userName, userPassword);
		saveGoogleAuthToken();
		isRemoteSyncEnabled = true;
	}

	/**
	 * Saves user Google Calendar Service access token to config file
	 * 
	 * @throws IOException
	 */
	private void saveGoogleAuthToken() throws IOException {
		String googleAuthToken = googleCalendar.getAuthToken();
		if (googleAuthToken != null) {
			configFile.setConfigParameter("GOOGLE_AUTH_TOKEN", googleAuthToken);
		}
	}

	// TODO BATCH OPERATIONS FOR DATABASE
	// TODO BATCH UPDATES FOR GOOGLE CALENDAR

	/**
	 * Syncronizes Databases
	 * 
	 * @throws ServiceException
	 * 
	 * @throws IOException
	 */
	public void syncronizeDatabases() throws ServiceException {
		try {
			syncronize.syncronizeDatabases();
			saveTaskRecordFile();
		} catch (ServiceException e) {
			isRemoteSyncEnabled = false;
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Returns List of all tasks
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<Task> query() throws IOException {
		List<Task> queryTaskList = new LinkedList<Task>();
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			if (!entry.getValue().isDeleted()) {
				queryTaskList.add(entry.getValue().clone());
			}
		}
		return queryTaskList;
	}

	/**
	 * Returns single task with specified taskId (deleted tasks queriable)
	 * 
	 * @param taskId
	 * @return
	 * @throws Exception
	 */
	public Task query(int taskId) throws Exception {
		if (!taskExists(taskId)) {
			throw new Exception(EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}
		return taskList.get(taskId).clone();
	}

	/**
	 * Return tasks with matching taskName, case-insensitive substring search
	 * 
	 * @param taskName
	 * @return
	 */
	public List<Task> query(String taskName) {
		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			if (entry.getValue().getTaskName().toLowerCase()
					.contains(taskName.toLowerCase())) {
				if (!entry.getValue().isDeleted()) {
					queriedTaskRecordset.add(entry.getValue().clone());
				}
			}
		}
		return queriedTaskRecordset;
	}

	/**
	 * Returns tasks that match specified TaskCategory
	 * 
	 * @param queryTaskCategory
	 * @return
	 */
	public List<Task> query(TaskCategory queryTaskCategory) {
		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
			TaskCategory taskCategory = entry.getValue().getTaskCategory();
			if (taskCategory.equals(queryTaskCategory)) {
				if (!entry.getValue().isDeleted()) {
					queriedTaskRecordset.add(entry.getValue().clone());
				}
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

		// Set interval for matched range (increase endtime by 1 ms to include)
		Interval dateTimeInterval = new Interval(startTime,
				endTime.plusMillis(1));

		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
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

		return queriedTaskRecordset;
	}

	/**
	 * Returns task that matches any of the specified parameters
	 * 
	 * @param taskName
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public List<Task> query(String queriedTaskName, DateTime queriedStartTime,
			DateTime queriedEndTime) {

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

		return null;
	}

	/**
	 * Returns task that matches any of the specified parameters
	 * 
	 * @param taskName
	 * @param taskCategory
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public List<Task> query(String queriedTaskName,
			TaskCategory queriedTaskCategory, DateTime queriedStartTime,
			DateTime queriedEndTime) {

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

		return null;
	}

	/**
	 * Adds a task
	 * 
	 * @param task
	 * @throws Exception
	 * @throws ServiceException
	 */
	public void add(Task task) throws Exception {

		if (!isTaskValid(task)) {
			throw new Exception(EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		}

		Task taskToAdd = task.clone();
		addTask(taskToAdd);

		if (isRemoteSyncEnabled) {
			syncronize.pushSyncTask(taskToAdd);
		}

		saveTaskRecordFile();
	}

	private void addTask(Task taskToAdd) {
		taskToAdd.setTaskId(getNewTaskId());
		new DateTime();
		taskToAdd.setTaskCreated(DateTime.now());
		taskToAdd.setTaskUpdated(DateTime.now());
		updateTaskLists(taskToAdd);
	}

	private static void updateTaskLists(Task task) {
		taskList.put(task.getTaskId(), task);
		gCalTaskList.put(task.getgCalTaskId(), task);
	}

	/**
	 * Checks if Task is valid for given format
	 */
	private boolean isTaskValid(Task task) {

		if (task.getTaskCategory() == null || task.getTaskName() == null) {
			return false;
		}

		switch (task.getTaskCategory()) {
		case FLOATING:
			break;
		case TIMED:
			if (task.getStartDateTime() == null
					|| task.getEndDateTime() == null) {
				return false;
			}
			break;
		case DEADLINE:
			if (task.getEndDateTime() == null) {
				return false;
			}
			break;
		default:
			return false;
		}
		return true;
	}

	/**
	 * Undeletes a task
	 * 
	 * @param taskId
	 * @throws Exception
	 * @throws ServiceException
	 */
	public void undelete(int taskId) throws Exception {

		if (!taskExists(taskId)) {
			throw new Exception(EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		Task taskToUndelete = taskList.get(taskId);
		undeleteTask(taskToUndelete);

		// TODO undelete google calendar task
		if (isRemoteSyncEnabled) {
			syncronize.pushSyncTask(taskToUndelete);
		}

		saveTaskRecordFile();
	}

	private void undeleteTask(Task taskToUndelete) {
		taskToUndelete.setDeleted(false);
		new DateTime();
		taskToUndelete.setTaskUpdated(DateTime.now());
	}

	/**
	 * Deletes a task
	 * 
	 * @param taskId
	 * @throws Exception
	 * @throws ServiceException
	 */
	public void delete(int taskId) throws Exception {

		if (!taskExists(taskId)) {
			throw new Exception(EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		Task taskToDelete = taskList.get(taskId);
		deleteTask(taskToDelete);

		if (isRemoteSyncEnabled) {
			syncronize.pushSyncTask(taskToDelete);
		}

		saveTaskRecordFile();

	}

	private static void deleteTask(Task taskToDelete) {
		taskToDelete.setDeleted(true);
		new DateTime();
		taskToDelete.setTaskUpdated(DateTime.now());
	}

	/**
	 * Updates task
	 * 
	 * @param updatedTask
	 * @throws Exception
	 * @throws ServiceException
	 */
	public void update(Task updatedTask) throws Exception {

		if (!taskExists(updatedTask.getTaskId())) {
			throw new Exception(EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		if (!isTaskValid(updatedTask)) {
			throw new Exception(EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		}

		Task updatedTaskToSave = updatedTask.clone();
		updateTask(updatedTaskToSave);

		if (isRemoteSyncEnabled) {
			syncronize.pushSyncTask(updatedTaskToSave);
		}

		saveTaskRecordFile();

	}

	private void updateTask(Task updatedTaskToSave) {
		new DateTime();
		updatedTaskToSave.setTaskUpdated(DateTime.now());

		updateTaskLists(updatedTaskToSave);
	}

	/**
	 * Save Tasks to local file
	 * 
	 * @throws IOException
	 */
	private void saveTaskRecordFile() throws IOException {
		taskRecordFile.saveTaskList(taskList);
	}

	/**
	 * Removes task from list
	 * 
	 * @param taskId
	 * @throws Exception
	 */
	private void removeRecord(int taskId) throws Exception {
		if (!taskExists(taskId)) {
			throw new Exception(EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}
		taskList.remove(taskId);
	}

	private void removeRecord(String gCalTaskId) throws Exception {
		if (!taskExists(gCalTaskId)) {
			throw new Exception(EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}
		taskList.remove(gCalTaskId);
	}

	/**
	 * Clears deleted and expired/done local and remote tasks
	 * 
	 * @throws Exception
	 */
	public void cleanupTasks() throws Exception {
		syncronizeDatabases();
		cleanupLocalTasks();
	}

	/**
	 * Clears deleted and expired/done local tasks
	 * 
	 * @throws Exception
	 */
	private void cleanupLocalTasks() throws Exception {
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {

			if (!entry.getValue().isDeleted()) {
				continue;
			}

			switch (entry.getValue().getTaskCategory()) {
			case TIMED:
			case DEADLINE:
				if (entry.getValue().getEndDateTime().isAfterNow()) {
					removeRecord(entry.getValue().getTaskId());
				}
				break;
			case FLOATING:
				if (entry.getValue().isDone()) {
					removeRecord(entry.getValue().getTaskId());
				}
				break;
			}
		}
		saveTaskRecordFile();
	}

	/**
	 * Clears database
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void clearDatabase() throws IOException, ServiceException {
		clearTaskLists();
		if (isRemoteSyncEnabled) {
			googleCalendar.deleteAllEvents();
		}
		saveTaskRecordFile();
	}

	public void clearLocalDatabase() throws IOException {
		clearTaskLists();
		saveTaskRecordFile();
	}

	public void clearRemoteDatabase() throws IOException, ServiceException {
		if (isRemoteSyncEnabled) {
			googleCalendar.deleteAllEvents();
		}
	}

	private void clearTaskLists() {
		taskList.clear();
		gCalTaskList.clear();
	}

	private static boolean taskExists(String gCalTaskId) {
		if (!gCalTaskList.containsKey(gCalTaskId)) {
			return false;
		}
		return true;
	}

	private boolean taskExists(int taskId) {
		if (!taskList.containsKey(taskId)) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param localTask
	 * @return
	 */
	private static boolean isUnsyncedTask(Task localTask) {
		return localTask.getgCalTaskId() == null
				|| localTask.getTaskLastSync() == null
				|| localTask.getTaskUpdated() == null;
	}

	/**
	 * Returns new taskId - (unique incremental)
	 * 
	 * @return
	 */
	private static int getNewTaskId() {
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