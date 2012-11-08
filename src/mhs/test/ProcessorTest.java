package mhs.test;

import static org.junit.Assert.*;

import mhs.src.logic.Processor;
import mhs.src.storage.DatabaseFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProcessorTest {

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
	public void testGetState() {
		Processor processor = new Processor();
		String input = new String();
		String output = new String();
		String expected = new String();
		
		input = "add running";
		processor.setCommand(input);
		processor.executeCommand();
		output = processor.getState();
		expected = "A FLOATING task - 'running' was successfully added";
		assertEquals(expected, output);	
		
		input = "delete running";
		processor.setCommand(input);
		processor.executeCommand();
		output = processor.getState();
		expected = "Deleted Task - 'running'";
		assertEquals(expected, output);	
		
		input = "add exam 4pm 5pm";
		processor.setCommand(input);
		processor.executeCommand();
		output = processor.getState();
		expected = "A TIMED task - 'exam' was successfully added";
		assertEquals(expected, output);	
		
		input = "edit exam to complete test 6pm";
		processor.setCommand(input);
		processor.executeCommand();
		output = processor.getState();
		expected = "Edited Task - 'exam'";
		assertEquals(expected, output);
		
		input = "delete complete";
		processor.setCommand(input);
		processor.executeCommand();
		output = processor.getState();
		expected = "Deleted Task - 'complete test'";
		assertEquals(expected, output);
		
		input = "add do work";
		processor.setCommand(input);
		processor.executeCommand();
		output = processor.getState();
		expected = "A FLOATING task - 'do work' was successfully added";
		assertEquals(expected, output);	
		
		input = "mark work";
		processor.setCommand(input);
		processor.executeCommand();
		output = processor.getState();
		expected = "Marked Task as done - 'do work'-Done? true";
		assertEquals(expected, output);
		
		input = "delete work";
		processor.setCommand(input);
		processor.executeCommand();
		output = processor.getState();
		expected = "Deleted Task - 'do work'";
		assertEquals(expected, output);
	}

}
