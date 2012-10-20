package mhs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import mhs.src.logic.Command;
import mhs.src.logic.CommandParser;
import mhs.src.logic.TimeExtractor;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

public class CommandProcessorTest {
	DateTime now = new DateTime();

	@Before
	public void setUpEnvironment() {
		
		now = DateTime.now();
	}

	@Test
	public void testcheckTimeFormat() {

		TimeExtractor timeParser = new TimeExtractor();
		assertTrue(timeParser.checkTimeFormat("12pm"));
		assertTrue(timeParser.checkTimeFormat("3pm"));
		assertTrue(timeParser.checkTimeFormat("4am"));
		assertTrue(timeParser.checkTimeFormat("12.40pm"));
		assertTrue(timeParser.checkTimeFormat("00:00"));
		assertTrue(timeParser.checkTimeFormat("00:12"));
		assertTrue(timeParser.checkTimeFormat("15:13"));

		assertFalse(timeParser.checkTimeFormat("15pm"));
		assertFalse(timeParser.checkTimeFormat("12.60pm"));
		assertFalse(timeParser.checkTimeFormat("00:70"));
		assertFalse(timeParser.checkTimeFormat("400"));
		assertFalse(timeParser.checkTimeFormat("1600"));
		assertFalse(timeParser.checkTimeFormat("27:00"));
	}

	@Test
	public void testProcessTime() {

		LocalTime expectedTime = null;
		TimeExtractor timeParser = new TimeExtractor();
		
		expectedTime = new LocalTime(12, 4 );
		assertEquals(timeParser.processTime("12:04"), expectedTime);

		expectedTime = new LocalTime(12, 40 );
		assertEquals(timeParser.processTime("12:40"), expectedTime);

		expectedTime = new LocalTime(0, 0 );
		assertEquals(timeParser.processTime("00:00"), expectedTime);

		expectedTime = new LocalTime(0, 12 );
		assertEquals(timeParser.processTime("00:12"), expectedTime);

		expectedTime = new LocalTime(15, 13 );
		assertEquals(timeParser.processTime("15:13"), expectedTime);

		expectedTime = new LocalTime(15, 13 );
		assertEquals(timeParser.processTime("3.13pm"), expectedTime);

		expectedTime = new LocalTime(4, 0 );
		assertEquals(timeParser.processTime("4am"), expectedTime);
	}


	@Test
	public void testCommandParser(){


		Command input;
		String inputName;
		String edittedName;
		CommandParser commandParser = new CommandParser();
		DateTime expectedStartDate;
		DateTime inputStartDate;
		DateTime expectedEndDate;
		DateTime inputEndDate;
		
		input = commandParser.getParsedCommand("meeting1");
		inputName = input.getTaskName().trim();
		assertEquals("meeting1", inputName);
		
		input = commandParser.getParsedCommand("class project by 10/12/2012 10pm");
		inputName = input.getTaskName().trim();
		expectedStartDate = new DateTime(2012, 12, 10, 22, 0);
		inputStartDate = input.getStartDate();
		assertEquals("class project", inputName);
		assertEquals(inputStartDate, expectedStartDate);

		input = commandParser.getParsedCommand("\"day after tomorrow\" on 4 dec 2012 22:12 to 6 12 2012 4.15pm");
		inputName = input.getTaskName().trim();
		expectedStartDate = new DateTime(2012, 12, 4, 22, 12);
		inputStartDate = input.getStartDate();
		expectedEndDate = new DateTime(2012, 12, 6, 16, 15);
		inputEndDate = input.getEndDate();
		assertEquals("day after tomorrow", inputName);
		assertEquals(inputStartDate, expectedStartDate);
		assertEquals(inputEndDate, expectedEndDate);
		
		input = commandParser.getParsedCommand("\"day after tomorrow\" 2 nov 2012 22:12 7 11 2012 4.15pm");
		inputName = input.getTaskName().trim();
		expectedStartDate = new DateTime(2012, 11, 2, 22, 12);
		inputStartDate = input.getStartDate();
		expectedEndDate = new DateTime(2012, 11, 7, 16, 15);
		inputEndDate = input.getEndDate();
		assertEquals("day after tomorrow", inputName);
		assertEquals(inputStartDate, expectedStartDate);
		assertEquals(inputEndDate, expectedEndDate);
		assertEquals(input.getCommandEnum(), Command.command.add);
		
		input = commandParser.getParsedCommand("edit \"watch movie\" \"laundry duties\"");
		inputName = input.getTaskName().trim();
		edittedName = input.getEdittedName().trim();
		assertEquals("watch movie", inputName);
		assertEquals("laundry duties", edittedName);
		assertEquals(input.getCommandEnum(), Command.command.edit);
		
		input = commandParser.getParsedCommand("edit watch movie to laundry duties");
		inputName = input.getTaskName().trim();
		edittedName = input.getEdittedName().trim();
		assertEquals("watch movie", inputName);
		assertEquals("laundry duties", edittedName);
		assertEquals(input.getCommandEnum(), Command.command.edit);
	}


}
