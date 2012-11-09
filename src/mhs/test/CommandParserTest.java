package mhs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.Queue;

import mhs.src.logic.CommandExtractor;
import mhs.src.logic.CommandInfo;
import mhs.src.logic.CommandParser;
import mhs.src.logic.DateExtractor;
import mhs.src.logic.NameExtractor;
import mhs.src.logic.TimeExtractor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

public class CommandParserTest {

	private DateExtractor dateExtractor;
	private TimeExtractor timeExtractor;
	private CommandExtractor commandExtractor;
	private NameExtractor nameExtractor;
	private int day, month, year;

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

	@Test
	public void testTimeExtractor() {

		Queue<LocalTime> testList = new LinkedList<LocalTime>();
		Queue<LocalTime> expectedList = new LinkedList<LocalTime>();
		LocalTime testStartTime;
		LocalTime testEndTime;

		testList = timeExtractor.processTime("5pm");
		testStartTime = new LocalTime(17, 0);
		expectedList.add(testStartTime);
		assertEquals(expectedList, testList);

		testList = timeExtractor.processTime("5Am");
		testStartTime = new LocalTime(5, 0);
		expectedList = new LinkedList<LocalTime>();
		expectedList.add(testStartTime);
		assertEquals(expectedList, testList);

		testList = timeExtractor.processTime("20:00");
		testStartTime = new LocalTime(20, 0);
		expectedList = new LinkedList<LocalTime>();
		expectedList.add(testStartTime);
		assertEquals(expectedList, testList);

		testList = timeExtractor.processTime("3AM to 19:00");
		testStartTime = new LocalTime(3, 0);
		testEndTime = new LocalTime(19, 0);
		expectedList = new LinkedList<LocalTime>();
		expectedList.add(testStartTime);
		expectedList.add(testEndTime);
		assertEquals(expectedList, testList);

		testList = timeExtractor.processTime("27AM to 35:72");
		expectedList = new LinkedList<LocalTime>();
		assertEquals(expectedList, testList);

	}

	@Test
	public void dateTimeExtractor() {

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

	@Test
	public void NameExtractor() {

		Queue<String> testList = new LinkedList<String>();
		Queue<String> expectedList = new LinkedList<String>();
		String testName;
		String testEdittedName;

		testList = nameExtractor.processName("task to task2");
		testName = "task";
		testEdittedName = "task2";
		expectedList.add(testName);
		expectedList.add(testEdittedName);
		assertEquals(expectedList, testList);

	}

	@Test
	public void commandExtractor() {

		String expectedCommand;
		String testCommand;

		testCommand = commandExtractor.extractCommand("add task");
		expectedCommand = "add";
		assertEquals(expectedCommand, testCommand);

		testCommand = commandExtractor.extractCommand("upDaTe task");
		expectedCommand = "edit";
		assertEquals(expectedCommand, testCommand);

	}

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
				.getParsedCommand("edit meeting 5 to do tutorial from 1AM to 4PM ");
		expectedStartDate = new DateTime(year, month, day, 16, 0);
		expectedEndDate = new DateTime(year, month, day+1, 1, 0);
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
