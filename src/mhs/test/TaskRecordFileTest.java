//@author A0087048X
package mhs.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import mhs.src.storage.persistence.local.TaskRecordFile;
import mhs.src.storage.persistence.task.DeadlineTask;
import mhs.src.storage.persistence.task.FloatingTask;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;
import mhs.src.storage.persistence.task.TimedTask;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * TaskRecordFileTest
 * 
 * jUnit test for TaskRecordFileTest
 * 
 * @author Timothy Lim Yi Wen A0087048X
 * 
 */
public class TaskRecordFileTest {

	TaskRecordFile taskRecordFile;
	Map<Integer, Task> taskList;

	Task task;
	Task task2;
	Task task3;
	Task task4;
	Task task5;

	DateTime dt;
	DateTime dt2;

	private static final String TEST_TASK_5_NAME = "task 5 - play more games";
	private static final String TEST_TASK_4_NAME = "task 4 - project due";
	private static final String TEST_TASK_3_NAME = "task 3 - assignment due";
	private static final String TEST_TASK_2_NAME = "task 2 - a project meeting";
	private static final String TEST_TASK_1_NAME = "task 1 - a meeting";

	private final static String TEST_TASK_RECORD_FILENAME = "testTaskRecordFile.json";

	@Before
	public void taskRecordFileSetup() throws IOException {
		taskRecordFile = new TaskRecordFile(TEST_TASK_RECORD_FILENAME);
		DateTime dt = initializeDateTime1();
		DateTime dt2 = initializeDateTime2();

		initializeTasks(dt, dt2);
		taskList = new LinkedHashMap<Integer, Task>();
		loadTaskListWithTasks();
	}

	protected void loadTaskListWithTasks() {
		taskList.put(task.getTaskId(), task);
		taskList.put(task2.getTaskId(), task2);
		taskList.put(task3.getTaskId(), task3);
		taskList.put(task4.getTaskId(), task4);
		taskList.put(task5.getTaskId(), task5);
	}

	protected void initializeTasks(DateTime dt, DateTime dt2) {
		task = new TimedTask(1, TEST_TASK_1_NAME, TaskCategory.TIMED, dt,
				dt2.plusHours(5), null, null, null, null, null, false, false);
		task2 = new TimedTask(2, TEST_TASK_2_NAME, TaskCategory.TIMED, dt,
				dt2.plusHours(1), null, null, null, null, null, false, false);
		task3 = new DeadlineTask(3, TEST_TASK_3_NAME, TaskCategory.DEADLINE,
				dt, null, null, null, null, null, false, false);
		task4 = new DeadlineTask(4, TEST_TASK_4_NAME, TaskCategory.DEADLINE,
				dt, null, null, null, null, null, false, false);
		task5 = new FloatingTask(5, TEST_TASK_5_NAME, TaskCategory.FLOATING,
				null, null, null, null, false, false);
	}

	protected DateTime initializeDateTime2() {
		new DateTime();
		DateTime dt2 = DateTime.now().plusDays(1);
		return dt2;
	}

	protected DateTime initializeDateTime1() {
		new DateTime();
		DateTime dt = DateTime.now();
		return dt;
	}

	@Test
	/**
	 * Tests save and load 
	 * @throws IOException
	 */
	public void testSaveAndLoadTasks() throws IOException {

		Map<Integer, Task> loadTaskList = new LinkedHashMap<Integer, Task>();

		taskRecordFile.saveTaskList(taskList);
		taskRecordFile.loadTaskListFromFile();

		loadTaskList = taskRecordFile.getTaskList();

		// Test size
		assertEquals(taskList.size(), loadTaskList.size());

		// Test values
		for (int i = 1; i <= taskList.size(); i++) {
			assertEquals(taskList.get(i).toJson(), loadTaskList.get(i).toJson());
		}
	}

}