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
	Task task;
	Task task2;
	Task task3;
	Task task4;
	Task task5;

	List<Task> queryList;

	private final static String TEST_TASK_RECORD_FILENAME = "testTaskRecordFile.json";

	@Before
	public void testDatabase() throws IOException, ServiceException {

		database = new Database(TEST_TASK_RECORD_FILENAME, true);
		database.clearDatabase();

		DateTime dt = new DateTime().now();
		DateTime dt2 = new DateTime().now();
		DateTime dt3 = new DateTime().now();
		DateTime dt4 = new DateTime().now();
		DateTime dt5 = new DateTime().now();

		task = new TimedTask(1, "task 1 - a meeting", "TIMED", dt, dt2, dt3,
				dt4, dt5, "null", false, false);
		task2 = new TimedTask(2, "task 2 - a project meeting", "TIMED", dt,
				dt2, dt3, dt4, dt5, "null", false, false);
		task3 = new DeadlineTask(3, "task 3 - assignment due", "DEADLINE", dt,
				dt2, dt3, dt4, "null", false, false);
		task4 = new DeadlineTask(4, "task 4 - project due", "DEADLINE", dt,
				dt2, dt3, dt4, "null", false, false);
		task5 = new FloatingTask(5, "task 5 - play more games", "FLOATING", dt,
				dt2, dt3, "null", false, false);

		taskList = new LinkedHashMap<Integer, Task>();

		taskList.put(task.getTaskId(), task);
		taskList.put(task2.getTaskId(), task2);
		taskList.put(task3.getTaskId(), task3);
		taskList.put(task4.getTaskId(), task4);
		taskList.put(task5.getTaskId(), task5);
	}

	@Test
	public void testSyncDatabase() throws IOException, ServiceException {
		database = new Database(TEST_TASK_RECORD_FILENAME, false);
		System.out.println("Sync Test");
		database.syncronizeDatabases();
	}

	@Test
	public void testSyncPushDatabase() throws IOException, ServiceException {

		database = new Database(TEST_TASK_RECORD_FILENAME, false);
		System.out.println("Adding new Tasks to push");
		
		database.add(task);
		database.add(task2);
		System.out.println("Manual Sync");
		database.syncronizeDatabases();
	}

	@Test
	public void testSyncPushNewerTask() throws IOException, ServiceException {

		System.out.println("Updating Local Task");

		database = new Database(TEST_TASK_RECORD_FILENAME, false);
		Task updatedTask = database.query(1);
		
		new DateTime();
		updatedTask.setTaskUpdated(DateTime.now());
		updatedTask.setTaskLastSync(DateTime.now().minusMinutes(5));

		System.out.println("task updated datetime : "
				+ updatedTask.getTaskUpdated());
		System.out.println("task sync datetime : "
				+ updatedTask.getTaskLastSync());

		database.update(updatedTask);
		// auto update when connection is available...

		database.syncronizeDatabases();

		System.out.println("updated task updated datetime : "
				+ updatedTask.getTaskUpdated());
		System.out.println("updated task sync datetime : "
				+ updatedTask.getTaskLastSync());

	}

	@Test
	public void testUpdatedPullSync() throws IOException, ServiceException {

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
	public void testClearDatabase() throws IOException, ServiceException {
		System.out.println("Clearing database Test");
		database.add(task);
		database.clearDatabase();
		queryList = database.query();
		assertEquals(0, queryList.size());
	}

	@Test
	public void testQueryTaskIdDatabase() throws IOException, ServiceException {
		System.out.println("Query Task Id Test");
		
		// Query by taskId
		
		database.add(task);
		database.add(task2);
		database.add(task3);
		database.add(task4);
		database.add(task5);
		
		Task queriedTask = database.query(1);
		System.out.println(task.toString());
		System.out.println(queriedTask.toString());
		
		assertEquals(task.toString(), queriedTask.toString());
	}

	@Test
	public void testQueryTaskNameDatabase() throws IOException,
			ServiceException {
		System.out.println("Query Task Name Test");
		// Query by Name
		database.add(task);
		database.add(task2);
		database.add(task3);
		database.add(task4);
		database.add(task5);

		queryList = database.query("Meeting");

		Iterator<Task> iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			assertTrue(matchedTask.toString().equals(task.toString())
					|| matchedTask.toString().equals(task2.toString()));
		}
	}

	@Test
	public void testQueryTaskCategoryDatabase() throws IOException,
			ServiceException {
		System.out.println("Query Task Category");

		database.add(task);
		database.add(task2);
		database.add(task3);
		database.add(task4);
		database.add(task5);

		System.out.println("Query Timed Task");
		queryList = database.query(TaskCategory.TIMED);

		Iterator<Task> iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			System.out.println(matchedTask.toString());
		}
		assertEquals(queryList.size(), 2);

		System.out.println("Query Deadline Task");
		queryList = database.query(TaskCategory.DEADLINE);
		assertEquals(queryList.size(), 2);

		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			System.out.println(matchedTask.toString());
		}

		System.out.println("Query Floating Task");
		queryList = database.query(TaskCategory.FLOATING);
		assertEquals(queryList.size(), 1);

		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			System.out.println(matchedTask.toString());
		}

	}

	@Test
	public void testQueryDateDatabase() throws IOException, ServiceException {
		System.out.println("Query Task by Date");

		DateTime testStartDt = new DateTime().now().minusDays(1).minusHours(1);
		DateTime testEndDt = new DateTime().now().minusDays(1);

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
	public void testAddToDatabase() throws IOException, ServiceException {

		System.out.println("Adding to database...");

		database.add(task);
		database.add(task2);

		Task addedTask = database.query(1);
		// System.out.println(task.toString());
		// System.out.println(addedTask.toString());
		assertEquals(task.toString(), addedTask.toString());

		Task addedTask2 = database.query(2);
		// System.out.println(task2.toString());
		// System.out.println(addedTask2.toString());
		assertEquals(task2.toString(), addedTask2.toString());

	}

	@Test
	public void testUpdateDatabase() throws IOException, ServiceException {
		System.out.println("Test update Database...");

		database.add(task);

		System.out.println("before update");
		queryList = database.query();
		Iterator<Task> iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			System.out.println(matchedTask.toString());
		}

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
		editTask.setTaskLastSync(new DateTime().now().plusDays(5));

		database.update(editTask);
		System.out.println(editTask.toString());

		System.out.println("after update");
		queryList = database.query();
		Iterator<Task> iterator2 = queryList.iterator();
		while (iterator2.hasNext()) {
			Task matchedTask = iterator2.next();
			System.out.println(matchedTask.toString());
		}

		assertEquals(newTaskName, queryList.get(0).getTaskName());
		assertEquals(editTask.getStartDateTime(), queryList.get(0)
				.getStartDateTime());
		assertEquals(editTask.getEndDateTime(), queryList.get(0)
				.getEndDateTime());
		assertEquals(editTask.getTaskCreated(), queryList.get(0)
				.getTaskCreated());
		assertEquals(editTask.getTaskLastSync(), queryList.get(0)
				.getTaskLastSync());
		assertEquals(editTask.getTaskUpdated(), queryList.get(0)
				.getTaskUpdated());

	}

	@Test
	public void testDeleteDatabase() throws IOException, ServiceException {
		System.out.println("Adding to database...");

		database.add(task);
		database.add(task2);
		database.add(task3);
		database.add(task4);
		database.add(task5);

		System.out.println("before delete");
		queryList = database.query();
		Iterator<Task> iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			System.out.println(matchedTask.toString());
		}

		System.out.println("after deleting task 1 and 2");
		database.delete(1);
		database.delete(2);

		queryList = database.query();
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
	public void testAfter() throws IOException {
		database.clearDatabase();
		System.out.println(System.lineSeparator());
	}
}
