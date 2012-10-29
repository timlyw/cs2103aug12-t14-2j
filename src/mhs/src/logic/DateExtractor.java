package mhs.src.logic;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Queue;

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

	//This is the error messages
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

	//These are the date parameters that can be set
	private LocalDate setDate;
	private LocalDate now;
	private int day, month, year;
	private LocalDate startDate;
	private LocalDate endDate;
	
	private static DateExtractor dateExtractor;
	private static DateFormat DEFAULT_FORMATTER;

	private static int counter;
	private static Queue<LocalDate> dateList;
	private Queue<String> dateQueue;

	//These are the flags to ensure a date parameter is not reset
	private boolean monthFlag = false;
	private boolean dayFlag = false;
	private boolean yearFlag = false;
	private boolean dateFlag = false;
	private boolean startDateFlag = false;
	private boolean uniqueDateFlag = false;

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

		String dateCommand;
		String[] processArray = setEnvironment(parseString);

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

						else if (isDateStandardFormat(dateCommand) && !dateFlag) {
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
		return dateList;

	}

	private void addDateToDateList() {
		if (!uniqueDateFlag) {
			if (!isAllFlagsSet()) {
				setDateRange();
			}

			else {
				if (!isdateValid()) {
					rectifyDate();
				}
				setDate = new LocalDate(year, month, day);
				startDateFlag = true;
				dateList.add(setDate);
			}
		}
	}

	private String setUpUniqueDateCommand(String dateCommand) {
		if (dateQueue.size() > 0 && isUniqueDateType(dateQueue.peek())) {
			dateCommand = dateCommand + REGEX_SPACE + dateQueue.poll();
		}
		return dateCommand;
	}

	private void setStringDay(String dateCommand) {
		int parameters;
		parameters = getDayParameters(dateCommand);
		setDay(parameters);
		dayFlag = true;
	}

	private void setStringMonth(String dateCommand) {
		int parameters;
		parameters = getMontParameters(dateCommand);
		setIntegerMonth(parameters);
	}

	private void setIntegerDate(String dateCommand) {
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
	}

	private void setIntegerYear(int parameters) {
		year = parameters;
		yearFlag = true;
	}

	private void setIntegerMonth(int parameters) {
		month = parameters;
		monthFlag = true;
	}

	private void setIntegerDay(int parameters) {
		day = parameters;
		dayFlag = true;
	}

	private void validateDateQueueParameters() {
		if (dateQueue.size() == 1) {
			if (isInteger(dateQueue.peek())) {
				if (isNumberOfDaysInMonth(Integer.parseInt(dateQueue.peek()))) {
					dateQueue.poll();
				}
			}
		}
	}

	private void resetDateParameterFlags() {
		monthFlag = false;
		dayFlag = false;
		yearFlag = false;
		dateFlag = false;
	}

	private String[] setEnvironment(String parseString) {
		dateQueue = new LinkedList<String>();
		String[] processArray = parseString.split(REGEX_WHITE_SPACE);
		dateList = new LinkedList<LocalDate>();
		startDate = null;
		endDate = null;
		startDateFlag = false;
		uniqueDateFlag = false;
		return processArray;
	}

	private void setDateRange() {

		if (!dayFlag && monthFlag) {
			setOneMonthPeriod();
		}

		else if (!dayFlag && !monthFlag && yearFlag) {
			setOneYearPeriod();
		}

		addDateRangesToList();

	}

	private void addDateRangesToList() {
		if (startDate != null) {
			dateList.add(startDate);

			if (endDate != null) {
				dateList.add(endDate);
			}
		}
	}

	private void setOneYearPeriod() {
		startDate = new LocalDate(year, 1, 1);
		endDate = startDate.plusYears(1);
	}

	private void setOneMonthPeriod() {
		startDate = new LocalDate(year, month, 1);
		endDate = startDate.plusMonths(1);
	}

	private boolean isAllFlagsSet() {
		if ((dayFlag && monthFlag && yearFlag) || (dateFlag)
				|| (dayFlag && monthFlag)) {
			return true;
		}
		return false;
	}

	private Queue<String> setUpDateQueue(String[] processArray) {
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

		return commandQueue;
	}

	/**
	 * This is the function to set the unique date types.
	 * 
	 * @param CommandKeyWords
	 *            This is the unique date type name.
	 */
	private void setUniqueDate(String dateCommand) {
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
	}

	private void setDateThisWeekend() {
		int numberOfDaysToEndOfWeek = NUMBER_OF_DAYS_IN_A_WEEK
				- now.getDayOfWeek();
		endDate = now.plusDays(numberOfDaysToEndOfWeek);
		startDate = endDate.minusDays(1);
	}

	private void setDateThisYear() {
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
	}

	private void setDateThisMonth() {
		startDate = now;
		int numberOfDaysToEndOfMonth = startDate.dayOfMonth().getMaximumValue()
				- startDate.getDayOfMonth();
		if (numberOfDaysToEndOfMonth == 0) {
			endDate = startDate.plusMonths(1);
		} else {
			endDate = startDate.plusDays(numberOfDaysToEndOfMonth);
		}
	}

	private void setDateThisWeek() {
		startDate = now;
		int numberOfDaysToEndOfWeek = NUMBER_OF_DAYS_IN_A_WEEK
				- startDate.getDayOfWeek();
		if (numberOfDaysToEndOfWeek == 0) {
			endDate = startDate.plusDays(startDate.getDayOfWeek()
					+ NUMBER_OF_DAYS_IN_A_WEEK);
		} else {
			endDate = startDate.plusDays(numberOfDaysToEndOfWeek);
		}
	}

	private void setDateTomorrow() {
		if (!startDateFlag) {
			startDate = now.plusDays(1);
			startDateFlag = true;
		} else {
			endDate = now.plusDays(1);
		}
	}

	private void setDateToday() {
		if (!startDateFlag) {
			startDate = now;
			startDateFlag = true;
		} else {
			endDate = now;
		}
	}

	/**
	 * This is a function to convert incorrect dates.
	 */
	private void rectifyDate() {

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

	}

	/**
	 * This is a function to check if a date is valid.
	 * 
	 * @return Returns a true or false if the date is valid.
	 */
	private boolean isdateValid() {
		DEFAULT_FORMATTER.setLenient(false);

		String dateString = day + REGEX_DASH + month + REGEX_DASH + year;
		try {
			DEFAULT_FORMATTER.parse(dateString);
			return true;
		} catch (ParseException e) {
			System.out.println(String.format(ERROR_MESSAGE_COULD_NOT_PARSE,
					dateString));
		}
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

		if (parameters < now.getDayOfWeek()) {
			day = (NUMBER_OF_DAYS_IN_A_WEEK - now.getDayOfWeek() + parameters + now
					.getDayOfMonth());
		} else if (parameters > now.getDayOfWeek()) {
			day = (parameters + now.getDayOfMonth() - 1);
		} else if (parameters == now.getDayOfWeek()) {
			day = (NUMBER_OF_DAYS_IN_A_WEEK + now.getDayOfMonth());
		}
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

		int dayOfWeek = 0;

		for (DayKeyWord d : DayKeyWord.values()) {
			if (command.equals(d.name())) {
				dayOfWeek = d.dayOfWeek;
			}
		}
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

		for (MonthKeyWord m : MonthKeyWord.values()) {
			if (command.equals(m.name())) {
				return m.monthOfYear;
			}
		}
		return 0;
	}

	/**
	 * This is the function to set the date if the full format is given,
	 * 
	 * @param command
	 *            This is the string of the whole date.
	 */
	private void setFullDateFormat(String command) {

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
		if (number > 0 && number < 32) {
			return true;
		}
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
		if (number > 0 && number < 13) {
			return true;
		}
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

		int year = now.getYear();
		if (number >= year && number < 9999) {
			return true;
		}
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
		if (isInteger(printString)) {
			return true;
		}
		if (isDateStandardFormat(printString)) {
			return true;
		}
		if (isDateWithMonthSpelled(printString)) {
			return true;
		}
		if (isDayOfWeek(printString)) {
			return true;
		}
		if (isUniqueDateType(printString)) {
			return true;
		}

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

		for (UniqueDateTypeKeyWord d : UniqueDateTypeKeyWord.values()) {
			if (printString.equalsIgnoreCase(d.name())) {
				return true;
			}
		}
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
		try {
			Integer.parseInt(printString);
			return true;
		} catch (NumberFormatException e) {
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

		for (DayKeyWord d : DayKeyWord.values()) {
			if (printString.equals(d.name())) {
				return true;
			}
		}
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
		for (MonthKeyWord m : MonthKeyWord.values()) {
			if (printString.equals(m.name())) {
				return true;
			}
		}
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

		if (printString.matches(REGEX_FULL_DATE_FORMAT)) {
			return true;
		} else {
			return false;
		}
	}

}
