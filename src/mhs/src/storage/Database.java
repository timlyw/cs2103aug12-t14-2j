//@author A0087048X

package mhs.src.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.InvalidTaskFormatException;
import mhs.src.common.exceptions.TaskNotFoundException;
import mhs.src.storage.persistence.TaskLists;
import mhs.src.storage.persistence.local.ConfigFile;
import mhs.src.storage.persistence.local.TaskRecordFile;
import mhs.src.storage.persistence.remote.GoogleCalendarMhs;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;

import org.joda.time.DateTime;

import com.google.gdata.util.AuthenticationException;
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

	private static final String PARAMETER_CONFIG_PARAMETER = "configParameter";
	static GoogleCalendarMhs googleCalendar;
	static TaskLists taskLists;
	static Syncronize syncronize;
	static TaskValidator taskValidator;
	private static TaskRecordFile taskRecordFile;
	static ConfigFile configFile;

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
	private static final String PARAMETERE_START_AND_END_DATE_TIMES = "start and end date times";

	// Config parameters
	private static final String CONFIG_PARAM_GOOGLE_USER_ACCOUNT = "GOOGLE_USER_ACCOUNT";
	private static final String CONFIG_PARAM_GOOGLE_AUTH_TOKEN = "GOOGLE_AUTH_TOKEN";
	private static final String GOOGLE_CALENDAR_APP_NAME = "My Hot Secretary";

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
	 * Initialize Sync Date Times Range
	 */
	private void initializeSyncDateTimes() {
		logEnterMethod("initializeSyncDateTimes");
		syncStartDateTime = DateTime.now().minusMonths(1).toDateMidnight()
				.toDateTime();
		syncEndDateTime = DateTime.now().plusMonths(12).toDateMidnight()
				.toDateTime();
		logExitMethod("initializeSyncDateTimes");
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
	 * Initialize Google Calendar Service with saved Google Information
	 * 
	 * @throws IOException
	 * @throws AuthenticationException
	 * @throws ServiceException
	 * @return true google calendar service is successfully initialized
	 */
	boolean initializeGoogleCalendarService() throws IOException,
			AuthenticationException, ServiceException {
		logEnterMethod("initializeGoogleCalendarService");
		if (!configFile
				.hasNonEmptyConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN)) {
			logExitMethod("initializeGoogleCalendarService");
			return false;
		}
		if (!configFile
				.hasNonEmptyConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT)) {
			logExitMethod("initializeGoogleCalendarService");
			return false;
		}

		String userGoogleAccount = configFile
				.getConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT);
		String userGoogleAuthToken = configFile
				.getConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN);

		assert (userGoogleAccount != null && userGoogleAuthToken != null);

		googleCalendar = new GoogleCalendarMhs(GOOGLE_CALENDAR_APP_NAME,
				userGoogleAccount, userGoogleAuthToken);

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
	 * @throws IOException
	 * @throws UnknownHostException
	 *             when no internet connection is available
	 * @throws ServiceException
	 *             when Google Calendar Service Exception occurs
	 */
	public void loginUserGoogleAccount(String userName, String userPassword)
			throws IOException, AuthenticationException, UnknownHostException,
			ServiceException {
		logEnterMethod("loginUserGoogleAccount");

		if (userName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, "userName"));
		}
		if (userPassword == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER, "userPassword"));
		}
		assert (syncronize != null);

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
		assert (syncronize != null);

		syncronize.disableRemoteSync();
		googleCalendar = null;
		configFile.removeConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN);
		configFile.removeConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT);
		configFile.save();
		logExitMethod("logOutUserGoogleAccount");
	}

	/**
	 * @return user google account name if it exists or null
	 */
	public String getUserGoogleAccountName() {
		logEnterMethod("getUserGoogleAccountName");
		logExitMethod("getUserGoogleAccountName");
		if (configFile.hasConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT)) {
			String[] googleUserAccountString = configFile.getConfigParameter(
					CONFIG_PARAM_GOOGLE_USER_ACCOUNT).split("@");
			if (googleUserAccountString.length > 0) {
				return googleUserAccountString[0];
			}
		}
		return null;
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
	synchronized void saveGoogleAccountInfo(String googleUserAccount,
			String googleAuthToken) throws IOException {
		logEnterMethod("saveGoogleAccountInfo");
		if (googleAuthToken != null) {
			configFile.setConfigParameter(CONFIG_PARAM_GOOGLE_AUTH_TOKEN,
					googleAuthToken);
		}
		if (googleUserAccount != null) {
			configFile.setConfigParameter(CONFIG_PARAM_GOOGLE_USER_ACCOUNT,
					googleUserAccount);
		}
		configFile.save();
		logExitMethod("saveGoogleAccountInfo");
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
	 * @param includeFloatingTasks
	 * @return list of matched tasks
	 */
	public List<Task> query(DateTime startDateTime, DateTime endDateTime,
			boolean includeFloatingTasks, boolean orderByStartDateTime) {
		logEnterMethod("query");
		if (startDateTime == null || endDateTime == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETERE_START_AND_END_DATE_TIMES));
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

		if (!TaskValidator.isTaskValid(task)) {
			throw new InvalidTaskFormatException(
					EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		}

		Task taskToAdd = task.clone();
		addTaskToTaskList(taskToAdd);

		if (isRemoteSyncEnabled) {
			syncronize.schedulePushSyncTask(taskToAdd);
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
			syncronize.schedulePushSyncTask(taskToDelete);
		}
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

		if (!TaskValidator.isTaskValid(updatedTask)) {
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
			syncronize.schedulePushSyncTask(updatedTaskToSave);
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
	 * Gets config parameter from Configuration File
	 * 
	 * @param configParameter
	 * @return configuration parameter value
	 */
	public String getConfigParameter(String configParameter) {
		if (configParameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_CONFIG_PARAMETER));
		}
		configFile.getConfigParameter(configParameter);
		return null;
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
		if (configParameterToSet == null || configParameterValueToSet == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_CONFIG_PARAMETER));
		}
		if (!configFile.hasConfigParameter(configParameterToSet)) {
		}
		configFile.setConfigParameter(configParameterToSet,
				configParameterValueToSet);
		return false;
	}

	/**
	 * Checks if configuration parameter exists in configuration file
	 * 
	 * @param configParameter
	 * @return true if configParameter exists
	 */
	public boolean hasConfigParameter(String configParameter) {
		if (configParameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_CONFIG_PARAMETER));
		}
		if (configFile.hasConfigParameter(configParameter)) {
			return true;
		}
		return false;
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
		if (configParameter == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_CONFIG_PARAMETER));
		}
		if (!configFile.hasConfigParameter(configParameter)) {
			return false;
		}
		configFile.removeConfigParameter(configParameter);
		return true;
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
			} catch (UnknownHostException e) {
				syncronize.disableRemoteSync();
			} catch (NullPointerException | ServiceException e) {
				// SilentFailSync Policy
				logger.log(Level.FINER, e.getMessage());
			}
		}
		logExitMethod("clearRemoteDatabase");
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

	static void logExitMethod(String methodName) {
		logger.exiting("Database", methodName);
	}

	static void logEnterMethod(String methodName) {
		logger.entering("Database", methodName);
	}

}