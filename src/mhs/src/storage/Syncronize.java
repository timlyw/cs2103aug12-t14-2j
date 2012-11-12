//@author A0087048X

package mhs.src.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
		logEnterMethod("initializeSyncronizeBackgroundExecutor");
		logExitMethod("initializeSyncronizeBackgroundExecutor");
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
		logEnterMethod("initializeGoogleCalendarServicesAndSync");
		try {
			if (isGoogleServicesInitializedAndSyncEnabled(disableSyncronize)) {
				enableRemoteSync();
				syncronizeDatabases();
			} else {
				disableRemoteSync();
			}
		} catch (AuthenticationException | UnknownHostException
				| NoActiveCredentialException e) {
			disableRemoteSync();
			logger.log(Level.FINER, e.getMessage());
		} catch (ServiceException e) {
			logger.log(Level.FINER, e.getMessage());
		}
		logExitMethod("initializeGoogleCalendarServicesAndSync");
	}

	/**
	 * Checks if Google Services is Initialized And Sync is Enabled
	 * 
	 * @param disableSyncronize
	 * @return
	 * @throws AuthenticationException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws NoActiveCredentialException
	 */
	protected boolean isGoogleServicesInitializedAndSyncEnabled(
			boolean disableSyncronize) throws AuthenticationException,
			IOException, ServiceException, NoActiveCredentialException {
		logEnterMethod("isGoogleServicesInitializedAndSyncEnabled");
		logExitMethod("isGoogleServicesInitializedAndSyncEnabled");
		return database.initializeGoogleServices() && !disableSyncronize;
	}

	/**
	 * Initialize Push-Sync Background tasks list
	 */
	private void initializePushSyncBackgroundTasksList() {
		logEnterMethod("initializePushSyncBackgroundTasksList");
		logExitMethod("initializePushSyncBackgroundTasksList");
		syncBackgroundTasks = new ConcurrentHashMap<String, Callable<Boolean>>();
	}

	/**
	 * Schedule Push-Sync Task
	 * 
	 * @param taskToScheduleSync
	 */
	synchronized void schedulePushSyncTask(Task taskToScheduleSync) {
		logEnterMethod("schedulePushSyncTask");
		Callable<Boolean> pushTaskToSchedule = new SyncPushTask(
				taskToScheduleSync, this);
		syncBackgroundTasks.put(getSyncTaskQueueUid(), pushTaskToSchedule);
		syncronizeBackgroundExecutor.submit(pushTaskToSchedule);
		logExitMethod("schedulePushSyncTask");
	}

	/**
	 * Get unique id for SyncTaskQueueUID for keeping track of background tasks
	 * 
	 * @return random uid string
	 */
	synchronized private String getSyncTaskQueueUid() {
		logEnterMethod("getSyncTaskQueueUid");
		logExitMethod("getSyncTaskQueueUid");
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
	 * Syncronizes Databases (local storage and google services)
	 * 
	 * @return true if successful
	 */
	boolean syncronizeDatabases() {
		logEnterMethod("syncronizeDatabases");
		logger.log(Level.INFO, "Syncronizing Databases");
		if (!Database.isGoogleServicesInstantiated()) {
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
	 * Google Calendar Pull Sync
	 */

	/**
	 * Pull Sync Google Calendar Events
	 * 
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	private void pullSyncGoogleCalendarEvents()
			throws ResourceNotFoundException, IOException,
			TaskNotFoundException, InvalidTaskFormatException {
		logEnterMethod("pullSyncGoogleCalendarEvents");
		Map<String, Event> allGoogleCalendarEvents = getAllGoogleCalendarEvents();
		pullSyncEventsInGoogleCalendarEventList(allGoogleCalendarEvents);
		logExitMethod("pullSyncGoogleCalendarEvents");
	}

	/**
	 * Returns all google calendar events within Database specified Sync start
	 * and end datetimes
	 * 
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 */
	private Map<String, Event> getAllGoogleCalendarEvents()
			throws ResourceNotFoundException, IOException {
		logEnterMethod("getAllGoogleCalendarEvents");
		logExitMethod("getAllGoogleCalendarEvents");
		return retrieveGoogleCalendarEvents(
				Database.syncStartDateTime.toString(),
				Database.syncEndDateTime.toString());
	}

	/**
	 * Retrieve Google Calendar Events from calendars
	 * 
	 * @return
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	private Map<String, Event> retrieveGoogleCalendarEvents(
			String startDateTime, String endDateTime)
			throws ResourceNotFoundException, IOException {
		logEnterMethod("retrieveGoogleCalendarEvents");

		Map<String, Event> allGoogleCalendarEventsList = new LinkedHashMap<>();

		loadAllGoogleCalendarEventsListWithDefaultEvents(startDateTime,
				endDateTime, allGoogleCalendarEventsList);
		loadAllGoogleCalendarEventsListWithCompletedEvents(startDateTime,
				endDateTime, allGoogleCalendarEventsList);
		loadAllGoogleCalendarEventsListWithDeletedDefaultEvents(startDateTime,
				endDateTime, allGoogleCalendarEventsList);
		loadAllGoogleCalendarEventsListWithDeletedCompletedEvents(
				startDateTime, endDateTime, allGoogleCalendarEventsList);

		logExitMethod("retrieveGoogleCalendarEvents");
		return allGoogleCalendarEventsList;
	}

	/**
	 * Load All Google Calendar Events List With Default Events
	 * 
	 * @param startDateTime
	 * @param endDateTime
	 * @param allGoogleCalendarEventsList
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	private void loadAllGoogleCalendarEventsListWithDefaultEvents(
			String startDateTime, String endDateTime,
			Map<String, Event> allGoogleCalendarEventsList) throws IOException,
			ResourceNotFoundException {
		logEnterMethod("loadAllGoogleCalendarEventsListWithDefaultEvents");
		List<Event> googleDefaultEvents = Database.googleCalendar
				.retrieveDefaultEvents(startDateTime, endDateTime, false);
		loadGoogleCalendarEventsFromList(allGoogleCalendarEventsList,
				googleDefaultEvents);
		logExitMethod("loadAllGoogleCalendarEventsListWithDefaultEvents");
	}

	/**
	 * Load All Google Calendar Events List With Completed Events
	 * 
	 * @param startDateTime
	 * @param endDateTime
	 * @param allGoogleCalendarEventsList
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	private void loadAllGoogleCalendarEventsListWithCompletedEvents(
			String startDateTime, String endDateTime,
			Map<String, Event> allGoogleCalendarEventsList) throws IOException,
			ResourceNotFoundException {
		logEnterMethod("loadAllGoogleCalendarEventsListWithCompletedEvents");
		List<Event> googleCompletedEvents = Database.googleCalendar
				.retrieveCompletedEvents(startDateTime, endDateTime, false);
		loadGoogleCalendarEventListWithUniqueNewerEvent(
				allGoogleCalendarEventsList, googleCompletedEvents);
		logExitMethod("loadAllGoogleCalendarEventsListWithCompletedEvents");
	}

	/**
	 * Load All Google Calendar Events List With Deleted Default Events
	 * 
	 * @param startDateTime
	 * @param endDateTime
	 * @param allGoogleCalendarEventsList
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	private void loadAllGoogleCalendarEventsListWithDeletedDefaultEvents(
			String startDateTime, String endDateTime,
			Map<String, Event> allGoogleCalendarEventsList) throws IOException,
			ResourceNotFoundException {
		logEnterMethod("loadAllGoogleCalendarEventsListWithDeletedDefaultEvents");
		List<Event> googleDeletedDefaultEvents = Database.googleCalendar
				.retrieveDefaultEvents(startDateTime, endDateTime, true);
		loadGoogleCalendarEventsFromList(allGoogleCalendarEventsList,
				googleDeletedDefaultEvents);
		logExitMethod("loadAllGoogleCalendarEventsListWithDeletedDefaultEvents");
	}

	/**
	 * Load All Google Calendar Events List With Deleted Completed Events
	 * 
	 * @param startDateTime
	 * @param endDateTime
	 * @param allGoogleCalendarEventsList
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	private void loadAllGoogleCalendarEventsListWithDeletedCompletedEvents(
			String startDateTime, String endDateTime,
			Map<String, Event> allGoogleCalendarEventsList) throws IOException,
			ResourceNotFoundException {
		logEnterMethod("loadAllGoogleCalendarEventsListWithDeletedCompletedEvents");
		List<Event> googleDeletedCompletedEvents = Database.googleCalendar
				.retrieveCompletedEvents(startDateTime, endDateTime, true);
		loadGoogleCalendarEventsFromList(allGoogleCalendarEventsList,
				googleDeletedCompletedEvents);
		logExitMethod("loadAllGoogleCalendarEventsListWithDeletedCompletedEvents");
	}

	/**
	 * Loads Google Calendar EventList Without Duplicates, if duplicates occur,
	 * most recent Event is placed
	 * 
	 * @param googleCalendarEventsToLoad
	 * @param googleEventList
	 */
	private void loadGoogleCalendarEventListWithUniqueNewerEvent(
			Map<String, Event> googleCalendarEventsToLoad,
			List<Event> googleEventList) {
		logEnterMethod("loadGoogleCalendarEventListWithoutDuplicates");
		if (googleEventList == null) {
			return;
		}
		Iterator<Event> googleEventListIterator = googleEventList.iterator();
		addEventsToGoogleCalendarListWithUniqueNewerEvent(
				googleCalendarEventsToLoad, googleEventListIterator);
		logExitMethod("loadGoogleCalendarEventListWithoutDuplicates");
	}

	/**
	 * Add Events To GoogleCalendarList With Unique Newer Event if duplicates
	 * occur
	 * 
	 * @param googleCalendarEventsToLoad
	 * @param googleEventListIterator
	 */
	private void addEventsToGoogleCalendarListWithUniqueNewerEvent(
			Map<String, Event> googleCalendarEventsToLoad,
			Iterator<Event> googleEventListIterator) {
		logEnterMethod("addEventsToGoogleCalendarListWithUniqueNewerEvent");
		while (googleEventListIterator.hasNext()) {
			Event googleEventToAddToList = googleEventListIterator.next();
			if (isDuplicateEvent(googleCalendarEventsToLoad,
					googleEventToAddToList)) {
				Event completedEventToCompare = googleCalendarEventsToLoad
						.get(googleEventToAddToList.getId());
				if (isEventToAddNewer(googleEventToAddToList,
						completedEventToCompare)) {
					continue;
				}
			}
			googleCalendarEventsToLoad.put(googleEventToAddToList.getId(),
					googleEventToAddToList);
		}
		logExitMethod("addEventsToGoogleCalendarListWithUniqueNewerEvent");
	}

	/**
	 * Returns true if event to add is newer
	 * 
	 * @param googleEventToAddToList
	 * @param completedEventToCompare
	 * @return
	 */
	private boolean isEventToAddNewer(Event googleEventToAddToList,
			Event completedEventToCompare) {
		return completedEventToCompare.getUpdated().getValue() > googleEventToAddToList
				.getUpdated().getValue();
	}

	/**
	 * Returns true if event is duplicate in list
	 * 
	 * @param googleCalendarEventsToLoad
	 * @param googleEventToAddToList
	 * @return
	 */
	private boolean isDuplicateEvent(
			Map<String, Event> googleCalendarEventsToLoad,
			Event googleEventToAddToList) {
		return googleCalendarEventsToLoad.containsKey(googleEventToAddToList
				.getId());
	}

	/**
	 * Load Google Calendar Events From List
	 * 
	 * @param googleCalendarEventsToLoad
	 * @param googleEventList
	 */
	private void loadGoogleCalendarEventsFromList(
			Map<String, Event> googleCalendarEventsToLoad,
			List<Event> googleEventList) {
		if (googleEventList == null) {
			return;
		}
		Iterator<Event> googleEventListIterator = googleEventList.iterator();
		while (googleEventListIterator.hasNext()) {
			Event googleEventToAddToList = googleEventListIterator.next();
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
		logEnterMethod("pullSyncGoogleCalendarTask");
		if (hasGoogleCalendarSyncTask(gCalEntry)) {
			performGoogleCalendarPullSyncForExistingSyncTask(gCalEntry);
		} else {
			performGoogleCalendarPullSyncForNewTask(gCalEntry);
		}
		logExitMethod("pullSyncGoogleCalendarTask");
	}

	/**
	 * Perform Pull Sync For Existing Google Calendar Task
	 * 
	 * @param gCalEntry
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 * @throws IOException
	 */
	private void performGoogleCalendarPullSyncForExistingSyncTask(
			Event gCalEntry) throws TaskNotFoundException,
			InvalidTaskFormatException, IOException {
		logEnterMethod("performPullSyncForExistingGoogleCalendarTask");

		Task localTask = Database.taskLists.getGoogleCalendarSyncTask(gCalEntry
				.getId());

		if (Database.googleCalendar.isDeleted(gCalEntry)) {
			performGoogleCalendarPullSyncForDeletedLocalSyncTask(gCalEntry,
					localTask);
			return;
		}

		if (isGoogleCalendarEventNewerThanLocalSyncTask(gCalEntry, localTask)) {
			logger.log(Level.INFO,
					"pulling newer event : " + localTask.getTaskName());
			performGoogleCalendarPullSyncforExistingGoogleCalendarSyncTask(
					gCalEntry, localTask);
		}
		logExitMethod("performPullSyncForExistingGoogleCalendarTask");
	}

	/**
	 * Perform Google Calendar Pull Sync For Deleted Local Sync Task
	 * 
	 * @param gCalEntry
	 * @param localTask
	 * @throws TaskNotFoundException
	 */
	private void performGoogleCalendarPullSyncForDeletedLocalSyncTask(
			Event gCalEntry, Task localTask) throws TaskNotFoundException {
		if (!localTask.isDeleted()) {
			logger.log(Level.INFO,
					"Deleting cancelled task : " + gCalEntry.getSummary());
			this.database.removeRecord(localTask);
		}
	}

	/**
	 * Perform Google Calendar Pull Sync For New Task
	 * 
	 * @param gCalEntry
	 * @throws UnknownHostException
	 */
	private void performGoogleCalendarPullSyncForNewTask(Event gCalEntry)
			throws UnknownHostException {
		if (Database.googleCalendar.isDeleted(gCalEntry)) {
			return;
		}
		logger.log(Level.INFO, "pulling new event : " + gCalEntry.getSummary());
		pullSyncNewTask(gCalEntry);
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
	private void performGoogleCalendarPullSyncforExistingGoogleCalendarSyncTask(
			Event gCalEntry, Task localTaskEntry) throws TaskNotFoundException,
			InvalidTaskFormatException, IOException {
		logEnterMethod("pullSyncExistingTask");
		updateSyncTask(localTaskEntry, gCalEntry);
		logExitMethod("pullSyncExistingTask");
	}

	/**
	 * Checks if Google Calendar Event is Newer Than Local Sync Task
	 * 
	 * @param gCalEntry
	 * @param localTask
	 * @return
	 */
	private boolean isGoogleCalendarEventNewerThanLocalSyncTask(
			Event gCalEntry, Task localTask) {
		return localTask.getTaskLastSync().isBefore(
				new DateTime(gCalEntry.getUpdated().getValue()));
	}

	/**
	 * Checks if local Google Calendar Sync Task exists
	 * 
	 * @param gCalEntry
	 * @return
	 */
	private boolean hasGoogleCalendarSyncTask(Event gCalEntry) {
		return Database.taskLists.containsGoogleCalendarSyncTask(gCalEntry
				.getId());
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
		if (isGoogleEventWithoutDuration(gCalEntry)) {
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
	 * Pull Sync Google Tasks
	 * 
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	private void pullSyncGoogleTasksTasks() throws IOException,
			ResourceNotFoundException, TaskNotFoundException,
			InvalidTaskFormatException {
		logExitMethod("pullSyncGoogleTasksTasks");
		List<com.google.api.services.tasks.model.Task> googleTasksTaskList = getGoogleTaskListToPullSync();
		performPullSyncOnGoogleTasksInTaskList(googleTasksTaskList);
		logExitMethod("pullSyncGoogleTasksTasks");
	}

	/**
	 * Pull sync events in specified GoogleCalendarEvent list
	 * 
	 * @param googleCalendarEventsListToPullSync
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 * @throws IOException
	 */
	private void pullSyncEventsInGoogleCalendarEventList(
			Map<String, Event> googleCalendarEventsListToPullSync)
			throws TaskNotFoundException, InvalidTaskFormatException,
			IOException {
		for (Map.Entry<String, Event> entry : googleCalendarEventsListToPullSync
				.entrySet()) {
			pullSyncGoogleCalendarTask(entry.getValue());
		}
	}

	/**
	 * Returns GoogleTaskList containing all remote Google Tasks for pull sync
	 * 
	 * @return
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	private List<com.google.api.services.tasks.model.Task> getGoogleTaskListToPullSync()
			throws IOException, ResourceNotFoundException {
		return Database.googleTasks.retrieveTasks();
	}

	/**
	 * Perform Pull Sync on all tasks in GoogleTasks Task list
	 * 
	 * @param googleTasksTaskList
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	private void performPullSyncOnGoogleTasksInTaskList(
			List<com.google.api.services.tasks.model.Task> googleTasksTaskList)
			throws ResourceNotFoundException, IOException,
			TaskNotFoundException, InvalidTaskFormatException {
		Iterator<com.google.api.services.tasks.model.Task> googleCompletedEventListIterator = googleTasksTaskList
				.iterator();
		while (googleCompletedEventListIterator.hasNext()) {
			com.google.api.services.tasks.model.Task googleTaskToPull = googleCompletedEventListIterator
					.next();
			pullSyncGoogleTask(googleTaskToPull);
		}
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
		logExitMethod("pullSyncGoogleTask");
		if (hasLocalGoogleSyncTask(googleTaskToPull)) {
			performGoogleTaskPullSyncOnExistingLocalSyncTask(googleTaskToPull);
		} else {
			performNewGoogleTaskPullSync(googleTaskToPull);
		}
		logExitMethod("pullSyncGoogleTask");
	}

	/**
	 * Perform Google Task Pull Sync On Existing Local Sync Task
	 * 
	 * @param googleTaskToPull
	 * @throws TaskNotFoundException
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	private void performGoogleTaskPullSyncOnExistingLocalSyncTask(
			com.google.api.services.tasks.model.Task googleTaskToPull)
			throws TaskNotFoundException, IOException,
			ResourceNotFoundException, InvalidTaskFormatException {
		logEnterMethod("performGoogleTaskPullSyncOnExistingLocalSyncTask");
		Task localTask = Database.taskLists
				.getGoogleTaskSyncTask(googleTaskToPull.getId());

		if (Database.googleTasks.isDeleted(googleTaskToPull)) {
			performPullSyncForDeletedGoogleTask(googleTaskToPull, localTask);
			logExitMethod("performGoogleTaskPullSyncOnExistingLocalSyncTask");
			return;
		}

		if (isGoogleTaskNewerThanLocalSyncTask(googleTaskToPull, localTask)) {
			performPullSyncForExistingGoogleTaskSyncTask(googleTaskToPull,
					localTask);
		}
		logExitMethod("performGoogleTaskPullSyncOnExistingLocalSyncTask");
	}

	/**
	 * Perform Pull Sync for Deleted Google Task
	 * 
	 * @param googleTaskToPull
	 * @param localTask
	 * @throws TaskNotFoundException
	 */
	private void performPullSyncForDeletedGoogleTask(
			com.google.api.services.tasks.model.Task googleTaskToPull,
			Task localTask) throws TaskNotFoundException {
		if (!localTask.isDeleted()) {
			logger.log(Level.INFO, "Deleting cancelled task : "
					+ googleTaskToPull.getTitle());
			this.database.removeRecord(localTask);
		}
	}

	/**
	 * Perform New Google Task Pull Sync
	 * 
	 * @param googleTaskToPull
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	private void performNewGoogleTaskPullSync(
			com.google.api.services.tasks.model.Task googleTaskToPull)
			throws IOException, ResourceNotFoundException {
		logExitMethod("performNewGoogleTaskPullSync");
		if (!Database.googleTasks.isDeleted(googleTaskToPull)) {
			performPullSyncForNewGoogleTaskLocalSyncTask(googleTaskToPull);
		}
		logExitMethod("performNewGoogleTaskPullSync");
	}

	/**
	 * Pull Sync for existing google task sync task
	 * 
	 * @param googleTaskToPull
	 * @param localTask
	 * @throws IOException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	private void performPullSyncForExistingGoogleTaskSyncTask(
			com.google.api.services.tasks.model.Task googleTaskToPull,
			Task localTask) throws IOException, TaskNotFoundException,
			InvalidTaskFormatException {
		logEnterMethod("pullSyncExistingTask");
		logger.log(Level.INFO,
				"pulling newer task : " + localTask.getTaskName());
		updateSyncTask(localTask, googleTaskToPull);
		logExitMethod("pullSyncExistingTask");
	}

	/**
	 * Pull Sync for New Google Task Task
	 * 
	 * @param googleTaskToPull
	 */
	private void performPullSyncForNewGoogleTaskLocalSyncTask(
			com.google.api.services.tasks.model.Task googleTaskToPull) {
		logEnterMethod("pullSyncNewGoogleTaskTask");
		DateTime syncDateTime = setSyncTime(googleTaskToPull);
		logger.log(Level.INFO, "Adding new pull sync google task..."
				+ googleTaskToPull.getTitle());
		Task newTask = new FloatingTask(this.database.getNewTaskId(),
				googleTaskToPull, syncDateTime);
		logger.log(Level.INFO, "Added new pull sync google task : "
				+ googleTaskToPull.getTitle());
		Database.taskLists.updateTaskInTaskLists(newTask);
		logExitMethod("pullSyncNewGoogleTaskTask");
	}

	/*
	 * Checks if Google Task is newer than synced local task
	 */
	private boolean isGoogleTaskNewerThanLocalSyncTask(
			com.google.api.services.tasks.model.Task googleTaskToPull,
			Task localTask) {
		return localTask.getTaskLastSync().isBefore(
				new DateTime(googleTaskToPull.getUpdated().getValue()));
	}

	/**
	 * Returns true if Google Task has a corresponding local task to sync with
	 * 
	 * @param googleTaskToPull
	 * @return
	 */
	private boolean hasLocalGoogleSyncTask(
			com.google.api.services.tasks.model.Task googleTaskToPull) {
		return Database.taskLists.containsGoogleTaskSyncTask(googleTaskToPull
				.getId());
	}

	/**
	 * Push Sync Logic
	 */

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

	/**
	 * Push sync logic for floating task
	 * 
	 * @param localTask
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	private void pushSyncFloatingTask(Task localTask)
			throws NullPointerException, IOException, ServiceException,
			TaskNotFoundException, InvalidTaskFormatException {
		logExitMethod("pushSyncFloatingTask");
		// push unsynced tasks
		if (TaskValidator.isUnsyncedTask(localTask)) {
			pushSyncNewFloatingTask(localTask);
		} else {
			performPushSyncForExistingFloatingTask(localTask);
		}
		logExitMethod("pushSyncFloatingTask");
	}

	/**
	 * Perform Push Sync For Existing Floating Task
	 * 
	 * @param localTask
	 */
	private void performPushSyncForExistingFloatingTask(Task localTask) {
		if (localTask.isDeleted()) {
			deleteExistingFloatingSyncTask(localTask);
		} else {
			pushUpdatedFloatingSyncTask(localTask);
		}
	}

	/**
	 * Delete Existing Floating Sync Task
	 * 
	 * @param localTask
	 */
	private void deleteExistingFloatingSyncTask(Task localTask) {
		logExitMethod("deleteExistingFloatingSyncTask");
		logger.log(Level.INFO, "Removing deleted synced floating task : "
				+ localTask.getTaskName());
		try {
			Database.googleTasks.deleteTask(localTask.getGTaskId());
		} catch (NullPointerException e) {
			logger.log(Level.FINER, e.getMessage());
			e.printStackTrace();
		} catch (ResourceNotFoundException e) {
			logger.log(Level.FINER, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.log(Level.FINER, e.getMessage());
			e.printStackTrace();
		}
		logExitMethod("deleteExistingFloatingSyncTask");
	}

	/**
	 * Push Updated Floating Sync Task
	 * 
	 * @param localTask
	 */
	private void pushUpdatedFloatingSyncTask(Task localTask) {
		// push updated sync tasks
		if (localTask.getTaskUpdated().isAfter(localTask.getTaskLastSync())) {
			logger.log(Level.INFO, "Pushing updated floating task : "
					+ localTask.getTaskName());
			pushSyncExistingFloatingSyncTask(localTask);
		}
	}

	/**
	 * Push Sync logic for Timed/Deadline Task
	 * 
	 * @param localTask
	 * @throws IOException
	 * @throws ServiceException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	private void pushSyncTimedAndDeadlineTask(Task localTask)
			throws IOException, ServiceException, TaskNotFoundException,
			InvalidTaskFormatException {
		logEnterMethod("pushSyncTimedAndDeadlineTask");
		if (TaskValidator.isUnsyncedTask(localTask)) {
			performPushSyncForUnsyncedTimedAndDeadlineTask(localTask);
		} else {
			performPushSyncForSyncedTimedAndDeadlineTask(localTask);
		}
		logExitMethod("pushSyncTimedAndDeadlineTask");
	}

	/**
	 * Perform Push Sync For Unsynced Timed/Deadline Task
	 * 
	 * @param localTask
	 * @throws IOException
	 * @throws ServiceException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	private void performPushSyncForUnsyncedTimedAndDeadlineTask(Task localTask)
			throws IOException, ServiceException, TaskNotFoundException,
			InvalidTaskFormatException {
		logger.log(Level.INFO,
				"Pushing new sync task : " + localTask.getTaskName());
		pushSyncNewTimedAndDeadlineTask(localTask);
	}

	/**
	 * Perform Push Sync For Synced Timed And Deadline Task
	 * 
	 * @param localTask
	 */
	private void performPushSyncForSyncedTimedAndDeadlineTask(Task localTask) {
		if (localTask.isDeleted()) {
			deleteExistingTimedAndDeadlineSyncTask(localTask);
		} else {
			if (localTask.getTaskUpdated().isAfter(localTask.getTaskLastSync())) {
				pushSyncExistingTimedAndDeadlineTask(localTask);
			}
		}
	}

	/**
	 * Delete Existing Timed And Deadline Sync Task
	 * 
	 * @param localTask
	 */
	private void deleteExistingTimedAndDeadlineSyncTask(Task localTask) {
		logger.log(Level.INFO,
				"Removing deleted synced task : " + localTask.getTaskName());
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

	/**
	 * Push Sync New Floating Task
	 * 
	 * @param localTask
	 * @throws IOException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	private void pushSyncNewFloatingTask(Task localTask) throws IOException,
			TaskNotFoundException, InvalidTaskFormatException {
		logEnterMethod("pushSyncNewFloatingTask");
		logger.log(Level.INFO,
				"Pushing new floating sync task : " + localTask.getTaskName());
		// adds event to google tasks
		com.google.api.services.tasks.model.Task addedGTask = Database.googleTasks
				.createTask(localTask.getTaskName(), localTask.isDone());
		updateSyncTask(localTask, addedGTask);
		logExitMethod("pushSyncNewFloatingTask");
	}

	/**
	 * Push Sync Existing Floating Sync Task
	 * 
	 * @param localTask
	 */
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
			logger.log(Level.FINER, e.getMessage());
		} catch (TaskNotFoundException e) {
			logger.log(Level.FINER, e.getMessage());
		} catch (InvalidTaskFormatException e) {
			logger.log(Level.FINER, e.getMessage());
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
		logger.log(Level.INFO,
				"Pushing updated task : " + localTask.getTaskName());
		// update remote task
		Event updatedGcalEvent;
		try {
			updatedGcalEvent = Database.googleCalendar.updateEvent(localTask
					.clone());

			updateSyncTask(localTask, updatedGcalEvent);
		} catch (ResourceNotFoundException | IOException e) {
			logger.log(Level.FINER, e.getMessage());
		} catch (TaskNotFoundException e) {
			logger.log(Level.FINER, e.getMessage());
		} catch (InvalidTaskFormatException e) {
			logger.log(Level.FINER, e.getMessage());
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
		localSyncTaskToUpdate = getUpdatedSyncTaskFromGoogleEvent(
				localSyncTaskToUpdate, gCalEntry, syncDateTime);
		Database.taskLists.updateTaskInTaskLists(localSyncTaskToUpdate);
		Database.saveTaskRecordFile();
		logExitMethod("updateSyncTask");
		return localSyncTaskToUpdate;
	}

	/**
	 * Update Sync Task with Google Task and Syncs Times
	 * 
	 * @param localSyncTaskToUpdate
	 * @param addedGTask
	 * @return
	 * @throws IOException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
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
		DateTime syncDateTime = setSyncTime(addedGTask);
		localSyncTaskToUpdate = getUpdatedSyncTaskFromGoogleTask(
				localSyncTaskToUpdate, addedGTask, syncDateTime);
		Database.taskLists.updateTaskInTaskLists(localSyncTaskToUpdate);
		Database.saveTaskRecordFile();

		logExitMethod("updateSyncTask");
		return localSyncTaskToUpdate;
	}

	/**
	 * Returns updated sync task from Google Task
	 * 
	 * @param localSyncTaskToUpdate
	 * @param addedGTask
	 * @param syncDateTime
	 * @return Updated SyncTask
	 */
	private Task getUpdatedSyncTaskFromGoogleTask(Task localSyncTaskToUpdate,
			com.google.api.services.tasks.model.Task addedGTask,
			DateTime syncDateTime) {
		logEnterMethod("updateLocalSyncTask");
		Task updatedlocalSyncTask = new FloatingTask(
				localSyncTaskToUpdate.getTaskId(), addedGTask, syncDateTime);
		logEnterMethod("updateLocalSyncTask");
		return updatedlocalSyncTask;
	}

	/**
	 * Returns updated sync task from Google Event
	 * 
	 * @param localSyncTaskToUpdate
	 * @param gCalEntry
	 * @param syncDateTime
	 * @throws IOException
	 */
	private Task getUpdatedSyncTaskFromGoogleEvent(Task localSyncTaskToUpdate,
			Event gCalEntry, DateTime syncDateTime) throws IOException {
		logEnterMethod("updateLocalSyncTask");
		Task updatedTask;
		if (isGoogleEventWithoutDuration(gCalEntry)) {
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
	 * Returns true if Google Event has no duration
	 * 
	 * @param gCalEntry
	 * @return
	 */
	private boolean isGoogleEventWithoutDuration(Event gCalEntry) {
		logEnterMethod("isGoogleEventWithoutDuration");
		logExitMethod("isGoogleEventWithoutDuration");
		return gCalEntry.getStart().getDateTime().toString()
				.equals(gCalEntry.getEnd().getDateTime().toString());
	}

	/**
	 * Sets sync time for local tasks from google calendar entry
	 * 
	 * @param gCalEntry
	 * @return sync datetime for updating local task
	 */
	private DateTime setSyncTime(Event gCalEntry) {
		logEnterMethod("setSyncTime");
		DateTime syncDateTime = getDateTimeNow();

		if (gCalEntry != null) {
			syncDateTime = new DateTime(gCalEntry.getUpdated().getValue());
		}
		assert isConvertedGoogleDateTimeSameAsSyncDateTime(
				gCalEntry.getUpdated(), syncDateTime);

		logExitMethod("setSyncTime");
		return syncDateTime;
	}

	/**
	 * Get date time defaulted to now
	 * 
	 * @return DateTime default to now
	 */
	protected DateTime getDateTimeNow() {
		logEnterMethod("getDateTimeNow");
		new DateTime();
		DateTime syncDateTime = DateTime.now();
		logExitMethod("getDateTimeNow");
		return syncDateTime;
	}

	/**
	 * Checks if Google DateTime is same as Sync Date Time
	 * 
	 * @param dateTime
	 * @param syncDateTime
	 * @return
	 */
	private boolean isConvertedGoogleDateTimeSameAsSyncDateTime(
			com.google.api.client.util.DateTime dateTime, DateTime syncDateTime) {
		logEnterMethod("isConvertedGoogleDateTimeSameAsSyncDateTime");
		logExitMethod("isConvertedGoogleDateTimeSameAsSyncDateTime");
		return syncDateTime.isEqual(new DateTime(dateTime.toString()));
	}

	/**
	 * Set sync time for Google Task
	 * 
	 * @param addedGTask
	 * @return Sync DateTIme
	 */
	private DateTime setSyncTime(
			com.google.api.services.tasks.model.Task addedGTask) {
		logEnterMethod("setSyncTime");
		DateTime syncDateTime = getDateTimeNow();
		if (addedGTask != null) {
			syncDateTime = new DateTime(addedGTask.getUpdated().getValue());
		}
		assert isConvertedGoogleDateTimeSameAsSyncDateTime(
				addedGTask.getUpdated(), syncDateTime);

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