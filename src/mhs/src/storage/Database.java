/**
 * Database 
 * 
 * - Database interfaces persistent the data storage mechanism, on local disk and remote 
 * (Google Calendar Service). 
 * - Handles task queries and CRUD operations 
 * - Handles user configuration setting operations
 * 
 * @author timlyw
 */

package mhs.src.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import mhs.src.common.MhsLogger;

import org.joda.time.DateTime;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.util.ServiceException;

public class Database {

	private static final String GOOGLE_CALENDAR_APP_NAME = "My Hot Secretary";
	private Syncronize syncronize;
	private GoogleCalendar googleCalendar;
	private TaskRecordFile taskRecordFile;
	private ConfigFile configFile;

	// Data Views - contains Task objects references
	private TaskLists taskLists;

	private DateTime syncStartDateTime;
	private DateTime syncEndDateTime;
	private boolean isRemoteSyncEnabled = true;

	// Exception Messages
	private static final String EXCEPTION_MESSAGE_INVALID_TASK_FORMAT = "Invalid Task Format";
	private static final String EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST = "Task does not exist";

	// Config parameters
	private static final String CONFIG_PARAM_GOOGLE_USER_ACCOUNT = "GOOGLE_USER_ACCOUNT";
	private static final String CONFIG_PARAM_GOOGLE_AUTH_TOKEN = "GOOGLE_AUTH_TOKEN";

	/**
	 * Handles Syncronization operations between local storage and Google
	 * Calendar Service.
	 * 
	 * @author timlyw
	 */
	private class Syncronize {

		private Timer DatabaseServiceTimer;
		private TimerTask pullSyncTimedTask;
		private static final int SYNC_TIMER_DEFAULT_PERIOD = 300000;
		private static final int SYNC_TIMER_DEFAULT_DELAY = 300000;

		private Syncronize() throws IOException {
			if (initializeGoogleCalendarService()) {
				enableRemoteSync();
			} else {
				disableRemoteSync();
			}
		}

		/**
		 * Syncronizes Databases
		 * 
		 * @throws Exception
		 */
		private boolean syncronizeDatabases() throws ServiceException,
				Exception {

			if (googleCalendar == null) {
				return false;
			}
			
			pullSync();
			pushSync();
			return true;
		}

		/**
		 * Enables remote sync for task operations and auto-sync
		 */
		private void enableRemoteSync() {
			isRemoteSyncEnabled = true;
			syncronizePullSyncTimer(SYNC_TIMER_DEFAULT_PERIOD);
		}

		/**
		 * Disables remote sync for task operations and auto-sync
		 */
		private void disableRemoteSync() {
			isRemoteSyncEnabled = false;

			cancelTimedPullSync();
		}

		/**
		 * Automated Pull Sync Timed Task
		 * 
		 * @param syncPeriodInMilis
		 */
		private void syncronizePullSyncTimer(int syncPeriodInMilis) {
			DatabaseServiceTimer = new Timer();

			cancelTimedPullSync();

			pullSyncTimedTask = new TimerTask() {
				@Override
				public void run() {
					MhsLogger.getLogger().log(Level.INFO, "Timed pull-sync");
					try {
						syncronize.pullSync();
					} catch (UnknownHostException e) {
						MhsLogger.getLogger().log(Level.FINER, e.getMessage());
						disableRemoteSync();
					} catch (Exception e) {
						MhsLogger.getLogger().log(Level.FINER, e.getMessage());
					}
				}
			};

			// schedule pull-sync
			DatabaseServiceTimer.schedule(pullSyncTimedTask,
					SYNC_TIMER_DEFAULT_DELAY, syncPeriodInMilis);
		}

		private void cancelTimedPullSync() {
			// Cancel timed task if it exists
			if (pullSyncTimedTask != null) {
				pullSyncTimedTask.cancel();
			}
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
				throw e;
			} catch (ServiceException e) {
				MhsLogger.getLogger().log(Level.FINER, e.getMessage());
			} catch (NullPointerException e) {
				MhsLogger.getLogger().log(Level.FINER, e.getMessage());
			} catch (IOException e) {
				MhsLogger.getLogger().log(Level.FINER, e.getMessage());
			} catch (Exception e) {
				MhsLogger.getLogger().log(Level.FINER, e.getMessage());
			}
		}

		/**
		 * Pull sync new or existing task from remote
		 * 
		 * @param gCalEntry
		 * @throws Exception
		 */
		private void pullSyncTask(CalendarEventEntry gCalEntry)
				throws UnknownHostException, Exception {

			if (taskLists.containsSyncTask(gCalEntry.getIcalUID())) {

				Task localTask = taskLists.getSyncTask(gCalEntry.getIcalUID());

				// pull sync deleted events
				if (googleCalendar.isDeleted(gCalEntry)) {
					MhsLogger.getLogger().log(
							Level.INFO,
							"Deleting cancelled task : "
									+ gCalEntry.getTitle().getPlainText());
					// delete local task
					deleteTaskInTaskList(localTask);
					return;
				}
				// pull sync newer task
				if (localTask.getTaskLastSync().isBefore(
						new DateTime(gCalEntry.getUpdated().getValue()))) {
					MhsLogger.getLogger().log(Level.INFO,
							"pulling newer event : " + localTask.getTaskName());
					System.out.println(localTask.getTaskLastSync() + " " + gCalEntry.getUpdated().toString());
					pullSyncExistingTask(gCalEntry, localTask);
				}

			} else {

				// Skip deleted events
				if (googleCalendar.isDeleted(gCalEntry)) {
					return;
				}

				MhsLogger.getLogger().log(
						Level.INFO,
						"pulling new event : "
								+ gCalEntry.getTitle().getPlainText());
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
				throws UnknownHostException, Exception {

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
				Task localTaskEntry) throws UnknownHostException, Exception {

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
			for (Map.Entry<Integer, Task> entry : taskLists.getTaskList()
					.entrySet()) {
				try {
					pushSyncTask(entry.getValue());
				} catch (UnknownHostException e) {
					throw e;
				} catch (ServiceException e) {
					MhsLogger.getLogger().log(Level.FINE, e.getMessage());
				} catch (Exception e) {
					MhsLogger.getLogger().log(Level.FINE, e.getMessage());
				}
			}
		}

		/**
		 * Push sync new or existing new tasks
		 * 
		 * @param localTask
		 * @throws Exception
		 */
		private void pushSyncTask(Task localTask) throws UnknownHostException,
				Exception {

			// skip floating tasks
			if (localTask.getTaskCategory().equals(TaskCategory.FLOATING)) {
				return;
			}
			// remove deleted task
			if (localTask.isDeleted()) {
				MhsLogger.getLogger().log(
						Level.INFO,
						"Removing deleted synced task : "
								+ localTask.getTaskName());
				googleCalendar.deleteEvent(localTask.getgCalTaskId());
				return;
			}

			// add unsynced tasks
			if (isUnsyncedTask(localTask)) {
				MhsLogger.getLogger().log(Level.INFO,
						"Pushing new sync task : " + localTask.getTaskName());
				pushSyncNewTask(localTask);
			} else {
				// add updated tasks
				if (localTask.getTaskUpdated().isAfter(
						localTask.getTaskLastSync())) {
					MhsLogger.getLogger()
							.log(Level.INFO,
									"Pushing updated task : "
											+ localTask.getTaskName());
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
		private void pushSyncNewTask(Task localTask)
				throws UnknownHostException, Exception {

			// adds event to google calendar
			CalendarEventEntry addedGCalEvent = googleCalendar.createEvent(
					localTask.getTaskName(), localTask.getStartDateTime()
							.toString(), localTask.getEndDateTime().toString());

			updateSyncTask(localTask, addedGCalEvent);
		}

		/**
		 * Push existing synced task. Call pushSyncTask to sync tasks instead as
		 * it contains sync validation logic.
		 * 
		 * @param localTask
		 * @throws Exception
		 */
		private void pushSyncExistingTask(Task localTask)
				throws UnknownHostException, Exception {

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

			if (!taskLists.containsTask(localSyncTaskToUpdate.getTaskId())) {
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
		// syncronize local and remote databases
		if (disableSyncronize) {
			syncronize.disableRemoteSync();
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
		taskLists = new TaskLists(taskRecordFile.getTaskList());
		syncronize = new Syncronize();
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
		taskLists = new TaskLists(taskRecordFile.getTaskList());
		syncronize = new Syncronize();
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

		syncronize.disableRemoteSync();

		String googleAccessToken = GoogleCalendar.retrieveUserToken(
				GOOGLE_CALENDAR_APP_NAME, userName, userPassword);
		googleCalendar = new GoogleCalendar(GOOGLE_CALENDAR_APP_NAME, userName,
				googleAccessToken);

		syncronize.enableRemoteSync();
		saveGoogleAccountInfo();
	}

	/**
	 * Logs user out of Google Account
	 * 
	 * @throws IOException
	 */
	public void logOutUserGoogleAccount() throws IOException {
		syncronize.disableRemoteSync();
		googleCalendar = null;
		configFile.setConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN, null);
		configFile.setConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT, null);
		configFile.save();
	}

	/**
	 * Checks if Remote Sync (Google Calendar) is currently active
	 * 
	 * @return
	 */
	public boolean isUserGoogleCalendarAuthenticated() {
		return isRemoteSyncEnabled;
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

	/**
	 * Syncronizes Databases
	 * 
	 * @throws ServiceException
	 * @throws UnknownHostException
	 * 
	 * @throws IOException
	 */
	public void syncronizeDatabases() throws UnknownHostException,
			ServiceException {
		try {
			syncronize.syncronizeDatabases();
			saveTaskRecordFile();
		} catch (UnknownHostException e) {
			syncronize.disableRemoteSync();
			throw e;
		} catch (ServiceException e) {
			MhsLogger.getLogger().log(Level.FINER, e.getMessage());
			throw e;
		} catch (Exception e) {
			MhsLogger.getLogger().log(Level.FINER, e.getMessage());
		}
	}

	/**
	 * Returns single task with specified taskId (deleted tasks queriable)
	 * 
	 * @param taskId
	 * @return matched task
	 * @throws Exception
	 */
	public Task query(int taskId) throws Exception {
		if (!taskLists.containsTask(taskId)) {
			throw new Exception(EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}
		return taskLists.getTask(taskId);
	}

	/**
	 * Returns List of all tasks (exclusive of deleted tasks)
	 * 
	 * @param orderByStartDateTime
	 * @return List of all Tasks
	 * @throws IOException
	 */
	public List<Task> query(boolean orderByStartDateTime) throws IOException {

		return taskLists.getTasks(orderByStartDateTime);

	}

	/**
	 * Return tasks with matching taskName, case-insensitive substring search
	 * (exclusive of deleted tasks)
	 * 
	 * @param orderByStartDateTime
	 * @param taskName
	 * @return list of matched tasks
	 */
	public List<Task> query(String taskName, boolean orderByStartDateTime) {
		return taskLists.getTasks(taskName, orderByStartDateTime);
	}

	/**
	 * Returns tasks that match specified TaskCategory (exclusive of deleted
	 * tasks)
	 * 
	 * @param orderByStartDateTime
	 * @param queryTaskCategory
	 * @return list of matched tasks
	 */
	public List<Task> query(TaskCategory queryTaskCategory,
			boolean orderByStartDateTime) {
		return taskLists.getTasks(queryTaskCategory, orderByStartDateTime);
	}

	/**
	 * Returns tasks that is within startDateTime or endDateTime inclusive
	 * (exclusive of deleted tasks)
	 * 
	 * @param orderByStartDateTime
	 * @param startDateTime
	 * @param endDateTime
	 * @return list of matched tasks
	 */
	public List<Task> query(DateTime startDateTime, DateTime endDateTime,
			boolean orderByStartDateTime) {
		return taskLists.getTasks(startDateTime, endDateTime,
				orderByStartDateTime);
	}

	/**
	 * Returns task that matches any of the specified parameters (exclusive of
	 * deleted tasks)
	 * 
	 * @param orderByStartDateTime
	 * @param taskName
	 * @param startDateTime
	 * @param endDateTime
	 * @return list of matched tasks
	 */
	public List<Task> query(String taskName, DateTime startDateTime,
			DateTime endDateTime, boolean orderByStartDateTime) {
		return taskLists.getTasks(taskName, startDateTime, endDateTime,
				orderByStartDateTime);
	}

	/**
	 * Returns task that matches any of the specified parameters (exclusive of
	 * deleted tasks)
	 * 
	 * @param orderByStartDateTime
	 * @param taskName
	 * @param taskCategory
	 * @param startDateTime
	 * @param endDateTime
	 * @return list of matched tasks
	 */
	public List<Task> query(String taskName, TaskCategory taskCategory,
			DateTime startDateTime, DateTime endDateTime,
			boolean orderByStartDateTime) {

		return taskLists.getTasks(taskName, taskCategory, startDateTime,
				endDateTime, orderByStartDateTime);
	}

	/**
	 * Adds a task to local storage and remote (if authenticated and connection
	 * exists)
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

	/**
	 * Add task to task list
	 * 
	 * @param taskToAdd
	 */
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

		if (!taskLists.containsTask(taskId)) {
			throw new Exception(EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		Task taskToUndelete = taskLists.getTask(taskId);
		undeleteTaskInTaskList(taskToUndelete);

		if (isRemoteSyncEnabled) {
			syncronize.pushSyncTask(taskToUndelete);
		}

		saveTaskRecordFile();
	}

	/**
	 * Undelete task in task list by setting deleted attribute
	 * 
	 * @param taskToUndelete
	 */
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

		if (!taskLists.containsTask(taskId)) {
			throw new Exception(EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		Task taskToDelete = taskLists.getTask(taskId);
		deleteTaskInTaskList(taskToDelete);

		if (isRemoteSyncEnabled) {
			syncronize.pushSyncTask(taskToDelete);
		}

		saveTaskRecordFile();
	}

	/**
	 * Deletes task in task list by setting deleted attribute
	 * 
	 * @param taskToDelete
	 */
	private void deleteTaskInTaskList(Task taskToDelete) {

		taskToDelete.setDeleted(true);

		// set updated time further ahead to force sync (timing issues)
		DateTime UpdateTime = new DateTime();
		UpdateTime = DateTime.now().plusMinutes(1);
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

		if (!taskLists.containsTask(updatedTask.getTaskId())) {
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

	/**
	 * Updates task in task list
	 * 
	 * @param updatedTaskToSave
	 */
	private void updateTaskinTaskList(Task updatedTaskToSave) {
		// set updated time further ahead to force sync (timing issues)
		DateTime UpdateTime = new DateTime();
		UpdateTime = DateTime.now();
		updatedTaskToSave.setTaskUpdated(UpdateTime.plusMinutes(1));

		updateTaskLists(updatedTaskToSave);
	}

	/**
	 * Save Tasks to local file
	 * 
	 * @throws IOException
	 */
	private void saveTaskRecordFile() throws IOException {
		taskRecordFile.saveTaskList(taskLists.getTaskList());
	}

	/**
	 * Removes task from list
	 * 
	 * @param taskId
	 * @throws Exception
	 */
	private void removeRecord(Task taskToRemove) throws Exception {
		if (!taskLists.containsTask(taskToRemove.getTaskId())) {
			throw new Exception(EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}
		taskLists.removeTaskInTaskLists(taskToRemove);
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
		for (Map.Entry<Integer, Task> entry : taskLists.getTaskList()
				.entrySet()) {

			if (!entry.getValue().isDeleted()) {
				continue;
			}

			switch (entry.getValue().getTaskCategory()) {
			case TIMED:
			case DEADLINE:
				if (entry.getValue().getEndDateTime().isAfterNow()) {
					removeRecord(entry.getValue());
				}
				break;
			case FLOATING:
				if (entry.getValue().isDone()) {
					removeRecord(entry.getValue());
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
		taskLists.clearTaskLists();
		clearRemoteDatabase();
		saveTaskRecordFile();
	}

	/**
	 * Clear local database
	 * 
	 * @throws IOException
	 */
	public void clearLocalDatabase() throws IOException {
		taskLists.clearTaskLists();
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
			googleCalendar.deleteEvents(syncStartDateTime.toString(),
					syncEndDateTime.toString());
		}
	}

	/**
	 * Update Task Lists
	 * 
	 * @param task
	 */
	private void updateTaskLists(Task task) {
		taskLists.updateTaskInTaskLists(task);
	}

	/**
	 * Checks if Task format is valid for given type
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
		int getNewTaskId = 0;
		Set<Integer> taskKeySet = taskLists.getTaskList().keySet();
		Iterator<Integer> iterator = taskKeySet.iterator();
		while (iterator.hasNext()) {
			getNewTaskId = iterator.next();
		}
		getNewTaskId++;
		return getNewTaskId;
	}
}