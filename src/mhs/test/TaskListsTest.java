//@author A0087048X
package mhs.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mhs.src.common.exceptions.TaskNotFoundException;
import mhs.src.storage.persistence.TaskLists;
import mhs.src.storage.persistence.task.DeadlineTask;
import mhs.src.storage.persistence.task.FloatingTask;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;
import mhs.src.storage.persistence.task.TimedTask;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * TaskListsTest
 * 
 * jUnit test for TaskListsTest
 * 
 * @author Timothy Lim Yi Wen A0087048X
 * 
 */
public class TaskListsTest {

	TaskLists taskLists;
	Map<Integer, Task> taskList;

	Task task;
	Task task2;
	Task task3;
	Task task4;
	Task task5;

	private static final String TEST_TASK_5_NAME = "task 5 - play more games";
	private static final String TEST_TASK_4_NAME = "task 4 - project due";
	private static final String TEST_TASK_3_NAME = "task 3 - assignment due";
	private static final String TEST_TASK_2_NAME = "task 2 - a project meeting";
	private static final String TEST_TASK_1_NAME = "task 1 - a meeting";

	@Before
	public void TaskListsTestSetup() throws IOException {
		initializeTasks();
		initializeTaskList();
	}

	private void initializeTaskList() {
		taskList = new LinkedHashMap<Integer, Task>();

		taskList.put(task.getTaskId(), task);
		taskList.put(task2.getTaskId(), task2);
		taskList.put(task3.getTaskId(), task3);
		taskList.put(task4.getTaskId(), task4);
		taskList.put(task5.getTaskId(), task5);
	}

	private void initializeTasks() {
		task = new TimedTask(1, TEST_TASK_1_NAME, TaskCategory.TIMED,
				DateTime.now(), DateTime.now(), null, null, null, null, null,
				false, false);
		task2 = new TimedTask(2, TEST_TASK_2_NAME, TaskCategory.TIMED, DateTime
				.now().plusHours(1), DateTime.now(), null, null, null, null,
				null, false, false);
		task3 = new DeadlineTask(3, TEST_TASK_3_NAME, TaskCategory.DEADLINE,
				DateTime.now().plusHours(2), null, null, null, null, null,
				false, false);
		task4 = new DeadlineTask(4, TEST_TASK_4_NAME, TaskCategory.DEADLINE,
				DateTime.now().plusHours(3), null, null, null, null, null,
				false, false);
		task5 = new FloatingTask(5, TEST_TASK_5_NAME, TaskCategory.FLOATING,
				null, null, null, null, false, false);
	}

	@Test
	public void TaskListTestInitialize() {
		taskLists = new TaskLists(taskList);
		assertEquals(5, taskLists.getTaskList().size());
	}

	@Test
	public void TaskListTestClear() {
		taskLists = new TaskLists(taskList);
		taskLists.clearTaskLists();
		assertEquals(0, taskLists.getTaskList().size());
	}

	@Test
	public void TaskListTestUpdate() throws TaskNotFoundException {
		taskList.clear();
		taskLists = new TaskLists(taskList);
		// Test add task
		taskLists.updateTaskInTaskLists(task);
		assertEquals(1, taskLists.getTaskList().size());
		assertEquals(task.toString(), taskLists.getTask(task.getTaskId())
				.toString());

		// Test update task
		String editedTaskName = TEST_TASK_1_NAME + " edited";
		task.setTaskName(editedTaskName);
		taskLists.updateTaskInTaskLists(task);

		assertEquals(1, taskLists.getTaskList().size());
		assertEquals(task.toString(), taskLists.getTask(task.getTaskId())
				.toString());
	}

	@Test
	public void TaskListTestRemove() {
		taskLists = new TaskLists(taskList);
		taskLists.removeTaskInTaskLists(task);
		assertEquals(4, taskLists.getTaskList().size());
	}

	@Test
	public void TaskListTestQuery() {
		taskLists = new TaskLists(taskList);

		// Test get tasks
		List<Task> retrievedTaskList = taskLists.getTasks(false);
		assertEquals(5, retrievedTaskList.size());

		assertEquals(task.toString(), retrievedTaskList.get(0).toString());
		assertEquals(task2.toString(), retrievedTaskList.get(1).toString());
		assertEquals(task3.toString(), retrievedTaskList.get(2).toString());
		assertEquals(task4.toString(), retrievedTaskList.get(3).toString());
		assertEquals(task5.toString(), retrievedTaskList.get(4).toString());

		// Test get tasks by Start Date Time
		retrievedTaskList = taskLists.getTasks(true);

		assertEquals(5, retrievedTaskList.size());

		assertEquals(task5.toString(), retrievedTaskList.get(0).toString());
		assertEquals(task.toString(), retrievedTaskList.get(1).toString());
		assertEquals(task2.toString(), retrievedTaskList.get(2).toString());
		assertEquals(task3.toString(), retrievedTaskList.get(3).toString());
		assertEquals(task4.toString(), retrievedTaskList.get(4).toString());

		// Test get tasks by Start Date Time
		retrievedTaskList = taskLists.getTasks(true);
	}

}
