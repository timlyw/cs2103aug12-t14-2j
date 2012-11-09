package mhs.test;

import java.io.BufferedReader;
import java.io.FileReader;

import mhs.src.logic.Processor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SystemTest {

	private Processor processor;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExecuteCommand() {
		processor = Processor.getProcessor("testDatabase.json");
		System.out.println("Created");
		processor.setDebugMode();
		processor.clearDatabase();
		startTests();
	}

	private void startTests() {
		/*BufferedReader readFile;
		try {
			readFile = new BufferedReader(new FileReader("inputfile.txt"));
			String currentLine;
			while ((currentLine = readFile.readLine()) != null) {
				processor.setCommand(currentLine);
				processor.executeCommand();
			}
		} catch (Exception e) {
			System.out.println("Error");
		}*/
		processor.setCommand("search running");
		processor.executeCommand();
	}
}
