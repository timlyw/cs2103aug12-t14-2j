package mhs.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.RandomAccessFile;

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
	Task task;
	Task task2;
	Task task3;
	Task task4;
	Task task5;
	private final static String TEST_TASK_RECORD_FILENAME = "testTaskRecordFile.txt";

	@Before
	public void initTestFile() throws IOException {

		RandomAccessFile raf = new RandomAccessFile(TEST_TASK_RECORD_FILENAME,
				"rw");

		raf.setLength(0);

		raf.writeBytes("taskId 		11" + System.lineSeparator());
		raf.writeBytes("taskName 	50" + System.lineSeparator());
		raf.writeBytes("taskCategory 	50" + System.lineSeparator());
		raf.writeBytes("startDateTime 	35" + System.lineSeparator());
		raf.writeBytes("endDateTime	35" + System.lineSeparator());
		raf.writeBytes("taskCreated 	35" + System.lineSeparator());
		raf.writeBytes("taskUpdated  	35" + System.lineSeparator());
		raf.writeBytes("taskLastSync 	11" + System.lineSeparator());
		raf.writeBytes("gCalTaskId	11" + System.lineSeparator());
		raf.writeBytes("isDone 		11" + System.lineSeparator());
		raf.writeBytes("isDeleted 	11" + System.lineSeparator());
		raf.writeBytes("=================================================="
				+ System.lineSeparator());

		raf.close();
		taskRecordFile = new TaskRecordFile(TEST_TASK_RECORD_FILENAME);

		DateTime dt = new DateTime();
		DateTime dt2 = new DateTime();
		DateTime dt3 = new DateTime();
		DateTime dt4 = new DateTime();
		DateTime dt5 = new DateTime();

		task = new TimedTask(
				1,
				"task 1 - task 1task 1task 1task 1task 1task 1task 1task 1task 1task 1task 1task 1task 1task 1task 1task 1task 1task 1task 1",
				"TIMED", dt, dt2, dt3, dt4, dt5, "null", false, false);
		task2 = new TimedTask(2, "task 2", "TIMED", dt, dt2, dt3, dt4, dt5,
				"null", false, false);
		task3 = new DeadlineTask(3, "task 3", "DEADLINE", dt, dt2, dt3, dt4, dt5,
				"null", false, false);
		task4 = new DeadlineTask(4, "task 4", "DEADLINE", dt, dt2, dt3, dt4, dt5,
				"null", false, false);
		task5 = new FloatingTask(5, "task 5", "FLOATING", dt, dt2, dt3, "null", false, false);

		// Create Records
		taskRecordFile.addRecord(task);
		taskRecordFile.addRecord(task2);
		taskRecordFile.addRecord(task3);
		taskRecordFile.addRecord(task4);
		taskRecordFile.addRecord(task5);

	}

	@Test
	public void testInitRecordFile() throws IOException {

		System.out.println("Setting up records field attributes...");
		taskRecordFile.printRecordFieldAttributes();
		System.out.println();

		// Print existing records
		System.out.println("Printing records...");
		taskRecordFile.printRecords();
		System.out.println();

	}

	@Test
	public void testFetchRecords() throws IOException {

		// Fetch Record
		System.out.println("Fetching Records...");
		for (int i = 0; i < 5; i++) {
			Task myTask = taskRecordFile.fetchTask(i + 1);
			System.out.printf("Fetching Task %s : %s\n", i + 1,
					myTask.getTaskName());
		}
		System.out.println();
	}

	@Test
	public void testUpdateRecords() throws IOException {

		// Update Record
		System.out.println("Updating Records...");
		task.setTaskName("Task 1 EDITED");
		taskRecordFile.updateRecord(task);

		// Print existing records
		Task myTask = taskRecordFile.fetchTask(1);
		assertEquals(myTask.getTaskName(), "Task 1 EDITED");
	}

	@Test
	public void testDeleteRecord() throws IOException {
		// Delete Task (set delete flag)
		System.out.println("Delete task 4...");
		task4.setDeleted(true);
		taskRecordFile.updateRecord(task4);

		// Fetch Record
		Task myTask = taskRecordFile.fetchTask(4);
		assertTrue(myTask.isDeleted());
	}

	@Test
	public void testMarkDone() throws IOException {

		// Mark Done Record
		System.out.println("Mark task 5 done...");
		task5.setDone(true);
		taskRecordFile.updateRecord(task5);

		// Fetch Record
		Task myTask = taskRecordFile.fetchTask(5);
		assertTrue(myTask.isDone());
	}

}
