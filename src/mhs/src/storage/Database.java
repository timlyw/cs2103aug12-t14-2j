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
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

import org.joda.time.DateTime;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.ResourceNotFoundException;
import com.google.gdata.util.ServiceException;

public class Database {

	private static GoogleCalendarMhs googleCalendar;
	private static TaskRecordFile taskRecordFile;
	private static ConfigFile configFile;
	private static TaskLists taskLists;
	private static Syncronize syncronize;

	private static DateTime syncStartDateTime;
	private static DateTime syncEndDateTime;
	private static boolean isRemoteSyncEnabled = true;

	private static final Logger logger = MhsLogger.getLogger();

	// Exception Messages
	private static final String EXCEPTION_MESSAGE_INVALID_TASK_FORMAT = "Invalid Task Format!";
	private static final String EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST = "Task does not exist!";
	private static final String EXCEPTION_MESSAGE_NULL_PARAMETER = "%1$s cannot be null!";

	private static final String PARAMETER_TASK_RECORD_FILE_NAME = "taskRecordFileName";
	private static final String PARAMETER_TASK = "task";
	private static final String PARAMETER_TASK_NAME = "taskName";
	private static final String PARAMETERE_START_AND_END_DATE_TIMES = "start and end date times";

	// Config parameters
	private static final String CONFIG_PARAM_GOOGLE_USER_ACCOUNT = "GOOGLE_USER_ACCOUNT";
	private static final String CONFIG_PARAM_GOOGLE_AUTH_TOKEN = "GOOGLE_AUTH_TOKEN";

	private static final String GOOGLE_CALENDAR_APP_NAME = "My Hot Secretary";

	/**
	 * Handles Syncronization operations between local storage and Google
	 * Calendar Service as background task in a separate thread
	 * 
	 * Functions:
	 * 
	 * - Pull-Push Sync to syncronize local storage and Google Calendar Service
	 * 
	 * - Timed Pull Sync to run at set interval
	 * 
	 * - Pull Sync for single CRUD operation
	 * 
	 * - Push Sync for single CRUD operation
	 * 
	 * @author timlyw
	 */
	private class Syncronize {

		private ScheduledThreadPoolExecutor syncronizeBackgroundExecutor;
		private Runnable syncronizeBackgroundTask;
		private TimerTask pullSyncTimedBackgroundTask;
		private Future<?> futureSyncronizeBackgroundTask;

		private static final int PULL_SYNC_TIMER_DEFAULT_INITIAL_DELAY_IN_MINUTES = 5;
		private static final int PULL_SYNC_TIMER_DEFAULT_PERIOD_IN_MINUTES = 5;

		/**
		 * Default Constructor for Syncronize.
		 * 
		 * Sets up thread for background sync and initializes runnable tasks and
		 * Google Calendar Service.
		 * 
		 * @throws IOException
		 */
		private Syncronize() throws IOException {
			logEnterMethod("Syncronize");
			syncronizeBackgroundExecutor = new ScheduledThreadPoolExecutor(1);

			initializeRunnableTasks();
			if (initializeGoogleCalendarService()) {
				enableRemoteSync();
			} else {
				disableRemoteSync();
			}
			logExitMethod("Syncronize");
		}

		private void waitForSyncronizeBackgroundTaskToComplete(
				int maxExecutionTimeInSeconds) throws InterruptedException,
				ExecutionException, TimeoutException {
			logEnterMethod("waitForSyncronizeBackgroundTaskToComplete");
			logger.log(Level.INFO, "Waiting for background task to complete.");
			futureSyncronizeBackgroundTask.get(maxExecutionTimeInSeconds,
					TimeUnit.SECONDS);
			logExitMethod("waitForSyncronizeBackgroundTaskToComplete");
		}

		/**
		 * Syncronizes Databases (local storage and google calendar service)
		 * 
		 * Sets up pull-push syncronize as background task.
		 * 
		 * @throws ServiceException
		 * @throws UnknownHostException
		 * @throws IOException
		 * @throws InvalidTaskFormatException
		 * @throws TaskNotFoundException
		 * @return
		 */
		private boolean syncronizeDatabases() {
			logEnterMethod("syncronizeDatabases");
			// checks if user is logged out
			if (googleCalendar == null) {
				return false;
			}
			futureSyncronizeBackgroundTask = syncronizeBackgroundExecutor
					.submit(syncronizeBackgroundTask);
			logExitMethod("syncronizeDatabases");
			return true;
		}

		/**
		 * Enables remote sync for task operations and auto-sync
		 */
		private void enableRemoteSync() {
			logEnterMethod("enableRemoteSync");
			logger.log(Level.INFO, "Enabling remote sync");
			isRemoteSyncEnabled = true;
			scheduleTimedPullSync();
			logExitMethod("enableRemoteSync");
		}

		/**
		 * Disables remote sync for task operations and auto-sync
		 */
		private void disableRemoteSync() {
			logEnterMethod("disableRemoteSync");
			logger.log(Level.INFO, "Disabling remote sync");
			isRemoteSyncEnabled = false;
			cancelTimedPullSync();
			logExitMethod("disableRemoteSync");
		}

		/**
		 * Initialize runnable tasks
		 */
		private void initializeRunnableTasks() {
			logEnterMethod("initializeRunnableTasks");
			initializeSyncronizeRunnableTask();
			initializeTimedPullSyncRunnableTask();
			logExitMethod("initializeRunnableTasks");
		}

		/**
		 * Initialize Syncronize Runnable Task
		 */
		private void initializeSyncronizeRunnableTask() {
			logEnterMethod("initializeSyncronizeRunnableTask");
			syncronizeBackgroundTask = new Runnable() {
				@Override
				public void run() {
					try {
						logger.log(Level.INFO,
								"Executing syncronize background task");
						pullSync();
						pushSync();
						saveTaskRecordFile();
					} catch (UnknownHostException e) {
						logger.log(Level.FINER, e.getMessage());
						disableRemoteSync();
					} catch (TaskNotFoundException e) {
						logger.log(Level.FINER, e.getMessage());
					} catch (InvalidTaskFormatException e) {
						logger.log(Level.FINER, e.getMessage());
					} catch (NullPointerException e) {
						logger.log(Level.FINER, e.getMessage());
					} catch (IOException e) {
						logger.log(Level.FINER, e.getMessage());
					} catch (ServiceException e) {
						// SilentFailSync Policy
						logger.log(Level.FINER, e.getMessage());
					}
				}
			};
			logExitMethod("initializeSyncronizeRunnableTask");
		}

		/**
		 * Schedules timed pull-sync
		 */
		private void scheduleTimedPullSync() {
			logEnterMethod("scheduleTimedPullSync");
			syncronizeBackgroundExecutor
					.scheduleAtFixedRate(pullSyncTimedBackgroundTask,
							PULL_SYNC_TIMER_DEFAULT_INITIAL_DELAY_IN_MINUTES,
							PULL_SYNC_TIMER_DEFAULT_PERIOD_IN_MINUTES,
							TimeUnit.MINUTES);
			logExitMethod("scheduleTimedPullSync");
		}

		/**
		 * Initialize Timed Pull Sync Runnable Task
		 */
		private void initializeTimedPullSyncRunnableTask() {
			logEnterMethod("initializeTimedPullSyncRunnableTask");
			pullSyncTimedBackgroundTask = new TimerTask() {
				@Override
				public void run() {
					logger.log(Level.INFO, "Executing timed pull sync task");
					try {
						syncronize.pullSync();
						saveTaskRecordFile();
					} catch (UnknownHostException e) {
						logger.log(Level.FINER, e.getMessage());
						disableRemoteSync();
					} catch (TaskNotFoundException e) {
						// SilentFailSync Policy
						logger.log(Level.FINER, e.getMessage());
					} catch (InvalidTaskFormatException e) {
						// SilentFailSyncPolicy
						logger.log(Level.FINER, e.getMessage());
					} catch (IOException e) {
						logger.log(Level.FINER, e.getMessage());
					}
				}
			};
			logExitMethod("initializeTimedPullSyncRunnableTask");
		}

		/**
		 * Cancel timed pull sync
		 */
		private void cancelTimedPullSync() {
			logEnterMethod("cancelTimedPullSync");
			if (pullSyncTimedBackgroundTask != null) {
				pullSyncTimedBackgroundTask.cancel();
			}
			logExitMethod("cancelTimedPullSync");
		}

		/**
		 * Pull Sync remote tasks to local
		 * 
		 * @throws UnknownHostException
		 * @throws InvalidTaskFormatException
		 * @throws TaskNotFoundException
		 * @throws ServiceException
		 */
		private void pullSync() throws UnknownHostException,
				TaskNotFoundException, InvalidTaskFormatException {
			logEnterMethod("pullSync");
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
				logger.log(Level.FINER, e.getMessage());
				throw e;
			} catch (ServiceException e) {
				logger.log(Level.FINER, e.getMessage());
			} catch (NullPointerException e) {
				logger.log(Level.FINER, e.getMessage());
			} catch (IOException e) {
				logger.log(Level.FINER, e.getMessage());
			}
			logExitMethod("pullSync");
		}

		/**
		 * Pull sync new or existing task from remote
		 * 
		 * @param gCalEntry
		 * @throws TaskNotFoundException
		 * @throws InvalidTaskFormatException
		 * @throws IOException
		 */
		private void pullSyncTask(CalendarEventEntry gCalEntry)
				throws TaskNotFoundException, InvalidTaskFormatException,
				IOException {
			logEnterMethod("pullSyncTask");
			if (taskLists.containsSyncTask(gCalEntry.getIcalUID())) {

				Task localTask = taskLists.getSyncTask(gCalEntry.getIcalUID());

				// pull sync deleted event
				if (googleCalendar.isDeleted(gCalEntry)) {
					logger.log(Level.INFO, "Deleting cancelled task : "
							+ gCalEntry.getTitle().getPlainText());
					// delete local task
					deleteTaskInTaskList(localTask);
					return;
				}

				// pull sync newer task
				if (localTask.getTaskLastSync().isBefore(
						new DateTime(gCalEntry.getUpdated().getValue()))) {

					logger.log(Level.INFO,
							"pulling newer event : " + localTask.getTaskName());

					logger.log(Level.FINER, "Local Task last sync : "
							+ localTask.getTaskLastSync()
							+ " Google Event Last Updated : "
							+ gCalEntry.getUpdated().getValue());

					pullSyncExistingTask(gCalEntry, localTask);
				}
			} else {
				// Skip deleted events
				if (googleCalendar.isDeleted(gCalEntry)) {
					return;
				}
				logger.log(Level.INFO, "pulling new event : "
						+ gCalEntry.getTitle().getPlainText());
				pullSyncNewTask(gCalEntry);
			}
			logExitMethod("pullSyncTask");
		}

		/**
		 * Creates new synced task from remote. Call pullSyncTask as it contains
		 * sync validation logic.
		 * 
		 * @param gCalEntry
		 * @throws UnknownHostException
		 */
		private void pullSyncNewTask(CalendarEventEntry gCalEntry)
				throws UnknownHostException {
			logEnterMethod("pullSyncNewTask");
			DateTime syncDateTime = setSyncTime(gCalEntry);

			// add task from google calendar entry
			if (gCalEntry.getTimes().get(0).getStartTime()
					.equals(gCalEntry.getTimes().get(0).getEndTime())) {
				// create new deadline task
				Task newTask = new DeadlineTask(getNewTaskId(), gCalEntry,
						syncDateTime);
				taskLists.updateTaskInTaskLists(newTask);

			} else {
				// create new timed task
				Task newTask = new TimedTask(getNewTaskId(), gCalEntry,
						syncDateTime);
				taskLists.updateTaskInTaskLists(newTask);
			}
			logExitMethod("pullSyncNewTask");
		}

		/**
		 * Syncs existing local task with updated remote task Call pullSyncTask
		 * as it contains sync validation logic.
		 * 
		 * @param gCalEntry
		 * @param localTaskEntry
		 * @throws InvalidTaskFormatException
		 * @throws TaskNotFoundException
		 * @throws Exception
		 */
		private void pullSyncExistingTask(CalendarEventEntry gCalEntry,
				Task localTaskEntry) throws UnknownHostException,
				TaskNotFoundException, InvalidTaskFormatException {
			logEnterMethod("pullSyncExistingTask");
			updateSyncTask(localTaskEntry, gCalEntry);
			logExitMethod("pullSyncExistingTask");
		}

		/**
		 * Pushes local Deadline and Timed tasks to remote Syncs deleted local
		 * tasks, new tasks and updated existing tasks
		 * 
		 * @throws IOException
		 * @throws ServiceException
		 * @throws InvalidTaskFormatException
		 * @throws TaskNotFoundException
		 * @throws NullPointerException
		 */
		private void pushSync() throws IOException, UnknownHostException,
				ServiceException, NullPointerException, TaskNotFoundException,
				InvalidTaskFormatException {
			logEnterMethod("pushSync");
			// push sync tasks from local to google calendar
			for (Map.Entry<Integer, Task> entry : taskLists.getTaskList()
					.entrySet()) {
				pushSyncTask(entry.getValue());
			}
			logExitMethod("pushSync");
		}

		/**
		 * Push sync new or existing new tasks
		 * 
		 * @param localTask
		 * @throws NullPointerException
		 * @throws IOException
		 * @throws TaskNotFoundException
		 * @throws InvalidTaskFormatException
		 * @throws ServiceException
		 */
		private void pushSyncTask(Task localTask) throws NullPointerException,
				IOException, TaskNotFoundException, InvalidTaskFormatException,
				ServiceException {
			logEnterMethod("pushSyncTask");
			// skip floating tasks
			if (localTask.getTaskCategory().equals(TaskCategory.FLOATING)) {
				logExitMethod("pushSyncTask");
				return;
			}
			// remove deleted task
			if (localTask.isDeleted()) {
				logger.log(Level.INFO, "Removing deleted synced task : "
						+ localTask.getTaskName());
				try {
					googleCalendar.deleteEvent(localTask.getgCalTaskId());
				} catch (ResourceNotFoundException e) {
					// SilentFailSync Policy
					logger.log(Level.FINER, e.getMessage());
				} catch (NullPointerException e) {
					logger.log(Level.FINER, e.getMessage());
				}
				logExitMethod("pushSyncTask");
				return;
			}

			// add unsynced tasks
			if (isUnsyncedTask(localTask)) {
				logger.log(Level.INFO,
						"Pushing new sync task : " + localTask.getTaskName());
				pushSyncNewTask(localTask);
			} else {
				// add updated tasks
				if (localTask.getTaskUpdated().isAfter(
						localTask.getTaskLastSync())) {
					logger.log(Level.INFO, "Pushing updated task : "
							+ localTask.getTaskName());
					pushSyncExistingTask(localTask);
				}
			}

			logger.exiting(getClass().getName(),
					new Exception().getStackTrace()[0].getMethodName());
		}

		/**
		 * Push task that is currently not synced. Call pushSyncTask to sync
		 * tasks instead as it contains sync validation logic.
		 * 
		 * @param localTask
		 * @throws ServiceException
		 * @throws IOException
		 * @throws NullPointerException
		 * @throws InvalidTaskFormatException
		 * @throws TaskNotFoundException
		 * @throws Exception
		 */
		private void pushSyncNewTask(Task localTask)
				throws NullPointerException, IOException, ServiceException,
				TaskNotFoundException, InvalidTaskFormatException {
			logEnterMethod("pushSyncNewTask");
			// adds event to google calendar
			CalendarEventEntry addedGCalEvent = googleCalendar
					.createEvent(localTask);
			updateSyncTask(localTask, addedGCalEvent);
			logExitMethod("pushSyncNewTask");
		}

		/**
		 * Push existing synced task. Call pushSyncTask to sync tasks instead as
		 * it contains sync validation logic.
		 * 
		 * @param localTask
		 * @throws ServiceException
		 * @throws IOException
		 * @throws NullPointerException
		 * @throws InvalidTaskFormatException
		 * @throws TaskNotFoundException
		 * @throws Exception
		 */
		private void pushSyncExistingTask(Task localTask)
				throws NullPointerException, IOException, ServiceException,
				TaskNotFoundException, InvalidTaskFormatException {
			logEnterMethod("pushSyncExistingTask");
			// update remote task
			CalendarEventEntry updatedGcalEvent = googleCalendar
					.updateEvent(localTask.clone());
			updateSyncTask(localTask, updatedGcalEvent);
			logExitMethod("pushSyncExistingTask");
		}

		/**
		 * Updates local synced task with newer Calendar Event
		 * 
		 * @param updatedTask
		 * @throws Exception
		 * @throws ServiceException
		 */
		private Task updateSyncTask(Task localSyncTaskToUpdate,
				CalendarEventEntry UpdatedCalendarEvent)
				throws TaskNotFoundException, InvalidTaskFormatException {
			logEnterMethod("updateSyncTask");
			if (!taskLists.containsTask(localSyncTaskToUpdate.getTaskId())) {
				throw new TaskNotFoundException(
						EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
			}

			if (!isTaskValid(localSyncTaskToUpdate)) {
				throw new InvalidTaskFormatException(
						EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
			}

			DateTime syncDateTime = setSyncTime(UpdatedCalendarEvent);
			updateLocalSyncTask(localSyncTaskToUpdate, UpdatedCalendarEvent,
					syncDateTime);
			taskLists.updateTaskInTaskLists(localSyncTaskToUpdate);
			logExitMethod("updateSyncTask");
			return localSyncTaskToUpdate;
		}

		/**
		 * Updates local sync task
		 * 
		 * @param localSyncTaskToUpdate
		 * @param UpdatedCalendarEvent
		 * @param syncDateTime
		 */
		private void updateLocalSyncTask(Task localSyncTaskToUpdate,
				CalendarEventEntry UpdatedCalendarEvent, DateTime syncDateTime) {
			logEnterMethod("updateLocalSyncTask");
			// TODO check task conversion
			localSyncTaskToUpdate.setgCalTaskId(UpdatedCalendarEvent
					.getIcalUID());
			localSyncTaskToUpdate.setTaskName(UpdatedCalendarEvent.getTitle()
					.getPlainText());

			When eventTimes = UpdatedCalendarEvent.getTimes().get(0);

			// Update Task Category
			if (eventTimes.getStartTime().equals(eventTimes.getEndTime())) {
				localSyncTaskToUpdate.setTaskCategory(TaskCategory.DEADLINE);
			} else {
				localSyncTaskToUpdate.setTaskCategory(TaskCategory.TIMED);
			}

			// Update Times
			localSyncTaskToUpdate.setStartDateTime(new DateTime(eventTimes
					.getStartTime().getValue()));
			localSyncTaskToUpdate.setEndDateTime(new DateTime(eventTimes
					.getEndTime().getValue()));
			localSyncTaskToUpdate.setTaskLastSync(syncDateTime);
			localSyncTaskToUpdate.setTaskUpdated(syncDateTime);
			logExitMethod("updateLocalSyncTask");
		}

		/**
		 * Sets sync time for local tasks from google calendar entry
		 * 
		 * @param gCalEntry
		 * @return sync datetime for updating local task
		 */
		private DateTime setSyncTime(CalendarEventEntry gCalEntry) {
			logEnterMethod("setSyncTime");
			new DateTime();
			DateTime syncDateTime = DateTime.now();

			if (gCalEntry != null) {
				syncDateTime = new DateTime(gCalEntry.getUpdated().getValue());
			}

			// assert that sync DateTimes between local and remote are equal
			assert (syncDateTime.isEqual(new DateTime(gCalEntry.getUpdated()
					.toString())));
			logExitMethod("setSyncTime");
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
		logEnterMethod("Database");
		initializeSyncDateTimes();
		initalizeDatabase();
		syncronizeDatabases();
		logExitMethod("Database");
	}

	/**
	 * Database constructor
	 * 
	 * @param taskRecordFileName
	 * @throws IOException
	 * @throws ServiceException
	 */
	public Database(String taskRecordFileName, boolean disableSyncronize)
			throws IllegalArgumentException, IOException, ServiceException {
		logEnterMethod("Database");
		if (taskRecordFileName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_TASK_RECORD_FILE_NAME));
		}
		initializeSyncDateTimes();
		initalizeDatabase(taskRecordFileName);
		// syncronize local and remote databases
		if (disableSyncronize) {
			syncronize.disableRemoteSync();
		} else {
			syncronizeDatabases();
		}
		logExitMethod("Database");
	}

	private void initializeSyncDateTimes() {
		logEnterMethod("initializeSyncDateTimes");
		syncStartDateTime = DateTime.now().toDateMidnight().toDateTime();
		syncEndDateTime = DateTime.now().plusMonths(12).toDateMidnight()
				.toDateTime();
		logExitMethod("initializeSyncDateTimes");
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
		logEnterMethod("initalizeDatabase");
		configFile = new ConfigFile();
		taskRecordFile = new TaskRecordFile(taskRecordFileName);
		taskLists = new TaskLists(taskRecordFile.getTaskList());
		syncronize = new Syncronize();
		logExitMethod("initalizeDatabase");

	}

	/**
	 * Initialize database
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void initalizeDatabase() throws IOException {
		logEnterMethod("initalizeDatabase");
		configFile = new ConfigFile();
		taskRecordFile = new TaskRecordFile();
		taskLists = new TaskLists(taskRecordFile.getTaskList());
		syncronize = new Syncronize();
		logExitMethod("initalizeDatabase");
	}

	/**
	 * Initializes Google Calendar Service with saved access token
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	private boolean initializeGoogleCalendarService() throws IOException {
		logEnterMethod("initializeGoogleCalendarService");
		if (!configFile
				.hasNonEmptyConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN)) {
			logger.exiting(getClass().getName(),
					new Exception().getStackTrace()[0].getMethodName());
			return false;
		}
		if (!configFile
				.hasNonEmptyConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT)) {
			logger.exiting(getClass().getName(),
					new Exception().getStackTrace()[0].getMethodName());
			return false;
		}
		authenticateGoogleAccount(
				configFile.getConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT),
				configFile.getConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN));

		logExitMethod("initializeGoogleCalendarService");
		return true;
	}

	/**
	 * Authenticates user google account with account email and access token
	 * 
	 * @param googleUserAccount
	 * @param googleAuthToken
	 * @throws IOException
	 */
	private void authenticateGoogleAccount(String googleUserAccount,
			String googleAuthToken) throws IOException {
		logEnterMethod("authenticateGoogleAccount");
		assert (googleUserAccount != null);
		assert (googleAuthToken != null);

		try {
			googleCalendar = new GoogleCalendarMhs(GOOGLE_CALENDAR_APP_NAME,
					googleUserAccount, googleAuthToken);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		saveGoogleAccountInfo(googleUserAccount, googleAuthToken);
		logExitMethod("authenticateGoogleAccount");
	}

	/**
	 * Logs in user google account with user details
	 * 
	 * @param userName
	 * @param userPassword
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void loginUserGoogleAccount(String userName, String userPassword)
			throws IOException, ServiceException {
		logEnterMethod("loginUserGoogleAccount");
		if (userName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, "userName"));
		}
		if (userPassword == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, "userPassword"));
		}

		syncronize.disableRemoteSync();

		String googleAccessToken = GoogleCalendarMhs.retrieveUserToken(
				GOOGLE_CALENDAR_APP_NAME, userName, userPassword);
		googleCalendar = new GoogleCalendarMhs(GOOGLE_CALENDAR_APP_NAME,
				userName, googleAccessToken);

		syncronize.enableRemoteSync();
		saveGoogleAccountInfo(userName, googleAccessToken);

		logExitMethod("loginUserGoogleAccount");
	}

	/**
	 * Logs user out of Google Account
	 * 
	 * @throws IOException
	 */
	public void logOutUserGoogleAccount() throws IOException {
		logEnterMethod("logOutUserGoogleAccount");
		syncronize.disableRemoteSync();
		googleCalendar = null;
		configFile.removeConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN);
		configFile.removeConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT);
		configFile.save();
		logExitMethod("logOutUserGoogleAccount");
	}

	/**
	 * Checks if Remote Sync (Google Calendar) is currently active
	 * 
	 * @return
	 */
	public boolean isUserGoogleCalendarAuthenticated() {
		logEnterMethod("isUserGoogleCalendarAuthenticated");
		logExitMethod("isUserGoogleCalendarAuthenticated");
		return isRemoteSyncEnabled;
	}

	/**
	 * Saves user Google Calendar Service access token and user google account
	 * email to config file
	 * 
	 * @param googleUserAccount
	 * @param googleAuthToken
	 * 
	 * @throws IOException
	 */
	private synchronized void saveGoogleAccountInfo(String googleUserAccount,
			String googleAuthToken) throws IOException {
		logEnterMethod("saveGoogleAccountInfo");
		if (googleAuthToken != null) {
			logger.log(Level.INFO, "Saving Google : "
					+ CONFIG_PARAM_GOOGLE_AUTH_TOKEN + " " + googleAuthToken);
			configFile.setConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN,
					googleAuthToken);
		}

		if (googleUserAccount != null) {
			logger.log(Level.INFO, "Saving Google : "
					+ CONFIG_PARAM_GOOGLE_USER_ACCOUNT + " "
					+ googleUserAccount);
			configFile.setConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT,
					googleUserAccount);
		}

		configFile.save();
		logExitMethod("saveGoogleAccountInfo");
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
		logEnterMethod("syncronizeDatabases");
		syncronize.syncronizeDatabases();
		logExitMethod("syncronizeDatabases");
	}

	/**
	 * Returns single task with specified taskId (deleted tasks queriable)
	 * 
	 * @param taskId
	 * @return matched task
	 * @throws Exception
	 */
	public Task query(int taskId) throws TaskNotFoundException {
		logEnterMethod("query");
		if (!taskLists.containsTask(taskId)) {
			throw new TaskNotFoundException(
					EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}
		logExitMethod("query");
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
		logEnterMethod("query");
		logExitMethod("query");
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
		logEnterMethod("query");
		if (taskName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_TASK_NAME));
		}
		logExitMethod("query");
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
		logEnterMethod("query");
		if (queryTaskCategory == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, "queryTaskCategory"));
		}
		logExitMethod("query");
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
		logEnterMethod("query");
		if (startDateTime == null || endDateTime == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETERE_START_AND_END_DATE_TIMES));
		}
		logExitMethod("query");
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
		logEnterMethod("query");
		if (taskName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_TASK_NAME));
		}
		if (startDateTime == null || endDateTime == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETERE_START_AND_END_DATE_TIMES));
		}
		logExitMethod("query");
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
		logEnterMethod("query");
		if (taskName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_TASK_NAME));
		}
		if (startDateTime == null || endDateTime == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETERE_START_AND_END_DATE_TIMES));
		}

		logExitMethod("query");
		return taskLists.getTasks(taskName, taskCategory, startDateTime,
				endDateTime, orderByStartDateTime);
	}

	/**
	 * Adds a task to local storage and remote (if authenticated and connection
	 * exists)
	 * 
	 * @param task
	 * @throws InvalidTaskFormatException
	 * @throws IOException
	 * @throws Exception
	 * @throws ServiceException
	 */
	public Task add(Task task) throws InvalidTaskFormatException, IOException,
			InvalidTaskFormatException {
		logEnterMethod("add");
		if (task == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, PARAMETER_TASK));
		}

		if (!isTaskValid(task)) {
			throw new InvalidTaskFormatException(
					EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		}

		Task taskToAdd = task.clone();
		addTaskToTaskList(taskToAdd);

		if (isRemoteSyncEnabled) {
			try {
				syncronize.pushSyncTask(taskToAdd);
			} catch (NullPointerException e) {
				logger.log(Level.FINER, e.getMessage());
			} catch (ServiceException e) {
				logger.log(Level.FINER, e.getMessage());
			} catch (TaskNotFoundException e) {
				logger.log(Level.FINER, e.getMessage());
			}
		}

		saveTaskRecordFile();
		logExitMethod("add");
		return taskToAdd;
	}

	/**
	 * Add task to task list
	 * 
	 * @param taskToAdd
	 */
	private void addTaskToTaskList(Task taskToAdd) {
		logEnterMethod("addTaskToTaskList");
		assert (taskToAdd != null);

		taskToAdd.setTaskId(getNewTaskId());
		DateTime UpdateTime = new DateTime();
		UpdateTime = DateTime.now();

		taskToAdd.setTaskCreated(UpdateTime);
		taskToAdd.setTaskUpdated(UpdateTime);

		taskToAdd.setTaskLastSync(null);
		taskToAdd.setgCalTaskId(null);

		taskLists.updateTaskInTaskLists(taskToAdd);

		logExitMethod("addTaskToTaskList");
	}

	/**
	 * Deletes a task
	 * 
	 * @param taskId
	 * @throws IOException
	 * @throws Exception
	 * @throws ServiceException
	 */
	public void delete(int taskId) throws TaskNotFoundException, IOException {
		logEnterMethod("delete");
		if (!taskLists.containsTask(taskId)) {
			throw new TaskNotFoundException(
					EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		Task taskToDelete = taskLists.getTask(taskId);
		deleteTaskInTaskList(taskToDelete);

		if (isRemoteSyncEnabled) {
			try {
				syncronize.pushSyncTask(taskToDelete);
			} catch (NullPointerException | ServiceException
					| InvalidTaskFormatException e) {
				// SilentFailSync Policy
				logger.log(Level.FINER, e.getMessage());
			}
		}
		saveTaskRecordFile();
		logExitMethod("delete");
	}

	/**
	 * Deletes task in task list by setting deleted attribute
	 * 
	 * @param taskToDelete
	 */
	private void deleteTaskInTaskList(Task taskToDelete) {
		logEnterMethod("deleteTaskInTaskList");
		assert (taskToDelete != null);

		taskToDelete.setDeleted(true);

		// set updated time to force push
		DateTime UpdateTime = new DateTime();
		UpdateTime = DateTime.now().plusMinutes(1);
		taskToDelete.setTaskUpdated(UpdateTime);

		taskLists.updateTaskInTaskLists(taskToDelete);
		logExitMethod("deleteTaskInTaskList");
	}

	/**
	 * Updates task
	 * 
	 * @param updatedTask
	 * @throws InvalidTaskFormatException
	 * @throws TaskNotFoundException
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws Exception
	 * @throws ServiceException
	 */
	public Task update(Task updatedTask) throws NullPointerException,
			IOException, ServiceException, TaskNotFoundException,
			InvalidTaskFormatException {
		logEnterMethod("update");
		assert (updatedTask != null);

		if (!taskLists.containsTask(updatedTask.getTaskId())) {
			throw new TaskNotFoundException(
					EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		if (!isTaskValid(updatedTask)) {
			throw new InvalidTaskFormatException(
					EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		}

		Task updatedTaskToSave = updatedTask.clone();

		// Preserve non-editable fields
		Task currentTask = query(updatedTaskToSave.getTaskId());
		updatedTaskToSave.setgCalTaskId(currentTask.getgCalTaskId());
		updatedTaskToSave.setTaskCreated(currentTask.getTaskCreated());
		updatedTaskToSave.setTaskLastSync(currentTask.getTaskLastSync());

		updateTaskinTaskList(updatedTaskToSave);

		if (isRemoteSyncEnabled) {
			try {
				syncronize.pushSyncTask(updatedTaskToSave);
			} catch (NullPointerException | ServiceException
					| InvalidTaskFormatException e) {
				// SilentFailSync Policy
				logger.log(Level.FINER, e.getMessage());
			}
		}

		saveTaskRecordFile();
		logExitMethod("update");
		return updatedTaskToSave;
	}

	/**
	 * Updates task in task list
	 * 
	 * @param updatedTaskToSave
	 */
	private void updateTaskinTaskList(Task updatedTaskToSave) {
		logEnterMethod("updateTaskinTaskList");
		assert (updatedTaskToSave != null);

		// set updated time ahead to force push
		DateTime UpdateTime = new DateTime();
		UpdateTime = DateTime.now().plusMinutes(1);
		updatedTaskToSave.setTaskUpdated(UpdateTime);

		taskLists.updateTaskInTaskLists(updatedTaskToSave);
		logExitMethod("updateTaskinTaskList");
	}

	/**
	 * Save Tasks to local file - Syncronized to prevent multiple File I/O
	 * 
	 * @throws IOException
	 */
	private synchronized void saveTaskRecordFile() throws IOException {
		logEnterMethod("saveTaskRecordFile");
		assert (taskRecordFile != null);

		taskRecordFile.saveTaskList(taskLists.getTaskList());

		logExitMethod("saveTaskRecordFile");
	}

	/**
	 * Removes task from list
	 * 
	 * @param taskId
	 * @throws TaskNotFoundException
	 */
	private void removeRecord(Task taskToRemove) throws TaskNotFoundException {
		logEnterMethod("removeRecord");
		assert (taskToRemove != null);
		assert (taskLists != null);

		if (!taskLists.containsTask(taskToRemove.getTaskId())) {
			throw new TaskNotFoundException(
					EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}
		taskLists.removeTaskInTaskLists(taskToRemove);

		logExitMethod("removeRecord");
	}

	/**
	 * Clears deleted and expired/done local and remote tasks
	 * 
	 * @throws Exception
	 */
	public void cleanupTasks() throws Exception {
		logEnterMethod("cleanupTasks");

		syncronizeDatabases();
		cleanupLocalTasks();

		logExitMethod("cleanupTasks");
	}

	/**
	 * Clears deleted and expired/done local tasks
	 * 
	 * @throws Exception
	 */
	private void cleanupLocalTasks() throws Exception {
		logEnterMethod("cleanupLocalTasks");

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
		logExitMethod("cleanupLocalTasks");
	}

	/**
	 * Clears all databases
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void clearDatabase() throws IOException, ServiceException {
		logEnterMethod("clearDatabase");
		assert (taskLists != null);

		taskLists.clearTaskLists();
		clearRemoteDatabase();
		saveTaskRecordFile();

		logExitMethod("clearDatabase");
	}

	/**
	 * Clear local database
	 * 
	 * @throws IOException
	 */
	public void clearLocalDatabase() throws IOException {
		logEnterMethod("clearLocalDatabase");
		assert (taskLists != null);

		taskLists.clearTaskLists();
		saveTaskRecordFile();
		logExitMethod("clearLocalDatabase");
	}

	/**
	 * Clears remote database
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void clearRemoteDatabase() throws IOException, ServiceException {
		logEnterMethod("clearRemoteDatabase");
		if (isRemoteSyncEnabled) {
			try {
				googleCalendar.deleteEvents(DateTime.now().minusYears(1)
						.toString(), DateTime.now().plusYears(1).toString());
			} catch (NullPointerException | ServiceException e) {
				// SilentFailSync Policy
				logger.log(Level.FINER, e.getMessage());
			}
		}
		logExitMethod("clearRemoteDatabase");
	}

	/**
	 * Checks if Task format is valid for given type
	 * 
	 * @param task
	 * @return boolean
	 */
	private boolean isTaskValid(Task task) {
		logEnterMethod("isTaskValid");
		assert (task != null);

		if (task.getTaskCategory() == null || task.getTaskName() == null) {
			logger.exiting(getClass().getName(),
					new Exception().getStackTrace()[0].getMethodName());
			return false;
		}

		boolean taskIsValid = true;

		switch (task.getTaskCategory()) {
		case FLOATING:
			break;
		case TIMED:
			taskIsValid = isTimedTaskValid(task, taskIsValid);
			break;
		case DEADLINE:
			taskIsValid = isDeadlineTaskValid(task, taskIsValid);
			break;
		default:
			taskIsValid = false;
			break;
		}

		logExitMethod("isTaskValid");
		return taskIsValid;
	}

	/**
	 * Checks if deadline task format is valid
	 * 
	 * @param task
	 * @param taskIsValid
	 * @return true if deadline task format is valid
	 */
	private boolean isDeadlineTaskValid(Task task, boolean taskIsValid) {
		logEnterMethod("isDeadlineTaskValid");
		assert (task != null);

		if (task.getEndDateTime() == null) {
			taskIsValid = false;
		}
		logExitMethod("isDeadlineTaskValid");
		return taskIsValid;
	}

	/**
	 * Checks if timed task is valid
	 * 
	 * @param task
	 * @param taskIsValid
	 * @return
	 */
	private boolean isTimedTaskValid(Task task, boolean taskIsValid) {
		logEnterMethod("isTimedTaskValid");
		assert (task != null);

		if (task.getStartDateTime() == null || task.getEndDateTime() == null) {
			taskIsValid = false;
		}
		logExitMethod("isTimedTaskValid");
		return taskIsValid;
	}

	/**
	 * Checks whether task is unsynced
	 * 
	 * @param localTask
	 * @return true if task is unsynced
	 */
	private boolean isUnsyncedTask(Task localTask) {
		logEnterMethod("isUnsyncedTask");
		assert (localTask != null);
		logExitMethod("isUnsyncedTask");
		return localTask.getgCalTaskId() == null
				|| localTask.getTaskLastSync() == null;
	}

	/**
	 * Returns new taskId - (unique incremental)
	 * 
	 * @return
	 */
	private int getNewTaskId() {
		logEnterMethod("getNewTaskId");

		int getNewTaskId = 0;
		Set<Integer> taskKeySet = taskLists.getTaskList().keySet();
		Iterator<Integer> iterator = taskKeySet.iterator();

		while (iterator.hasNext()) {
			getNewTaskId = iterator.next();
		}
		getNewTaskId++;

		logExitMethod("getNewTaskId");
		return getNewTaskId;
	}

	public void waitForSyncronizeBackgroundTaskToComplete(
			int maxExecutionTimeInSeconds) throws InterruptedException,
			ExecutionException, TimeoutException {
		logEnterMethod("waitForSyncronizeBackgroundTaskToComplete");
		syncronize
				.waitForSyncronizeBackgroundTaskToComplete(maxExecutionTimeInSeconds);
		logExitMethod("waitForSyncronizeBackgroundTaskToComplete");
	}

	private void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}

	private void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}

}