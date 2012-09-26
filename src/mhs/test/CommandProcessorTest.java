package mhs.test;
import static org.junit.Assert.assertEquals;

import mhs.src.Command;
import mhs.src.CommandParser;
import mhs.src.TimeExtractor;

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

		assertEquals(TimeExtractor.checkTimeFormat("12pm"), true);
		assertEquals(TimeExtractor.checkTimeFormat("3pm"), true);
		assertEquals(TimeExtractor.checkTimeFormat("4am"), true);
		assertEquals(TimeExtractor.checkTimeFormat("12.40pm"), true);
		assertEquals(TimeExtractor.checkTimeFormat("00:00"), true);
		assertEquals(TimeExtractor.checkTimeFormat("00:12"), true);
		assertEquals(TimeExtractor.checkTimeFormat("15:13"), true);

		assertEquals(TimeExtractor.checkTimeFormat("15pm"), false);
		assertEquals(TimeExtractor.checkTimeFormat("12.60pm"), false);
		assertEquals(TimeExtractor.checkTimeFormat("00:70"), false);
		assertEquals(TimeExtractor.checkTimeFormat("400"), false);
		assertEquals(TimeExtractor.checkTimeFormat("1600"), false);
		assertEquals(TimeExtractor.checkTimeFormat("27:00"), false);
	}

	@Test
	public void testProcessTime() {

		LocalTime expectedTime = null;
		
		expectedTime = new LocalTime(12, 4 );
		assertEquals(TimeExtractor.processTime("12:04"), expectedTime);

		expectedTime = new LocalTime(12, 40 );
		assertEquals(TimeExtractor.processTime("12:40"), expectedTime);

		expectedTime = new LocalTime(0, 0 );
		assertEquals(TimeExtractor.processTime("00:00"), expectedTime);

		expectedTime = new LocalTime(0, 12 );
		assertEquals(TimeExtractor.processTime("00:12"), expectedTime);

		expectedTime = new LocalTime(15, 13 );
		assertEquals(TimeExtractor.processTime("15:13"), expectedTime);

		expectedTime = new LocalTime(15, 13 );
		assertEquals(TimeExtractor.processTime("3.13pm"), expectedTime);

		expectedTime = new LocalTime(4, 0 );
		assertEquals(TimeExtractor.processTime("4am"), expectedTime);
	}


	@Test
	public void testCommandParser(){


		Command input;
		String inputName;
		String edittedName;
		
		DateTime expectedStartDate;
		DateTime inputStartDate;
		DateTime expectedEndDate;
		DateTime inputEndDate;
		
		
		input = CommandParser.getParsedCommand("meeting1");
		inputName = input.getTaskName().trim();
		assertEquals("meeting1", inputName);
		
		input = CommandParser.getParsedCommand("meeting 10/12/2012 10pm");
		inputName = input.getTaskName().trim();
		expectedStartDate = new DateTime(2012, 12, 10, 22, 0);
		inputStartDate = Command.getStartDate();
		assertEquals("meeting", inputName);
		assertEquals(inputStartDate, expectedStartDate);
		
		input = CommandParser.getParsedCommand("\"day after tomorrow\" 10 dec 2012 22:12 12 12 2012 4.15pm");
		inputName = input.getTaskName().trim();
		expectedStartDate = new DateTime(2012, 12, 10, 22, 12);
		inputStartDate = Command.getStartDate();
		expectedEndDate = new DateTime(2012, 12, 12, 16, 15);
		inputEndDate = Command.getEndDate();
		assertEquals("day after tomorrow", inputName);
		assertEquals(inputStartDate, expectedStartDate);
		assertEquals(inputEndDate, expectedEndDate);
		
		input = CommandParser.getParsedCommand("\"day after tomorrow\" 10 dec 2012 22:12 12 12 2012 4.15pm");
		inputName = input.getTaskName().trim();
		expectedStartDate = new DateTime(2012, 12, 10, 22, 12);
		inputStartDate = Command.getStartDate();
		expectedEndDate = new DateTime(2012, 12, 12, 16, 15);
		inputEndDate = Command.getEndDate();
		assertEquals("day after tomorrow", inputName);
		assertEquals(inputStartDate, expectedStartDate);
		assertEquals(inputEndDate, expectedEndDate);
		assertEquals(input.getCommandEnum(), Command.command.add);
		
		input = CommandParser.getParsedCommand("edit \"day after tomorrow\" movie at vivo");
		inputName = input.getTaskName().trim();
		edittedName = input.getEdittedName().trim();
		assertEquals("day after tomorrow", inputName);
		assertEquals("movie at vivo", edittedName);
		assertEquals(input.getCommandEnum(), Command.command.edit);
	}


}
