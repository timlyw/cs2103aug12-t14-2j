// @author A0087048X

package mhs.src.storage;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mhs.src.common.ConfigFile;
import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.InvalidTaskFormatException;
import mhs.src.common.exceptions.NoActiveCredentialException;
import mhs.src.common.exceptions.TaskNotFoundException;
import mhs.src.storage.persistence.TaskLists;
import mhs.src.storage.persistence.local.TaskRecordFile;
import mhs.src.storage.persistence.remote.GoogleCalendarMhs;
import mhs.src.storage.persistence.remote.GoogleTasks;
import mhs.src.storage.persistence.remote.MhsGoogleOAuth2;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;

import org.joda.time.DateTime;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ResourceNotFoundException;
import com.google.gdata.util.ServiceException;

/**
 * Database
 * 
 * Database interfaces persistent the data storage mechanism, on local disk and
 * remote (Google Calendar Service).
 * 
 * - Handles task queries and CRUD operations - Handles user configuration
 * setting operations
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */

public class Database {

	static GoogleTasks googleTasks;
	static GoogleCalendarMhs googleCalendar;
	static TaskLists taskLists;
	static Syncronize syncronize;
	static TaskValidator taskValidator;
	private static TaskRecordFile taskRecordFile;
	static ConfigFile configFile;
	static MhsGoogleOAuth2 mhsGoogleOAuth2;

	static DateTime syncStartDateTime;
	static DateTime syncEndDateTime;
	static boolean isRemoteSyncEnabled = false;

	static final Logger logger = MhsLogger.getLogger();

	// Exception Messages
	static final String EXCEPTION_MESSAGE_INVALID_TASK_FORMAT = "Invalid Task Format!";
	static final String EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST = "Task does not exist!";
	private static final String EXCEPTION_MESSAGE_NULL_PARAMETER = "%1$s cannot be null!";

	private static final String PARAMETER_TASK_RECORD_FILE_NAME = "taskRecordFileName";
	private static final String PARAMETER_TASK = "task";
	private static final String PARAMETER_TASK_NAME = "taskName";
	private static final String PARAMETER_START_AND_END_DATE_TIMES = "start and end date times";
	private static final String PARAMETER_CONFIG_PARAMETER = "configParameter";

	// Config parameters
	private static final String CONFIG_PARAM_GOOGLE_USER_ACCOUNT = "GOOGLE_USER_ACCOUNT";

	private static final String EXCEPTION_MESSAGE_SYNCRONIZATION_WITH_REMOTE_STORAGE_FAILED = "Syncronization with remote storage failed.";
	private static final String EXCEPTION_MESSAGE_NO_CONNECTIVITY_WITH_REMOTE_STORAGE = "No Connection with Remote Storage.";

	private static final int SYNC_FORCE_PUSH_UPDATED_DATE_TIME_AHEAD_VALUE = 1;
	private static final int SYNC_START_DATE_TIME_MONTHS_BEFORE_NOW = 1;
	private static final int SYNC_END_DATE_TIME_MONTHS_FROM_NOW = 12;

	private static final String URL_REMOTE_SERVICE_GOOGLE = "http://google.com/";

	/**
	 * Database constructor
	 * 
	 * @param taskRecordFileName
	 * @throws IOException
	 * @throws ServiceException
	 */
	protected Database(String taskRecordFileName, boolean disableSyncronize)
			throws IllegalArgumentException, IOException {
		logEnterMethod("Database");

		if (taskRecordFileName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_TASK_RECORD_FILE_NAME));
		}

		initializeSyncDateTimes();
		initalizeDatabase(taskRecordFileName);
		initializeGoogleOAuth2();
		initializeSyncronize(disableSyncronize);

		logExitMethod("Database");
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

		logExitMethod("initalizeDatabase");
	}

	/**
	 * Syncronization
	 */

	/**
	 * Initialize Sync Date Times Range
	 */
	private void initializeSyncDateTimes() {
		logEnterMethod("initializeSyncDateTimes");
		setSyncStartDateTime(SYNC_START_DATE_TIME_MONTHS_BEFORE_NOW);
		setSyncEndDateTime(SYNC_END_DATE_TIME_MONTHS_FROM_NOW);
		logExitMethod("initializeSyncDateTimes");
	}

	/**
	 * Set Sync Start DateTime
	 * 
	 * @param monthsBeforeNow
	 */
	private void setSyncStartDateTime(int monthsBeforeNow) {
		new DateTime();
		syncStartDateTime = DateTime.now().minusMonths(monthsBeforeNow)
				.toDateMidnight().toDateTime();
	}

	/**
	 * Set Sync End DateTime
	 * 
	 * @param monthsBeforeNow
	 */
	private void setSyncEndDateTime(int monthsAfterNow) {
		new DateTime();
		syncEndDateTime = DateTime.now().plusMonths(monthsAfterNow)
				.toDateMidnight().toDateTime();
	}

	/**
	 * Initializes GoogleOAuth2 and authenticates silently with default user
	 * 
	 * @throws Exception
	 */
	private void initializeGoogleOAuth2() {
		try {
			MhsGoogleOAuth2.getInstance();
			if (isRemoteServiceConnectionActive()) {
				authenticateOAuth2WithDefaultUser();
			}
		} catch (Exception e) {
			// Connectivity Errors
		}
	}

	/**
	 * Silently Authenticate OAuth2 With DefaultUser
	 * 
	 * @throws Exception
	 * @throws IOException
	 */
	protected void authenticateOAuth2WithDefaultUser() throws IOException,
			Exception {
		String defaultUserGoogleAccountName = getConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT);
		if (defaultUserGoogleAccountName != null) {
			MhsGoogleOAuth2.setupUserId(defaultUserGoogleAccountName);
			MhsGoogleOAuth2.authorizeCredentialAndStoreInCredentialStore();
			saveGoogleAccountInfo(defaultUserGoogleAccountName);
		}
	}

	/**
	 * Initialize Syncronize to start sync between local and remote storage
	 * 
	 * Updates remoteSyncEnabled to true if successful
	 * 
	 * @param disableSyncronize
	 * 
	 * @throws IOException
	 */
	private void initializeSyncronize(boolean disableSyncronize)
			throws IOException {
		logEnterMethod("initializeSyncronize");
		syncronize = new Syncronize(this, disableSyncronize);
		logExitMethod("initializeSyncronize");
	}

	/**
	 * Syncronizes Databases
	 * 
	 * @throws ServiceException
	 * @throws NoActiveCredentialException
	 * @throws IOException
	 */
	public void syncronizeDatabases() throws ServiceException, IOException,
			NoActiveCredentialException {
		logEnterMethod("syncronizeDatabases");
		if (!isRemoteServiceConnectionActive()) {
			syncronize.disableRemoteSync();
			throw new UnknownHostException(
					EXCEPTION_MESSAGE_NO_CONNECTIVITY_WITH_REMOTE_STORAGE);
		}
		if (!isRemoteSyncEnabled) {
			initializeGoogleServices();
		}
		boolean isSyncronizeSchedulingSuccessful = syncronize
				.syncronizeDatabases();
		if (!isSyncronizeSchedulingSuccessful) {
			syncronize.disableRemoteSync();
			throw new ServiceException(
					EXCEPTION_MESSAGE_SYNCRONIZATION_WITH_REMOTE_STORAGE_FAILED);
		}
		logExitMethod("syncronizeDatabases");
	}

	/**
	 * Checks if internet connection to remote services are active
	 * 
	 * @return true if connection if active
	 */
	private boolean isRemoteServiceConnectionActive() {
		logEnterMethod("isRemoteServiceConnectionActive");
		try {
			URL googleUrl = new URL(URL_REMOTE_SERVICE_GOOGLE);
			googleUrl.openConnection();
			googleUrl.getContent();
			logExitMethod("isRemoteServiceConnectionActive");
			return true;
		} catch (UnknownHostException e) {
			logExitMethod("isRemoteServiceConnectionActive");
			return false;
		} catch (IOException e) {
			logExitMethod("isRemoteServiceConnectionActive");
			return false;
		}
	}

	/**
	 * Initializes Google Services
	 * 
	 * @return true if all services succeed
	 * @throws AuthenticationException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws NoActiveCredentialException
	 */
	boolean initializeGoogleServices() throws AuthenticationException,
			IOException, ServiceException, NoActiveCredentialException {
		logEnterMethod("initializeGoogleServices");
		if (initializeGoogleCalendarService() && initializeGoogleTaskService()) {
			logger.log(Level.INFO, "Google Services instantiated");
			logExitMethod("initializeGoogleServices");
			return true;
		}
		logExitMethod("initializeGoogleServices");
		return false;
	}

	/**
	 * Initialize Google Tasks with saved Google Information
	 * 
	 * @return
	 * @throws NoActiveCredentialException
	 */
	boolean initializeGoogleTaskService() throws NoActiveCredentialException {
		String userGoogleAccount = getSavedUserGoogleAccountName();
		if (userGoogleAccount == null) {
			logger.log(Level.FINER, "User not authenticated with google.");
			logExitMethod("initializeGoogleCalendarService");
			return false;
		}
		googleTasks = new GoogleTasks(MhsGoogleOAuth2.getHttpTransport(),
				MhsGoogleOAuth2.getJsonFactory(),
				MhsGoogleOAuth2.getCredential());
		assert (googleTasks != null);
		logExitMethod("initializeGoogleCalendarService");
		return true;
	}

	/**
	 * Initialize Google Calendar Service with saved Google Information
	 * 
	 * @throws IOException
	 * @throws AuthenticationException
	 * @throws ServiceException
	 * @return true google calendar service is successfully initialized
	 * @throws NoActiveCredentialException
	 */
	boolean initializeGoogleCalendarService() throws IOException,
			AuthenticationException, ServiceException,
			NoActiveCredentialException {
		logEnterMethod("initializeGoogleCalendarService");
		String userGoogleAccount = getSavedUserGoogleAccountName();
		if (userGoogleAccount == null) {
			logExitMethod("initializeGoogleCalendarService");
			return false;
		}
		googleCalendar = new GoogleCalendarMhs(
				MhsGoogleOAuth2.getHttpTransport(),
				MhsGoogleOAuth2.getJsonFactory(),
				MhsGoogleOAuth2.getCredential());

		assert (googleCalendar != null);
		logExitMethod("initializeGoogleCalendarService");
		return true;
	}

	/**
	 * Logs in user google account with user details and starts Syncronize
	 * 
	 * @param userName
	 *            user google account
	 * @param userPassword
	 *            user google account password
	 * @throws Exception
	 */
	public void loginUserGoogleAccount(String userName) throws Exception {
		logEnterMethod("loginUserGoogleAccount");
		if (!isRemoteServiceConnectionActive()) {
			throw new UnknownHostException(
					EXCEPTION_MESSAGE_NO_CONNECTIVITY_WITH_REMOTE_STORAGE);
		}
		try {
			MhsGoogleOAuth2.setupUserId(userName);
			MhsGoogleOAuth2.authorizeCredentialAndStoreInCredentialStore();
			saveGoogleAccountInfo(userName);
		} catch (Exception e) {
			syncronize.disableRemoteSync();
			throw e;
		}
		if (!isGoogleServicesInstantiated()) {
			initializeGoogleServices();
		}
		syncronize.enableRemoteSync();
		syncronize.syncronizeDatabases();
		logExitMethod("loginUserGoogleAccount");
	}

	/**
	 * Checks if Google Services are Instantiated
	 * 
	 * @return
	 */
	protected static boolean isGoogleServicesInstantiated() {
		return googleCalendar != null && googleTasks != null;
	}

	/**
	 * Logs user out of Google Account
	 * 
	 * @throws IOException
	 */
	public void logOutUserGoogleAccount() throws IOException {
		logEnterMethod("logOutUserGoogleAccount");
		assert (syncronize != null);
		syncronize.disableRemoteSync();
		MhsGoogleOAuth2.deleteCurrentCredential();
		disableGoogleServices();
		removeUserLoggedInData();
		logExitMethod("logOutUserGoogleAccount");
	}

	protected void removeUserLoggedInData() throws IOException {
		configFile.removeConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT);
		configFile.save();
	}

	protected void disableGoogleServices() {
		googleCalendar = null;
		googleTasks = null;
	}

	/**
	 * @return user google account name if it exists or null otherwise
	 */
	public String getAuthenticatedUserGoogleAccountName() {
		logEnterMethod("getAuthenticatedUserGoogleAccountName");
		logExitMethod("getAuthenticatedUserGoogleAccountName");
		return MhsGoogleOAuth2.getUserName();
	}

	/**
	 * @return user google account name if it exists or null otherwise
	 */
	public String getAuthenticatedUserGoogleAccountEmail() {
		logEnterMethod("getAuthenticatedUserGoogleAccountEmail");
		logExitMethod("getAuthenticatedUserGoogleAccountEmail");
		return MhsGoogleOAuth2.getUserEmail();
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
	 * @param googleUserAccountName
	 * @param googleAuthToken
	 * 
	 * @throws IOException
	 */
	synchronized void saveGoogleAccountInfo(String googleUserAccountName)
			throws IOException {
		logEnterMethod("saveGoogleAccountInfo");
		if (googleUserAccountName != null) {
			configFile.setConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT,
					googleUserAccountName);
		}
		configFile.save();
		logExitMethod("saveGoogleAccountInfo");
	}

	/**
	 * Get saved user google account from Configuration File
	 * 
	 * @return getSavedUserGoogleAccount or null if it does not exist
	 */
	public String getSavedUserGoogleAccountName() {
		if (!configFile
				.hasNonEmptyConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT)) {
			return null;
		}
		String savedUserGoogleAccount = configFile
				.getConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT);
		return savedUserGoogleAccount;
	}

	/**
	 * Task CRUD Operations
	 */

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
	 * @param includeFloatingTasks
	 * @return list of matched tasks
	 */
	public List<Task> query(DateTime startDateTime, DateTime endDateTime,
			boolean includeFloatingTasks, boolean orderByStartDateTime) {
		logEnterMethod("query");
		if (startDateTime == null || endDateTime == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_START_AND_END_DATE_TIMES));
		}
		logExitMethod("query");
		return taskLists.getTasks(startDateTime, endDateTime,
				includeFloatingTasks, orderByStartDateTime);
	}

	/**
	 * Returns task that matches any of the specified parameters (exclusive of
	 * deleted tasks)
	 * 
	 * @param taskName
	 * @param startDateTime
	 * @param endDateTime
	 * @param orderByStartDateTime
	 * @return list of tasks matching query
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
					PARAMETER_START_AND_END_DATE_TIMES));
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
					PARAMETER_START_AND_END_DATE_TIMES));
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

		if (!TaskValidator.isTaskValid(task)) {
			throw new InvalidTaskFormatException(
					EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		}

		Task taskToAdd = task.clone();
		setTaskToAddParameters(taskToAdd);
		addTaskToTaskList(taskToAdd);
		schedulePushSyncTask(taskToAdd);
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
		taskLists.updateTaskInTaskLists(taskToAdd);
		logExitMethod("addTaskToTaskList");
	}

	/**
	 * Creates task to add
	 * 
	 * Updates the following attributes: - unique taskId - TaskCreated -
	 * TaskUpdated
	 * 
	 * Sets the following attributes to new task default: - TaskLastSync -
	 * gCalTaskId
	 * 
	 * @param taskToAdd
	 */
	private void setTaskToAddParameters(Task taskToAdd) {
		taskToAdd.setTaskId(getNewTaskId());
		DateTime UpdateTime = getCurrentDateTime();
		taskToAdd.setTaskCreated(UpdateTime);
		taskToAdd.setTaskUpdated(UpdateTime);
		taskToAdd.setTaskLastSync(null);
		taskToAdd.setGTaskId(null);
		taskToAdd.setGcalTaskUid(null);
		taskToAdd.setGcalTaskUid(null);
	}

	/**
	 * Gets current DateTime object
	 * 
	 * @return current DateTime
	 */
	private DateTime getCurrentDateTime() {
		DateTime currentDateTime = new DateTime();
		currentDateTime = DateTime.now();
		return currentDateTime;
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
		schedulePushSyncTask(taskToDelete);
		saveTaskRecordFile();

		logExitMethod("delete");
	}

	/**
	 * Deletes task in task list by setting deleted attribute
	 * 
	 * @param taskToDelete
	 */
	void deleteTaskInTaskList(Task taskToDelete) {
		logEnterMethod("deleteTaskInTaskList");
		assert (taskToDelete != null);
		taskToDelete.setDeleted(true);
		setUpdatedTimeAheadToForcePush(taskToDelete,
				SYNC_FORCE_PUSH_UPDATED_DATE_TIME_AHEAD_VALUE);
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

		if (!TaskValidator.isTaskValid(updatedTask)) {
			throw new InvalidTaskFormatException(
					EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		}

		Task updatedTaskToSave = updatedTask.clone();
		preserveTaskNonEditableFields(updatedTaskToSave);
		updateTaskinTaskList(updatedTaskToSave);
		schedulePushSyncTask(updatedTaskToSave);

		saveTaskRecordFile();
		logExitMethod("update");
		return updatedTaskToSave;
	}

	/**
	 * Schedule Push Sync Task
	 * 
	 * @param updatedTaskToSave
	 */
	private void schedulePushSyncTask(Task updatedTaskToSave) {
		if (isRemoteSyncEnabled) {
			syncronize.schedulePushSyncTask(updatedTaskToSave);
		}
	}

	/**
	 * Preserve non-editable fields in task
	 * 
	 * @param updatedTaskToSave
	 * @throws TaskNotFoundException
	 */
	private void preserveTaskNonEditableFields(Task updatedTaskToSave)
			throws TaskNotFoundException {
		Task currentTask = query(updatedTaskToSave.getTaskId());
		updatedTaskToSave.setGTaskId(currentTask.getGTaskId());
		updatedTaskToSave.setGcalTaskId(currentTask.getgCalTaskId());
		updatedTaskToSave.setTaskCreated(currentTask.getTaskCreated());
		updatedTaskToSave.setTaskLastSync(currentTask.getTaskLastSync());
	}

	/**
	 * Updates task in task list
	 * 
	 * @param updatedTaskToSave
	 */
	private void updateTaskinTaskList(Task updatedTaskToSave) {
		logEnterMethod("updateTaskinTaskList");
		assert (updatedTaskToSave != null);
		setUpdatedTimeAheadToForcePush(updatedTaskToSave,
				SYNC_FORCE_PUSH_UPDATED_DATE_TIME_AHEAD_VALUE);
		taskLists.updateTaskInTaskLists(updatedTaskToSave);
		logExitMethod("updateTaskinTaskList");
	}

	/**
	 * Sets Updated Time Ahead To ForcePush
	 * 
	 * @param taskToForcePushSync
	 */
	private void setUpdatedTimeAheadToForcePush(Task taskToForcePushSync,
			int minsToSetUpdatedDateTimeAhead) {
		// set updated time ahead to force push
		DateTime UpdateTime = new DateTime();
		UpdateTime = DateTime.now().plusMinutes(minsToSetUpdatedDateTimeAhead);
		taskToForcePushSync.setTaskUpdated(UpdateTime);
	}

	/**
	 * Waits for syncronize background task to complete
	 * 
	 * @param maxExecutionTimeInSeconds
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public void waitForAllBackgroundTasks(int maxExecutionTimeInSeconds)
			throws InterruptedException, ExecutionException, TimeoutException {
		logEnterMethod("waitForSyncronizeBackgroundTaskToComplete");

		syncronize.waitForAllBackgroundTasks(maxExecutionTimeInSeconds);

		logExitMethod("waitForSyncronizeBackgroundTaskToComplete");
	}

	/**
	 * Save Tasks to local file - Syncronized to prevent multiple File I/O
	 * 
	 * @throws IOException
	 */
	synchronized static void saveTaskRecordFile() throws IOException {
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
	public void removeRecord(Task taskToRemove) throws TaskNotFoundException {
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
	public void clearRemoteDatabase() {
		logEnterMethod("clearRemoteDatabase");
		if (isRemoteSyncEnabled) {
			clearGoogleCalendar();
			clearGoogleTasks();
		}
		logExitMethod("clearRemoteDatabase");
	}

	private void clearGoogleTasks() {
		List<com.google.api.services.tasks.model.Task> googleTaskList;
		try {
			googleTaskList = googleTasks.retrieveTasks();
			if (googleTaskList == null) {
				return;
			}
			Iterator<com.google.api.services.tasks.model.Task> googleTaskListIterator = googleTaskList
					.iterator();
			while (googleTaskListIterator.hasNext()) {
				com.google.api.services.tasks.model.Task googleTaskToDelete = googleTaskListIterator
						.next();
				googleTasks.deleteTask(googleTaskToDelete.getId());
			}
		} catch (ResourceNotFoundException | IOException e) {
			// SilentFailSync Policy
			logger.log(Level.FINER, e.getMessage());
		}
	}

	protected void clearGoogleCalendar() {
		try {
			googleCalendar.deleteEvents(
					DateTime.now().minusYears(1).toString(), DateTime.now()
							.plusYears(1).toString());
		} catch (NullPointerException e) {
			// SilentFailSync Policy
			logger.log(Level.FINER, e.getMessage());
		}
	}

	/**
	 * Returns new taskId - (unique incremental)
	 * 
	 * @return
	 */
	int getNewTaskId() {
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

	/**
	 * Configuration File Methods
	 */

	/**
	 * Gets config parameter from Configuration File
	 * 
	 * @param configParameter
	 * @return configuration parameter value
	 */
	public String getConfigParameter(String configParameter) {
		logEnterMethod("getConfigParameter");
		if (configParameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_CONFIG_PARAMETER));
		}
		logExitMethod("getConfigParameter");
		return configFile.getConfigParameter(configParameter);
	}

	/**
	 * Sets config parameter
	 * 
	 * @param configParameterToSet
	 * @param configParameterValueToSet
	 * @return
	 * @throws IOException
	 */
	public boolean setConfigParameter(String configParameterToSet,
			String configParameterValueToSet) throws IOException {
		logEnterMethod("setConfigParameter");
		if (configParameterToSet == null || configParameterValueToSet == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_CONFIG_PARAMETER));
		}
		if (!configFile.hasConfigParameter(configParameterToSet)) {
		}
		configFile.setConfigParameter(configParameterToSet,
				configParameterValueToSet);
		logExitMethod("setConfigParameter");
		return false;
	}

	/**
	 * Checks if configuration parameter exists in configuration file
	 * 
	 * @param configParameter
	 * @return true if configParameter exists
	 */
	public boolean hasConfigParameter(String configParameter) {
		logEnterMethod("hasConfigParameter");
		if (configParameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_CONFIG_PARAMETER));
		}
		if (configFile.hasConfigParameter(configParameter)) {
			logExitMethod("hasConfigParameter");
			return true;
		} else {
			logExitMethod("hasConfigParameter");
			return false;
		}
	}

	/**
	 * Removes specified configuration parameter from configuration file
	 * 
	 * @param configParameter
	 * @return true if configuration parameter is removed successfully or false
	 *         if it does not exist
	 * @throws IOException
	 */
	public boolean removeConfigParameter(String configParameter)
			throws IOException {
		logEnterMethod("removeConfigParameter");
		if (configParameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_CONFIG_PARAMETER));
		}
		if (!configFile.hasConfigParameter(configParameter)) {
			logExitMethod("removeConfigParameter");
			return false;
		}
		configFile.removeConfigParameter(configParameter);
		logExitMethod("removeConfigParameter");
		return true;
	}

	/**
	 * Logger methods
	 */

	/**
	 * Log trace for method entry
	 * 
	 * @param methodName
	 */
	static void logEnterMethod(String methodName) {
		logger.entering("Database", methodName);
	}

	/**
	 * Log trace for method exit
	 * 
	 * @param methodName
	 */
	static void logExitMethod(String methodName) {
		logger.exiting("Database", methodName);
	}

}
