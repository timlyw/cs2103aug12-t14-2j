/**
 * Unit test for Task Record File
 * @author timlyw
 */
package mhs.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import mhs.src.DeadlineTask;
import mhs.src.FloatingTask;
import mhs.src.Task;
import mhs.src.TaskCategory;
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

		new DateTime();
		DateTime dt = DateTime.now();
		new DateTime();
		DateTime dt2 = DateTime.now().plusDays(1);
		new DateTime();

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

		taskList = new LinkedHashMap<Integer, Task>();

		taskList.put(task.getTaskId(), task);
		taskList.put(task2.getTaskId(), task2);
		taskList.put(task3.getTaskId(), task3);
		taskList.put(task4.getTaskId(), task4);
		taskList.put(task5.getTaskId(), task5);

	}

	@Test
	/**
	 * Tests save and load 
	 * @throws IOException
	 */
	public void testSaveAndLoadTasks() throws IOException {

		Map<Integer, Task> loadTaskList = new LinkedHashMap<Integer, Task>();

		taskRecordFile.saveTaskList(taskList);
		loadTaskList = taskRecordFile.loadTaskList();

		// Test size
		assertEquals(taskList.size(), loadTaskList.size());

		// Test values
		for (int i = 1; i <= taskList.size(); i++) {
			assertEquals(taskList.get(i).toJson(), loadTaskList.get(i).toJson());
		}
	}

}
