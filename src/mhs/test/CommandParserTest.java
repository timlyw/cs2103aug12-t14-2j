//author A0086805X
package mhs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.Queue;

import mhs.src.logic.CommandInfo;
import mhs.src.logic.parser.CommandExtractor;
import mhs.src.logic.parser.CommandParser;
import mhs.src.logic.parser.DateExtractor;
import mhs.src.logic.parser.NameExtractor;
import mhs.src.logic.parser.TimeExtractor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

/**
 *Component test to test the parser package
 */
public class CommandParserTest {

	private DateExtractor dateExtractor;
	private TimeExtractor timeExtractor;
	private CommandExtractor commandExtractor;
	private NameExtractor nameExtractor;
	private int day, month, year;

	/**
	 * Set up the environment for testing and defaulting current day to 7/11/2012.
	 */
	@Before
	public void setUpEnvironment() {

		day = 7;
		month = 11;
		year = 2012;
		DateTime now = new DateTime(year, month, day, 12 , 0);
		DateTimeUtils.setCurrentMillisFixed(now.getMillis());
		
		dateExtractor = DateExtractor.getDateExtractor();
		timeExtractor = TimeExtractor.getTimeExtractor();
		commandExtractor = CommandExtractor.getCommandExtractor();
		nameExtractor = NameExtractor.getNameExtractor();
	}

	/**
	 * Tests time extractor.
	 * 
	 * Test cases
	 * 1. Tests normal 12hr timing for pm.
	 * 2. Tests normal 12hr timing for am.
	 * 3. Tests normal 24hr timing.
	 * 4. Tests time range with am as upper case.
	 * 5. Tests time range with start of day to mid day for in upper case.
	 * 6. Invalid time range. 
	 */
	@Test
	public void testTimeExtractor() {

		Queue<LocalTime> testList = new LinkedList<LocalTime>();
		Queue<LocalTime> expectedList = new LinkedList<LocalTime>();
		LocalTime testStartTime;
		LocalTime testEndTime;

		testList = timeExtractor.extractTime("5pm");
		testStartTime = new LocalTime(17, 0);
		expectedList.add(testStartTime);
		assertEquals(expectedList, testList);

		testList = timeExtractor.extractTime("5Am");
		testStartTime = new LocalTime(5, 0);
		expectedList = new LinkedList<LocalTime>();
		expectedList.add(testStartTime);
		assertEquals(expectedList, testList);

		testList = timeExtractor.extractTime("20:00");
		testStartTime = new LocalTime(20, 0);
		expectedList = new LinkedList<LocalTime>();
		expectedList.add(testStartTime);
		assertEquals(expectedList, testList);

		testList = timeExtractor.extractTime("3.22AM to 19:15");
		testStartTime = new LocalTime(3, 22);
		testEndTime = new LocalTime(19, 15);
		expectedList = new LinkedList<LocalTime>();
		expectedList.add(testStartTime);
		expectedList.add(testEndTime);
		assertEquals(expectedList, testList);

		testList = timeExtractor.extractTime("12AM to 12PM");
		testStartTime = new LocalTime(00, 0);
		testEndTime = new LocalTime(12, 0);
		expectedList = new LinkedList<LocalTime>();
		expectedList.add(testStartTime);
		expectedList.add(testEndTime);
		assertEquals(expectedList, testList);
		
		testList = timeExtractor.extractTime("27AM to 35:72");
		expectedList = new LinkedList<LocalTime>();
		assertEquals(expectedList, testList);

	}

	/**
	 * Tests date extractor.
	 * 
	 * Test cases
	 * 1. Test a day in the next week from the current day.
	 * 2. Test a day that is exactly one week from current day.
	 * 3. Test a day that is in the same week as the current day.
	 * 4. Test a full date date range. 
	 * 5. Test a date range with special key words. 
	 * 6. Test a date range with a special key word and normal date. 
	 * 7. Test a date range with a half date. 
	 * 8. Test a date range with half dates. 
	 * 9. Test a particular month of the year.
	 * 10.Test this month.
	 * 11.Test this week.
	 * 12.Test this year.
	 * 13.Test this weekend. 
	 * 14.Test today.
	 * 15.Test tomorrow.
	 */
	@Test
	public void testDateExtractor() {

		Queue<LocalDate> testList = new LinkedList<LocalDate>();
		Queue<LocalDate> expectedList = new LinkedList<LocalDate>();
		LocalDate testStartDate;
		LocalDate testEndDate;
		LocalDate now = new LocalDate(year, month, day);

		
		testList = dateExtractor.extractDate("monday");
		expectedList = new LinkedList<LocalDate>();
		testStartDate = new LocalDate(2012, 11, 12);
		expectedList.add(testStartDate);
		assertEquals(expectedList, testList);
		
		testList = dateExtractor.extractDate("weD");
		expectedList = new LinkedList<LocalDate>();
		testStartDate = new LocalDate(2012, 11, 14);
		expectedList.add(testStartDate);
		assertEquals(expectedList, testList);
		
		testList = dateExtractor.extractDate("sunday");
		expectedList = new LinkedList<LocalDate>();
		testStartDate = new LocalDate(2012, 11, 11);
		expectedList.add(testStartDate);
		assertEquals(expectedList, testList);
		
		testList = dateExtractor.extractDate("10 10 2012 to 17 oct 2012");
		expectedList = new LinkedList<LocalDate>();
		testStartDate = new LocalDate(2012, 10, 10);
		testEndDate = new LocalDate(2012, 10, 17);
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);

		testList = dateExtractor.extractDate("today to tomorrow");
		testStartDate = now;
		testEndDate = now.plusDays(1);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);

		testList = dateExtractor.extractDate("today to 5/12/2012");
		testStartDate = now;
		testEndDate = new LocalDate(2012, 12, 5);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);
		
		testList = dateExtractor.extractDate("today to 5/12");
		testStartDate = now;
		testEndDate = new LocalDate(year, 12, 5);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);

		testList = dateExtractor.extractDate("17 oct to 21 10");
		testStartDate = new LocalDate(year+1, 10, 17);
		testEndDate = new LocalDate(year+1, 10, 21);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);

		testList = dateExtractor.extractDate("oct 2012");
		testStartDate = new LocalDate(2012, 10, 1);
		testEndDate = new LocalDate(2012, 11, 1);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);
		
		testList = dateExtractor.extractDate("this month");
		testStartDate = new LocalDate(year, month, day);
		testEndDate = new LocalDate(year, month, 30);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);
		
		testList = dateExtractor.extractDate("this week");
		testStartDate = new LocalDate(year, month, day);
		testEndDate = new LocalDate(year, month, 11);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);
		
		testList = dateExtractor.extractDate("this year");
		testStartDate = new LocalDate(year, month, day);
		testEndDate = new LocalDate(year, 12, 31);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);
		
		testList = dateExtractor.extractDate("this weekend");
		testStartDate = new LocalDate(2012, 11, 10);
		testEndDate = new LocalDate(2012, 11, 11);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);
		
		testList = dateExtractor.extractDate("today");
		testStartDate = new LocalDate(year, month, day);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		assertEquals(expectedList, testList);
		
		testList = dateExtractor.extractDate("ToMorrow");
		testStartDate = new LocalDate(year, month, day+1);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		assertEquals(expectedList, testList);


	}

	/**
	 * Test the nameExtractor.
	 */
	@Test
	public void testNameExtractor() {

		Queue<String> testList = new LinkedList<String>();
		Queue<String> expectedList = new LinkedList<String>();
		String testName;
		String testEdittedName;

		testList = nameExtractor.extractName("task to task2");
		testName = "task";
		testEdittedName = "task2";
		expectedList.add(testName);
		expectedList.add(testEdittedName);
		assertEquals(expectedList, testList);

	}

	/**
	 * Test the command extractor.
	 * 
	 * Test case
	 * 1. Test normal case.
	 * 2. Test normal case with different cases in string.
	 */
	@Test
	public void testCommandExtractor() {

		String expectedCommand;
		String testCommand;

		testCommand = commandExtractor.extractCommand("add task");
		expectedCommand = "add";
		assertEquals(expectedCommand, testCommand);

		testCommand = commandExtractor.extractCommand("upDaTe task");
		expectedCommand = "edit";
		assertEquals(expectedCommand, testCommand);

	}

	/**
	 * Test the whole parser package and the defaulting of the variables. 
	 * 
	 * Test Case
	 * 1. Test add of a full task with all parameters and special keyword appended to the the name.
	 * 2. Test searching for an event in a specific time range. 
	 * 3. Test marking by index.
	 * 4. Test editing a task by index and editing the time range. 
	 * 5. Test defaulting of the date when just time is written.
	 * 6. Test appending a number to the name and time extracting in upper case.
	 * 7. Test searching of a time.
	 * 8. Test default search for a single date input.
	 * 9. Test date range this year.
	 * 10.Test one year period.
	 * 11.Test clearing parameters for mark.
	 * 12.Test name parsing of quotation marks.
	 * 13.Test appending the back of the sentence to the name for adding. 
	 * 14.Test clearing parameters for renaming.
	 * 15.Test clearing parameters for login.
	 * 
	 */
	@Test
	public void testCommandParser() {

		CommandParser commandParser = CommandParser.getCommandParser();
		DateTime expectedStartDate;
		DateTime expectedEndDate;

		CommandInfo testCommand;
		CommandInfo expectedCommand;
	
		testCommand = commandParser
				.getParsedCommand("meeting at vivo at 10pm 12 oct 2012 to 23:30 4/11");
		expectedStartDate = new DateTime(2012, 10, 12, 22, 0);
		expectedEndDate = new DateTime(2013, 11, 4, 23, 30);
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.add,
				"meeting at vivo", null, expectedStartDate, expectedEndDate, 0);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));

		testCommand = commandParser
				.getParsedCommand("Find meeting at vivo this month");
		expectedStartDate = new DateTime(year, month, day, 0, 0);
		expectedEndDate = new DateTime(year, month, 30, 23, 59);
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.search,
				"meeting at vivo", null, expectedStartDate, expectedEndDate, 0);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));
		
		
		testCommand = commandParser
				.getParsedCommand("mark 1");
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.mark,
				null, null, null, null, 1);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));
		
		testCommand = commandParser
				.getParsedCommand("edit 1 to do tutorial from 2pm tomorrow to 4pm tomorrow");
		expectedStartDate = new DateTime(year, month, day+1, 14, 0);
		expectedEndDate = new DateTime(year, month, day+1, 16, 0);
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.edit,
				null, "do tutorial", expectedStartDate, expectedEndDate, 1);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));

		testCommand = commandParser
				.getParsedCommand("edit 1 to do tutorial from 2pm  to 4pm ");
		expectedStartDate = new DateTime(year, month, day, 14, 0);
		expectedEndDate = new DateTime(year, month, day, 16, 0);
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.edit,
				null, "do tutorial", expectedStartDate, expectedEndDate, 1);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));
		
		testCommand = commandParser
				.getParsedCommand("edit meeting 5 to do tutorial from 4PM  to 1AM ");
		expectedStartDate = new DateTime(year, month, day, 16, 0);
		expectedEndDate = new DateTime(year, month, day + 1, 1, 0);
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.edit,
				"meeting 5", "do tutorial", expectedStartDate, expectedEndDate, 0);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));

		testCommand = commandParser
				.getParsedCommand("1AM");
		expectedStartDate = new DateTime(year, month, day+1, 1, 0);
		expectedEndDate = new DateTime(year, month, day+2, 1, 0);
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.search,
				null, null, expectedStartDate, expectedEndDate, 0);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));
		
		testCommand = commandParser.getParsedCommand("29 feb 2013");
		expectedStartDate = new DateTime(2013, 3, 1, 0, 0);
		expectedEndDate = new DateTime(2013, 3, 2 , 0, 0);
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.search,
				null, null, expectedStartDate, expectedEndDate, 0);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));
		
		testCommand = commandParser.getParsedCommand("this year");
		expectedStartDate = new DateTime(2012, month, day, 0, 0);
		expectedEndDate = new DateTime(2012, 12, 31 , 23, 59);
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.search,
				null, null, expectedStartDate, expectedEndDate, 0);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));
		
		testCommand = commandParser.getParsedCommand("search 2013");
		expectedStartDate = new DateTime(2013, 1, 1, 0, 0);
		expectedEndDate = new DateTime(2013, 12, 31 , 23, 59);
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.search,
				null, null, expectedStartDate, expectedEndDate, 0);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));
		
		testCommand = commandParser
				.getParsedCommand("mark meeting 5 to do tutorial from 4pm  to 2pm ");
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.mark,
				"meeting 5", null, null, null, 0);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));

		testCommand = commandParser
				.getParsedCommand("add \"watch day after tomorrow\" ");
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.add,
				"watch day after tomorrow", null, null, null, 0);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));

		testCommand = commandParser
				.getParsedCommand("test at 2pm tomorrow at lt3");
		expectedStartDate = new DateTime(year, month, day+1, 14, 0);
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.add,
				"test lt3", null, expectedStartDate, null, 0);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));
		
		testCommand = commandParser
				.getParsedCommand("rename meeting 5 to do tutorial from 4pm  to 2pm ");
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.rename,
				"meeting 5", "do tutorial", null, null, 0);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));
		
		testCommand = commandParser
				.getParsedCommand("login meeting 5 to do tutorial from 4pm  to 2pm ");
		expectedCommand = new CommandInfo(CommandInfo.CommandKeyWords.login,
				null, null, null, null, 0);
		assertTrue(testCommand.isEqual(expectedCommand, testCommand));
	}

}
