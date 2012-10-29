/**
 * Component Test for Database
 * @author timlyw
 */
package mhs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mhs.src.storage.Database;
import mhs.src.storage.DeadlineTask;
import mhs.src.storage.FloatingTask;
import mhs.src.storage.GoogleCalendar;
import mhs.src.storage.Task;
import mhs.src.storage.TaskCategory;
import mhs.src.storage.TimedTask;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class DatabaseTest {

	private static final String TEST_TASK_5_NAME = "task 5 - play more games";
	private static final String TEST_TASK_4_NAME = "task 4 - project due";
	private static final String TEST_TASK_3_NAME = "task 3 - assignment due";
	private static final String TEST_TASK_2_NAME = "task 2 - a project meeting";
	private static final String TEST_TASK_1_NAME = "task 1 - a meeting";
	private static final String GOOGLE_APP_NAME = "My Hot Secretary";
	private static final String GOOGLE_TEST_ACCOUNT_NAME = "cs2103mhs@gmail.com";
	private static final String GOOGLE_TEST_ACCOUNT_PASSWORD = "myhotsec2103";

	Database database;
	Map<Integer, Task> taskList;
	List<Task> queryList;

	Task task;
	Task task2;
	Task task3;
	Task task4;
	Task task5;

	private final static String TEST_TASK_RECORD_FILENAME = "testTaskRecordFile.json";

	@Before
	public void testDatabaseSetup() throws IOException, ServiceException {
		initializeTasks();
		initializeTaskList();
	}

	private void initializeTaskList() {
		// create new taskList
		taskList = new LinkedHashMap<Integer, Task>();

		taskList.put(task.getTaskId(), task);
		taskList.put(task2.getTaskId(), task2);
		taskList.put(task3.getTaskId(), task3);
		taskList.put(task4.getTaskId(), task4);
		taskList.put(task5.getTaskId(), task5);
	}

	private void initializeTasks() {
		// create test tasks
		DateTime dt = DateTime.now();
		DateTime dt2 = DateTime.now();

		task = new TimedTask(1, TEST_TASK_1_NAME, TaskCategory.TIMED, dt, dt2,
				null, null, null, null, false, false);
		task2 = new TimedTask(2, TEST_TASK_2_NAME, TaskCategory.TIMED, dt, dt2,
				null, null, null, null, false, false);
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
	 * @throws Exception
	 */
	public void testQueryDatabase() throws Exception {

		initializeCleanDatabaseWithoutSync();

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

	private void testQueryByDate() throws Exception {
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

	private void testQueryByTaskId() throws Exception {
		// Test query by taskId
		Task queriedTask = database.query(1);
		assertEquals(queriedTask.getTaskId(), 1);
	}

	@Test
	/**
	 * Test database add, and taskKeyId generator under local environment
	 * @throws IOException
	 */
	public void testAddDatabase() throws Exception {

		initializeCleanDatabaseWithoutSync();

		System.out.println("Adding to database...");

		database.add(task);
		database.add(task3);
		database.add(task5);

		testAddTimedTask();
		testAddDeadlineTask();
		testAddFloatingTask();

	}

	private void testAddFloatingTask() throws Exception {
		// Add floating task
		Task addedTask3 = database.query(3);
		assertEquals(task5.getTaskName(), addedTask3.getTaskName());
		assertEquals(task5.getStartDateTime(), addedTask3.getStartDateTime());
		assertEquals(task5.getEndDateTime(), addedTask3.getEndDateTime());
		assertEquals(task5.getgCalTaskId(), addedTask3.getgCalTaskId());
		assertEquals(task5.getTaskCategory(), addedTask3.getTaskCategory());
	}

	private void testAddDeadlineTask() throws Exception {
		// Add deadline task
		Task addedTask2 = database.query(2);
		assertEquals(task3.getTaskName(), addedTask2.getTaskName());
		assertEquals(task3.getEndDateTime(), addedTask2.getEndDateTime());
		assertEquals(task3.getgCalTaskId(), addedTask2.getgCalTaskId());
		assertEquals(task3.getTaskCategory(), addedTask2.getTaskCategory());
	}

	private void testAddTimedTask() throws Exception {
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
	 * Tests update database under local environment
	 * @throws Exception
	 */
	public void testUpdateDatabase() throws Exception {

		initializeCleanDatabaseWithoutSync();

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

	private void initializeCleanDatabaseWithoutSync() throws IOException,
			ServiceException {
		// initialize database without sync for testing
		database = new Database(TEST_TASK_RECORD_FILENAME, true);
		database.clearDatabase();
	}

	@Test
	/**
	 * Test delete under local environment
	 * @throws Exception
	 */
	public void testDeleteDatabase() throws Exception {

		initializeCleanDatabaseWithoutSync();

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
	 * @throws Exception
	 */
	public void testSyncDatabase() throws Exception {

		initializeCleanDatabaseWithSync();
		GoogleCalendar gCal = initializeGoogleCalendar();
		
		System.out.println("Adding new Tasks to push");
		database.add(task);
		database.add(task2);

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
	 */
	private void testPullSyncNewerTask(GoogleCalendar gCal,
			CalendarEventEntry createdEvent) throws IOException,
			ServiceException, UnknownHostException {

		// Test pull newer task sync
		String updatedEventName = "Updated Event on Google";
		CalendarEventEntry updatedCreatedEvent = gCal.updateEvent(createdEvent
				.getIcalUID(), updatedEventName, task3.getStartDateTime()
				.toString(), task3.getEndDateTime().toString());

		database.syncronizeDatabases();
		queryList = database.query(updatedCreatedEvent.getTitle()
				.getPlainText(), false);
		
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
	 */
	private CalendarEventEntry testPullSyncNewTask(GoogleCalendar gCal)
			throws IOException, ServiceException, UnknownHostException {
		// Test pull new task sync
		CalendarEventEntry createdEvent = gCal.createEvent(task3);
		database.syncronizeDatabases();

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
	 * @throws Exception
	 * @throws IOException
	 */
	private void testPushSyncExistingTask(GoogleCalendar gCal)
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
	 * @throws Exception
	 * @throws IOException
	 */
	private void testPushSyncNewTask(GoogleCalendar gCal) throws Exception,
			IOException {
		// Test push new task sync
		queryList = database.query(false);

		assertTrue(gCal.retrieveEvent(queryList.get(0).getgCalTaskId()) != null);
		assertTrue(gCal.retrieveEvent(queryList.get(1).getgCalTaskId()) != null);
	}

	private GoogleCalendar initializeGoogleCalendar()
			throws AuthenticationException {
		// we use a separate GoogleCalendar to query events (need to pullEvents
		// manually)
		String googleAccessToken = (GoogleCalendar.retrieveUserToken(
				GOOGLE_APP_NAME, GOOGLE_TEST_ACCOUNT_NAME,
				GOOGLE_TEST_ACCOUNT_PASSWORD));

		GoogleCalendar gCal = new GoogleCalendar(GOOGLE_APP_NAME,
				GOOGLE_TEST_ACCOUNT_NAME, googleAccessToken);
		return gCal;
	}

	private void initializeCleanDatabaseWithSync() throws IOException,
			ServiceException {
		// Clear database (local and remote)
		database = new Database(TEST_TASK_RECORD_FILENAME, false);
		database.loginUserGoogleAccount(GOOGLE_TEST_ACCOUNT_NAME,
				GOOGLE_TEST_ACCOUNT_PASSWORD);
		database.clearDatabase();
	}

	@After
	public void testAfter() throws IOException, ServiceException {
		database.clearDatabase();
	}
}
