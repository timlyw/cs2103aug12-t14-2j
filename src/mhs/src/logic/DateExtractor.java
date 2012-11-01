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
 * 
 * @author Cheong Kahou
 *A0086805X
 */

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

	// This an error message
	private static final String ERROR_MESSAGE_NOT_NUMERICAL_DATE = "error not numerical date!";

	// These are regex to check the dateformat and for clearing.
	private static final String REGEX_FULL_DATE_FORMAT = "(0?[1-9]|[12][0-9]|3[01])(/|-)(0?[1-9]|1[012])(/|-)(((20)\\d\\d)?)";
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
		logger.entering(getClass().getName(), this.getClass().getName());
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
		logger.exiting(getClass().getName(), this.getClass().getName());

	}

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
		logger.entering(getClass().getName(), this.getClass().getName());
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
			return null;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return dateList;

	}

	private void addDateToDateList() {
		logger.entering(getClass().getName(), this.getClass().getName());
		if (!uniqueDateFlag) {
			if (!isAllFlagsSet()) {
				setDateRange();
			}

			else {
				if (!isdateValid()) {
					rectifyDate();
				}
				try {
					setDate = new LocalDate(year, month, day);
					startDateFlag = true;
					dateList.add(setDate);
				} catch (InvalidParameterException e) {
					logger.log(Level.FINER, e.getMessage());
				}

			}
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private String setUpUniqueDateCommand(String dateCommand) {
		logger.entering(getClass().getName(), this.getClass().getName());
		if (dateQueue.size() > 0 && isUniqueDateType(dateQueue.peek())) {
			dateCommand = dateCommand + REGEX_SPACE + dateQueue.poll();
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return dateCommand;
	}

	private void setStringDay(String dateCommand) {
		logger.entering(getClass().getName(), this.getClass().getName());
		int parameters;
		parameters = getDayParameters(dateCommand);
		setDay(parameters);
		dayFlag = true;
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void setStringMonth(String dateCommand) {
		logger.entering(getClass().getName(), this.getClass().getName());
		int parameters;
		parameters = getMontParameters(dateCommand);
		setIntegerMonth(parameters);
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void setIntegerDate(String dateCommand) {
		logger.entering(getClass().getName(), this.getClass().getName());
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
		} else {
			System.out.println(ERROR_MESSAGE_NOT_NUMERICAL_DATE);

		}
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void setIntegerYear(int parameters) {
		logger.entering(getClass().getName(), this.getClass().getName());
		year = parameters;
		yearFlag = true;
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void setIntegerMonth(int parameters) {
		logger.entering(getClass().getName(), this.getClass().getName());
		month = parameters;
		monthFlag = true;
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void setIntegerDay(int parameters) {
		logger.entering(getClass().getName(), this.getClass().getName());
		day = parameters;
		dayFlag = true;
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void validateDateQueueParameters() {
		logger.entering(getClass().getName(), this.getClass().getName());
		if (dateQueue.size() == 1) {
			if (isInteger(dateQueue.peek())) {
				if (isNumberOfDaysInMonth(Integer.parseInt(dateQueue.peek()))) {
					dateQueue.poll();
				}
			}
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void resetDateParameterFlags() {
		logger.entering(getClass().getName(), this.getClass().getName());
		monthFlag = false;
		dayFlag = false;
		yearFlag = false;
		dateFlag = false;
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private String[] setEnvironment(String parseString) {
		logger.entering(getClass().getName(), this.getClass().getName());
		dateQueue = new LinkedList<String>();
		dateList = new LinkedList<LocalDate>();
		startDate = null;
		endDate = null;
		startDateFlag = false;
		uniqueDateFlag = false;
		try {
			String[] processArray = parseString.split(REGEX_WHITE_SPACE);
			logger.exiting(getClass().getName(), this.getClass().getName());
			return processArray;
		} catch (NullPointerException e) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return null;
		}

	}

	private void setDateRange() {
		logger.entering(getClass().getName(), this.getClass().getName());
		if (!dayFlag && monthFlag) {
			setOneMonthPeriod();
		}

		else if (!dayFlag && !monthFlag && yearFlag) {
			setOneYearPeriod();
		}

		addDateRangesToList();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void addDateRangesToList() {
		logger.entering(getClass().getName(), this.getClass().getName());
		if (startDate != null) {
			dateList.add(startDate);

			if (endDate != null) {
				dateList.add(endDate);
			}
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void setOneYearPeriod() {
		logger.entering(getClass().getName(), this.getClass().getName());
		startDate = new LocalDate(year, 1, 1);
		endDate = startDate.plusYears(1);
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void setOneMonthPeriod() {
		logger.entering(getClass().getName(), this.getClass().getName());
		startDate = new LocalDate(year, month, 1);
		endDate = startDate.plusMonths(1);
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private boolean isAllFlagsSet() {
		logger.entering(getClass().getName(), this.getClass().getName());
		if ((dayFlag && monthFlag && yearFlag) || (dateFlag)
				|| (dayFlag && monthFlag)) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return false;
	}

	private Queue<String> setUpDateQueue(String[] processArray) {
		logger.entering(getClass().getName(), this.getClass().getName());
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
		logger.exiting(getClass().getName(), this.getClass().getName());
		return commandQueue;
	}

	/**
	 * This is the function to set the unique date types.
	 * 
	 * @param CommandKeyWords
	 *            This is the unique date type name.
	 */
	private void setUniqueDate(String dateCommand) {
		logger.entering(getClass().getName(), this.getClass().getName());
		dateCommand = setUpUniqueDateCommand(dateCommand);
		dateList = new LinkedList<LocalDate>();
		if (dateCommand.equalsIgnoreCase(UniqueDateTypeKeyWord.today.name())) {
			setDateToday();

		} else if (dateCommand.equalsIgnoreCase(UniqueDateTypeKeyWord.tomorrow
				.name())) {
			setDateTomorrow();
		} else if (dateCommand.equalsIgnoreCase(UniqueDateTypeKeyWord.THIS
				.name() + REGEX_SPACE + UniqueDateTypeKeyWord.week.name())) {
			setDateThisWeek();
		}

		else if (dateCommand.equalsIgnoreCase(UniqueDateTypeKeyWord.THIS.name()
				+ REGEX_SPACE + UniqueDateTypeKeyWord.month.name())) {
			setDateThisMonth();
		}

		else if (dateCommand.equalsIgnoreCase(UniqueDateTypeKeyWord.THIS.name()
				+ REGEX_SPACE + UniqueDateTypeKeyWord.year.name())) {
			setDateThisYear();

		}

		else if (dateCommand.equalsIgnoreCase(UniqueDateTypeKeyWord.THIS.name()
				+ REGEX_SPACE + UniqueDateTypeKeyWord.weekend.name())) {
			setDateThisWeekend();

		}

		uniqueDateFlag = true;
		addDateRangesToList();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void setDateThisWeekend() {
		logger.entering(getClass().getName(), this.getClass().getName());
		int numberOfDaysToEndOfWeek = NUMBER_OF_DAYS_IN_A_WEEK
				- now.getDayOfWeek();
		endDate = now.plusDays(numberOfDaysToEndOfWeek);
		startDate = endDate.minusDays(1);
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void setDateThisYear() {
		logger.entering(getClass().getName(), this.getClass().getName());
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
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void setDateThisMonth() {
		logger.entering(getClass().getName(), this.getClass().getName());
		startDate = now;
		int numberOfDaysToEndOfMonth = startDate.dayOfMonth().getMaximumValue()
				- startDate.getDayOfMonth();
		if (numberOfDaysToEndOfMonth == 0) {
			endDate = startDate.plusMonths(1);
		} else {
			endDate = startDate.plusDays(numberOfDaysToEndOfMonth);
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void setDateThisWeek() {
		logger.entering(getClass().getName(), this.getClass().getName());
		startDate = now;
		int numberOfDaysToEndOfWeek = NUMBER_OF_DAYS_IN_A_WEEK
				- startDate.getDayOfWeek();
		if (numberOfDaysToEndOfWeek == 0) {
			endDate = startDate.plusDays(startDate.getDayOfWeek()
					+ NUMBER_OF_DAYS_IN_A_WEEK);
		} else {
			endDate = startDate.plusDays(numberOfDaysToEndOfWeek);
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void setDateTomorrow() {
		logger.entering(getClass().getName(), this.getClass().getName());
		if (!startDateFlag) {
			startDate = now.plusDays(1);
			startDateFlag = true;
		} else {
			endDate = now.plusDays(1);
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void setDateToday() {
		logger.entering(getClass().getName(), this.getClass().getName());
		if (!startDateFlag) {
			startDate = now;
			startDateFlag = true;
		} else {
			endDate = now;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	/**
	 * This is a function to convert incorrect dates.
	 */
	private void rectifyDate() {
		logger.entering(getClass().getName(), this.getClass().getName());
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
		logger.exiting(getClass().getName(), this.getClass().getName());

	}

	/**
	 * This is a function to check if a date is valid.
	 * 
	 * @return Returns a true or false if the date is valid.
	 */
	private boolean isdateValid() {
		logger.entering(getClass().getName(), this.getClass().getName());
		DEFAULT_FORMATTER.setLenient(false);

		String dateString = day + REGEX_DASH + month + REGEX_DASH + year;
		try {
			DEFAULT_FORMATTER.parse(dateString);
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		} catch (ParseException e) {
			logger.log(Level.FINER, e.getMessage());
			logger.log(Level.FINER,
					String.format(ERROR_MESSAGE_COULD_NOT_PARSE, dateString));

		}
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		if (parameters < now.getDayOfWeek()) {
			day = (NUMBER_OF_DAYS_IN_A_WEEK - now.getDayOfWeek() + parameters + now
					.getDayOfMonth());
		} else if (parameters > now.getDayOfWeek()) {
			day = (parameters + now.getDayOfMonth() - 1);
		} else if (parameters == now.getDayOfWeek()) {
			day = (NUMBER_OF_DAYS_IN_A_WEEK + now.getDayOfMonth());
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		int dayOfWeek = 0;

		for (DayKeyWord d : DayKeyWord.values()) {
			if (command.equals(d.name())) {
				dayOfWeek = d.dayOfWeek;
			}
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		for (MonthKeyWord m : MonthKeyWord.values()) {
			if (command.equals(m.name())) {
				logger.exiting(getClass().getName(), this.getClass().getName());
				return m.monthOfYear;
			}
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return 0;
	}

	/**
	 * This is the function to set the date if the full format is given,
	 * 
	 * @param command
	 *            This is the string of the whole date.
	 */
	private void setFullDateFormat(String command) {
		logger.entering(getClass().getName(), this.getClass().getName());
		int[] dateParameters = new int[3];
		String[] dateArray = command.split(REGEX_NON_WORD_CHAR);

		for (int i = 0; i < dateArray.length; i++) {
			dateParameters[i] = Integer.parseInt(dateArray[i]);
		}

		day = dateParameters[0];
		month = dateParameters[1];
		if (dateParameters[2] != 0) {
			year = dateParameters[2];
		}
		dateFlag = true;
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		if (number > 0 && number < 32) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		if (number > 0 && number < 13) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		int year = now.getYear();
		if (number >= year && number < 9999) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		if (isInteger(printString)) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		}
		if (isDateStandardFormat(printString)) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		}
		if (isDateWithMonthSpelled(printString)) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		}
		if (isDayOfWeek(printString)) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		}
		if (isUniqueDateType(printString)) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		for (UniqueDateTypeKeyWord d : UniqueDateTypeKeyWord.values()) {
			if (printString.equalsIgnoreCase(d.name())) {
				logger.exiting(getClass().getName(), this.getClass().getName());
				return true;
			}
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		try {
			Integer.parseInt(printString);
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		} catch (NumberFormatException e) {
			logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		for (DayKeyWord d : DayKeyWord.values()) {
			if (printString.equals(d.name())) {
				logger.exiting(getClass().getName(), this.getClass().getName());
				return true;
			}
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		for (MonthKeyWord m : MonthKeyWord.values()) {
			if (printString.equals(m.name())) {
				logger.exiting(getClass().getName(), this.getClass().getName());
				return true;
			}
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		if (printString.matches(REGEX_FULL_DATE_FORMAT)) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return true;
		} else {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return false;
		}
	}

}
