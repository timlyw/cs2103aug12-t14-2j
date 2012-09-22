package mhs.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import mhs.src.DeadlineTask;
import mhs.src.FloatingTask;
import mhs.src.Task;
import mhs.src.TaskRecordFile;
import mhs.src.TimedTask;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

public class testTaskRecordFile {

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

	/*
	 * @Test public void testAddTask() throws IOException { // Create Records
	 * taskRecordFile.addRecord(task); taskRecordFile.addRecord(task2);
	 * taskRecordFile.addRecord(task3); taskRecordFile.addRecord(task4);
	 * taskRecordFile.addRecord(task5); }
	 */

	@Test
	public void testSaveTasks() throws IOException {

		taskRecordFile.saveTaskList(taskList);

	}

	@Test
	public void testLoadTasks() throws IOException {

		taskRecordFile.saveTaskList(taskList);

		Map<Integer, Task> loadTaskList = new LinkedHashMap<Integer, Task>();
		loadTaskList = taskRecordFile.loadTaskList();

		for (Map.Entry<Integer, Task> loadedEntry : loadTaskList.entrySet()) {
			for (Map.Entry<Integer, Task> savedEntry : taskList.entrySet()) {

				// assertEquals(savedEntry.getValue().toString(),loadedEntry.getValue().toString());
				System.out.println(loadedEntry.getValue().getTaskName() + " "
						+ savedEntry.getValue().getTaskName());
			}
		}
		Set<Task> savedListSet = new LinkedHashSet<Task>(taskList.values());
		Set<Task> loadTaskListSet = new LinkedHashSet<Task>(
				loadTaskList.values());

		System.out.println(savedListSet);
		System.out.println(loadTaskListSet);

		// assertEquals(savedListSet,loadTaskListSet);

	}

}
