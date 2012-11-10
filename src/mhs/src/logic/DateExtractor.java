//@author A0086805X
package mhs.src.logic;

import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

import org.joda.time.LocalDate;

/**
 * This is a class to check for date formats and set the date.
 */
public class DateExtractor {

	// This is the error messages
	private static final String ERROR_MESSAGE_COULD_NOT_PARSE = "Could not parse %1$s";

	// This is the date format for checing if the date is valid.
	private static final String DATE_FORMAT = "dd-MM-yyyy";

	// These are the fixed number of days/months.
	private static final int NUMBER_OF_DAYS_IN_A_WEEK = 7;
	private static final int NUMBER_MONTHS_IN_YEAR = 12;
	private static final int NUMBER_OF_DAYS_IN_NORMAL_YEAR = 365;
	private static final int NUMBER_OF_DAYS_IN_LEAP_YEAR = 366;

	// These are regex to check the dateformat and for clearing.
	private static final String REGEX_FULL_DATE_FORMAT = "(0?[1-9]|[12][0-9]|3[01])(/|-)(0?[1-9]|1[012])((/|-)(((20)\\d\\d)))?";
	private static final String REGEX_NON_WORD_CHAR = "\\W";
	private static final String REGEX_WHITE_SPACE = "\\s+";
	private static final String REGEX_SPACE = " ";
	private static final String REGEX_DASH = "-";

	// These are the date parameters that can be set
	private static LocalDate setDate;
	private static LocalDate now;
	private static int day, month, year;
	private static LocalDate startDate;
	private static LocalDate endDate;

	private static DateExtractor dateExtractor;
	private static DateFormat DEFAULT_FORMATTER;

	private static int counter;
	private static Queue<LocalDate> dateList;
	private static Queue<String> dateQueue;

	// These are the flags to ensure a date parameter is not reset
	private static boolean monthFlag = false;
	private static boolean dayFlag = false;
	private static boolean yearFlag = false;
	private static boolean dateFlag = false;
	private static boolean startDateFlag = false;
	private static boolean uniqueDateFlag = false;

	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * This is the enum of the different days and the the day of the week they
	 * correspond to.
	 */
	private enum DayKeyWord {
		monday(1), mon(1), tuesday(2), tue(2), tues(2), wednesday(3), weds(3), wed(
				3), thursday(4), thurs(4), thur(4), friday(5), fri(5), saturday(
				6), sat(6), sunday(7), sun(7);

		private final int dayOfWeek;

		DayKeyWord(int dayOfWeek) {
			this.dayOfWeek = dayOfWeek;
		}

	}

	/**
	 * This is the enum of the different months and the month of the year they
	 * correspond to.
	 */
	private enum MonthKeyWord {
		janurary(1), jan(1), february(2), feb(2), march(3), april(4), may(5), june(
				6), july(7), august(8), aug(8), september(9), sep(9), sept(9), october(
				10), oct(10), november(11), nov(11), decemeber(12), dec(12);

		private final int monthOfYear;

		MonthKeyWord(int monthOfyear) {
			this.monthOfYear = monthOfyear;
		}
	}

	/**
	 * This is the enum of unique date types not covered in other formats.
	 */
	private enum UniqueDateTypeKeyWord {
		today, tomorrow, week, month, year, THIS, weekend;
	}

	/**
	 * This is the constructor for this class that initializes the values.
	 */
	private DateExtractor() {
		logEnterMethod("DateExtractor");

		setDate = null;
		now = LocalDate.now();
		day = now.getDayOfMonth();
		month = now.getMonthOfYear();
		year = now.getYear();
		startDate = new LocalDate();
		endDate = new LocalDate();
		DEFAULT_FORMATTER = new SimpleDateFormat(DATE_FORMAT);
		counter = 0;

		resetDateParameterFlags();
		startDateFlag = false;
		uniqueDateFlag = false;

		logExitMethod("DateExtractor");

	}

	/**
	 * This is the getter method to get a single instance of the date extractor
	 * class.
	 * 
	 * @return Returns a date extractor object.
	 */
	public static DateExtractor getDateExtractor() {
		if (dateExtractor == null) {
			dateExtractor = new DateExtractor();
		}
		return dateExtractor;
	}

	/**
	 * This is the function to process the date and set the values.
	 * 
	 * @param commandQueue
	 *            This is the queue of date types.
	 * 
	 * @return Returns a local date type with the day,month, year set.
	 */
	public Queue<LocalDate> processDate(String parseString) {

		logEnterMethod("processDate");
		assert (parseString != null);
		String dateCommand;
		String[] processArray = setEnvironment(parseString);
		try {
			for (counter = 0; counter < processArray.length; counter++) {
				resetDateParameterFlags();

				if (checkDateFormat(processArray[counter])) {
					dateQueue = setUpDateQueue(processArray);

					validateDateQueueParameters();
					if (!dateQueue.isEmpty()) {
						while (!dateQueue.isEmpty()) {
							dateCommand = dateQueue.poll();
							uniqueDateFlag = false;
							if (isInteger(dateCommand)) {
								setIntegerDate(dateCommand);
							}

							else if (isDateStandardFormat(dateCommand)
									&& !dateFlag) {
								setFullDateFormat(dateCommand);
							}

							else if (isDateWithMonthSpelled(dateCommand)
									&& !monthFlag) {
								setStringMonth(dateCommand);
							}

							else if (isDayOfWeek(dateCommand) && !dayFlag) {
								setStringDay(dateCommand);
							}

							else if (isUniqueDateType(dateCommand)) {
								setUniqueDate(dateCommand);
							}
						}
						addDateToDateList();
					}
				}
			}
		} catch (NullPointerException e) {
			logger.log(Level.FINER, e.getMessage());
			return dateList;
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.log(Level.FINER, e.getMessage());
			return dateList;
		}
		logExitMethod("processDate");
		return dateList;

	}

	/**
	 * This is the method to set the date and push them into a list
	 */
	private void addDateToDateList() {
		logEnterMethod("addDateToDateList");
		if (!uniqueDateFlag) {
			if (!dayFlag && monthFlag) {
				setDateRange();
			}

			else {
				if (!isdateValid()) {
					rectifyDate();
				}
				setDate();
			}
		}
		logExitMethod("addDateToDateList");
	}

	/**
	 * This is the method to set the date.
	 */
	private void setDate() {
		logEnterMethod("setDate");
		try {
			setDate = new LocalDate(year, month, day);
			startDateFlag = true;
			if (!yearFlag) {
				if (setDate.isBefore(now)) {
					setDate = setDate.plusYears(1);
				}
			}
			dateList.add(setDate);
		} catch (InvalidParameterException e) {
			logger.log(Level.FINER, e.getMessage());
		}
		logExitMethod("setDate");
	}

	/**
	 * This is the method to get all the inputs that are unique date commands.
	 * 
	 * @param dateCommand
	 *            This is the string with all the data commands in a row
	 *            appended together.
	 * 
	 * @return Returns the appended date command.
	 */
	private String setUpUniqueDateCommand(String dateCommand) {
		logEnterMethod("setUpUniqueDateCommand");
		if (dateQueue.size() > 0 && isUniqueDateType(dateQueue.peek())) {
			dateCommand = dateCommand + REGEX_SPACE + dateQueue.poll();
		}
		logExitMethod("setUpUniqueDateCommand");
		return dateCommand;
	}

	/**
	 * THis is the method to set a day that is spelled out.
	 * 
	 * @param dateCommand
	 *            This is the command that is a string day.
	 */
	private void setStringDay(String dateCommand) {
		logEnterMethod("setStringDay");
		int parameters;
		parameters = getDayParameters(dateCommand);
		setDay(parameters);
		dayFlag = true;
		logExitMethod("setStringDay");
	}

	/**
	 * This is the method to set a month that is spelled out.
	 * 
	 * @param dateCommand
	 *            This is the command that is a string month.
	 */
	private void setStringMonth(String dateCommand) {
		logEnterMethod("setStringMonth");
		int parameters;
		parameters = getMontParameters(dateCommand);
		setIntegerMonth(parameters);
		logExitMethod("setStringMonth");
	}

	/**
	 * This is the method to set a date that is an integer.
	 * 
	 * @param dateCommand
	 *            This is the date command to be set as a date.
	 */
	private void setIntegerDate(String dateCommand) {
		logEnterMethod("setIntegerDate");
		int parameters;
		parameters = Integer.parseInt(dateCommand);

		if (isNumberOfDaysInMonth(parameters) && !dayFlag) {
			setIntegerDay(parameters);
		}

		else if (isMonthFormatInt(parameters) && !monthFlag) {
			setIntegerMonth(parameters);
		}

		else if (isYearFormat(parameters) && !yearFlag) {
			setIntegerYear(parameters);
		}
		logExitMethod("setIntegerDate");
	}

	/**
	 * This is the method to set the integer year.
	 * 
	 * @param parameters
	 *            This is the year to be set.
	 */
	private void setIntegerYear(int parameters) {
		logEnterMethod("setIntegerYear");
		year = parameters;
		yearFlag = true;
		logExitMethod("setIntegerYear");
	}

	/**
	 * This is the method to set the integer month.
	 * 
	 * @param parameters
	 *            This is the month to be set.
	 */
	private void setIntegerMonth(int parameters) {
		logEnterMethod("setIntegerMonth");
		month = parameters;
		monthFlag = true;
		logExitMethod("setIntegerMonth");
	}

	/**
	 * This is the method to set the integer day.
	 * 
	 * @param parameters
	 *            This is the day to be set.
	 */
	private void setIntegerDay(int parameters) {
		logEnterMethod("setIntegerDay");
		day = parameters;
		dayFlag = true;
		logExitMethod("setIntegerDay");
	}

	/**
	 * This is the method to ensure that queue does not have a single integer
	 * date.
	 */
	private void validateDateQueueParameters() {
		logEnterMethod("validateDateQueueParameters");
		if (dateQueue.size() == 1) {
			if (isInteger(dateQueue.peek())) {
				if (isNumberOfDaysInMonth(Integer.parseInt(dateQueue.peek()))) {
					dateQueue.poll();
				}
			}
		}
		logExitMethod("validateDateQueueParameters");
	}

	/**
	 * This is the method to reset all the date parameter flags.
	 */
	private void resetDateParameterFlags() {
		logEnterMethod("resetDateParameterFlags");
		monthFlag = false;
		dayFlag = false;
		yearFlag = false;
		dateFlag = false;
		logExitMethod("resetDateParameterFlags");
	}

	/**
	 * This is the method to set up the environment for date extracting.
	 * 
	 * @param parseString
	 *            This is the string that needs to be parsed.
	 * 
	 * @return Returns a string array.
	 */
	private String[] setEnvironment(String parseString) {
		logEnterMethod("setEnvironment");
		dateQueue = new LinkedList<String>();
		dateList = new LinkedList<LocalDate>();
		startDate = null;
		endDate = null;
		startDateFlag = false;
		uniqueDateFlag = false;
		try {
			String[] processArray = parseString.split(REGEX_WHITE_SPACE);
			logExitMethod("setEnvironment");
			return processArray;
		} catch (NullPointerException e) {
			logger.log(Level.FINER, e.getMessage());
			return null;
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.log(Level.FINER, e.getMessage());
			return null;
		}

	}

	/**
	 * This is the method that sets a date range.
	 */
	private void setDateRange() {
		logEnterMethod("setDateRange");
		if (!dayFlag && monthFlag) {
			setOneMonthPeriod();
		}

		else if (!dayFlag && !monthFlag && yearFlag) {
			setOneYearPeriod();
		}

		addDateRangesToList();
		logExitMethod("setDateRange");

	}

	/**
	 * This is a method to add the date range to the list.
	 */
	private void addDateRangesToList() {
		logEnterMethod("addDateRangesToList");
		if (startDate != null) {
			dateList.add(startDate);

			if (endDate != null) {
				dateList.add(endDate);
			}
		}
		logExitMethod("addDateRangesToList");
	}

	/**
	 * This is a method to set a date range of 1 year.
	 */
	private void setOneYearPeriod() {
		logEnterMethod("setOneYearPeriod");
		startDate = new LocalDate(year, 1, 1);
		endDate = startDate.plusYears(1);
		logExitMethod("setOneYearPeriod");
	}

	/**
	 * This is a method to set a date range of 1 month.
	 */
	private void setOneMonthPeriod() {
		logEnterMethod("setOneMonthPeriod");
		startDate = new LocalDate(year, month, 1);
		endDate = startDate.plusMonths(1);
		logExitMethod("setOneMonthPeriod");
	}

	/**
	 * This is a method to set up a queue with all the date parameters in a row.
	 * 
	 * @param processArray
	 * 
	 * @return Returns the queue.
	 */
	private Queue<String> setUpDateQueue(String[] processArray) {
		logEnterMethod("setUpDateQueue");
		int j;
		Queue<String> commandQueue = new LinkedList<String>();
		for (j = counter; j < processArray.length; j++) {
			if (checkDateFormat(processArray[j])) {
				commandQueue.add(processArray[j]);
			} else {
				break;
			}
		}
		counter = j - 1;
		logExitMethod("setUpDateQueue");
		return commandQueue;
	}

	/**
	 * This is the function to set the unique date types.
	 * 
	 * @param CommandKeyWords
	 *            This is the unique date type name.
	 */
	private void setUniqueDate(String dateCommand) {
		logEnterMethod("setUniqueDate");
		dateCommand = setUpUniqueDateCommand(dateCommand);
		dateList = new LinkedList<LocalDate>();
		if (isToday(dateCommand)) {
			setDateToday();

		} else if (isTomorrow(dateCommand)) {
			setDateTomorrow();
		} else if (isThisWeek(dateCommand)) {
			setDateThisWeek();
		} else if (isThisMonth(dateCommand)) {
			setDateThisMonth();
		} else if (isThisYear(dateCommand)) {
			setDateThisYear();
		} else if (isThisWeekend(dateCommand)) {
			setDateThisWeekend();
		}

		uniqueDateFlag = true;
		addDateRangesToList();
		logExitMethod("setUniqueDate");
	}

	/**
	 * Method to check if dateCommand is "this weekend".
	 * @param dateCommand
	 * @return
	 */
	private boolean isThisWeekend(String dateCommand) {
		logEnterMethod("isThisWeekend");
		logExitMethod("isThisWeekend");
		return dateCommand.equalsIgnoreCase(UniqueDateTypeKeyWord.THIS.name()
				+ REGEX_SPACE + UniqueDateTypeKeyWord.weekend.name());
	}

	/**
	 * Method to check if dateCommand is "this year".
	 * @param dateCommand
	 * @return
	 */
	private boolean isThisYear(String dateCommand) {
		logEnterMethod("isThisYear");
		logExitMethod("isThisYear");
		return dateCommand.equalsIgnoreCase(UniqueDateTypeKeyWord.THIS.name()
				+ REGEX_SPACE + UniqueDateTypeKeyWord.year.name());
	}

	/**
	 * Method to check if dateCommand is " this month".
	 * @param dateCommand
	 * @return
	 */
	private boolean isThisMonth(String dateCommand) {
		logEnterMethod("isThisMonth");
		logExitMethod("isThisMonth");
		return dateCommand.equalsIgnoreCase(UniqueDateTypeKeyWord.THIS.name()
				+ REGEX_SPACE + UniqueDateTypeKeyWord.month.name());
	}


	/**
	 * Method to check if dateCommand is "this year".
	 * @param dateCommand
	 * @return
	 */
	private boolean isThisWeek(String dateCommand) {
		logEnterMethod("isThisWeek");
		logExitMethod("isThisWeek");
		return dateCommand.equalsIgnoreCase(UniqueDateTypeKeyWord.THIS.name()
				+ REGEX_SPACE + UniqueDateTypeKeyWord.week.name());
	}

	/**
	 * Method to check if dateCommand is "tomorrow".
	 * @param dateCommand
	 * @return
	 */
	private boolean isTomorrow(String dateCommand) {
		logEnterMethod("isTomorrow");
		logExitMethod("isTomorrow");
		return dateCommand.equalsIgnoreCase(UniqueDateTypeKeyWord.tomorrow
				.name());
	}

	/**
	 * Method to check if dateCommand is "today".
	 * @param dateCommand
	 * @return
	 */
	private boolean isToday(String dateCommand) {
		logEnterMethod("isToday");
		logExitMethod("isToday");
		return dateCommand.equalsIgnoreCase(UniqueDateTypeKeyWord.today.name());
	}

	/**
	 * Method to set the date for this weekend.
	 */
	private void setDateThisWeekend() {
		logEnterMethod("setDateThisWeekend");
		int numberOfDaysToEndOfWeek = NUMBER_OF_DAYS_IN_A_WEEK
				- now.getDayOfWeek();
		endDate = now.plusDays(numberOfDaysToEndOfWeek);
		startDate = endDate.minusDays(1);
		logExitMethod("setDateThisWeekend");
	}

	/**
	 * Method to set the date for this year
	 */
	private void setDateThisYear() {
		logEnterMethod("setDateThisYear");
		startDate = now;
		int numberOfDaysToEndOfYear;
		if (startDate.year().isLeap()) {
			numberOfDaysToEndOfYear = NUMBER_OF_DAYS_IN_LEAP_YEAR
					- startDate.getDayOfYear();
		} else {
			numberOfDaysToEndOfYear = NUMBER_OF_DAYS_IN_NORMAL_YEAR
					- startDate.getDayOfYear();
		}
		if (numberOfDaysToEndOfYear == 0) {
			endDate = startDate.plusYears(1);
		} else {
			endDate = startDate.plusDays(numberOfDaysToEndOfYear);
		}
		logExitMethod("setDateThisYear");
	}

	/**
	 * Method to set the date for this month.
	 */
	private void setDateThisMonth() {
		logEnterMethod("setDateThisMonth");
		startDate = now;
		int numberOfDaysToEndOfMonth = startDate.dayOfMonth().getMaximumValue()
				- startDate.getDayOfMonth();
		if (numberOfDaysToEndOfMonth == 0) {
			endDate = startDate.plusMonths(1);
		} else {
			endDate = startDate.plusDays(numberOfDaysToEndOfMonth);
		}
		logExitMethod("setDateThisMonth");
	}

	/**
	 * Method to set the date for this week.
	 */
	private void setDateThisWeek() {
		logEnterMethod("setDateThisWeek");
		startDate = now;
		int numberOfDaysToEndOfWeek = NUMBER_OF_DAYS_IN_A_WEEK
				- startDate.getDayOfWeek();
		if (numberOfDaysToEndOfWeek == 0) {
			endDate = startDate.plusDays(startDate.getDayOfWeek()
					+ NUMBER_OF_DAYS_IN_A_WEEK);
		} else {
			endDate = startDate.plusDays(numberOfDaysToEndOfWeek);
		}
		logExitMethod("setDateThisWeek");
	}

	/**
	 * Method to set the date for tomorrow.
	 */
	private void setDateTomorrow() {
		logEnterMethod("setDateTomorrow");
		if (!startDateFlag) {
			startDate = now.plusDays(1);
			startDateFlag = true;
		} else {
			endDate = now.plusDays(1);
		}
		logExitMethod("setDateTomorrow");
	}

	/**
	 * Method to set the date for today.
	 */
	private void setDateToday() {
		logEnterMethod("setDateToday");
		if (!startDateFlag) {
			startDate = now;
			startDateFlag = true;
		} else {
			endDate = now;
		}
		logExitMethod("setDateToday");
	}

	/**
	 * This is a function to convert incorrect dates.
	 */
	private void rectifyDate() {
		logEnterMethod("rectifyDate");
		int lastDayOfMonth;
		LocalDate tempDate = new LocalDate(year, month, 1);
		lastDayOfMonth = tempDate.dayOfMonth().getMaximumValue();

		if (day > lastDayOfMonth) {

			if (month < NUMBER_MONTHS_IN_YEAR) {
				month++;
			} else {
				month = 1;
				year++;
			}
			day = day - lastDayOfMonth;

		}
		logExitMethod("rectifyDate");

	}

	/**
	 * This is a function to check if a date is valid.
	 * 
	 * @return Returns a true or false if the date is valid.
	 */
	private boolean isdateValid() {
		logEnterMethod("isdateValid");
		DEFAULT_FORMATTER.setLenient(false);

		String dateString = day + REGEX_DASH + month + REGEX_DASH + year;
		try {
			DEFAULT_FORMATTER.parse(dateString);
			logExitMethod("isdateValid");
			return true;
		} catch (ParseException e) {
			logger.log(Level.FINER, e.getMessage());
			logger.log(Level.FINER,
					String.format(ERROR_MESSAGE_COULD_NOT_PARSE, dateString));

		}
		logExitMethod("isdateValid");
		return false;

	}

	/**
	 * This is a function to set the day if it is input in the day of the week
	 * format.
	 * 
	 * @param parameters
	 *            This is the int that is that corresponds to the day of the
	 *            week.
	 */
	private void setDay(int parameters) {
		logEnterMethod("setDay");
		System.out.println(parameters);
		System.out.println(now.getDayOfWeek());
		if (parameters > now.getDayOfWeek()) {
			day = setDayInSameWeek(parameters);
			System.out.println(parameters + " " + day);
		} else if (parameters < now.getDayOfWeek()) {
			day = setDayInNextWeek(parameters);
		} else if (parameters == now.getDayOfWeek()) {
			day = setDayInExactlyOneWeek();
		}
		logExitMethod("setDay");
	}

	/**
	 * Method to set the day one week from today.
	 * 
	 * @return Returns the day of the month.
	 */
	private int setDayInExactlyOneWeek() {
		logEnterMethod("setDayInExactlyOneWeek");
		logExitMethod("setDayInExactlyOneWeek");
		return NUMBER_OF_DAYS_IN_A_WEEK + now.getDayOfMonth();
	}

	/**
	 * Method to set a day in the next week.
	 * 
	 * @param parameters Current day of the week.
	 * 
	 * @return Returns the day of the month
	 */
	private int setDayInNextWeek(int parameters) {
		logEnterMethod("setDayInNextWeek");
		logExitMethod("setDayInNextWeek");
		return NUMBER_OF_DAYS_IN_A_WEEK - now.getDayOfWeek() + parameters + now
				.getDayOfMonth();	}

	/**
	 * Method to set a day in the same week.
	 * 
	 * @param parameters Current day of the week.
	 * 
	 * @return Returns the day of the month.
	 */
	private int setDayInSameWeek(int parameters) {
		logEnterMethod("setDayInSameWeek");
		logExitMethod("setDayInSameWeek");
		return parameters - now.getDayOfWeek() + now.getDayOfMonth();
	}

	/**
	 * This is the function to get the int value of the day of the week from the
	 * string.
	 * 
	 * @param command
	 *            This is the string of the day.
	 * 
	 * @return Returns the int value of the day of the week.
	 */
	private int getDayParameters(String command) {
		logEnterMethod("getDayParameters");
		int dayOfWeek = 0;

		for (DayKeyWord d : DayKeyWord.values()) {
			if (command.equals(d.name())) {
				dayOfWeek = d.dayOfWeek;
			}
		}
		logExitMethod("getDayParameters");
		return dayOfWeek;
	}

	/**
	 * This is a function to get the int month of the year from the string
	 * month.
	 * 
	 * @param command
	 *            This is the string of the month.
	 * 
	 * @return Returns the int value of the month of the year.
	 */
	private int getMontParameters(String command) {
		logEnterMethod("getMontParameters");
		for (MonthKeyWord m : MonthKeyWord.values()) {
			if (command.equals(m.name())) {
				logger.exiting(getClass().getName(), this.getClass().getName());
				return m.monthOfYear;
			}
		}
		logExitMethod("getMontParameters");
		return 0;
	}

	/**
	 * This is the function to set the date if the full format is given,
	 * 
	 * @param command
	 *            This is the string of the whole date.
	 */
	private void setFullDateFormat(String command) {
		logEnterMethod("setFullDateFormat");
		int[] dateParameters = new int[3];
		String[] dateArray = command.split(REGEX_NON_WORD_CHAR);

		for (int i = 0; i < dateArray.length; i++) {
			dateParameters[i] = Integer.parseInt(dateArray[i]);
		}

		day = dateParameters[0];
		month = dateParameters[1];
		dayFlag = true;
		monthFlag = true;
		if (dateParameters[2] != 0) {
			year = dateParameters[2];
			yearFlag = true;
		}
		dateFlag = true;
		logExitMethod("setFullDateFormat");
	}

	/**
	 * This is the function to check if the input is a within the range of the
	 * number of days in the month.
	 * 
	 * @param number
	 *            This the number that is being checked.
	 * 
	 * @return Returns true if its a valid number.
	 */
	private boolean isNumberOfDaysInMonth(int number) {
		logEnterMethod("isNumberOfDaysInMonth");
		if (number > 0 && number < 32) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		}
		logExitMethod("isNumberOfDaysInMonth");
		return false;
	}

	/**
	 * This is the function to check if the input is within the range of number
	 * of months in a year.
	 * 
	 * @param number
	 *            This is the number that is being checked.
	 * 
	 * @return Returns true if its a valid number.
	 */
	private boolean isMonthFormatInt(int number) {
		logEnterMethod("isMonthFormatInt");
		if (number > 0 && number < 13) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		}
		logExitMethod("isMonthFormatInt");
		return false;
	}

	/**
	 * This is the function to check if the input is of a valid year format.
	 * 
	 * @param number
	 *            This is the number that is being checked.
	 * 
	 * @return Returns true if its a valid number.
	 */
	private boolean isYearFormat(int number) {
		logEnterMethod("isYearFormat");
		int year = now.getYear();
		if (number >= year && number < 9999) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		}
		logExitMethod("isYearFormat");
		return false;
	}

	/**
	 * This is a format that checks if a string is of a date format.
	 * 
	 * @param printString
	 *            This is the string to be checked.
	 * 
	 * @return Returns true if it is of a valid date type.
	 */
	public boolean checkDateFormat(String printString) {
		logEnterMethod("checkDateFormat");
		if (isInteger(printString)) {
			logExitMethod("checkDateFormat");
			return true;
		}
		if (isDateStandardFormat(printString)) {
			logExitMethod("checkDateFormat");
			return true;
		}
		if (isDateWithMonthSpelled(printString)) {
			logExitMethod("checkDateFormat");
			return true;
		}
		if (isDayOfWeek(printString)) {
			logExitMethod("checkDateFormat");
			return true;
		}
		if (isUniqueDateType(printString)) {
			logExitMethod("checkDateFormat");
			return true;
		}
		logExitMethod("checkDateFormat");
		return false;
	}

	/**
	 * This is the function to check if the string is a unique date type.
	 * 
	 * @param printString
	 *            This is the string to be checked.
	 * 
	 * @return Returns true if it is valid.
	 */
	private boolean isUniqueDateType(String printString) {
		logEnterMethod("isUniqueDateType");
		for (UniqueDateTypeKeyWord d : UniqueDateTypeKeyWord.values()) {
			if (printString.equalsIgnoreCase(d.name())) {
				logExitMethod("isUniqueDateType");
				return true;
			}
		}
		logExitMethod("isUniqueDateType");
		return false;
	}

	/**
	 * This is the function to check if the string is an integer.
	 * 
	 * @param printString
	 *            This is the string to be checked.
	 * 
	 * @return Returns true if the string is an int.
	 */
	private boolean isInteger(String printString) {
		logEnterMethod("isInteger");
		try {
			Integer.parseInt(printString);
			logExitMethod("isInteger");
			return true;
		} catch (NumberFormatException e) {
			logExitMethod("isInteger");
			return false;
		}
	}

	/**
	 * This is the function to check if the string is a day spelled out.
	 * 
	 * @param printString
	 *            This is the string to be checked.
	 * 
	 * @return Returns true if valid.
	 */
	private boolean isDayOfWeek(String printString) {
		logEnterMethod("isDayOfWeek");
		for (DayKeyWord d : DayKeyWord.values()) {
			if (printString.equals(d.name())) {
				logExitMethod("isDayOfWeek");
				return true;
			}
		}
		logExitMethod("isDayOfWeek");
		return false;

	}

	/**
	 * This is the function to check if the string is a month spelled out.
	 * 
	 * @param printString
	 *            This is the string to be checked.
	 * 
	 * @return Returns true if valid.
	 */
	private boolean isDateWithMonthSpelled(String printString) {
		logEnterMethod("isDateWithMonthSpelled");
		for (MonthKeyWord m : MonthKeyWord.values()) {
			if (printString.equals(m.name())) {
				logExitMethod("isDateWithMonthSpelled");
				return true;
			}
		}
		logExitMethod("isDateWithMonthSpelled");
		return false;

	}

	/**
	 * This is the function to check if the date is written as a full date.
	 * 
	 * @param printString
	 *            This is the string to be checked.
	 * 
	 * @return Return true if valid
	 */
	private boolean isDateStandardFormat(String printString) {
		logEnterMethod("isDateStandardFormat");
		if (printString.matches(REGEX_FULL_DATE_FORMAT)) {
			logExitMethod("isDateStandardFormat");
			return true;
		} else {
			logExitMethod("isDateStandardFormat");
			return false;
		}
	}

	/**
	 * Logger enter method
	 * 
	 * @param methodName
	 */
	void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}

	/**
	 * Logger enter method
	 * 
	 * @param methodName
	 */
	void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}

}
