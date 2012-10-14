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
import mhs.src.Task;
import mhs.src.TaskCategory;
import mhs.src.TimedTask;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
	public void testDatabase() throws IOException, ServiceException {

		// initialize database without sync for testing
		database = new Database(TEST_TASK_RECORD_FILENAME, true);
		database.clearDatabase();

		// create test tasks
		new DateTime();
		DateTime dt = DateTime.now();
		new DateTime();
		DateTime dt2 = DateTime.now();
		new DateTime();
		DateTime dt3 = DateTime.now();
		new DateTime();
		DateTime dt4 = DateTime.now();
		new DateTime();
		DateTime dt5 = DateTime.now();

		task = new TimedTask(1, "task 1 - a meeting", "TIMED", dt, dt2, dt3,
				dt4, dt5, "null", false, false);
		task2 = new TimedTask(2, "task 2 - a project meeting", "TIMED", dt,
				dt2, dt3, dt4, dt5, "null", false, false);
		task3 = new DeadlineTask(3, "task 3 - assignment due", "DEADLINE", dt,
				dt2, dt3, dt4, "null", false, false);
		task4 = new DeadlineTask(4, "task 4 - project due", "DEADLINE", dt,
				dt2, dt3, dt4, "null", false, false);
		task5 = new FloatingTask(5, "task 5 - play more games", "FLOATING", dt,
				dt2, dt3, false, false);

		// create new taskList
		taskList = new LinkedHashMap<Integer, Task>();

		taskList.put(task.getTaskId(), task);
		taskList.put(task2.getTaskId(), task2);
		taskList.put(task3.getTaskId(), task3);
		taskList.put(task4.getTaskId(), task4);
		taskList.put(task5.getTaskId(), task5);
	}

	@Test
	public void testSyncDatabase() throws IOException, ServiceException {
		System.out.println("Sync Test");
		database.syncronizeDatabases();
		database.clearDatabase();
	}

	@Test
	public void testSyncPushDatabase() throws Exception {
		// Clear database (local and remote)
		database = new Database(TEST_TASK_RECORD_FILENAME, false);
		database.clearDatabase();

		System.out.println("Adding new Tasks to push");
		database.add(task);
		database.add(task2);

		database = new Database(TEST_TASK_RECORD_FILENAME, true);
		List<Task> queryTaskList = database.query();

		assertEquals(2, queryTaskList.size());
	}

	@Test
	public void testSyncPushNewerTask() throws Exception {

		// Clear database (local and remote)
		database = new Database(TEST_TASK_RECORD_FILENAME, false);
		database.clearDatabase();

		// add task and push
		System.out.println("Adding new task");
		database.add(task);

		// update task and push
		Task updatedTask = database.query(1);
		updatedTask.setTaskName("Task 1 Updated");
		database.update(updatedTask);

		database.clearLocalDatabase();
		database.syncronizeDatabases();

		List<Task> queryTaskList = database.query();
		assertEquals(1, queryTaskList.size());

		Task pushedTask = database.query(1);
		assertEquals("Task 1 Updated", pushedTask.getTaskName());
	}

	@Test
	public void testUpdatedPullSync() throws Exception {

		database.clearDatabase();
		database.syncronizeDatabases();

		List<Task> tempTasks = database.query();
		Iterator<Task> iterator = tempTasks.iterator();
		// while (iterator.hasNext()) {
		// implement test
		// }

		if (tempTasks.size() > 1) {
			// update task to pull
			tempTasks.get(1).setTaskLastSync(new DateTime().now().minusDays(1));

			database.update(tempTasks.get(1));
		}

	}

	@Test
	public void testSyncLocalDeletedTask() throws IOException, ServiceException {
	}

	@Test
	public void testClearDatabase() throws Exception {
		System.out.println("Clearing database Test");
		database.add(task);
		database.clearDatabase();
		queryList = database.query();
		assertEquals(0, queryList.size());
	}

	@Test
	public void testQueryTaskIdDatabase() throws Exception {
		System.out.println("Query Task Id Test");

		// Query by taskId
		database.add(task);
		database.add(task2);
		database.add(task3);
		database.add(task4);
		database.add(task5);

		Task queriedTask = database.query(1);
		assertEquals(queriedTask.getTaskId(), 1);
	}

	@Test
	public void testQueryTaskNameDatabase() throws Exception {
		System.out.println("Query Task Name Test");
		// Query by Name
		database.add(task);
		database.add(task2);
		database.add(task3);
		database.add(task4);
		database.add(task5);

		// word query
		queryList = database.query("assignment");
		assertEquals(queryList.size(), 1);
		assertEquals(queryList.get(0).getTaskId(), 3);

		// multiple match query
		queryList = database.query("meeting");
		assertEquals(queryList.size(), 2);

		// substring name query
		queryList = database.query("meet");
		assertEquals(queryList.size(), 2);
	}

	@Test
	public void testQueryTaskCategoryDatabase() throws Exception {
		System.out.println("Query Task Category");

		database.add(task);
		database.add(task2);
		database.add(task3);
		database.add(task4);
		database.add(task5);

		// Query Timed Tasks
		System.out.println("Query Timed Task");
		queryList = database.query(TaskCategory.TIMED);

		Iterator<Task> iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			System.out.println(matchedTask.toString());
		}
		assertEquals(queryList.size(), 2);

		// Query Deadline Tasks
		System.out.println("Query Deadline Task");
		queryList = database.query(TaskCategory.DEADLINE);
		assertEquals(queryList.size(), 2);

		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			System.out.println(matchedTask.toString());
		}

		// Query Floating Tasks
		System.out.println("Query Floating Task");
		queryList = database.query(TaskCategory.FLOATING);
		assertEquals(queryList.size(), 1);

		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			System.out.println(matchedTask.toString());
		}

	}

	@Test
	public void testQueryDateDatabase() throws Exception {
		System.out.println("Query Task by Date");

		new DateTime();
		DateTime testStartDt = DateTime.now().minusDays(1).minusHours(1);
		new DateTime();
		DateTime testEndDt = DateTime.now().minusDays(1);

		task = new TimedTask(1, "task 1 - a meeting", "TIMED", testStartDt,
				testEndDt, testStartDt, testStartDt, testStartDt, "null",
				false, false);

		database.add(task);
		database.add(task2);
		database.add(task3);
		database.add(task4);
		database.add(task5);

		queryList = database.query(testStartDt, testEndDt);

		Iterator<Task> iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			assertEquals(matchedTask.getTaskId(), 1);
			assertFalse(matchedTask.getTaskId() == 2);
			assertFalse(matchedTask.getTaskId() == 3);
			assertFalse(matchedTask.getTaskId() == 4);
			assertFalse(matchedTask.getTaskId() == 5);
			System.out.println(matchedTask.toString());
		}

		queryList = database.query(testStartDt.minusMinutes(10),
				testEndDt.plusMinutes(10));

		Iterator<Task> iterator2 = queryList.iterator();
		while (iterator2.hasNext()) {
			Task matchedTask = iterator2.next();
			assertEquals(matchedTask.getTaskId(), 1);
			assertFalse(matchedTask.getTaskId() == 2);
			assertFalse(matchedTask.getTaskId() == 3);
			assertFalse(matchedTask.getTaskId() == 4);
			assertFalse(matchedTask.getTaskId() == 5);
			System.out.println(matchedTask.toString());
		}
	}

	@Test
	/**
	 * Test database add, and taskKeyId generator
	 * @throws IOException
	 */
	public void testAddToDatabase() throws Exception {

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

		assertFalse(task.getTaskCreated().isEqual(addedTask.getTaskCreated()));
		assertFalse(task.getTaskUpdated().isEqual(addedTask.getTaskUpdated()));

		// Add deadline task
		Task addedTask2 = database.query(2);
		assertEquals(task3.getTaskName(), addedTask2.getTaskName());
		assertEquals(task3.getEndDateTime(), addedTask2.getEndDateTime());
		assertEquals(task3.getgCalTaskId(), addedTask2.getgCalTaskId());
		assertEquals(task3.getTaskCategory(), addedTask2.getTaskCategory());

		assertFalse(task3.getTaskCreated().isEqual(addedTask2.getTaskCreated()));
		assertFalse(task3.getTaskUpdated().isEqual(addedTask2.getTaskUpdated()));

		// Add floating task
		Task addedTask3 = database.query(3);
		assertEquals(task5.getTaskName(), addedTask3.getTaskName());
		assertEquals(task5.getStartDateTime(), addedTask3.getStartDateTime());
		assertEquals(task5.getEndDateTime(), addedTask3.getEndDateTime());
		assertEquals(task5.getgCalTaskId(), addedTask3.getgCalTaskId());
		assertEquals(task5.getTaskCategory(), addedTask3.getTaskCategory());

		assertFalse(task5.getTaskCreated().isEqual(addedTask3.getTaskCreated()));
		assertFalse(task5.getTaskUpdated().isEqual(addedTask3.getTaskUpdated()));

	}

	@Test
	public void testUpdateDatabase() throws Exception {

		System.out.println("Test update Database...");

		database.add(task);

		System.out.println("before update");
		queryList = database.query();
		System.out.println(queryList.get(0).toString());

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
	public void testDeleteDatabase() throws Exception {
		System.out.println("Adding to database...");

		database.add(task);
		database.add(task2);
		database.add(task3);
		database.add(task4);
		database.add(task5);

		System.out.println("before delete");
		queryList = database.query();
		assertEquals(queryList.size(), 5);

		Iterator<Task> iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			System.out.println(matchedTask.toString());
		}

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
			System.out.println(matchedTask.toString());
		}
	}

	@After
	public void testAfter() throws IOException, ServiceException {
		// database.clearDatabase();
		System.out.println(System.lineSeparator());
	}
}
