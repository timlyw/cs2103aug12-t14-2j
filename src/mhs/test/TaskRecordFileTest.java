package mhs.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

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
	DateTime dt;
	DateTime dt2;
	DateTime dt3;
	DateTime dt4;
	DateTime dt5;

	private final static String TEST_TASK_RECORD_FILENAME = "testTaskRecordFile.json";

	@Before
	public void initTestFile() throws IOException {

		taskRecordFile = new TaskRecordFile(TEST_TASK_RECORD_FILENAME);

		new DateTime();
		DateTime dt = DateTime.now();
		new DateTime();
		DateTime dt2 = DateTime.now().plusDays(1);
		new DateTime();
		DateTime dt3 = DateTime.now().plusDays(2);
		new DateTime();
		DateTime dt4 = DateTime.now().plusDays(3);
		new DateTime();
		DateTime dt5 = DateTime.now().plusDays(4);

		task = new TimedTask(1, "task 1", "TIMED", dt, dt2, dt3, dt4, dt5,
				"null", false, false);
		task2 = new TimedTask(2, "task 2", "TIMED", dt, dt2, dt3, dt4, dt5,
				"null", false, false);
		task3 = new DeadlineTask(3, "task 3", "DEADLINE", dt, dt2, dt3, dt4,
				"null", false, false);
		task4 = new DeadlineTask(4, "task 4", "DEADLINE", dt, dt2, dt3, dt4,
				"null", false, false);
		task5 = new FloatingTask(5, "task 5", "FLOATING", dt, dt2, dt3, false,
				false);

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
			assertEquals(taskList.get(i).toString(), loadTaskList.get(i)
					.toString());
		}
	}

}
