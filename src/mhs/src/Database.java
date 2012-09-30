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

	private ConfigFile configFile;
	private TaskRecordFile taskRecordFile;
	private GoogleCalendar googleCalendar;
	private boolean isRemoteSyncEnabled = true;

	// Data Views
	// contains Task objects references
	private Map<Integer, Task> taskList; // primary task list with index as key
	private Map<String, Task> gCalTaskList; // task list with gCalId as key

	/**
	 * Database constructor
	 * 
	 * @param taskRecordFileName
	 * @throws IOException
	 * @throws ServiceException
	 */
	public Database(String taskRecordFileName, boolean disableSyncronize)
			throws IOException {
		initalizeDatabase(taskRecordFileName);
		// syncronize local and web databases
		if (disableSyncronize) {
			isRemoteSyncEnabled = false;
		} else {
			syncronizeDatabases();
		}
	}

	/**
	 * Database default constructor
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	public Database() throws IOException {
		initalizeDatabase();
		syncronizeDatabases();
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
	 * @throws IOException
	 */
	public void syncronizeDatabases() {
		try {
			pullSync();
			pushSync();
			saveTaskRecordFile();
		} catch (ServiceException e) {
			isRemoteSyncEnabled = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

		// remove deleted task
		if (localTask.isDeleted()) {
			System.out.println("Removing deleted synced task");
			googleCalendar.deleteEvent(localTask.getgCalTaskId());
			return;
		}

		// skip floating tasks
		if (localTask.getTaskCategory().equals(TaskCategory.FLOATING)) {
			return;
		}

		// add unsynced tasks
		if (isUnsyncedTask(localTask)) {
			System.out.println("Pushing new sync task");
			pushSyncNewTask(localTask);

		} else {
			// add updated tasks
			// checks if task is updated after last sync
			if (localTask.getTaskUpdated().isAfter(localTask.getTaskLastSync())) {
				System.out.println("Pushing updated task");
				pushSyncExistingTask(localTask);
			}
		}
	}

	private boolean isUnsyncedTask(Task localTask) {
		return localTask.getgCalTaskId() == null
				|| localTask.getTaskLastSync() == null
				|| localTask.getTaskUpdated() == null;
	}

	/**
	 * Push task that is currently not synced. Call pushSyncTask to sync tasks
	 * instead as it contains sync validation logic.
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
	}

	/**
	 * Push existing synced task Call pushSyncTask to sync task as it handles
	 * sync logic Call pushSyncTask to sync tasks instead as it contains sync
	 * validation logic.
	 * 
	 * @param localTask
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void pushSyncExistingTask(Task localTask) throws IOException,
			ServiceException {

		// update remote task
		CalendarEventEntry updatedGCalEvent = googleCalendar.updateEvent(
				localTask.getgCalTaskId(), localTask.getTaskName(), localTask
						.getStartDateTime().toString(), localTask
						.getEndDateTime().toString());

		if (updatedGCalEvent == null) {
			return;
		}

		DateTime syncDateTime = new DateTime();
		if (updatedGCalEvent.getUpdated() == null) {
			updatedGCalEvent.setUpdated(com.google.gdata.data.DateTime.now());
		}
		syncDateTime = new DateTime(updatedGCalEvent.getUpdated().toString());

		// Set local task sync details
		localTask.setTaskLastSync(syncDateTime);
	}

	/**
	 * Pull Sync remote tasks to local TODO deleted tasks on remote -> delete
	 * local files Syncs new tasks from remote Syncs existing local task with
	 * updated remote task
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void pullSync() throws IOException, ServiceException {
		List<CalendarEventEntry> googleCalendarEvents = googleCalendar
				.getEventList();
		Iterator<CalendarEventEntry> iterator = googleCalendarEvents.iterator();

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
	 */
	private void pullSyncTask(CalendarEventEntry gCalEntry) {

		//TODO delete local task if google calendar entry is deleted		

		if (gCalTaskList.containsKey(gCalEntry.getIcalUID())) {

			Task localTask = gCalTaskList.get(gCalEntry.getIcalUID());
			
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
	 */
	private void pullSyncNewTask(CalendarEventEntry gCalEntry) {

		// pull new remote task
		System.out.println("pulling new event");

		DateTime syncDateTime = new DateTime();
		if (gCalEntry.getUpdated() == null) {
			gCalEntry.setUpdated(com.google.gdata.data.DateTime.now());
		}
		syncDateTime = new DateTime(gCalEntry.getUpdated().toString());

		// add task from google calendar entry
		if (gCalEntry.getTimes().get(0).getStartTime()
				.equals(gCalEntry.getTimes().get(0).getEndTime())) {
			// create new deadline task
			Task newTask = new DeadlineTask(getNewTaskId(), gCalEntry,
					syncDateTime);
			taskList.put(newTask.getTaskId(), newTask);
		} else {
			// create new timed task
			Task newTask = new TimedTask(getNewTaskId(), gCalEntry,
					syncDateTime);
			taskList.put(newTask.getTaskId(), newTask);
		}
	}

	/**
	 * Syncs existing local task with updated remote task Call pullSyncTask as
	 * it contains sync validation logic.
	 * 
	 * @param gCalEntry
	 * @param localTaskEntry
	 */
	private void pullSyncExistingTask(CalendarEventEntry gCalEntry,
			Task localTaskEntry) {

		System.out.println("pulling newer event : "
				+ localTaskEntry.getTaskName());

		DateTime syncDateTime = new DateTime();
		if (gCalEntry.getUpdated() == null) {
			gCalEntry.setUpdated(com.google.gdata.data.DateTime.now());
		}
		syncDateTime = new DateTime(gCalEntry.getUpdated().toString());

		// update local task
		localTaskEntry.setTaskName(gCalEntry.getTitle().getPlainText());
		localTaskEntry.setStartDateTime(new DateTime(gCalEntry.getTimes()
				.get(0).getStartTime().toString()));
		localTaskEntry.setEndDateTime(new DateTime(gCalEntry.getTimes().get(0)
				.getEndTime().toString()));
		localTaskEntry.setTaskLastSync(syncDateTime);
		localTaskEntry.setTaskUpdated(syncDateTime);

		taskList.put(localTaskEntry.getTaskId(), localTaskEntry.clone());
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
	 * @throws IOException
	 */
	public Task query(int taskId) throws IOException {
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
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void add(Task task) throws IOException {

		if (!isTaskValid(task)) {
			throw new Error("Invalid Task format : " + task.toJson());
		}

		task.setTaskId(getNewTaskId());
		new DateTime();
		task.setTaskCreated(DateTime.now());
		task.setTaskUpdated(DateTime.now());

		Task taskToAdd = task.clone();

		if (isRemoteSyncEnabled) {
			try {
				pushSyncTask(taskToAdd);
			} catch (ServiceException e) {
				// TODO Auto-generated catch bloctk
				e.printStackTrace();
				isRemoteSyncEnabled = false;
			}
		}

		taskList.put(taskToAdd.getTaskId(), taskToAdd);

		saveTaskRecordFile();
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
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void undelete(int taskId) throws IOException {

		// check whether task exists
		if (!taskList.containsKey(taskId)) {
			throw new Error("Invalid Task");
		}

		Task taskToUndelete = taskList.get(taskId);
		taskToUndelete.setDeleted(false);
		new DateTime();
		taskToUndelete.setTaskUpdated(DateTime.now());

		if (isRemoteSyncEnabled) {
			try {
				pushSyncTask(taskToUndelete);
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isRemoteSyncEnabled = false;
			}
		}

		saveTaskRecordFile();
	}

	/**
	 * Deletes a task
	 * 
	 * @param taskId
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void delete(int taskId) throws IOException {

		// check whether task exists
		if (!taskList.containsKey(taskId)) {
			throw new Error("Invalid Task");

		}

		Task taskToDelete = taskList.get(taskId);
		taskToDelete.setDeleted(true);
		new DateTime();
		taskToDelete.setTaskUpdated(DateTime.now());

		if (isRemoteSyncEnabled) {
			try {
				pushSyncTask(taskToDelete);
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isRemoteSyncEnabled = false;
			}
		}

		saveTaskRecordFile();

	}

	/**
	 * Updates task
	 * 
	 * @param updatedTask
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void update(Task updatedTask) throws IOException {

		// check whether task exists
		if (!taskList.containsKey(updatedTask.getTaskId())) {
			throw new Error("Invalid Task");
		}

		if (!isTaskValid(updatedTask)) {
			throw new Error("Invalid Task format : " + updatedTask.toJson());
		}

		new DateTime();
		updatedTask.setTaskUpdated(DateTime.now());

		Task updatedTaskToSave = updatedTask.clone();

		if (isRemoteSyncEnabled) {
			try {
				pushSyncTask(updatedTaskToSave);
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isRemoteSyncEnabled = false;
			}
		}

		taskList.put(updatedTask.getTaskId(), updatedTaskToSave);
		saveTaskRecordFile();

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
	 * Permanently removes task record
	 * 
	 * @param taskId
	 * @throws IOException
	 */
	private void removeRecord(int taskId) throws IOException {
		// check if task exists
		if (taskList.containsKey(taskId)) {
			taskList.remove(taskId);
			saveTaskRecordFile();
		} else {
			throw new Error("Invalid Task");
		}
	}

	/**
	 * TODO Clears expired and deleted tasks
	 * 
	 * @throws IOException
	 */
	public void clearExpiredTasks() throws IOException {
	}

	/**
	 * Clears database
	 * 
	 * @throws IOException
	 */
	public void clearDatabase() throws IOException {
		taskList.clear();
		// TODO clear associated Google Events
		saveTaskRecordFile();
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

}