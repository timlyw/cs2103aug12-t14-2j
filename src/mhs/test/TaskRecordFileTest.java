package mhs.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import mhs.src.DeadlineTask;
import mhs.src.FloatingTask;
import mhs.src.Task;
import mhs.src.TaskRecordFile;
import mhs.src.TimedTask;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

public class TaskRecordFileTest {

	TaskRecordFile taskRecordFile;
	Map<Integer, Task> taskList;
	Task task;
	Task task2;
	Task task3;
	Task task4;
	Task task5;

	private final static String TEST_TASK_RECORD_FILENAME = "testTaskRecordFile.json";

	@Before
	public void initTestFile() throws IOException {

		taskRecordFile = new TaskRecordFile(TEST_TASK_RECORD_FILENAME);

		DateTime dt = new DateTime().now();
		DateTime dt2 = new DateTime().now();
		DateTime dt3 = new DateTime().now();
		DateTime dt4 = new DateTime().now();
		DateTime dt5 = new DateTime().now();

		task = new TimedTask(1, "task 1", "TIMED", dt, dt2, dt3, dt4, dt5,
				"null", false, false);
		task2 = new TimedTask(2, "task 2", "TIMED", dt, dt2, dt3, dt4, dt5,
				"null", false, false);
		task3 = new DeadlineTask(3, "task 3", "DEADLINE", dt, dt2, dt3, dt4,
				"null", false, false);
		task4 = new DeadlineTask(4, "task 4", "DEADLINE", dt, dt2, dt3, dt4,
				"null", false, false);
		task5 = new FloatingTask(5, "task 5", "FLOATING", dt, dt2, dt3, "null",
				false, false);

		taskList = new LinkedHashMap<Integer, Task>();

		taskList.put(task.getTaskId(), task);
		taskList.put(task2.getTaskId(), task2);
		taskList.put(task3.getTaskId(), task3);
		taskList.put(task4.getTaskId(), task4);
		taskList.put(task5.getTaskId(), task5);

	}

	@Test
	public void testSaveAndLoadTasks() throws IOException {

		Map<Integer, Task> loadTaskList = new LinkedHashMap<Integer, Task>();

		taskRecordFile.saveTaskList(taskList);

		loadTaskList = taskRecordFile.loadTaskList();

		for (int i = 1; i <= taskList.size(); i++) {
			assertTrue(taskList.get(i).toString()
					.equals(loadTaskList.get(i).toString()));
			assertTrue(taskList.get(i).toString()
					.equals(loadTaskList.get(i).toString()));
			assertTrue(taskList.get(i).toString()
					.equals(loadTaskList.get(i).toString()));
			assertTrue(taskList.get(i).toString()
					.equals(loadTaskList.get(i).toString()));
			assertTrue(taskList.get(i).toString()
					.equals(loadTaskList.get(i).toString()));
		}
	}

}
