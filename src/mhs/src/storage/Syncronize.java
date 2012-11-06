//@author A0087048X

package mhs.src.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import mhs.src.common.exceptions.InvalidTaskFormatException;
import mhs.src.common.exceptions.TaskNotFoundException;
import mhs.src.storage.persistence.remote.GoogleCalendarMhs;
import mhs.src.storage.persistence.task.DeadlineTask;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;
import mhs.src.storage.persistence.task.TimedTask;

import org.joda.time.DateTime;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.ResourceNotFoundException;
import com.google.gdata.util.ServiceException;

/**
 * Syncronize
 * 
 * Handles Syncronization operations between local storage and Google Calendar
 * Service as background task in a separate thread
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
 * @author Timothy Lim Yi Wen A0087048X
 */
class Syncronize {

	private final Database database;
	private ScheduledThreadPoolExecutor syncronizeBackgroundExecutor;
	private Runnable syncronizeDatabasesBackgroundTask;
	private TimerTask pullSyncTimedBackgroundTask;
	private Future<?> futureSyncronizeBackgroundTask;

	private static final int PULL_SYNC_TIMER_DEFAULT_INITIAL_DELAY_IN_MINUTES = 2;
	private static final int PULL_SYNC_TIMER_DEFAULT_PERIOD_IN_MINUTES = 1;

	// Config parameters
	private static final String CONFIG_PARAM_GOOGLE_USER_ACCOUNT = "GOOGLE_USER_ACCOUNT";
	private static final String CONFIG_PARAM_GOOGLE_AUTH_TOKEN = "GOOGLE_AUTH_TOKEN";
	private static final String GOOGLE_CALENDAR_APP_NAME = "My Hot Secretary";
		
	/**
	 * Default Constructor for Syncronize.
	 * 
	 * Sets up thread for background sync and initializes runnable tasks and
	 * Google Calendar Service.
	 * 
	 * @param database
	 * @param disableSyncronize
	 * @throws IOException
	 * @throws ServiceException
	 */
	Syncronize(Database database, boolean disableSyncronize) throws IOException {
		this.database = database;
		this.database.logEnterMethod("Syncronize");

		syncronizeBackgroundExecutor = new ScheduledThreadPoolExecutor(1);
		initializeRunnableTasks();
		if (initializeGoogleCalendarService() && !disableSyncronize) {
			enableRemoteSync();
		} else {
			disableRemoteSync();
		}

		this.database.logExitMethod("Syncronize");
	}

	synchronized void waitForSyncronizeBackgroundTaskToComplete(
			int maxExecutionTimeInSeconds) throws InterruptedException,
			ExecutionException, TimeoutException {
		this.database
				.logEnterMethod("waitForSyncronizeBackgroundTaskToComplete");
		Database.logger.log(Level.INFO,
				"Waiting for background task to complete.");
		if (futureSyncronizeBackgroundTask == null) {
			return;
		}
		futureSyncronizeBackgroundTask.get(maxExecutionTimeInSeconds,
				TimeUnit.SECONDS);
		this.database
				.logExitMethod("waitForSyncronizeBackgroundTaskToComplete");
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
	boolean syncronizeDatabases() {
		this.database.logEnterMethod("syncronizeDatabases");
		// checks if user is logged out
		if (Database.googleCalendar == null) {
			return false;
		}
		futureSyncronizeBackgroundTask = syncronizeBackgroundExecutor
				.submit(syncronizeDatabasesBackgroundTask);
		this.database.logExitMethod("syncronizeDatabases");
		return true;
	}

	/**
	 * Enables remote sync for task operations and auto-sync
	 */
	void enableRemoteSync() {
		this.database.logEnterMethod("enableRemoteSync");
		Database.logger.log(Level.INFO, "Enabling remote sync");
		Database.isRemoteSyncEnabled = true;
		scheduleTimedPullSync();
		this.database.logExitMethod("enableRemoteSync");
	}

	/**
	 * Disables remote sync for task operations and auto-sync
	 */
	void disableRemoteSync() {
		this.database.logEnterMethod("disableRemoteSync");
		Database.logger.log(Level.INFO, "Disabling remote sync");
		Database.isRemoteSyncEnabled = false;
		cancelTimedPullSync();
		this.database.logExitMethod("disableRemoteSync");
	}

	/**
	 * Initialize runnable tasks
	 */
	private void initializeRunnableTasks() {
		this.database.logEnterMethod("initializeRunnableTasks");
		initializeSyncronizeRunnableTask();
		initializeTimedPullSyncRunnableTask();
		this.database.logExitMethod("initializeRunnableTasks");
	}

	/**
	 * Initialize Syncronize Runnable Task
	 */
	private void initializeSyncronizeRunnableTask() {
		this.database.logEnterMethod("initializeSyncronizeRunnableTask");
		syncronizeDatabasesBackgroundTask = new Runnable() {
			@Override
			public void run() {
				try {
					Database.logger.log(Level.INFO,
							"Executing syncronize background task");
					pullSync();
					pushSync();
					Syncronize.this.database.saveTaskRecordFile();
				} catch (UnknownHostException e) {
					Database.logger.log(Level.FINER, e.getMessage());
					disableRemoteSync();
				} catch (TaskNotFoundException e) {
					// SilentFailSync Policy
					Database.logger.log(Level.FINER, e.getMessage());
				} catch (InvalidTaskFormatException e) {
					// SilentFailSync Policy
					Database.logger.log(Level.FINER, e.getMessage());
				} catch (NullPointerException e) {
					Database.logger.log(Level.FINER, e.getMessage());
				} catch (IOException e) {
					Database.logger.log(Level.FINER, e.getMessage());
				} catch (ServiceException e) {
					// SilentFailSync Policy
					Database.logger.log(Level.FINER, e.getMessage());
				}
			}
		};
		this.database.logExitMethod("initializeSyncronizeRunnableTask");
	}

	/**
	 * Schedules timed pull-sync
	 */
	private void scheduleTimedPullSync() {
		this.database.logEnterMethod("scheduleTimedPullSync");
		syncronizeBackgroundExecutor.scheduleAtFixedRate(
				pullSyncTimedBackgroundTask,
				PULL_SYNC_TIMER_DEFAULT_INITIAL_DELAY_IN_MINUTES,
				PULL_SYNC_TIMER_DEFAULT_PERIOD_IN_MINUTES, TimeUnit.MINUTES);
		this.database.logExitMethod("scheduleTimedPullSync");
	}

	/**
	 * Initialize Timed Pull Sync Runnable Task
	 */
	private void initializeTimedPullSyncRunnableTask() {
		this.database.logEnterMethod("initializeTimedPullSyncRunnableTask");
		pullSyncTimedBackgroundTask = new TimerTask() {
			@Override
			public void run() {
				Database.logger.log(Level.INFO,
						"Executing timed pull sync task");
				try {
					Database.syncronize.pullSync();
					Syncronize.this.database.saveTaskRecordFile();
				} catch (UnknownHostException e) {
					Database.logger.log(Level.FINER, e.getMessage());
					disableRemoteSync();
				} catch (TaskNotFoundException e) {
					// SilentFailSync Policy
					Database.logger.log(Level.FINER, e.getMessage());
				} catch (InvalidTaskFormatException e) {
					// SilentFailSyncPolicy
					Database.logger.log(Level.FINER, e.getMessage());
				} catch (IOException e) {
					Database.logger.log(Level.FINER, e.getMessage());
				}
			}
		};
		this.database.logExitMethod("initializeTimedPullSyncRunnableTask");
	}

	/**
	 * Cancel timed pull sync
	 */
	private void cancelTimedPullSync() {
		this.database.logEnterMethod("cancelTimedPullSync");
		if (pullSyncTimedBackgroundTask != null) {
			pullSyncTimedBackgroundTask.cancel();
		}
		this.database.logExitMethod("cancelTimedPullSync");
	}

	/**
	 * Pull Sync remote tasks to local
	 * 
	 * @throws UnknownHostException
	 * @throws InvalidTaskFormatException
	 * @throws TaskNotFoundException
	 * @throws ServiceException
	 */
	private void pullSync() throws UnknownHostException, TaskNotFoundException,
			InvalidTaskFormatException {
		this.database.logEnterMethod("pullSync");
		List<CalendarEventEntry> googleCalendarEvents;

		try {
			googleCalendarEvents = Database.googleCalendar.retrieveEvents(
					Database.syncStartDateTime.toString(),
					Database.syncEndDateTime.toString());
			Iterator<CalendarEventEntry> iterator = googleCalendarEvents
					.iterator();
			// pull sync remote tasks
			while (iterator.hasNext()) {
				CalendarEventEntry gCalEntry = iterator.next();
				pullSyncTask(gCalEntry);
			}
		} catch (UnknownHostException e) {
			Database.logger.log(Level.FINER, e.getMessage());
			throw e;
		} catch (ServiceException e) {
			Database.logger.log(Level.FINER, e.getMessage());
		} catch (NullPointerException e) {
			Database.logger.log(Level.FINER, e.getMessage());
		} catch (IOException e) {
			Database.logger.log(Level.FINER, e.getMessage());
		}
		this.database.logExitMethod("pullSync");
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
		this.database.logEnterMethod("pullSyncTask");

		if (Database.taskLists.containsSyncTask(gCalEntry.getIcalUID())) {

			Task localTask = Database.taskLists.getSyncTask(gCalEntry
					.getIcalUID());

			// pull sync deleted event
			if (Database.googleCalendar.isDeleted(gCalEntry)) {
				Database.logger.log(Level.INFO, "Deleting cancelled task : "
						+ gCalEntry.getTitle().getPlainText());
				// delete local task
				this.database.deleteTaskInTaskList(localTask);
				return;
			}

			// pull sync newer task
			if (localTask.getTaskLastSync().isBefore(
					new DateTime(gCalEntry.getUpdated().getValue()))) {

				Database.logger.log(Level.INFO, "pulling newer event : "
						+ localTask.getTaskName());

				pullSyncExistingTask(gCalEntry, localTask);
			}
		} else {
			// Skip deleted events
			if (Database.googleCalendar.isDeleted(gCalEntry)) {
				return;
			}
			Database.logger.log(Level.INFO, "pulling new event : "
					+ gCalEntry.getTitle().getPlainText());
			pullSyncNewTask(gCalEntry);
		}
		this.database.logExitMethod("pullSyncTask");
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
		this.database.logEnterMethod("pullSyncNewTask");
		DateTime syncDateTime = setSyncTime(gCalEntry);

		// add task from google calendar entry
		if (gCalEntry.getTimes().get(0).getStartTime()
				.equals(gCalEntry.getTimes().get(0).getEndTime())) {
			// create new deadline task
			Task newTask = new DeadlineTask(this.database.getNewTaskId(),
					gCalEntry, syncDateTime);
			Database.taskLists.updateTaskInTaskLists(newTask);

		} else {
			// create new timed task
			Task newTask = new TimedTask(this.database.getNewTaskId(),
					gCalEntry, syncDateTime);
			Database.taskLists.updateTaskInTaskLists(newTask);
		}
		this.database.logExitMethod("pullSyncNewTask");
	}

	/**
	 * Syncs existing local task with updated remote task Call pullSyncTask as
	 * it contains sync validation logic.
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
		this.database.logEnterMethod("pullSyncExistingTask");
		updateSyncTask(localTaskEntry, gCalEntry);
		this.database.logExitMethod("pullSyncExistingTask");
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
		this.database.logEnterMethod("pushSync");
		// push sync tasks from local to google calendar
		for (Map.Entry<Integer, Task> entry : Database.taskLists.getTaskList()
				.entrySet()) {
			pushSyncTask(entry.getValue());
		}
		this.database.logExitMethod("pushSync");
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
	void pushSyncTask(Task localTask) throws NullPointerException, IOException,
			TaskNotFoundException, InvalidTaskFormatException, ServiceException {
		this.database.logEnterMethod("pushSyncTask");
		// skip floating tasks
		if (localTask.getTaskCategory().equals(TaskCategory.FLOATING)) {
			this.database.logExitMethod("pushSyncTask");
			return;
		}
		// remove deleted task
		if (localTask.isDeleted()) {
			Database.logger.log(Level.INFO, "Removing deleted synced task : "
					+ localTask.getTaskName());
			try {
				Database.googleCalendar.deleteEvent(localTask.getgCalTaskId());
			} catch (ResourceNotFoundException e) {
				// SilentFailSync Policy
				Database.logger.log(Level.FINER, e.getMessage());
			} catch (NullPointerException e) {
				Database.logger.log(Level.FINER, e.getMessage());
			}
			this.database.logExitMethod("pushSyncTask");
			return;
		}

		// add unsynced tasks
		if (TaskValidator.isUnsyncedTask(localTask)) {
			Database.logger.log(Level.INFO, "Pushing new sync task : "
					+ localTask.getTaskName());
			pushSyncNewTask(localTask);
		} else {
			// add updated tasks
			if (localTask.getTaskUpdated().isAfter(localTask.getTaskLastSync())) {
				Database.logger.log(Level.INFO, "Pushing updated task : "
						+ localTask.getTaskName());
				pushSyncExistingTask(localTask);
			}
		}

		Database.logger.exiting(getClass().getName(),
				new Exception().getStackTrace()[0].getMethodName());
	}

	/**
	 * Push task that is currently not synced. Call pushSyncTask to sync tasks
	 * instead as it contains sync validation logic.
	 * 
	 * @param localTask
	 * @throws ServiceException
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws InvalidTaskFormatException
	 * @throws TaskNotFoundException
	 * @throws Exception
	 */
	private void pushSyncNewTask(Task localTask) throws NullPointerException,
			IOException, ServiceException, TaskNotFoundException,
			InvalidTaskFormatException {
		this.database.logEnterMethod("pushSyncNewTask");
		// adds event to google calendar
		CalendarEventEntry addedGCalEvent = Database.googleCalendar
				.createEvent(localTask);
		updateSyncTask(localTask, addedGCalEvent);
		this.database.logExitMethod("pushSyncNewTask");
	}

	/**
	 * Push existing synced task. Call pushSyncTask to sync tasks instead as it
	 * contains sync validation logic.
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
		this.database.logEnterMethod("pushSyncExistingTask");
		// update remote task
		CalendarEventEntry updatedGcalEvent = Database.googleCalendar
				.updateEvent(localTask.clone());
		updateSyncTask(localTask, updatedGcalEvent);
		this.database.logExitMethod("pushSyncExistingTask");
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
		this.database.logEnterMethod("updateSyncTask");
		if (!Database.taskLists.containsTask(localSyncTaskToUpdate.getTaskId())) {
			throw new TaskNotFoundException(
					Database.EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		if (!TaskValidator.isTaskValid(localSyncTaskToUpdate)) {
			throw new InvalidTaskFormatException(
					Database.EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		}

		DateTime syncDateTime = setSyncTime(UpdatedCalendarEvent);
		localSyncTaskToUpdate = updateLocalSyncTask(localSyncTaskToUpdate,
				UpdatedCalendarEvent, syncDateTime);
		Database.taskLists.updateTaskInTaskLists(localSyncTaskToUpdate);
		this.database.logExitMethod("updateSyncTask");
		return localSyncTaskToUpdate;
	}

	/**
	 * Updates local sync task
	 * 
	 * @param localSyncTaskToUpdate
	 * @param UpdatedCalendarEvent
	 * @param syncDateTime
	 */
	private Task updateLocalSyncTask(Task localSyncTaskToUpdate,
			CalendarEventEntry UpdatedCalendarEvent, DateTime syncDateTime) {
		this.database.logEnterMethod("updateLocalSyncTask");
		When eventTimes = UpdatedCalendarEvent.getTimes().get(0);
		// Update Task Type
		if (eventTimes.getStartTime().equals(eventTimes.getEndTime())) {
			localSyncTaskToUpdate = new DeadlineTask(
					localSyncTaskToUpdate.getTaskId(), UpdatedCalendarEvent,
					syncDateTime);
		} else {
			localSyncTaskToUpdate = new TimedTask(
					localSyncTaskToUpdate.getTaskId(), UpdatedCalendarEvent,
					syncDateTime);
		}
		this.database.logExitMethod("updateLocalSyncTask");
		return localSyncTaskToUpdate;
	}

	/**
	 * Sets sync time for local tasks from google calendar entry
	 * 
	 * @param gCalEntry
	 * @return sync datetime for updating local task
	 */
	private DateTime setSyncTime(CalendarEventEntry gCalEntry) {
		this.database.logEnterMethod("setSyncTime");
		new DateTime();
		DateTime syncDateTime = DateTime.now();

		if (gCalEntry != null) {
			syncDateTime = new DateTime(gCalEntry.getUpdated().getValue());
		}

		// assert that sync DateTimes between local and remote are equal
		assert (syncDateTime.isEqual(new DateTime(gCalEntry.getUpdated()
				.toString())));

		this.database.logExitMethod("setSyncTime");
		return syncDateTime;
	}

	/**
	 * Initializes Google Calendar Service with saved access token
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	boolean initializeGoogleCalendarService() {
		this.database.logEnterMethod("initializeGoogleCalendarService");
		if (!Database.configFile
				.hasNonEmptyConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN)) {
			this.database.logExitMethod("initializeGoogleCalendarService");
			return false;
		}
		if (!Database.configFile
				.hasNonEmptyConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT)) {
			this.database.logExitMethod("initializeGoogleCalendarService");
			return false;
		}

		try {
			authenticateGoogleAccount(
					Database.configFile
							.getConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT),
					Database.configFile
							.getConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN));
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		} catch (ServiceException e) {
		}

		this.database.logExitMethod("initializeGoogleCalendarService");
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
			String googleAuthToken) throws IOException, ServiceException,
			UnknownHostException {
		this.database.logEnterMethod("authenticateGoogleAccount");
		assert (googleUserAccount != null);
		assert (googleAuthToken != null);

		try {
			Database.googleCalendar = new GoogleCalendarMhs(
					GOOGLE_CALENDAR_APP_NAME, googleUserAccount,
					googleAuthToken);
		} catch (NullPointerException e) {
			Database.logger.log(Level.FINER, e.getMessage());
		} catch (UnknownHostException e) {
			Database.logger.log(Level.FINER, e.getMessage());
			throw e;
		} catch (ServiceException e) {
			Database.logger.log(Level.FINER, e.getMessage());
			throw e;
		}

		this.database.saveGoogleAccountInfo(googleUserAccount, googleAuthToken);
		this.database.logExitMethod("authenticateGoogleAccount");
	}

}