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

		database.add(task);
		database.add(task2);
		database.add(task3);
		database.add(task4);
		database.add(task5);

		queryList = database.query(task2.getStartDateTime(),
				task2.getEndDateTime());

		Iterator<Task> iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
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
		System.out.println(task.toString());
		System.out.println(addedTask.toString());

		Task addedTask2 = database.query(2);
		System.out.println(task2.toString());
		System.out.println(addedTask2.toString());

		assertEquals(task.toString(), addedTask.toString());
		assertEquals(task2.toString(), addedTask2.toString());

	}

	@Test
	public void testUpdateDatabase() throws IOException {
		System.out.println("Updating record...");

		database.add(task);

		queryList = database.query();

		Iterator<Task> iterator = queryList.iterator();
		while (iterator.hasNext()) {
			Task matchedTask = iterator.next();
			System.out.println(matchedTask.toString());
		}

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(DateTime.class,
				new DateTimeTypeConverter());
		gsonBuilder.registerTypeAdapter(Task.class, new TaskTypeConverter());
		Gson gson = gsonBuilder.create();

		Task editTask = gson.fromJson(gson.toJson(task), Task.class);

		editTask.setTaskName("task 5 - assignment du!");
		editTask.setTaskCreated(new DateTime().now().plusDays(1));

		database.add(editTask);
		
		queryList = database.query();

		Iterator<Task> iterator2 = queryList.iterator();
		while (iterator2.hasNext()) {
			Task matchedTask = iterator2.next();
			System.out.println(matchedTask.toString());
		}

	}

	@Test
	public void testDeleteDatabase() {
	}

	@After
	public void testAfter() throws IOException {
		// System.out.println(System.lineSeparator());
	}
}
