//@author A0087048X

package mhs.src.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.InvalidTaskFormatException;
import mhs.src.common.exceptions.TaskNotFoundException;
import mhs.src.storage.persistence.task.DeadlineTask;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;
import mhs.src.storage.persistence.task.TimedTask;

import org.joda.time.DateTime;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;
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

	final Database database;
	private ScheduledThreadPoolExecutor syncronizeBackgroundExecutor;
	private TimerTask pullSyncTimedBackgroundTask;
	private Future<?> futureSyncronizeBackgroundTask;
	Map<String, Callable<Boolean>> syncBackgroundTasks;

	private static final int PULL_SYNC_TIMER_DEFAULT_INITIAL_DELAY_IN_MINUTES = 2;
	private static final int PULL_SYNC_TIMER_DEFAULT_PERIOD_IN_MINUTES = 1;

	static final Logger logger = MhsLogger.getLogger();

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
		logEnterMethod("Syncronize");

		syncronizeBackgroundExecutor = new ScheduledThreadPoolExecutor(1);

		initializeTimedPullSyncTasks();
		initializePushSyncBackgroundTasksList();

		try {
			if (this.database.initializeGoogleCalendarService()
					&& !disableSyncronize) {
				enableRemoteSync();
				syncronizeDatabases();
			} else {
				disableRemoteSync();
			}
		} catch (AuthenticationException e) {
			disableRemoteSync();
			logger.log(Level.FINER, e.getMessage());
		} catch (ServiceException e) {
			logger.log(Level.FINER, e.getMessage());
		}

		logExitMethod("Syncronize");
	}

	/**
	 * Initialize Push-Sync Background tasks list
	 */
	private void initializePushSyncBackgroundTasksList() {
		syncBackgroundTasks = new ConcurrentHashMap<String, Callable<Boolean>>();
	}

	/**
	 * Schedule Push-Sync Task
	 * 
	 * @param taskToScheduleSync
	 */
	synchronized void schedulePushSyncTask(Task taskToScheduleSync) {
		Callable<Boolean> pushTaskToSchedule = new SyncPushTask(
				taskToScheduleSync, this);
		syncBackgroundTasks.put(getSyncTaskQueueUid(), pushTaskToSchedule);
		syncronizeBackgroundExecutor.submit(pushTaskToSchedule);
	}

	/**
	 * Get unique id for SyncTaskQueueUID
	 * 
	 * @return
	 */
	synchronized private String getSyncTaskQueueUid() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Removes push sync task from list
	 * 
	 * @param syncTaskQueueUid
	 */
	synchronized void removePushSyncTaskFromList(String syncTaskQueueUid) {
		syncBackgroundTasks.remove(syncTaskQueueUid);
	}

	/**
	 * Wait for all background tasks to end.
	 * 
	 * - Background Sync
	 * 
	 * - Background push sync tasks
	 * 
	 * @param maxExecutionTimeInSeconds
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	synchronized void waitForAllBackgroundTasks(int maxExecutionTimeInSeconds)
			throws InterruptedException, ExecutionException, TimeoutException {
		logEnterMethod("waitForAllBackgroundTasks");
		waitForSyncronizeBackgroundTask(maxExecutionTimeInSeconds);
		waitForBackgroundPushSyncTasks();
		logExitMethod("waitForAllBackgroundTasks");
	}

	/**
	 * Waits for syncronize background task to complete given maxExecutionTime
	 * 
	 * @param maxExecutionTimeInSeconds
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private synchronized void waitForSyncronizeBackgroundTask(
			int maxExecutionTimeInSeconds) throws InterruptedException,
			ExecutionException, TimeoutException {
		logEnterMethod("waitForSyncronizeBackgroundTaskToComplete");
		logger.log(Level.INFO, "Waiting for background task to complete.");
		if (futureSyncronizeBackgroundTask == null) {
			return;
		}
		futureSyncronizeBackgroundTask.get(maxExecutionTimeInSeconds,
				TimeUnit.SECONDS);
		waitForBackgroundPushSyncTasks();

		logExitMethod("waitForSyncronizeBackgroundTaskToComplete");
	}

	private synchronized void waitForBackgroundPushSyncTasks()
			throws InterruptedException {
		logEnterMethod("waitForBackgroundPushSyncTasks");
		if (syncBackgroundTasks.isEmpty()) {
			return;
		}
		syncronizeBackgroundExecutor.invokeAll(syncBackgroundTasks.values());
		syncBackgroundTasks.clear();
		logExitMethod("waitForBackgroundPushSyncTasks");
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
		logEnterMethod("syncronizeDatabases");
		// checks if user is logged out
		if (Database.googleCalendar == null) {
			return false;
		}
		SyncAllTasks syncronizeAllTasks = new SyncAllTasks(this);
		futureSyncronizeBackgroundTask = syncronizeBackgroundExecutor
				.submit(syncronizeAllTasks);
		logExitMethod("syncronizeDatabases");
		return true;
	}

	/**
	 * Enables remote sync for task operations and auto-sync
	 */
	void enableRemoteSync() {
		logEnterMethod("enableRemoteSync");
		logger.log(Level.INFO, "Enabling remote sync");
		Database.isRemoteSyncEnabled = true;
		scheduleTimedPullSync();
		logExitMethod("enableRemoteSync");
	}

	/**
	 * Disables remote sync for task operations and auto-sync
	 */
	void disableRemoteSync() {
		logEnterMethod("disableRemoteSync");
		logger.log(Level.INFO, "Disabling remote sync");
		Database.isRemoteSyncEnabled = false;
		cancelTimedPullSync();
		logExitMethod("disableRemoteSync");
	}

	/**
	 * Initialize runnable tasks
	 */
	private void initializeTimedPullSyncTasks() {
		logEnterMethod("initializeRunnableTasks");
		initializeTimedPullSyncRunnableTask();
		logExitMethod("initializeRunnableTasks");
	}

	/**
	 * Schedules timed pull-sync
	 */
	private void scheduleTimedPullSync() {
		logEnterMethod("scheduleTimedPullSync");
		syncronizeBackgroundExecutor.scheduleAtFixedRate(
				pullSyncTimedBackgroundTask,
				PULL_SYNC_TIMER_DEFAULT_INITIAL_DELAY_IN_MINUTES,
				PULL_SYNC_TIMER_DEFAULT_PERIOD_IN_MINUTES, TimeUnit.MINUTES);
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
					Database.syncronize.pullSync();
					Syncronize.this.database.saveTaskRecordFile();
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
	void pullSync() throws UnknownHostException, TaskNotFoundException,
			InvalidTaskFormatException {
		logEnterMethod("pullSync");
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

		if (Database.taskLists.containsSyncTask(gCalEntry.getIcalUID())) {

			Task localTask = Database.taskLists.getSyncTask(gCalEntry
					.getIcalUID());

			// pull sync deleted event
			if (Database.googleCalendar.isDeleted(gCalEntry)) {
				logger.log(Level.INFO, "Deleting cancelled task : "
						+ gCalEntry.getTitle().getPlainText());
				// delete local task
				this.database.deleteTaskInTaskList(localTask);
				return;
			}

			// pull sync newer task
			if (localTask.getTaskLastSync().isBefore(
					new DateTime(gCalEntry.getUpdated().getValue()))) {

				logger.log(Level.INFO,
						"pulling newer event : " + localTask.getTaskName());

				pullSyncExistingTask(gCalEntry, localTask);
			}
		} else {
			// Skip deleted events
			if (Database.googleCalendar.isDeleted(gCalEntry)) {
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
			Task newTask = new DeadlineTask(this.database.getNewTaskId(),
					gCalEntry, syncDateTime);
			Database.taskLists.updateTaskInTaskLists(newTask);

		} else {
			// create new timed task
			Task newTask = new TimedTask(this.database.getNewTaskId(),
					gCalEntry, syncDateTime);
			Database.taskLists.updateTaskInTaskLists(newTask);
		}
		logExitMethod("pullSyncNewTask");
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
	void pushSync() throws IOException, UnknownHostException, ServiceException,
			NullPointerException, TaskNotFoundException,
			InvalidTaskFormatException {
		logEnterMethod("pushSync");
		// push sync tasks from local to google calendar
		for (Map.Entry<Integer, Task> entry : Database.taskLists.getTaskList()
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
	void pushSyncTask(Task localTask) throws NullPointerException, IOException,
			TaskNotFoundException, InvalidTaskFormatException, ServiceException {
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
				Database.googleCalendar.deleteEvent(localTask.getgCalTaskId());
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
		if (TaskValidator.isUnsyncedTask(localTask)) {
			logger.log(Level.INFO,
					"Pushing new sync task : " + localTask.getTaskName());
			pushSyncNewTask(localTask);
		} else {
			// add updated tasks
			if (localTask.getTaskUpdated().isAfter(localTask.getTaskLastSync())) {
				logger.log(Level.INFO,
						"Pushing updated task : " + localTask.getTaskName());
				pushSyncExistingTask(localTask);
			}
		}

		logExitMethod("pushSyncTask");
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
		logEnterMethod("pushSyncNewTask");
		// adds event to google calendar
		CalendarEventEntry addedGCalEvent = Database.googleCalendar
				.createEvent(localTask);
		updateSyncTask(localTask, addedGCalEvent);
		logExitMethod("pushSyncNewTask");
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
		logEnterMethod("pushSyncExistingTask");
		// update remote task
		CalendarEventEntry updatedGcalEvent = Database.googleCalendar
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
	private Task updateLocalSyncTask(Task localSyncTaskToUpdate,
			CalendarEventEntry UpdatedCalendarEvent, DateTime syncDateTime) {
		logEnterMethod("updateLocalSyncTask");
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
		logExitMethod("updateLocalSyncTask");
		return localSyncTaskToUpdate;
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

	void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}

	void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}

}