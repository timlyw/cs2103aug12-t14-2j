package mhs.test;

import static org.junit.Assert.assertEquals;
import mhs.src.logic.Command;
import mhs.src.logic.CommandParser;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

public class CommandParserTest {
	DateTime now = new DateTime();

	@Before
	public void setUpEnvironment() {
		
		now = DateTime.now();
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
		
		input = commandParser.getParsedCommand("edit \"watch movie\" to \"laundry duties\"");
		inputName = input.getTaskName().trim();
		edittedName = input.getEdittedName().trim();
		assertEquals("watch movie", inputName);
		assertEquals("laundry duties", edittedName);
		assertEquals(input.getCommandEnum(), Command.command.edit);
		
		input = commandParser.getParsedCommand("edit watch movie 5pm laundry duties");
		inputName = input.getTaskName().trim();
		edittedName = input.getEdittedName().trim();
		assertEquals("watch movie", inputName);
		assertEquals("laundry duties", edittedName);
		assertEquals(input.getCommandEnum(), Command.command.edit);
	}


}
