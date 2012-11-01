package mhs.test;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.Queue;

import mhs.src.logic.CommandExtractor;
import mhs.src.logic.CommandInfo;
import mhs.src.logic.CommandParser;
import mhs.src.logic.DateExtractor;
import mhs.src.logic.NameExtractor;
import mhs.src.logic.TimeExtractor;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

public class CommandParserTest {
	private DateTime now;
	private DateExtractor dateExtractor;
	private TimeExtractor timeExtractor;
	private CommandExtractor commandExtractor;
	private NameExtractor nameExtractor;
	private int day, month , year;
	@Before
	public void setUpEnvironment() {
		
		now = DateTime.now();
		day = now.getDayOfMonth();
		month = now.getMonthOfYear();
		year = now.getYear();
		dateExtractor = DateExtractor.getDateExtractor();
		timeExtractor = TimeExtractor.getTimeExtractor();
		commandExtractor = CommandExtractor.getCommandExtractor();
		nameExtractor = NameExtractor.getNameExtractor();
	}
	
	@Test
	public void testTimeExtractor(){
		
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
		testStartTime = new LocalTime(3 , 0);
		testEndTime = new LocalTime(19 , 0);
		expectedList = new LinkedList<LocalTime>();
		expectedList.add(testStartTime);
		expectedList.add(testEndTime);
		assertEquals(expectedList, testList);
		
		testList = timeExtractor.processTime("27AM to 35:72");
		expectedList = new LinkedList<LocalTime>();
		assertEquals(expectedList, testList);
		
	}

	@Test
	public void dateTimeExtractor(){
		
		Queue<LocalDate> testList = new LinkedList<LocalDate>();
		Queue<LocalDate> expectedList = new LinkedList<LocalDate>();
		LocalDate testStartDate;
		LocalDate testEndDate;
		LocalDate now = LocalDate.now();
		
		testList = dateExtractor.processDate("10 10 2012 to 17 oct 2012");
		testStartDate = new LocalDate(2012, 10, 10);
		testEndDate = new LocalDate(2012, 10, 17);
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);
		
		testList = dateExtractor.processDate("today to tomorrow");
		testStartDate = now;
		testEndDate = now.plusDays(1);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);
		
		testList = dateExtractor.processDate("today to 5/12/2012");
		testStartDate = now;
		testEndDate = new LocalDate(2012, 12, 5);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);
		
		testList = dateExtractor.processDate("17 oct to 21 10");
		testStartDate = new LocalDate(year, 10, 17);
		testEndDate = new LocalDate(year, 10, 21);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);
		
		testList = dateExtractor.processDate("oct 2012");
		testStartDate = new LocalDate(2012, 10, 1);
		testEndDate = new LocalDate(2012, 11, 1);
		expectedList = new LinkedList<LocalDate>();
		expectedList.add(testStartDate);
		expectedList.add(testEndDate);
		assertEquals(expectedList, testList);

		
	}
	
	@Test
	public void NameExtractor(){
		
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
	public void commandExtractor(){

		String expectedCommand;
		String testCommand;

		testCommand = commandExtractor.setCommand("add task");
		expectedCommand = "add";
		assertEquals(expectedCommand, testCommand);

		testCommand = commandExtractor.setCommand("upDaTe task");
		expectedCommand = "edit";
		assertEquals(expectedCommand, testCommand);

		
	}
	@Test
	public void testCommandParser(){


		CommandInfo testInput;
		String inputName;
		String edittedName;
		CommandParser commandParser = CommandParser.getCommandParser();
		DateTime expectedStartDate;
		DateTime inputStartDate;
		DateTime expectedEndDate;
		DateTime inputEndDate;
		
		testInput = commandParser.getParsedCommand("meeting1");
		inputName = testInput.getTaskName().trim();
		assertEquals("meeting1", inputName);
		
		testInput = commandParser.getParsedCommand("class project by 10/12/2012 10pm");
		inputName = testInput.getTaskName().trim();
		expectedStartDate = new DateTime(2012, 12, 10, 22, 0);
		inputStartDate = testInput.getStartDate();
		assertEquals("class project", inputName);
		assertEquals(inputStartDate, expectedStartDate);
		
		testInput = commandParser.getParsedCommand("class project by 10/12/2012 10pm to 10/12/2012 3pm");
		inputName = testInput.getTaskName().trim();
		expectedStartDate = new DateTime(2012, 12, 10, 15, 0);
		expectedEndDate = new DateTime(2012, 12, 10, 22, 0);
		inputStartDate = testInput.getStartDate();
		assertEquals("class project", inputName);
		assertEquals(inputStartDate, expectedStartDate);

		testInput = commandParser.getParsedCommand("\"day after tomorrow\" on 4 dec 2012 22:12 to 6 12 2012 4.15pm");
		inputName = testInput.getTaskName().trim();
		expectedStartDate = new DateTime(2012, 12, 4, 22, 12);
		inputStartDate = testInput.getStartDate();
		expectedEndDate = new DateTime(2012, 12, 6, 16, 15);
		inputEndDate = testInput.getEndDate();
		assertEquals("day after tomorrow", inputName);
		assertEquals(inputStartDate, expectedStartDate);
		assertEquals(inputEndDate, expectedEndDate);
		
		testInput = commandParser.getParsedCommand("\"day after tomorrow\"  7 11 2012 4.15pm 2 nov 2012 22:12");
		inputName = testInput.getTaskName().trim();
		expectedStartDate = new DateTime(2012, 11, 2, 22, 12);
		inputStartDate = testInput.getStartDate();
		expectedEndDate = new DateTime(2012, 11, 7, 16, 15);
		inputEndDate = testInput.getEndDate();
		assertEquals("day after tomorrow", inputName);
		assertEquals(inputStartDate, expectedStartDate);
		assertEquals(inputEndDate, expectedEndDate);
		assertEquals(testInput.getCommandEnum(), CommandInfo.CommandKeyWords.add);
		
		testInput = commandParser.getParsedCommand("edit \"watch movie\" to \"laundry duties\"");
		inputName = testInput.getTaskName().trim();
		edittedName = testInput.getEdittedName().trim();
		assertEquals("watch movie", inputName);
		assertEquals("laundry duties", edittedName);
		assertEquals(testInput.getCommandEnum(), CommandInfo.CommandKeyWords.edit);
		
		testInput = commandParser.getParsedCommand("edit watch movie 5pm laundry duties");
		inputName = testInput.getTaskName().trim();
		edittedName = testInput.getEdittedName().trim();
		assertEquals("watch movie", inputName);
		assertEquals("laundry duties", edittedName);
		assertEquals(testInput.getCommandEnum(), CommandInfo.CommandKeyWords.edit);
	}


}
