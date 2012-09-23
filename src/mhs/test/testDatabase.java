package mhs.test;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mhs.src.Database;
import mhs.src.DateTimeTypeConverter;
import mhs.src.DeadlineTask;
import mhs.src.FloatingTask;
import mhs.src.Task;
import mhs.src.TaskCategory;
import mhs.src.TaskRecordFile;
import mhs.src.TaskTypeConverter;
import mhs.src.TimedTask;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class testDatabase {

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
	public void testDatabase() throws IOException {

		database = new Database(TEST_TASK_RECORD_FILENAME);
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
	public void testSyncDatabase() {
		fail("Not yet implemented");
	}

	@Test
	public void testClearDatabase() throws IOException {
		System.out.println("Clearing database Test");
		database.add(task);
		database.clearDatabase();
		queryList = database.query();
		assertEquals(0, queryList.size());
	}

	@Test
	public void testQueryTaskIdDatabase() throws IOException {
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
	public void testQueryTaskNameDatabase() throws IOException {
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
	public void testQueryTaskCategoryDatabase() throws IOException {
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
	public void testQueryDateDatabase() throws IOException {
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
	public void testAddToDatabase() throws IOException {

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
	public void testUpdateDatabase() throws IOException {
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
		editTask.setTaskCreated(new DateTime().now().plusDays(1));

		database.update(editTask);

		System.out.println("after update");
		queryList = database.query();
		Iterator<Task> iterator2 = queryList.iterator();
		while (iterator2.hasNext()) {
			Task matchedTask = iterator2.next();
			System.out.println(matchedTask.toString());
		}

		assertEquals(newTaskName, queryList.get(0).getTaskName());
	}

	@Test
	public void testDeleteDatabase() throws IOException {
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
