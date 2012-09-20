package mhs.test;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import mhs.src.Database;
import mhs.src.Task;
import mhs.src.TaskCategory;
import mhs.src.TaskRecordFile;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class testDatabase {

	Database database;

	@Before
	public void testDatabase() throws IOException {
		database = new Database();
	}

	@Test
	public void testInitDatabase() {
	}

	@Test
	public void testSyncDatabase() {
	}

	@Test
	public void testTaskList() {
		System.out.println("Printing tasks...");
		database.printTasks();
	}

	@Test
	public void testSortedTaskList() {
		System.out.println("Printing sorted tasks...");
		database.printSortedTasks();
	}
	@Test
	public void testRecordQueryDatabase() throws IOException {
		System.out.println("Fetching record index 1...");
		Task queriedTask;
		queriedTask = database.query(1);
		System.out.println(queriedTask.getTaskName());
	}
	@Test
	public void testRecordsQueryDatabase() throws IOException {
		System.out.println("Fetching records with 'task 1'...");
		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		queriedTaskRecordset = database.query("Task 1"); 

		Iterator<Task> iterator = queriedTaskRecordset.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next().getTaskName());
		}		
	}
	@Test
	public void testRecordsQueryByCategoryDatabase() throws IOException {
		System.out.println("Fetching records with category timed task...");
		List<Task> queriedTaskRecordset = new LinkedList<Task>();
		TaskCategory taskCategory = null;		
		queriedTaskRecordset = database.query(taskCategory.TIMED);
		
		Iterator<Task> iterator = queriedTaskRecordset.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next().getTaskName());
		}		
	}
	
	@Test
	public void testAddDatabase() {
	}
	@Test
	public void testUpdateDatabase() {
	}
	@Test
	public void testDeleteDatabase() {
	}
	@After 
	public void testAfter(){
		System.out.println(System.lineSeparator());
	}
}
