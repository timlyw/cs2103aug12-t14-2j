/**
 * Database 
 * 
 * Database interfaces persistent the data storage mechanism, on local disk and remote 
 * (Google Calendar Service). Handles task queries and operations and user configuration settings.
 * 
 * @author timlyw
 */

package mhs.src.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import mhs.src.common.MhsLogger;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.util.ServiceException;

public class Database {

	private static final String GOOGLE_CALENDAR_APP_NAME = "My Hot Secretary";
	private static final int SYNC_TIMER_PERIOD = 300000;
	private static final int SYNC_TIMER_DELAY = 300000;
	private Syncronize syncronize;
	private GoogleCalendar googleCalendar;
	private TaskRecordFile taskRecordFile;
	private ConfigFile configFile;

	// Data Views - contains Task objects references

	// primary task list with index as key
	private Map<Integer, Task> taskList;
	// task list with gCalId as key
	private Map<String, Task> gCalTaskList;

	private DateTime syncStartDateTime;
	private DateTime syncEndDateTime;
	private boolean isRemoteSyncEnabled = true;
	private Timer DatabaseServiceTimer;
	private TimerTask pullSyncTimedTask;

	// Exception Messages
	private static final String EXCEPTION_MESSAGE_INVALID_TASK_FORMAT = "Invalid Task Format";
	private static final String EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST = "Task does not exist";

	// Config parameters
	private static final String CONFIG_PARAM_GOOGLE_USER_ACCOUNT = "GOOGLE_USER_ACCOUNT";
	private static final String CONFIG_PARAM_GOOGLE_AUTH_TOKEN = "GOOGLE_AUTH_TOKEN";

	private class Syncronize {

		/**
		 * Syncronizes Databases
		 * 
		 * @throws Exception
		 */
		public boolean syncronizeDatabases() throws ServiceException, Exception {
			if (googleCalendar == null) {
				return false;
			}

			pullSync();
			pushSync();

			return true;
		}

		/**
		 * Pull Sync remote tasks to local
		 * 
		 * @throws UnknownHostException
		 * @throws ServiceException
		 * 
		 * @throws Exception
		 */
		private void pullSync() throws UnknownHostException {
			List<CalendarEventEntry> googleCalendarEvents;
			try {
				googleCalendarEvents = googleCalendar.retrieveEvents(
						syncStartDateTime.toString(),
						syncEndDateTime.toString());
				Iterator<CalendarEventEntry> iterator = googleCalendarEvents
						.iterator();

				// pull sync remote tasks
				while (iterator.hasNext()) {
					CalendarEventEntry gCalEntry = iterator.next();
					pullSyncTask(gCalEntry);
				}

			} catch (UnknownHostException e) {
				e.printStackTrace();
				throw e;
			} catch (ServiceException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			} catch (IOException e) {
			} catch (Exception e) {
				e.printStackTrace();
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
				MhsLogger.getLogger()
						.log(Level.INFO, "Deleting cancelled task");

				if (googleCalendar.isDeleted(gCalEntry)) {
					// delete local task
					deleteTaskInTaskList(gCalTaskList.get(gCalEntry
							.getIcalUID()));
					return;
				}

				// pull sync newer task
				if (localTask.getTaskLastSync().isBefore(
						new DateTime(gCalEntry.getUpdated().getValue()))) {
					pullSyncExistingTask(gCalEntry, localTask);
				}

			} else {
				// Skip deleted events
				if (googleCalendar.isDeleted(gCalEntry)) {
					return;
				}
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
			MhsLogger.getLogger().log(Level.INFO, "pulling new event");

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
		 * @throws Exception
		 */
		private void pullSyncExistingTask(CalendarEventEntry gCalEntry,
				Task localTaskEntry) throws Exception {

			MhsLogger.getLogger().log(Level.INFO,
					"pulling newer event : " + localTaskEntry.getTaskName());

			updateSyncTask(localTaskEntry, gCalEntry);
		}

		/**
		 * Pushes local Deadline and Timed tasks to remote Syncs deleted local
		 * tasks, new tasks and updated existing tasks
		 * 
		 * @throws IOException
		 * @throws ServiceException
		 */
		private void pushSync() throws IOException, UnknownHostException,
				ServiceException {
			// push sync tasks from local to google calendar
			for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {
				try {
					pushSyncTask(entry.getValue());
				} catch (ServiceException e) {
					MhsLogger.getLogger().log(Level.FINE, e.getMessage());
					e.printStackTrace();
				} catch (Exception e) {
					MhsLogger.getLogger().log(Level.FINE, e.getMessage());
					e.printStackTrace();
				}
			}
		}

		/**
		 * Push sync new or existing new tasks
		 * 
		 * @param localTask
		 * @throws Exception
		 */
		private void pushSyncTask(Task localTask) throws Exception {

			// skip floating tasks
			if (localTask.getTaskCategory().equals(TaskCategory.FLOATING)) {
				return;
			}
			// remove deleted task
			if (localTask.isDeleted()) {
				MhsLogger.getLogger().log(Level.INFO,
						"Removing deleted synced task");
				googleCalendar.deleteEvent(localTask.getgCalTaskId());
				return;
			}

			// add unsynced tasks
			if (isUnsyncedTask(localTask)) {
				MhsLogger.getLogger().log(Level.INFO, "Pushing new sync task");
				pushSyncNewTask(localTask);
			} else {
				// add updated tasks
				if (localTask.getTaskUpdated().isAfter(
						localTask.getTaskLastSync())) {
					MhsLogger.getLogger().log(Level.INFO,
							"Pushing updated task");
					pushSyncExistingTask(localTask);
				}
			}
		}

		/**
		 * Push task that is currently not synced. Call pushSyncTask to sync
		 * tasks instead as it contains sync validation logic.
		 * 
		 * @param localTask
		 * @throws Exception
		 */
		private void pushSyncNewTask(Task localTask) throws Exception {

			// adds event to google calendar
			CalendarEventEntry addedGCalEvent = googleCalendar.createEvent(
					localTask.getTaskName(), localTask.getStartDateTime()
							.toString(), localTask.getEndDateTime().toString());

			updateSyncTask(localTask, addedGCalEvent);
		}

		/**
		 * Push existing synced task Call pushSyncTask to sync task as it
		 * handles sync logic Call pushSyncTask to sync tasks instead as it
		 * contains sync validation logic.
		 * 
		 * @param localTask
		 * @throws Exception
		 */
		private void pushSyncExistingTask(Task localTask) throws Exception {

			// update remote task
			CalendarEventEntry updatedGcalEvent = googleCalendar
					.updateEvent(localTask.clone());

			updateSyncTask(localTask, updatedGcalEvent);
		}

		/**
		 * Updates local synced task with newer Calendar Event
		 * 
		 * @param updatedTask
		 * @throws Exception
		 * @throws ServiceException
		 */
		private Task updateSyncTask(Task localSyncTaskToUpdate,
				CalendarEventEntry UpdatedCalendarEvent) throws Exception {

			if (!taskExists(localSyncTaskToUpdate.getTaskId())) {
				throw new Exception(EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
			}

			if (!isTaskValid(localSyncTaskToUpdate)) {
				throw new Exception(EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
			}

			DateTime syncDateTime = setSyncTime(UpdatedCalendarEvent);

			// update local task
			localSyncTaskToUpdate.setgCalTaskId(UpdatedCalendarEvent
					.getIcalUID());
			localSyncTaskToUpdate.setTaskName(UpdatedCalendarEvent.getTitle()
					.getPlainText());
			localSyncTaskToUpdate.setStartDateTime(new DateTime(
					UpdatedCalendarEvent.getTimes().get(0).getStartTime()
							.toString()));
			localSyncTaskToUpdate.setEndDateTime(new DateTime(
					UpdatedCalendarEvent.getTimes().get(0).getEndTime()
							.toString()));
			localSyncTaskToUpdate.setTaskLastSync(syncDateTime);
			localSyncTaskToUpdate.setTaskUpdated(syncDateTime);

			updateTaskLists(localSyncTaskToUpdate);
			return localSyncTaskToUpdate;
		}

		/**
		 * Sets sync time for google calendar entry
		 * 
		 * @param gCalEntry
		 * @return sync datetime for updating local task
		 */
		private DateTime setSyncTime(CalendarEventEntry gCalEntry) {
			new DateTime();
			DateTime syncDateTime = DateTime.now();

			if (gCalEntry != null) {
				gCalEntry.setUpdated(com.google.gdata.data.DateTime.now());
				syncDateTime = new DateTime(gCalEntry.getUpdated().toString());
			}

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
		initializeSyncDateTimes();
		initalizeDatabase();
		syncronizeDatabases();

		MhsLogger.getLogger().log(Level.INFO, "Database Initialized");
	}

	private void initializeSyncDateTimes() {
		syncStartDateTime = DateTime.now().toDateMidnight().toDateTime();
		syncEndDateTime = DateTime.now().plusMonths(12).toDateMidnight()
				.toDateTime();
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
		initializeSyncDateTimes();
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

		configFile = new ConfigFile();
		taskRecordFile = new TaskRecordFile(taskRecordFileName);
		taskList = taskRecordFile.getTaskList();
		gCalTaskList = taskRecordFile.getGCalTaskList();

		syncronize = new Syncronize();
		if (initializeGoogleCalendarService()) {
			enableRemoteSync();
		} else {
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

		configFile = new ConfigFile();

		taskRecordFile = new TaskRecordFile();
		taskList = taskRecordFile.getTaskList();
		gCalTaskList = taskRecordFile.getGCalTaskList();

		syncronize = new Syncronize();
		if (initializeGoogleCalendarService()) {
			enableRemoteSync();
		} else {
			disableRemoteSync();
		}
	}

	private void enableRemoteSync() {
		isRemoteSyncEnabled = true;
		syncronizePullSyncTimer();
	}

	private void disableRemoteSync() {
		isRemoteSyncEnabled = false;

		if (pullSyncTimedTask != null) {
			pullSyncTimedTask.cancel();
		}
	}

	private void syncronizePullSyncTimer() {
		DatabaseServiceTimer = new Timer();
		pullSyncTimedTask = new TimerTask() {
			@Override
			public void run() {
				MhsLogger.getLogger().log(Level.INFO, "Timed pull-sync");
				try {
					syncronize.pullSync();
				} catch (UnknownHostException e) {
					disableRemoteSync();
				} catch (Exception e) {
				}
			}
		};

		// schedule pull-sync every 5 minutes
		DatabaseServiceTimer.schedule(pullSyncTimedTask, SYNC_TIMER_DELAY,
				SYNC_TIMER_PERIOD);
	}

	/**
	 * Initializes Google Calendar Service with saved access token
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	private boolean initializeGoogleCalendarService() throws IOException {
		if (!configFile.hasConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN)
				|| configFile
						.getConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN)
						.isEmpty()) {
			return false;
		}
		if (!configFile.hasConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT)
				|| configFile.getConfigParameter(
						CONFIG_PARAM_GOOGLE_USER_ACCOUNT).isEmpty()) {
			return false;
		}
		googleCalendar = new GoogleCalendar(
				GOOGLE_CALENDAR_APP_NAME,
				configFile.getConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT),
				configFile.getConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN));
		saveGoogleAccountInfo();
		return true;
	}

	/**
	 * Logs in user google account with user details
	 * 
	 * @param userName
	 * @param userPassword
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void authenticateUserGoogleAccount(String userName,
			String userPassword) throws IOException, ServiceException {

		disableRemoteSync();

		String googleAccessToken = GoogleCalendar.retrieveUserToken(
				GOOGLE_CALENDAR_APP_NAME, userName, userPassword);
		googleCalendar = new GoogleCalendar(GOOGLE_CALENDAR_APP_NAME, userName,
				googleAccessToken);

		enableRemoteSync();
		saveGoogleAccountInfo();
	}

	/**
	 * Logs user out of Google Account
	 * 
	 * @throws IOException
	 */
	public void logOutUserGoogleAccount() throws IOException {
		disableRemoteSync();
		configFile.setConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN, null);
		configFile.setConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT, null);
		configFile.save();
	}

	/**
	 * Saves user Google Calendar Service access token and user google account
	 * email to config file
	 * 
	 * @throws IOException
	 */
	private void saveGoogleAccountInfo() throws IOException {
		String googleAuthToken = googleCalendar.getUserToken();
		String googleUserAccount = googleCalendar.getUserEmail();

		if (googleAuthToken != null) {
			configFile.setConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN,
					googleAuthToken);
		}

		if (googleUserAccount != null) {
			configFile.setConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT,
					googleUserAccount);
		}
		configFile.save();
	}

	// TODO BATCH OPERATIONS FOR DATABASE

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
			disableRemoteSync();
			throw e;
		} catch (Exception e) {
		}
	}

	/**
	 * Returns List of all tasks (exclusive of deleted tasks)
	 * 
	 * @return List of all Tasks
	 * @throws IOException
	 */
	public List<Task> query() throws IOException {
		List<Task> queryTaskList = new LinkedList<Task>();
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {

			if (entry.getValue().isDeleted()) {
				continue;
			}

			queryTaskList.add(entry.getValue().clone());
		}
		return queryTaskList;
	}

	/**
	 * Returns single task with specified taskId (deleted tasks queriable)
	 * 
	 * @param taskId
	 * @return matched task
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
	 * (exclusive of deleted tasks)
	 * 
	 * @param taskName
	 * @return list of matched tasks
	 */
	public List<Task> query(String taskName) {
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
		return queriedTaskRecordset;
	}

	/**
	 * Returns tasks that match specified TaskCategory (exclusive of deleted
	 * tasks)
	 * 
	 * @param queryTaskCategory
	 * @return list of matched tasks
	 */
	public List<Task> query(TaskCategory queryTaskCategory) {
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
		return queriedTaskRecordset;
	}

	/**
	 * Returns tasks that is within startTime or endTime inclusive (exclusive of
	 * deleted tasks)
	 * 
	 * @param startTime
	 * @param endTime
	 * @return list of matched tasks
	 */
	public List<Task> query(DateTime startTime, DateTime endTime) {
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

		return queriedTaskRecordset;
	}

	/**
	 * Returns task that matches any of the specified parameters (exclusive of
	 * deleted tasks)
	 * 
	 * @param queriedTaskName
	 * @param queriedStartTime
	 * @param queriedEndTime
	 * @return list of matched tasks
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
	 * Returns task that matches any of the specified parameters (exclusive of
	 * deleted tasks)
	 * 
	 * @param queriedTaskName
	 * @param queriedTaskCategory
	 * @param queriedStartTime
	 * @param queriedEndTime
	 * @return list of matched tasks
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
	public Task add(Task task) throws Exception {

		if (!isTaskValid(task)) {
			throw new Exception(EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		}

		Task taskToAdd = task.clone();
		addTaskToTaskList(taskToAdd);

		if (isRemoteSyncEnabled) {
			syncronize.pushSyncTask(taskToAdd);
		}

		saveTaskRecordFile();

		return taskToAdd;
	}

	private void addTaskToTaskList(Task taskToAdd) {
		taskToAdd.setTaskId(getNewTaskId());
		DateTime UpdateTime = new DateTime();
		UpdateTime = DateTime.now();

		taskToAdd.setTaskCreated(UpdateTime);
		taskToAdd.setTaskUpdated(UpdateTime);

		taskToAdd.setTaskLastSync(null);
		taskToAdd.setgCalTaskId(null);

		updateTaskLists(taskToAdd);
	}

	/**
	 * Undeletes a task (deprecated due to Google Calendar Sync, manually re-add
	 * task instead)
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
		undeleteTaskInTaskList(taskToUndelete);

		// TODO undelete google calendar task
		if (isRemoteSyncEnabled) {
			syncronize.pushSyncTask(taskToUndelete);
		}

		saveTaskRecordFile();
	}

	private void undeleteTaskInTaskList(Task taskToUndelete) {
		taskToUndelete.setDeleted(false);
		DateTime UpdateTime = new DateTime();
		UpdateTime = DateTime.now().plusMinutes(1);
		// set updated time further ahead to force sync (timing issues)
		taskToUndelete.setTaskUpdated(UpdateTime.plusMinutes(1));
		updateTaskLists(taskToUndelete);
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
		deleteTaskInTaskList(taskToDelete);

		if (isRemoteSyncEnabled) {
			syncronize.pushSyncTask(taskToDelete);
		}

		saveTaskRecordFile();

	}

	private void deleteTaskInTaskList(Task taskToDelete) {
		taskToDelete.setDeleted(true);
		DateTime UpdateTime = new DateTime();
		UpdateTime = DateTime.now().plusMinutes(1);
		// set updated time further ahead to force sync (timing issues)
		taskToDelete.setTaskUpdated(UpdateTime);
		updateTaskLists(taskToDelete);
	}

	/**
	 * Updates task
	 * 
	 * @param updatedTask
	 * @throws Exception
	 * @throws ServiceException
	 */
	public Task update(Task updatedTask) throws Exception {

		if (!taskExists(updatedTask.getTaskId())) {
			throw new Exception(EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		if (!isTaskValid(updatedTask)) {
			throw new Exception(EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		}

		Task updatedTaskToSave = updatedTask.clone();

		// Preserve non-editable fields
		Task currentTask = query(updatedTaskToSave.getTaskId());
		updatedTaskToSave.setgCalTaskId(currentTask.getgCalTaskId());
		updatedTaskToSave.setTaskCreated(currentTask.getTaskCreated());
		updatedTaskToSave.setTaskLastSync(currentTask.getTaskLastSync());

		updateTaskinTaskList(updatedTaskToSave);

		if (isRemoteSyncEnabled) {
			syncronize.pushSyncTask(updatedTaskToSave);
		}

		saveTaskRecordFile();

		return updatedTaskToSave;
	}

	private void updateTaskinTaskList(Task updatedTaskToSave) {
		DateTime UpdateTime = new DateTime();
		UpdateTime = DateTime.now();
		// set updated time further ahead to force sync (timing issues)
		updatedTaskToSave.setTaskUpdated(UpdateTime.plusMinutes(1));
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

	@SuppressWarnings("unused")
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
	 * Clears all databases
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void clearDatabase() throws IOException, ServiceException {
		clearTaskLists();
		clearRemoteDatabase();
		saveTaskRecordFile();
	}

	/**
	 * Clear local database
	 * 
	 * @throws IOException
	 */
	public void clearLocalDatabase() throws IOException {
		clearTaskLists();
		saveTaskRecordFile();
	}

	/**
	 * Clears remote database
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void clearRemoteDatabase() throws IOException, ServiceException {
		if (isRemoteSyncEnabled) {
			googleCalendar.deleteEvents(syncStartDateTime.toString(), syncEndDateTime.toString());
		}
	}

	private void updateTaskLists(Task task) {
		taskList.put(task.getTaskId(), task);
		if (task.getgCalTaskId() != null && !task.getgCalTaskId().isEmpty()) {
			gCalTaskList.put(task.getgCalTaskId(), task);
		}
	}

	/**
	 * Checks if Task is valid for given format
	 * 
	 * @param task
	 * @return boolean
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

	private void clearTaskLists() {
		taskList.clear();
		gCalTaskList.clear();
	}

	private boolean taskExists(String gCalTaskId) {
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
	 * Checks whether task is unsynced
	 * 
	 * @param localTask
	 * @return true if task is unsynced
	 */
	private static boolean isUnsyncedTask(Task localTask) {
		return localTask.getgCalTaskId() == null
				|| localTask.getTaskLastSync() == null;
	}

	/**
	 * Returns new taskId - (unique incremental)
	 * 
	 * @return
	 */
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

	/**
	 * Checks if Remote Sync (Google Calendar) is active
	 * 
	 * @return
	 */
	public boolean isUserGoogleCalendarAuthenticated() {
		return isRemoteSyncEnabled;
	}
}