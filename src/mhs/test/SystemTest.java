//@author A0088669A

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

	private static final String COMMAND_HOME = "home";
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
	/**
	 * Sets up environment and starts System tests
	 */
	public void testExecuteCommand() {
		processor = Processor.getProcessor(TEST_DATABASE_JSON);
		processor.setDebugMode();
		processor.clearDatabase();
		initializeLists();
		processor.setLineLimit(20);
		startTests();
	}

	/**
	 * Sets up a task List
	 */
	private void initializeLists() {
		processor.setCommand(COMMAND_HOME);
		processor.executeCommand();
	}

	/**
	 * Starts executing all commands in inputfile.txt
	 */
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
