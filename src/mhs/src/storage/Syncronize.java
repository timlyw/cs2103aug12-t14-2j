//@author A0087048X

package mhs.src.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import mhs.src.common.exceptions.NoActiveCredentialException;
import mhs.src.common.exceptions.TaskNotFoundException;
import mhs.src.storage.persistence.task.DeadlineTask;
import mhs.src.storage.persistence.task.FloatingTask;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TimedTask;

import org.joda.time.DateTime;

import com.google.api.services.calendar.model.Event;
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
 * - Pull-Push Sync to syncronize local storage and Google Calendar Service<br>
 * - Timed Pull Sync to run at set interval<br>
 * - Pull Sync for single CRUD operation<br>
 * - Push Sync for single CRUD operation<br>
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */
class Syncronize {

	final Database database;
	private ScheduledThreadPoolExecutor syncronizeBackgroundExecutor;
	private TimerTask pullSyncTimedBackgroundTask;
	private Future<?> futureSyncronizeBackgroundTask;
	Map<String, Callable<Boolean>> syncBackgroundTasks;

	private static final int THREADS_TO_INITIALIZE_1 = 1;
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

		initializeSyncronizeBackgroundExecutor();
		initializeTimedPullSyncTasks();
		initializePushSyncBackgroundTasksList();
		initializeGoogleCalendarServicesAndSync(disableSyncronize);

		logExitMethod("Syncronize");
	}

	/**
	 * Initialize Syncronize Background Executor
	 */
	private void initializeSyncronizeBackgroundExecutor() {
		syncronizeBackgroundExecutor = new ScheduledThreadPoolExecutor(
				THREADS_TO_INITIALIZE_1);
	}

	/**
	 * Initialize Google Services And Startup Sync
	 * 
	 * @param disableSyncronize
	 * @throws IOException
	 */
	private void initializeGoogleCalendarServicesAndSync(
			boolean disableSyncronize) throws IOException {
		try {
			if (database.initializeGoogleServices() && !disableSyncronize) {
				enableRemoteSync();
				syncronizeDatabases();
			} else {
				disableRemoteSync();
			}
		} catch (AuthenticationException | UnknownHostException
				| NoActiveCredentialException e) {
			disableRemoteSync();
			e.printStackTrace();
			logger.log(Level.FINER, e.getMessage());
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.log(Level.FINER, e.getMessage());
		}
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
		logEnterMethod("removePushSyncTaskFromList");
		syncBackgroundTasks.remove(syncTaskQueueUid);
		logExitMethod("removePushSyncTaskFromList");
	}

	/**
	 * Wait for all background tasks to end.
	 * 
	 * - Background Sync<br>
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

		if (futureSyncronizeBackgroundTask == null) {
			return;
		}

		logger.log(Level.INFO, "Waiting for background task to complete.");
		futureSyncronizeBackgroundTask.get(maxExecutionTimeInSeconds,
				TimeUnit.SECONDS);
		waitForBackgroundPushSyncTasks();

		logExitMethod("waitForSyncronizeBackgroundTaskToComplete");
	}

	/**
	 * Wait for background push sync tasks
	 * 
	 * @throws InterruptedException
	 */
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
	 * @return true if successful
	 */
	boolean syncronizeDatabases() {
		logEnterMethod("syncronizeDatabases");
		logger.log(Level.INFO, "Syncronizing Databases");
		// checks if google services are instantiated
		if (Database.googleCalendar == null || Database.googleTasks == null) {
			logger.log(Level.INFO, "Google Services not instantiated");
			disableRemoteSync();
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
		pullSyncTimedBackgroundTask = new SyncPullSyncTimed(this);
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
	 * Sync Task Operations Logic
	 */

	/**
	 * Pull Sync remote tasks to local
	 * 
	 * @throws InvalidTaskFormatException
	 * @throws TaskNotFoundException
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 * @throws ServiceException
	 */
	void pullSync() throws ResourceNotFoundException, IOException,
			TaskNotFoundException, InvalidTaskFormatException {
		logEnterMethod("pullSync");
		logger.log(Level.INFO, "Pull Sync Tasks");
		pullSyncGoogleCalendarEvents();
		pullSyncGoogleTasksTasks();
		logExitMethod("pullSync");
	}

	/**
	 * Pull Sync Google Calendar Events
	 * 
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	protected void pullSyncGoogleCalendarEvents()
			throws ResourceNotFoundException, IOException,
			TaskNotFoundException, InvalidTaskFormatException {
		logEnterMethod("pullSyncGoogleCalendarEvents");

		Map<String, Event> googleCalendarEvents;
		googleCalendarEvents = retrieveGoogleCalendarEvents(
				Database.syncStartDateTime.toString(),
				Database.syncEndDateTime.toString());

		for (Map.Entry<String, Event> entry : googleCalendarEvents.entrySet()) {
			pullSyncGoogleCalendarTask(entry.getValue());
		}

		logExitMethod("pullSyncGoogleCalendarEvents");
	}

	/**
	 * Pull Sync Google Tasks
	 * 
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	protected void pullSyncGoogleTasksTasks() throws IOException,
			ResourceNotFoundException, TaskNotFoundException,
			InvalidTaskFormatException {
		logExitMethod("pullSyncGoogleTasksTasks");

		List<com.google.api.services.tasks.model.Task> googleTasksTaskList = Database.googleTasks
				.retrieveTasks();
		Iterator<com.google.api.services.tasks.model.Task> googleCompletedEventListIterator = googleTasksTaskList
				.iterator();

		while (googleCompletedEventListIterator.hasNext()) {
			com.google.api.services.tasks.model.Task googleTaskToPull = googleCompletedEventListIterator
					.next();
			pullSyncGoogleTask(googleTaskToPull);
		}
		logExitMethod("pullSyncGoogleTasksTasks");
	}

	/**
	 * Pull Sync Google Task
	 * 
	 * @param googleTaskToPull
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	private void pullSyncGoogleTask(
			com.google.api.services.tasks.model.Task googleTaskToPull)
			throws ResourceNotFoundException, IOException,
			TaskNotFoundException, InvalidTaskFormatException {

		if (Database.taskLists.containsGoogleTaskSyncTask(googleTaskToPull
				.getId())) {

			System.out
					.println("!Contained" + googleTaskToPull.toPrettyString());

			Task localTask = Database.taskLists
					.getGoogleTaskSyncTask(googleTaskToPull.getId());

			// pull sync deleted event
			if (Database.googleTasks.isDeleted(googleTaskToPull)) {
				if (!localTask.isDeleted()) {
					logger.log(Level.INFO, "Deleting cancelled task : "
							+ googleTaskToPull.getTitle());
					// delete local task
					this.database.deleteTaskInTaskList(localTask);
				}
				return;
			}

			System.out.println(localTask.getTaskLastSync());
			System.out.println(new DateTime(googleTaskToPull.getUpdated()
					.getValue()));
			System.out.println(localTask.getTaskLastSync().isBefore(
					new DateTime(googleTaskToPull.getUpdated().getValue())));
			// pull sync newer task
			if (localTask.getTaskLastSync().isBefore(
					new DateTime(googleTaskToPull.getUpdated().getValue()))) {
				logger.log(Level.INFO,
						"pulling newer task : " + localTask.getTaskName());
				pullSyncExistingGoogleTaskSyncedTask(googleTaskToPull,
						localTask);
			}
		} else {
			if (!Database.googleTasks.isDeleted(googleTaskToPull)) {
				logger.log(Level.INFO,
						"pulling new task : " + googleTaskToPull.getTitle());
				pullSyncNewGoogleTaskTask(googleTaskToPull);
			}
		}
		logExitMethod("pullSyncTask");
	}

	/**
	 * Retrieve Google Calendar Events
	 * 
	 * @return
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	private Map<String, Event> retrieveGoogleCalendarEvents(
			String startDateTime, String endDateTime)
			throws ResourceNotFoundException, IOException {
		Map<String, Event> googleCalendarEvents = new LinkedHashMap<>();

		List<Event> googleDefaultEvents = Database.googleCalendar
				.retrieveDefaultEvents(startDateTime, endDateTime, false);
		List<Event> googleDeletedDefaultEvents = Database.googleCalendar
				.retrieveDefaultEvents(startDateTime, endDateTime, true);
		List<Event> googleCompletedEvents = Database.googleCalendar
				.retrieveCompletedEvents(startDateTime, endDateTime, false);
		List<Event> googleDeletedCompletedEvents = Database.googleCalendar
				.retrieveCompletedEvents(startDateTime, endDateTime, true);

		loadGoogleCalendarEventsFromList(googleCalendarEvents,
				googleDefaultEvents);

		loadGoogleCalendarEventsWithDuplicates(googleCalendarEvents,
				googleCompletedEvents);

		loadGoogleCalendarEventsFromList(googleCalendarEvents,
				googleDeletedDefaultEvents);

		loadGoogleCalendarEventsFromList(googleCalendarEvents,
				googleDeletedCompletedEvents);

		return googleCalendarEvents;
	}

	protected void loadGoogleCalendarEventsWithDuplicates(
			Map<String, Event> googleCalendarEventsToLoad,
			List<Event> googleEventList) {
		if (googleEventList == null) {
			return;
		}
		// Completed events
		Iterator<Event> googleCompletedEventListIterator = googleEventList
				.iterator();
		while (googleCompletedEventListIterator.hasNext()) {
			Event googleEventToAddToList = googleCompletedEventListIterator
					.next();
			// duplicate
			if (googleCalendarEventsToLoad.containsKey(googleEventToAddToList
					.getId())) {
				Event completedEventToCompare = googleCalendarEventsToLoad
						.get(googleEventToAddToList.getId());
				if (completedEventToCompare.getUpdated().getValue() > googleEventToAddToList
						.getUpdated().getValue()) {
					continue;
				}
			}
			googleCalendarEventsToLoad.put(googleEventToAddToList.getId(),
					googleEventToAddToList);
		}
	}

	/**
	 * Load Google Calendar Events From List
	 * 
	 * @param googleCalendarEventsToLoad
	 * @param googleEventList
	 */
	protected void loadGoogleCalendarEventsFromList(
			Map<String, Event> googleCalendarEventsToLoad,
			List<Event> googleEventList) {
		if (googleEventList == null) {
			return;
		}
		// Default Events
		Iterator<Event> googleDefaultEventListIterator = googleEventList
				.iterator();
		while (googleDefaultEventListIterator.hasNext()) {
			Event googleEventToAddToList = googleDefaultEventListIterator
					.next();
			googleCalendarEventsToLoad.put(googleEventToAddToList.getId(),
					googleEventToAddToList);
		}
	}

	/**
	 * Pull sync new or existing task from remote
	 * 
	 * @param gCalEntry
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 * @throws IOException
	 */
	private void pullSyncGoogleCalendarTask(Event gCalEntry)
			throws TaskNotFoundException, InvalidTaskFormatException,
			IOException {
		logEnterMethod("pullSyncTask");
		if (Database.taskLists
				.containsGoogleCalendarSyncTask(gCalEntry.getId())) {

			Task localTask = Database.taskLists
					.getGoogleCalendarSyncTask(gCalEntry.getId());

			// pull sync deleted event
			if (Database.googleCalendar.isDeleted(gCalEntry)) {
				if (!localTask.isDeleted()) {
					logger.log(Level.INFO, "Deleting cancelled task : "
							+ gCalEntry.getSummary());
					// delete local task
					this.database.deleteTaskInTaskList(localTask);
				}
				return;
			}

			// pull sync newer task
			if (localTask.getTaskLastSync().isBefore(
					new DateTime(gCalEntry.getUpdated().getValue()))) {
				logger.log(Level.INFO,
						"pulling newer event : " + localTask.getTaskName());
				pullSyncExistingGoogleCalendarSyncedTask(gCalEntry, localTask);
			}
		} else {
			// Skip deleted events
			if (Database.googleCalendar.isDeleted(gCalEntry)) {
				return;
			}
			logger.log(Level.INFO,
					"pulling new event : " + gCalEntry.getSummary());
			pullSyncNewTask(gCalEntry);
		}
		logExitMethod("pullSyncTask");
	}

	/**
	 * 
	 * @param googleTaskToPull
	 */
	private void pullSyncNewGoogleTaskTask(
			com.google.api.services.tasks.model.Task googleTaskToPull) {
		logEnterMethod("pullSyncNewGoogleTaskTask");
		DateTime syncDateTime = setSyncTime(googleTaskToPull);
		// TODO
		logger.log(Level.INFO, "Adding new pull sync google task..."
				+ googleTaskToPull.getTitle());
		Task newTask = new FloatingTask(this.database.getNewTaskId(),
				googleTaskToPull, syncDateTime);
		logger.log(Level.INFO, "Added new pull sync google task : "
				+ googleTaskToPull.getTitle());
		Database.taskLists.updateTaskInTaskLists(newTask);
		logExitMethod("pullSyncNewGoogleTaskTask");
	}

	/**
	 * Creates new synced task from remote. Call pullSyncTask as it contains
	 * sync validation logic.
	 * 
	 * @param gCalEntry
	 * @throws UnknownHostException
	 */
	private void pullSyncNewTask(Event gCalEntry) throws UnknownHostException {
		logEnterMethod("pullSyncNewTask");
		DateTime syncDateTime = setSyncTime(gCalEntry);

		// add task from google calendar entry
		if (gCalEntry.getStart().getDateTime().toString()
				.equals(gCalEntry.getEnd().getDateTime().toString())) {
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
	 * Pull Sync
	 * 
	 * @param googleTaskToPull
	 * @param localTask
	 * @throws IOException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	private void pullSyncExistingGoogleTaskSyncedTask(
			com.google.api.services.tasks.model.Task googleTaskToPull,
			Task localTask) throws IOException, TaskNotFoundException,
			InvalidTaskFormatException {
		logEnterMethod("pullSyncExistingTask");
		updateSyncTask(localTask, googleTaskToPull);
		logExitMethod("pullSyncExistingTask");
	}

	/**
	 * Syncs existing local task with updated remote task Call pullSyncTask as
	 * it contains sync validation logic.
	 * 
	 * @param gCalEntry
	 * @param localTaskEntry
	 * @throws InvalidTaskFormatException
	 * @throws TaskNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	private void pullSyncExistingGoogleCalendarSyncedTask(Event gCalEntry,
			Task localTaskEntry) throws TaskNotFoundException,
			InvalidTaskFormatException, IOException {
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
		logger.log(Level.INFO, "Push Sync Tasks");
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
		if (localTask.isFloating()) {
			pushSyncFloatingTask(localTask);
		} else {
			pushSyncTimedAndDeadlineTask(localTask);
		}
	}

	protected void pushSyncFloatingTask(Task localTask)
			throws NullPointerException, IOException, ServiceException,
			TaskNotFoundException, InvalidTaskFormatException {
		logExitMethod("pushSyncFloatingTask");
		// push unsynced tasks
		if (TaskValidator.isUnsyncedTask(localTask)) {
			logger.log(Level.INFO, "Pushing new floating sync task : "
					+ localTask.getTaskName());
			pushSyncNewFloatingTask(localTask);
		} else {
			// remove deleted sync task
			if (localTask.isDeleted()) {
				logger.log(
						Level.INFO,
						"Removing deleted synced floating task : "
								+ localTask.getTaskName());
				deleteExistingFloatingSyncTask(localTask);
			} else {
				// push updated sync tasks
				if (localTask.getTaskUpdated().isAfter(
						localTask.getTaskLastSync())) {
					logger.log(Level.INFO, "Pushing updated floating task : "
							+ localTask.getTaskName());
					pushSyncExistingFloatingSyncTask(localTask);
				}
			}
		}
		logExitMethod("pushSyncFloatingTask");
	}

	protected void deleteExistingFloatingSyncTask(Task localTask) {
		logExitMethod("deleteExistingFloatingSyncTask");
		try {
			Database.googleTasks.deleteTask(localTask.getGTaskId());
		} catch (NullPointerException e) {
			e.printStackTrace();
			logger.log(Level.FINER, e.getMessage());
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			logger.log(Level.FINER, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.log(Level.FINER, e.getMessage());
			e.printStackTrace();
		}
		logExitMethod("deleteExistingFloatingSyncTask");
	}

	protected void pushSyncTimedAndDeadlineTask(Task localTask)
			throws IOException, ServiceException, TaskNotFoundException,
			InvalidTaskFormatException {
		logEnterMethod("pushSyncTimedAndDeadlineTask");
		// push unsynced tasks
		if (TaskValidator.isUnsyncedTask(localTask)) {
			logger.log(Level.INFO,
					"Pushing new sync task : " + localTask.getTaskName());
			pushSyncNewTimedAndDeadlineTask(localTask);
		} else {
			// remove deleted sync task
			if (localTask.isDeleted()) {
				logger.log(Level.INFO, "Removing deleted synced task : "
						+ localTask.getTaskName());
				deleteExistingTimedAndDeadlineSyncTask(localTask);
			} else {
				// push updated sync tasks
				if (localTask.getTaskUpdated().isAfter(
						localTask.getTaskLastSync())) {
					logger.log(Level.INFO, "Pushing updated task : "
							+ localTask.getTaskName());
					pushSyncExistingTimedAndDeadlineTask(localTask);
				}
			}
		}
		logExitMethod("pushSyncTimedAndDeadlineTask");
	}

	protected void deleteExistingTimedAndDeadlineSyncTask(Task localTask) {
		try {
			Database.googleCalendar.deleteEvent(localTask.getgCalTaskId());
		} catch (NullPointerException e) {
			logger.log(Level.FINER, e.getMessage());
		}
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
	private void pushSyncNewTimedAndDeadlineTask(Task localTask)
			throws NullPointerException, IOException, ServiceException,
			TaskNotFoundException, InvalidTaskFormatException {
		logEnterMethod("pushSyncNewTask");
		// adds event to google calendar
		Event addedGCalEvent = Database.googleCalendar.createEvent(localTask);
		updateSyncTask(localTask, addedGCalEvent);
		logExitMethod("pushSyncNewTask");
	}

	private void pushSyncNewFloatingTask(Task localTask) throws IOException,
			TaskNotFoundException, InvalidTaskFormatException {
		logEnterMethod("pushSyncNewFloatingTask");
		// adds event to google tasks
		com.google.api.services.tasks.model.Task addedGTask = Database.googleTasks
				.createTask(localTask.getTaskName(), localTask.isDone());
		updateSyncTask(localTask, addedGTask);
		logExitMethod("pushSyncNewFloatingTask");
	}

	private void pushSyncExistingFloatingSyncTask(Task localTask) {
		logEnterMethod("pushSyncExistingFloatingSyncTask");
		// update remote task
		com.google.api.services.tasks.model.Task updatedGTask;
		try {
			updatedGTask = Database.googleTasks.updateTask(
					localTask.getGTaskId(), localTask.getTaskName(),
					localTask.isDone());
			updateSyncTask(localTask, updatedGTask);
		} catch (ResourceNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TaskNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidTaskFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logExitMethod("pushSyncExistingFloatingSyncTask");
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
	private void pushSyncExistingTimedAndDeadlineTask(Task localTask) {
		logEnterMethod("pushSyncExistingTask");
		// update remote task
		Event updatedGcalEvent;
		try {
			updatedGcalEvent = Database.googleCalendar.updateEvent(localTask
					.clone());

			updateSyncTask(localTask, updatedGcalEvent);
		} catch (ResourceNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TaskNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidTaskFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logExitMethod("pushSyncExistingTask");
	}

	/**
	 * Updates local synced task with newer Calendar Event
	 * 
	 * @param updatedTask
	 * @throws IOException
	 * @throws Exception
	 * @throws ServiceException
	 */
	private Task updateSyncTask(Task localSyncTaskToUpdate, Event gCalEntry)
			throws TaskNotFoundException, InvalidTaskFormatException,
			IOException {
		logEnterMethod("updateSyncTask");
		if (!Database.taskLists.containsTask(localSyncTaskToUpdate.getTaskId())) {
			throw new TaskNotFoundException(
					Database.EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		if (!TaskValidator.isTaskValid(localSyncTaskToUpdate)) {
			throw new InvalidTaskFormatException(
					Database.EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		}

		DateTime syncDateTime = setSyncTime(gCalEntry);

		localSyncTaskToUpdate = updateLocalSyncTask(localSyncTaskToUpdate,
				gCalEntry, syncDateTime);

		Database.taskLists.updateTaskInTaskLists(localSyncTaskToUpdate);
		Database.saveTaskRecordFile();
		logExitMethod("updateSyncTask");
		return localSyncTaskToUpdate;
	}

	private Task updateSyncTask(Task localSyncTaskToUpdate,
			com.google.api.services.tasks.model.Task addedGTask)
			throws IOException, TaskNotFoundException,
			InvalidTaskFormatException {
		logEnterMethod("updateSyncTask");
		if (!Database.taskLists.containsTask(localSyncTaskToUpdate.getTaskId())) {
			throw new TaskNotFoundException(
					Database.EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		}

		if (!TaskValidator.isTaskValid(localSyncTaskToUpdate)) {
			throw new InvalidTaskFormatException(
					Database.EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		}
		System.out.println("Update Sync Task");
		DateTime syncDateTime = setSyncTime(addedGTask);
		localSyncTaskToUpdate = updateLocalSyncTask(localSyncTaskToUpdate,
				addedGTask, syncDateTime);

		Database.taskLists.updateTaskInTaskLists(localSyncTaskToUpdate);
		Database.saveTaskRecordFile();
		logExitMethod("updateSyncTask");
		return localSyncTaskToUpdate;
	}

	protected Task updateLocalSyncTask(Task localSyncTaskToUpdate,
			com.google.api.services.tasks.model.Task addedGTask,
			DateTime syncDateTime) {
		logEnterMethod("updateLocalSyncTask");
		localSyncTaskToUpdate.setGTaskId(addedGTask.getId());
		localSyncTaskToUpdate.setTaskLastSync(syncDateTime);
		logEnterMethod("updateLocalSyncTask");
		return localSyncTaskToUpdate;
	}

	/**
	 * Updates local sync task
	 * 
	 * @param localSyncTaskToUpdate
	 * @param gCalEntry
	 * @param syncDateTime
	 * @throws IOException
	 */
	private Task updateLocalSyncTask(Task localSyncTaskToUpdate,
			Event gCalEntry, DateTime syncDateTime) throws IOException {
		logEnterMethod("updateLocalSyncTask");
		Task updatedTask;
		// Update Task Type
		if (gCalEntry.getStart().getDateTime().toString()
				.equals(gCalEntry.getEnd().getDateTime().toString())) {
			updatedTask = new DeadlineTask(localSyncTaskToUpdate.getTaskId(),
					gCalEntry, syncDateTime);
		} else {
			updatedTask = new TimedTask(localSyncTaskToUpdate.getTaskId(),
					gCalEntry, syncDateTime);
		}
		logExitMethod("updateLocalSyncTask");
		return updatedTask;
	}

	/**
	 * Sets sync time for local tasks from google calendar entry
	 * 
	 * @param gCalEntry
	 * @return sync datetime for updating local task
	 */
	private DateTime setSyncTime(Event gCalEntry) {
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

	private DateTime setSyncTime(
			com.google.api.services.tasks.model.Task addedGTask) {
		logEnterMethod("setSyncTime");
		new DateTime();
		DateTime syncDateTime = DateTime.now();
		if (addedGTask != null) {
			syncDateTime = new DateTime(addedGTask.getUpdated().getValue());
		}
		// assert that sync DateTimes between local and remote are equal
		assert (syncDateTime.isEqual(new DateTime(addedGTask.getUpdated()
				.toString())));
		logExitMethod("setSyncTime");
		return syncDateTime;
	}

	/**
	 * Logger Methods
	 */

	/**
	 * Logger trace method entry
	 * 
	 * @param methodName
	 */
	void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}

	/**
	 * Logger trace method exit
	 * 
	 * @param methodName
	 */
	void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}

}