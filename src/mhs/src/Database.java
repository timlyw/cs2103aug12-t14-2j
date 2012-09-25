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
import com.google.gdata.util.ServiceException;

public class Database {

	private ConfigFile configFile;
	private TaskRecordFile taskRecordFile;
	private GoogleCalendar googleCalendar;

	// Data Views
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
			throws IOException, ServiceException {
		initalizeDatabase(taskRecordFileName);
		// syncronize local and web databases
		if (!disableSyncronize) {
			// syncronize local and web databases
			syncronizeDatabases();
		}
	}

	/**
	 * Database default constructor
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	public Database() throws IOException, ServiceException {
		initalizeDatabase();
		// syncronize local and web databases
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
			throws IOException, ServiceException {

		configFile = new ConfigFile();
		taskRecordFile = new TaskRecordFile(taskRecordFileName);
		initializeGoogleCalendarService();

		taskList = taskRecordFile.getTaskList();
		gCalTaskList = taskRecordFile.getGCalTaskList();
	}

	/**
	 * Initialize database
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void initalizeDatabase() throws IOException, ServiceException {
		configFile = new ConfigFile();

		taskRecordFile = new TaskRecordFile();
		taskList = taskRecordFile.loadTaskList();

		initializeGoogleCalendarService();
	}

	private void initializeGoogleCalendarService() throws IOException,
			ServiceException {
		if (configFile.hasConfigParameter("GOOGLE_AUTH_TOKEN")
				&& !configFile.getConfigParameter("GOOGLE_AUTH_TOKEN")
						.isEmpty()) {
			googleCalendar = new GoogleCalendar(
					configFile.getConfigParameter("GOOGLE_AUTH_TOKEN"));
			saveGoogleAuthToken();

		} else if (configFile.hasConfigParameter("USER_GOOGLE_EMAIL")) {
			// TODO prompt user password
			googleCalendar = new GoogleCalendar();
			saveGoogleAuthToken();
		}
	}

	private void saveGoogleAuthToken() throws IOException {
		String googleAuthToken = googleCalendar.getAuthToken();
		if (googleAuthToken != null) {
			configFile.setConfigParameter("GOOGLE_AUTH_TOKEN",
					googleAuthToken);
		}
	}

	// TODO BATCH OPERATIONS FOR DATABASE
	// TODO BATCH UPDATES FOR GOOGLE CALENDAR

	public void syncronizeDatabases() throws IOException, ServiceException {

		DateTime syncDateTime = new DateTime().now();

		pullSync(syncDateTime);

		pushSync(syncDateTime);

		saveTaskRecordFile();
	}

	// TODO
	private void pushSync(DateTime syncDateTime) throws IOException,
			ServiceException {
		// push sync tasks from local to google calendar
		for (Map.Entry<Integer, Task> entry : taskList.entrySet()) {

			// Skip floating tasks
			if (entry.getValue().getTaskCategory()
					.equals(TaskCategory.FLOATING)) {
				continue;
			}

			// add unsynced tasks
			if (entry.getValue().getgCalTaskId().equalsIgnoreCase("null")
					|| entry.getValue().getgCalTaskId() == null) {
				System.out.println("push unsync event : "
						+ entry.getValue().getTaskName());
				System.out.println("!" + entry.getValue().getgCalTaskId());

				String eventGCalId = googleCalendar.createEvent(entry
						.getValue().getTaskName(), entry.getValue()
						.getStartDateTime().toString(), entry.getValue()
						.getEndDateTime().toString(), syncDateTime.toString());

				// Set local task sync details
				entry.getValue().setgCalTaskId(eventGCalId);
				entry.getValue().setTaskUpdated(syncDateTime);
				entry.getValue().setTaskLastSync(syncDateTime);

			}
			// add updated tasks
			else {
				// gCalId not null

				// checks if task is updated after last sync
				if (entry.getValue().getTaskUpdated()
						.compareTo(entry.getValue().getTaskLastSync()) > 0) {

					System.out.println("push updated event : "
							+ entry.getValue().getTaskName());
					System.out.println(entry.getValue().getgCalTaskId());
					System.out.println(entry.getValue().getTaskUpdated() + " "
							+ entry.getValue().getTaskLastSync());

					// update remote task
					googleCalendar.updateEvent(
							entry.getValue().getgCalTaskId(), entry.getValue()
									.getTaskName(), entry.getValue()
									.getStartDateTime().toString(), entry
									.getValue().getEndDateTime().toString(),
							syncDateTime.toString());

					// Set local task sync details
					entry.getValue().setTaskUpdated(syncDateTime);
					entry.getValue().setTaskLastSync(syncDateTime);
				}
			}
		}
	}

	private void pullSync(DateTime syncDateTime) throws IOException,
			ServiceException {
		List<CalendarEventEntry> googleCalendarEvents = googleCalendar
				.getEventList();
		Iterator<CalendarEventEntry> iterator = googleCalendarEvents.iterator();

		// pull sync remote tasks
		while (iterator.hasNext()) {
			CalendarEventEntry gCalEntry = iterator.next();
			if (gCalTaskList.containsKey(gCalEntry.getId())) {

				Task localTaskEntry = gCalTaskList.get(gCalEntry.getId());

				// pull newer remote task
				if (localTaskEntry.getTaskLastSync().compareTo(
						new DateTime(gCalEntry.getUpdated().getValue())) < 0) {

					System.out.println("pulling newer event : "
							+ localTaskEntry.getTaskName());
					System.out.println(localTaskEntry.getTaskLastSync() + " "
							+ new DateTime(gCalEntry.getUpdated().getValue()));

					// update remote task
					CalendarEventEntry updatedEvent = googleCalendar
							.updateEvent(gCalEntry.getId(), gCalEntry
									.getTitle().getPlainText(), gCalEntry
									.getTimes().get(0).getStartTime()
									.toString(), gCalEntry.getTimes().get(0)
									.getStartTime().toString(), syncDateTime
									.toString());

					// edit local task
					localTaskEntry.setTaskName(gCalEntry.getTitle()
							.getPlainText());
					localTaskEntry.setStartDateTime(new DateTime(gCalEntry
							.getTimes().get(0).getStartTime().toString()));
					localTaskEntry.setEndDateTime(new DateTime(gCalEntry
							.getTimes().get(0).getEndTime().toString()));
					localTaskEntry.setTaskLastSync(syncDateTime);
					localTaskEntry.setTaskUpdated(syncDateTime);

					System.out.println(syncDateTime + " "
							+ updatedEvent.getUpdated());

				}
			} else {
				// pull new remote task
				System.out.println("pulling new event");
				// add task
				Task newTask = new TimedTask(getNewTaskId(), gCalEntry,
						syncDateTime);
				// create new task
				taskList.put(newTask.getTaskId(), newTask);

				System.out.println(newTask.getTaskLastSync());

				// update remote task
				CalendarEventEntry updatedEvent = googleCalendar.updateEvent(
						gCalEntry.getId(), gCalEntry.getTitle().getPlainText(),
						gCalEntry.getTimes().get(0).getStartTime().toString(),
						gCalEntry.getTimes().get(0).getStartTime().toString(),
						syncDateTime.toString());

				System.out.println(updatedEvent.getUpdated());

			}
		}
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
			if (taskCategory.compareTo(queryTaskCategory) == 0) {
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
	public List<Task> query(String taskName, TaskCategory taskCategory,
			DateTime startTime, DateTime endTime) {
		List<Task> queriedTaskRecordset = new LinkedList<Task>();

		// TODO

		return null;
	}

	/**
	 * Adds a task
	 * 
	 * @param task
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void add(Task task) throws IOException, ServiceException {
		task.setTaskId(getNewTaskId());

		Task taskToAdd = task.clone();
		taskList.put(taskToAdd.getTaskId(), taskToAdd);

		// TODO add to google calendar logic

		googleCalendar.createEvent(taskToAdd);

		saveTaskRecordFile();
	}

	/**
	 * Deletes a task
	 * 
	 * @param taskId
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void delete(int taskId) throws IOException, ServiceException {
		// check if task exists
		if (taskList.containsKey(taskId)) {
			Task taskToDelete = taskList.get(taskId);
			taskToDelete.setDeleted(true);
			taskList.put(taskToDelete.getTaskId(), taskToDelete);

			// TODO delete from google calendar logic

			googleCalendar.getEvent(taskToDelete.getgCalTaskId());

			saveTaskRecordFile();
		} else {
			throw new Error("Invalid Task");
		}
	}

	/**
	 * Updates task
	 * 
	 * @param updatedTask
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void update(Task updatedTask) throws IOException, ServiceException {
		// check if task exists
		if (taskList.containsKey(updatedTask.getTaskId())) {

			// TODO update from google calendar logic

			String gCalId = null;
			CalendarEventEntry updatedGCalEvent;
			if (updatedTask.getgCalTaskId().equalsIgnoreCase("null")
					|| updatedTask.getgCalTaskId() == null) {
				gCalId = googleCalendar.createEvent(updatedTask);
			} else {
				updatedGCalEvent = googleCalendar.updateEvent(updatedTask
						.getgCalTaskId(), updatedTask.getTaskName(),
						updatedTask.getStartDateTime().toString(), updatedTask
								.getEndDateTime().toString(), updatedTask
								.getTaskUpdated().toString());
			}
			taskList.put(updatedTask.getTaskId(), updatedTask.clone());
			saveTaskRecordFile();

		} else {
			throw new Error("Invalid Task");
		}
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
	 * Clears expired and deleted tasks
	 * 
	 * @throws IOException
	 */
	public void clearExpiredTasks() throws IOException {
		// TODO
	}

	/**
	 * Clears database
	 * 
	 * @throws IOException
	 */
	public void clearDatabase() throws IOException {
		taskList.clear();
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