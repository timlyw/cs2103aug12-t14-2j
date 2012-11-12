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

	private static final String TEST_DATABASE_JSON = "testDatabase.json";
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
		processor = Processor.getProcessor(TEST_DATABASE_JSON);
		processor.setDebugMode();
		processor.clearDatabase();
		initializeLists();
		processor.setLineLimit(20);
		startTests();
	}

	private void initializeLists() {
		processor.setCommand("home");
		processor.executeCommand();
	}

	private void startTests() {

		BufferedReader readFile;
		try {
			readFile = new BufferedReader(new FileReader(
					"SystemTestFiles/inputfile.txt"));
			String currentLine;
			while ((currentLine = readFile.readLine()) != null) {
				processor.setCommand(currentLine);
				processor.executeCommand();
			}
		} catch (Exception e) {
			System.out.println("Error");
		}

	}
}
