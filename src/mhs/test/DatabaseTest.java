/**
 * Component Test for Database
 * @author timlyw
 */
package mhs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mhs.src.Database;
import mhs.src.DeadlineTask;
import mhs.src.FloatingTask;
import mhs.src.GoogleCalendar;
import mhs.src.Task;
import mhs.src.TaskCategory;
import mhs.src.TimedTask;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gdata.data.calendar.CalendarEventEntry;
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

	private final static String TEST_TASK_RECORD_FILENAME = "testTaskRecordFile.json";

	@Before
	public void testDatabaseSetup() throws IOException, ServiceException {

		// create test tasks
		new DateTime();
		DateTime dt = DateTime.now();
		new DateTime();
		DateTime dt2 = DateTime.now();
		new DateTime();

		task = new TimedTask(1, "task 1 - a meeting", "TIMED", dt, dt2, null,
				null, null, null, false, false);
		task2 = new TimedTask(2, "task 2 - a project meeting", "TIMED", dt,
				dt2, null, null, null, null, false, false);
		task3 = new DeadlineTask(3, "task 3 - assignment due", "DEADLINE", dt,
				null, null, null, null, false, false);
		task4 = new DeadlineTask(4, "task 4 - project due", "DEADLINE", dt,
				null, null, null, null, false, false);
		task5 = new FloatingTask(5, "task 5 - play more games", "FLOATING",
				null, null, null, false, false);

		// create new taskList
		taskList = new LinkedHashMap<Integer, Task>();

		taskList.put(task.getTaskId(), task);
		taskList.put(task2.getTaskId(), task2);
		taskList.put(task3.getTaskId(), task3);
		taskList.put(task4.getTaskId(), task4);
		taskList.put(task5.getTaskId(), task5);

	}

	@Test
	/**
	 * Tests database query under local environment
	 * @throws Exception
	 */
	public void testQueryDatabase() throws Exception {

		// initialize database without sync for testing
		database = new Database(TEST_TASK_RECORD_FILENAME, true);
		database.clearDatabase();

		database.add(task);
		database.add(task2);
		database.add(task3);
		database.add(task4);
		database.add(task5);

		// Test query by taskId
		Task queriedTask = database.query(1);
		assertEquals(queriedTask.getTaskId(), 1);

		// Test query by task name
		// word query
		queryList = database.query("assignment");
		assertEquals(queryList.size(), 1);
		assertEquals(queryList.get(0).getTaskId(), 3);
		Iterator<Task> iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			assertTrue(matchedTask.getTaskId() == 3);
		}

		// multiple match query
		queryList = database.query("meeting");
		iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			assertTrue(matchedTask.getTaskId() == 1
					|| matchedTask.getTaskId() == 2);
		}

		// substring name query
		queryList = database.query("meet");
		assertEquals(queryList.size(), 2);

		iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			assertTrue(matchedTask.getTaskId() == 1
					|| matchedTask.getTaskId() == 2);
		}

		// Test query by task category
		// Query Timed Tasks
		queryList = database.query(TaskCategory.TIMED);

		iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			assertTrue(matchedTask.getTaskId() == 1
					|| matchedTask.getTaskId() == 2);
		}
		assertEquals(queryList.size(), 2);

		// Query Deadline Tasks
		queryList = database.query(TaskCategory.DEADLINE);
		assertEquals(queryList.size(), 2);

		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			assertTrue(matchedTask.getTaskId() == 3
					|| matchedTask.getTaskId() == 4);
		}

		// Query Floating Tasks
		queryList = database.query(TaskCategory.FLOATING);
		assertEquals(queryList.size(), 1);

		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			assertTrue(matchedTask.getTaskId() == 5);
		}

		// Query by date
		new DateTime();
		DateTime testStartDt = DateTime.now().minusDays(1).minusHours(1);
		new DateTime();
		DateTime testEndDt = DateTime.now().minusDays(1);

		task = new TimedTask(1, "task 1 - a meeting", "TIMED", testStartDt,
				testEndDt, testStartDt, testStartDt, testStartDt, "null",
				false, false);

		database.update(task);

		// Boundary testing
		// Test on boundary
		queryList = database.query(testStartDt, testEndDt);

		assertEquals(queryList.size(), 1);
		assertEquals(queryList.get(0).getTaskId(), 1);

		// Test around boundary
		queryList = database.query(testStartDt.minusMinutes(1), testEndDt);

		assertEquals(queryList.size(), 1);
		assertEquals(queryList.get(0).getTaskId(), 1);

		queryList = database.query(testStartDt, testEndDt.plusMinutes(1));

		assertEquals(queryList.size(), 1);
		assertEquals(queryList.get(0).getTaskId(), 1);
	}

	@Test
	/**
	 * Test database add, and taskKeyId generator under local environment
	 * @throws IOException
	 */
	public void testAddDatabase() throws Exception {

		// initialize database without sync for testing
		database = new Database(TEST_TASK_RECORD_FILENAME, true);
		database.clearDatabase();

		System.out.println("Adding to database...");

		database.add(task);
		database.add(task3);
		database.add(task5);

		// Add timed task
		Task addedTask = database.query(1);
		assertEquals(task.getTaskName(), addedTask.getTaskName());
		assertEquals(task.getStartDateTime(), addedTask.getStartDateTime());
		assertEquals(task.getEndDateTime(), addedTask.getEndDateTime());
		assertEquals(task.getgCalTaskId(), addedTask.getgCalTaskId());
		assertEquals(task.getTaskCategory(), addedTask.getTaskCategory());

		// Add deadline task
		Task addedTask2 = database.query(2);
		assertEquals(task3.getTaskName(), addedTask2.getTaskName());
		assertEquals(task3.getEndDateTime(), addedTask2.getEndDateTime());
		assertEquals(task3.getgCalTaskId(), addedTask2.getgCalTaskId());
		assertEquals(task3.getTaskCategory(), addedTask2.getTaskCategory());

		// Add floating task
		Task addedTask3 = database.query(3);
		assertEquals(task5.getTaskName(), addedTask3.getTaskName());
		assertEquals(task5.getStartDateTime(), addedTask3.getStartDateTime());
		assertEquals(task5.getEndDateTime(), addedTask3.getEndDateTime());
		assertEquals(task5.getgCalTaskId(), addedTask3.getgCalTaskId());
		assertEquals(task5.getTaskCategory(), addedTask3.getTaskCategory());

	}

	@Test
	/**
	 * Tests update database under local environment
	 * @throws Exception
	 */
	public void testUpdateDatabase() throws Exception {

		// initialize database without sync for testing
		database = new Database(TEST_TASK_RECORD_FILENAME, true);
		database.clearDatabase();

		System.out.println("Test update Database...");

		database.add(task);

		System.out.println("before update");
		queryList = database.query();

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

		System.out.println("after update");
		queryList = database.query();
		System.out.println(queryList.get(0).toString());

		assertEquals(newTaskName, queryList.get(0).getTaskName());
		assertEquals(editTask.getStartDateTime(), queryList.get(0)
				.getStartDateTime());
		assertEquals(editTask.getEndDateTime(), queryList.get(0)
				.getEndDateTime());
		assertEquals(editTask.getTaskCreated(), queryList.get(0)
				.getTaskCreated());
		assertEquals(editTask.getTaskLastSync(), queryList.get(0)
				.getTaskLastSync());

		// updated time is changed
		assertFalse(editTask.getTaskUpdated().equals(
				queryList.get(0).getTaskUpdated()));

	}

	@Test
	/**
	 * Test delete under local environment
	 * @throws Exception
	 */
	public void testDeleteDatabase() throws Exception {

		// initialize database without sync for testing
		database = new Database(TEST_TASK_RECORD_FILENAME, true);
		database.clearDatabase();

		System.out.println("Adding to database...");

		database.add(task);
		database.add(task2);
		database.add(task3);
		database.add(task4);
		database.add(task5);

		queryList = database.query();

		System.out.println("after deleting task 1 and 2");
		database.delete(1);
		database.delete(2);

		queryList = database.query();
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
	 * Tests Sync methods (push-sync new task, push sync existing task, pull-sync new task, pull-sync existing task)
	 * @throws Exception
	 */
	public void testSyncDatabase() throws Exception {

		// Clear database (local and remote)
		database = new Database(TEST_TASK_RECORD_FILENAME, false);
		database.clearDatabase();

		// we use a separate GoogleCalendar to query events (need to pullEvents
		// manually)
		GoogleCalendar gCal = new GoogleCalendar();

		// Test push new task sync
		System.out.println("Adding new Tasks to push");
		database.add(task);
		database.add(task2);

		queryList = database.query();
		gCal.pullEvents();

		assertTrue(gCal.getEvent(queryList.get(0).getgCalTaskId()) != null);
		assertTrue(gCal.getEvent(queryList.get(1).getgCalTaskId()) != null);

		// Test push updated task sync
		Task updatedTask = queryList.get(0);
		updatedTask.setTaskName("Updated Task");
		database.update(updatedTask);
		gCal.pullEvents();

		Task queryTask = database.query(updatedTask.getTaskId());

		assertEquals(updatedTask.getTaskName(),
				gCal.getEvent(queryTask.getgCalTaskId()).getTitle()
						.getPlainText());

		// Test pull new task sync
		CalendarEventEntry createdEvent = gCal.createEvent(task3);
		database.syncronizeDatabases();

		queryList = database.query(task3.getTaskName());
		assertEquals(1, queryList.size());
		assertEquals(createdEvent.getIcalUID(), queryList.get(0)
				.getgCalTaskId());

		// Test pull newer task sync
		String updatedEventName = "Updated Event on Google";
		CalendarEventEntry updatedCreatedEvent = gCal.updateEvent(createdEvent
				.getIcalUID(), updatedEventName, task3.getStartDateTime()
				.toString(), task3.getEndDateTime().toString());
		database.syncronizeDatabases();

		queryList = database.query(updatedCreatedEvent.getTitle()
				.getPlainText());

		assertEquals(1, queryList.size());
		assertEquals(updatedCreatedEvent.getIcalUID(), queryList.get(0)
				.getgCalTaskId());
		assertEquals(updatedCreatedEvent.getTitle().getPlainText(), queryList
				.get(0).getTaskName());

	}

	@After
	public void testAfter() throws IOException, ServiceException {
		System.out.println(System.lineSeparator());
		database.clearDatabase();
	}
}
