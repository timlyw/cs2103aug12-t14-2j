/**
 * Component Test for Database
 * @author timlyw
 */
package mhs.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import mhs.src.storage.Database;
import mhs.src.storage.DatabaseAlreadyInstantiatedException;
import mhs.src.storage.DatabaseFactory;
import mhs.src.storage.DatabaseFactoryNotInstantiatedException;
import mhs.src.storage.DeadlineTask;
import mhs.src.storage.FloatingTask;
import mhs.src.storage.GoogleCalendarMhs;
import mhs.src.storage.InvalidTaskFormatException;
import mhs.src.storage.Task;
import mhs.src.storage.TaskCategory;
import mhs.src.storage.TaskNotFoundException;
import mhs.src.storage.TimedTask;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ResourceNotFoundException;
import com.google.gdata.util.ServiceException;

public class DatabaseTest {

	Database database;
	Map<Integer, Task> taskList;
	List<Task> queryList;

	Task task;
	Task task2;
	Task task3;
	Task task4;
	Task task5;

	private static final int MAX_TIMEOUT_BACKGROUND_SYNC_TIME_IN_SECONDS = 60;
	private static final String TEST_TASK_1_NAME = "task 1 - a meeting";
	private static final String TEST_TASK_2_NAME = "task 2 - a project meeting";
	private static final String TEST_TASK_3_NAME = "task 3 - assignment due";
	private static final String TEST_TASK_4_NAME = "task 4 - project due";
	private static final String TEST_TASK_5_NAME = "task 5 - play more games";
	private static final String GOOGLE_APP_NAME = "My Hot Secretary";
	private static final String GOOGLE_TEST_ACCOUNT_NAME = "cs2103mhs@gmail.com";
	private static final String GOOGLE_TEST_ACCOUNT_PASSWORD = "myhotsec2103";

	// Exception Messages
	private static final String EXCEPTION_MESSAGE_INVALID_TASK_FORMAT = "Invalid Task Format!";
	private static final String EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST = "Task does not exist!";
	private static final String EXCEPTION_MESSAGE_NULL_PARAMETER = "%1$s cannot be null!";

	private static final String PARAMETER_TASK_RECORD_FILE_NAME = "taskRecordFileName";
	private static final String PARAMETER_TASK = "task";
	private static final String PARAMETER_TASK_NAME = "taskName";
	private static final String PARAMETER_START_AND_END_DATE_TIMES = "start and end date times";
	private final static String TEST_TASK_RECORD_FILENAME = "testTaskRecordFile.json";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	/**
	 * Setup database environment for test
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void testDatabaseSetup() throws IOException, ServiceException,
			DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {
		initializeDatabase();
		initializeTasks();
		initializeTaskList();
	}

	/**
	 * Initialize tasklist with test tasks
	 */
	private void initializeTaskList() {
		// create new taskList
		taskList = new LinkedHashMap<Integer, Task>();

		taskList.put(task.getTaskId(), task);
		taskList.put(task2.getTaskId(), task2);
		taskList.put(task3.getTaskId(), task3);
		taskList.put(task4.getTaskId(), task4);
		taskList.put(task5.getTaskId(), task5);
	}

	/**
	 * Initilize test tasks
	 */
	private void initializeTasks() {
		// create test tasks
		DateTime dt = DateTime.now();
		DateTime dt2 = DateTime.now();

		task = new TimedTask(1, TEST_TASK_1_NAME, TaskCategory.TIMED, dt,
				dt2.plusHours(5), null, null, null, null, false, false);
		task2 = new TimedTask(2, TEST_TASK_2_NAME, TaskCategory.TIMED, dt,
				dt2.plusHours(1), null, null, null, null, false, false);
		task3 = new DeadlineTask(3, TEST_TASK_3_NAME, TaskCategory.DEADLINE,
				dt, null, null, null, null, false, false);
		task4 = new DeadlineTask(4, TEST_TASK_4_NAME, TaskCategory.DEADLINE,
				dt, null, null, null, null, false, false);
		task5 = new FloatingTask(5, TEST_TASK_5_NAME, TaskCategory.FLOATING,
				null, null, null, false, false);
	}

	@Test
	/**
	 * Tests database query under local environment
	 * 
	 */
	public void testQueryDatabase() throws InvalidTaskFormatException,
			IOException, ServiceException, TaskNotFoundException,
			IllegalArgumentException, DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {

		getCleanDatabaseWithoutSync();

		database.add(task);
		database.add(task2);
		database.add(task3);
		database.add(task4);
		database.add(task5);

		testQueryByTaskId();
		testQueryByTaskName();
		testQueryByTaskCategory();
		testQueryByDate();
	}

	/**
	 * Test query by date
	 * 
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	private void testQueryByDate() throws NullPointerException, IOException,
			ServiceException, TaskNotFoundException, InvalidTaskFormatException {
		// Query by date
		new DateTime();
		DateTime testStartDt = DateTime.now().minusDays(1).minusHours(1);
		new DateTime();
		DateTime testEndDt = DateTime.now().minusDays(1);

		task = new TimedTask(1, TEST_TASK_1_NAME, TaskCategory.TIMED,
				testStartDt, testEndDt, testStartDt, testStartDt, testStartDt,
				null, false, false);

		database.update(task);

		// Boundary testing
		// Test on boundary
		queryList = database.query(testStartDt, testEndDt, false);

		assertEquals(queryList.size(), 1);
		assertEquals(queryList.get(0).getTaskId(), 1);

		// Test around boundary
		queryList = database.query(testStartDt.minusMinutes(1), testEndDt,
				false);

		assertEquals(queryList.size(), 1);
		assertEquals(queryList.get(0).getTaskId(), 1);

		queryList = database
				.query(testStartDt, testEndDt.plusMinutes(1), false);

		assertEquals(queryList.size(), 1);
		assertEquals(queryList.get(0).getTaskId(), 1);
	}

	/**
	 * Test Query by task category
	 */
	private void testQueryByTaskCategory() {
		Iterator<Task> iterator;
		// Test query by task category
		// Query Timed Tasks
		queryList = database.query(TaskCategory.TIMED, false);

		iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			assertTrue(matchedTask.getTaskId() == 1
					|| matchedTask.getTaskId() == 2);
		}
		assertEquals(queryList.size(), 2);

		// Query Deadline Tasks
		queryList = database.query(TaskCategory.DEADLINE, false);
		assertEquals(queryList.size(), 2);

		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			assertTrue(matchedTask.getTaskId() == 3
					|| matchedTask.getTaskId() == 4);
		}

		// Query Floating Tasks
		queryList = database.query(TaskCategory.FLOATING, false);
		assertEquals(queryList.size(), 1);

		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			assertTrue(matchedTask.getTaskId() == 5);
		}
	}

	/**
	 * Test query by task name
	 */
	private void testQueryByTaskName() {
		// Test query by task name
		// word query
		queryList = database.query("assignment", false);
		assertEquals(queryList.size(), 1);
		assertEquals(queryList.get(0).getTaskId(), 3);
		Iterator<Task> iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			assertTrue(matchedTask.getTaskId() == 3);
		}

		// multiple match query
		queryList = database.query("meeting", false);
		iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			assertTrue(matchedTask.getTaskId() == 1
					|| matchedTask.getTaskId() == 2);
		}

		// substring name query
		queryList = database.query("meet", false);
		assertEquals(queryList.size(), 2);

		iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			assertTrue(matchedTask.getTaskId() == 1
					|| matchedTask.getTaskId() == 2);
		}
	}

	/**
	 * Test query by task id
	 * 
	 * @throws TaskNotFoundException
	 */
	private void testQueryByTaskId() throws TaskNotFoundException {
		// Test query by taskId
		Task queriedTask = database.query(1);
		assertEquals(queriedTask.getTaskId(), 1);
	}

	@Test
	/**
	 * Test IllegalArgumentException for query
	 * @throws IOException
	 * @throws ServiceException
	 * @throws TaskNotFoundException
	 */
	public void testQueryTaskIllegalArgumentException() throws IOException,
			ServiceException, TaskNotFoundException, IllegalArgumentException,
			DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(String.format(EXCEPTION_MESSAGE_NULL_PARAMETER,
				PARAMETER_TASK_NAME));
		getCleanDatabaseWithoutSync();
		database.query(null, null, null, false);
	}

	@Test
	/**
	 * Test TaskNotFoundException for query
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 * @throws TaskNotFoundException
	 */
	public void testQueryTaskNotFoundException() throws IOException,
			ServiceException, TaskNotFoundException, IllegalArgumentException,
			DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {
		thrown.expect(TaskNotFoundException.class);
		thrown.expectMessage(EXCEPTION_MESSAGE_TASK_DOES_NOT_EXIST);
		getCleanDatabaseWithoutSync();
		database.query(-1);
	}

	@Test
	/**
	 * Test database add, and taskKeyId generator under local environment
	 * @throws IOException
	 */
	public void testAddDatabase() throws IOException, ServiceException,
			InvalidTaskFormatException, TaskNotFoundException,
			IllegalArgumentException, DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {

		getCleanDatabaseWithoutSync();

		System.out.println("Adding to database...");

		database.add(task);
		database.add(task3);
		database.add(task5);

		testAddTimedTask();
		testAddDeadlineTask();
		testAddFloatingTask();

	}

	private void testAddFloatingTask() throws TaskNotFoundException {
		// Add floating task
		Task addedTask3 = database.query(3);
		assertEquals(task5.getTaskName(), addedTask3.getTaskName());
		assertEquals(task5.getStartDateTime(), addedTask3.getStartDateTime());
		assertEquals(task5.getEndDateTime(), addedTask3.getEndDateTime());
		assertEquals(task5.getgCalTaskId(), addedTask3.getgCalTaskId());
		assertEquals(task5.getTaskCategory(), addedTask3.getTaskCategory());
	}

	private void testAddDeadlineTask() throws TaskNotFoundException {
		// Add deadline task
		Task addedTask2 = database.query(2);
		assertEquals(task3.getTaskName(), addedTask2.getTaskName());
		assertEquals(task3.getEndDateTime(), addedTask2.getEndDateTime());
		assertEquals(task3.getgCalTaskId(), addedTask2.getgCalTaskId());
		assertEquals(task3.getTaskCategory(), addedTask2.getTaskCategory());
	}

	private void testAddTimedTask() throws TaskNotFoundException {
		// Add timed task
		Task addedTask = database.query(1);
		assertEquals(task.getTaskName(), addedTask.getTaskName());
		assertEquals(task.getStartDateTime(), addedTask.getStartDateTime());
		assertEquals(task.getEndDateTime(), addedTask.getEndDateTime());
		assertEquals(task.getgCalTaskId(), addedTask.getgCalTaskId());
		assertEquals(task.getTaskCategory(), addedTask.getTaskCategory());
	}

	@Test
	/**
	 * Test TaskNotFoundException for query
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 * @throws TaskNotFoundException
	 */
	public void testInvalidTaskFormatAdd() throws IOException,
			ServiceException, TaskNotFoundException,
			InvalidTaskFormatException, IllegalArgumentException,
			DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {
		thrown.expect(InvalidTaskFormatException.class);
		thrown.expectMessage(EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		getCleanDatabaseWithoutSync();

		// Set task with null
		task.setTaskName(null);
		database.add(task);
	}

	@Test
	public void testInvalidTaskFormatAddInvalidTimed() throws IOException,
			ServiceException, TaskNotFoundException,
			InvalidTaskFormatException, IllegalArgumentException,
			DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {
		thrown.expect(InvalidTaskFormatException.class);
		thrown.expectMessage(EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		getCleanDatabaseWithoutSync();

		// Set timed task without endDateTime
		task.setStartDateTime(null);
		database.add(task);
	}

	@Test
	public void testInvalidTaskFormatAddInvalidDeadline() throws IOException,
			ServiceException, TaskNotFoundException,
			InvalidTaskFormatException, IllegalArgumentException,
			DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {
		thrown.expect(InvalidTaskFormatException.class);
		thrown.expectMessage(EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		getCleanDatabaseWithoutSync();

		// Set deadline task without endDateTime
		task3.setEndDateTime(null);
		database.add(task3);
	}

	@Test
	/**
	 * Test TaskNotFoundException for query
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 * @throws TaskNotFoundException
	 */
	public void testInvalidTaskFormatAddFloating() throws IOException,
			ServiceException, TaskNotFoundException,
			InvalidTaskFormatException, IllegalArgumentException,
			DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {
		thrown.expect(InvalidTaskFormatException.class);
		thrown.expectMessage(EXCEPTION_MESSAGE_INVALID_TASK_FORMAT);
		getCleanDatabaseWithoutSync();

		// Set task with null
		task.setTaskName(null);
		database.add(task);
	}

	@Test
	/**
	 * Tests update database under local environment
	 */
	public void testUpdateDatabase() throws IOException, ServiceException,
			NullPointerException, TaskNotFoundException,
			InvalidTaskFormatException, IllegalArgumentException,
			DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {

		getCleanDatabaseWithoutSync();

		System.out.println("Test update Database...");

		database.add(task);
		queryList = database.query(false);

		Task editTask = task.clone();
		String newTaskName = "edited! task 1 - meeting";
		editTask.setTaskName(newTaskName);

		new DateTime();
		DateTime editedDateTime = DateTime.now().plusDays(1);
		new DateTime();
		DateTime editedDateTime2 = DateTime.now().plusDays(2);
		new DateTime();
		DateTime editedDateTime3 = DateTime.now().plusDays(3);
		new DateTime();
		DateTime editedDateTime4 = DateTime.now().plusDays(4);
		new DateTime();
		DateTime editedDateTime5 = DateTime.now().plusDays(5);

		editTask.setStartDateTime(editedDateTime);
		editTask.setEndDateTime(editedDateTime2);
		editTask.setTaskCreated(editedDateTime3);
		editTask.setTaskUpdated(editedDateTime4);
		editTask.setTaskLastSync(editedDateTime5);

		database.update(editTask);
		queryList = database.query(false);

		assertEquals(newTaskName, queryList.get(0).getTaskName());
		assertEquals(editTask.getStartDateTime(), queryList.get(0)
				.getStartDateTime());
		assertEquals(editTask.getEndDateTime(), queryList.get(0)
				.getEndDateTime());

		// non-editable fields
		assertFalse(editTask.getTaskCreated() == queryList.get(0)
				.getTaskCreated());
		assertFalse(editTask.getTaskLastSync() == queryList.get(0)
				.getTaskLastSync());

		// updated time is changed
		assertFalse(editTask.getTaskUpdated().equals(
				queryList.get(0).getTaskUpdated()));

	}

	@Test
	/**
	 * Test delete under local environment
	 */
	public void testDeleteDatabase() throws IOException, ServiceException,
			InvalidTaskFormatException, TaskNotFoundException,
			IllegalArgumentException, DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {

		getCleanDatabaseWithoutSync();
		System.out.println("Adding to database...");

		database.add(task);
		database.add(task2);
		database.add(task3);
		database.add(task4);
		database.add(task5);

		queryList = database.query(false);

		System.out.println("after deleting task 1 and 2");
		database.delete(1);
		database.delete(2);

		queryList = database.query(false);
		assertEquals(queryList.size(), 3);

		Iterator<Task> iterator2 = queryList.iterator();
		while (iterator2.hasNext()) {
			Task matchedTask = iterator2.next();
			assertFalse(matchedTask.getTaskId() == 1
					|| matchedTask.getTaskId() == 2);
			assertTrue(matchedTask.getTaskId() == 3
					|| matchedTask.getTaskId() == 4
					|| matchedTask.getTaskId() == 5);
		}
	}

	@Test
	/**
	 * Tests Sync methods 
	 *  - push-sync new task
	 *  - push sync existing task
	 *  - pull-sync new task 
	 *  - pull-sync existing task
	 */
	public void testSyncDatabase() throws Exception {

		getCleanDatabaseWithSync();
		GoogleCalendarMhs gCal = initializeGoogleCalendar();

		assertTrue(database.isUserGoogleCalendarAuthenticated());

		System.out.println("Push Sync Newer Task");
		testPushSyncNewTask(gCal);
		System.out.println("Push sync existing task");
		testPushSyncExistingTask(gCal);
		System.out.println("Pull sync new task");
		CalendarEventEntry createdEvent = testPullSyncNewTask(gCal);
		System.out.println("Pull sync newer task");
		testPullSyncNewerTask(gCal, createdEvent);
	}

	/**
	 * Test Pull Sync Newer Task
	 * 
	 * @param gCal
	 * @param createdEvent
	 * @throws IOException
	 * @throws ServiceException
	 * @throws UnknownHostException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private void testPullSyncNewerTask(GoogleCalendarMhs gCal,
			CalendarEventEntry createdEvent) throws IOException,
			ServiceException, UnknownHostException, InterruptedException,
			ExecutionException, TimeoutException {

		queryList = database.query(createdEvent.getTitle().getPlainText()
				.toString(), false);
		Task addedTask = queryList.get(0);

		// Test pull newer task sync
		String updatedEventName = "Updated Event on Google";
		addedTask.setTaskName(updatedEventName);
		addedTask.setgCalTaskId(createdEvent.getIcalUID());
		CalendarEventEntry updatedCreatedEvent = gCal.updateEvent(addedTask);

		assertTrue(addedTask.getTaskLastSync().isBefore(
				new DateTime(updatedCreatedEvent.getUpdated().getValue())));

		database.syncronizeDatabases();
		database.waitForSyncronizeBackgroundTaskToComplete(MAX_TIMEOUT_BACKGROUND_SYNC_TIME_IN_SECONDS);

		queryList = database.query(updatedEventName, false);

		// Check that task is updated and not created
		assertEquals(1, queryList.size());
		// Check that local task is updated
		assertEquals(updatedCreatedEvent.getIcalUID(), queryList.get(0)
				.getgCalTaskId());
		assertEquals(updatedCreatedEvent.getTitle().getPlainText(), queryList
				.get(0).getTaskName());
	}

	/**
	 * Test Pull Sync New Task
	 * 
	 * @param gCal
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 * @throws UnknownHostException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private CalendarEventEntry testPullSyncNewTask(GoogleCalendarMhs gCal)
			throws IOException, ServiceException, UnknownHostException,
			InterruptedException, ExecutionException, TimeoutException {
		// Test pull new task sync
		CalendarEventEntry createdEvent = gCal.createEvent(task3);

		database.syncronizeDatabases();
		database.waitForSyncronizeBackgroundTaskToComplete(MAX_TIMEOUT_BACKGROUND_SYNC_TIME_IN_SECONDS);

		queryList = database.query(false);
		Iterator<Task> iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			System.out.println(matchedTask.toString());
		}

		queryList = database.query(task3.getTaskName(), false);
		assertEquals(1, queryList.size());
		assertEquals(createdEvent.getIcalUID(), queryList.get(0)
				.getgCalTaskId());
		return createdEvent;
	}

	/**
	 * Test Push Sync Existing Task
	 * 
	 * @param gCal
	 * @throws IOException
	 */
	private void testPushSyncExistingTask(GoogleCalendarMhs gCal)
			throws Exception, IOException {

		// Test push updated task sync
		Task updatedTask = queryList.get(0);
		updatedTask.setTaskName("Updated Task");
		database.update(updatedTask);

		Task queryTask = database.query(updatedTask.getTaskId());

		assertEquals(updatedTask.getTaskName(),
				gCal.retrieveEvent(queryTask.getgCalTaskId()).getTitle()
						.getPlainText());
	}

	/**
	 * Test Push Sync New Task
	 * 
	 * @param gCal
	 * @throws IOException
	 * @throws InvalidTaskFormatException
	 * @throws ResourceNotFoundException
	 * @throws NullPointerException
	 */
	private void testPushSyncNewTask(GoogleCalendarMhs gCal)
			throws IOException, InvalidTaskFormatException,
			NullPointerException, ResourceNotFoundException {
		database.add(task);
		database.add(task2);

		// Test push new task sync
		queryList = database.query(false);

		assertEquals(2, queryList.size());
		assertTrue(gCal.retrieveEvent(queryList.get(0).getgCalTaskId()) != null);
		assertTrue(gCal.retrieveEvent(queryList.get(1).getgCalTaskId()) != null);
	}

	/**
	 * Initialize google calendar for testing purposes
	 * 
	 * @return
	 * @throws ServiceException
	 * @throws IOException
	 * @throws NullPointerException
	 */
	private GoogleCalendarMhs initializeGoogleCalendar()
			throws NullPointerException, IOException, ServiceException {
		// we use a separate GoogleCalendar to query events (need to pullEvents
		// manually)
		String googleAccessToken = (GoogleCalendarMhs.retrieveUserToken(
				GOOGLE_APP_NAME, GOOGLE_TEST_ACCOUNT_NAME,
				GOOGLE_TEST_ACCOUNT_PASSWORD));
		GoogleCalendarMhs gCal = new GoogleCalendarMhs(GOOGLE_APP_NAME,
				GOOGLE_TEST_ACCOUNT_NAME, googleAccessToken);
		return gCal;
	}

	private void initializeDatabase() throws IOException, ServiceException,
			DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {
		DatabaseFactory.destroy();
		DatabaseFactory.getDatabaseFactory(TEST_TASK_RECORD_FILENAME, true);
		database = DatabaseFactory.getDatabaseInstance();
	}

	/**
	 * Initialize empty database with sync enabled
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 * @throws DatabaseFactoryNotInstantiatedException
	 * @throws IllegalArgumentException
	 * @throws DatabaseAlreadyInstantiatedException
	 */
	private void getCleanDatabaseWithSync() throws IOException,
			ServiceException, InterruptedException, ExecutionException,
			TimeoutException, IllegalArgumentException,
			DatabaseFactoryNotInstantiatedException,
			DatabaseAlreadyInstantiatedException {

		database.loginUserGoogleAccount(GOOGLE_TEST_ACCOUNT_NAME,
				GOOGLE_TEST_ACCOUNT_PASSWORD);

		// Test whether isUserGoogleCalendarAuthenticated reflects correct
		// status
		assertTrue(database.isUserGoogleCalendarAuthenticated());
		database.waitForSyncronizeBackgroundTaskToComplete(MAX_TIMEOUT_BACKGROUND_SYNC_TIME_IN_SECONDS);
		database.clearDatabase();
		assertEquals(0, database.query(false).size());
	}

	/**
	 * Initializes a clean database with sync disabled
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 * @throws DatabaseAlreadyInstantiatedException
	 * @throws DatabaseFactoryNotInstantiatedException
	 * @throws IllegalArgumentException
	 */
	private void getCleanDatabaseWithoutSync() throws IOException,
			ServiceException, DatabaseAlreadyInstantiatedException,
			IllegalArgumentException, DatabaseFactoryNotInstantiatedException {
		database.clearDatabase();
		database.logOutUserGoogleAccount();
		// Test whether isUserGoogleCalendarAuthenticated reflects correct
		// status
		assertFalse(database.isUserGoogleCalendarAuthenticated());
	}

	@After
	public void testAfter() throws IOException, ServiceException {
		if (database != null) {
			database.clearDatabase();
		}
	}
}
