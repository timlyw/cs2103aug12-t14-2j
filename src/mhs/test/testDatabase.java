package mhs.test;

import java.io.IOException;

import mhs.src.Database;
import mhs.src.Task;
import mhs.src.TaskRecordFile;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

public class testDatabase {

	Database database;
	
	@Before
	public void testDatabase() throws IOException {
		database = new Database();
	}
	
	@Test
	public void testInitDatabase(){
		System.out.println("Printing tasks...");
		database.printTasks();
		System.out.println("Printing sorted tasks...");
		database.printSortedTasks();
	} 

}
