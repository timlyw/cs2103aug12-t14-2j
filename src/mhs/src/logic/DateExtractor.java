package mhs.src.logic;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	// This is the date format for checing if the date is valid.
	private static final String DATE_FORMAT = "dd-MM-yyyy";

	// These are the fixed number of days/months.
	private static final int NUMBER_NUMBER_OF_DAYS_IN_A_WEEK = 7;
	private static final int NUMBER_MONTHS_IN_YEAR = 12;

	// This an error message
	private static final String ERROR_MESSAGE_NOT_NUMERICAL_DATE = "error not numerical date!";

	// These are regex to check the dateformat and for clearing.
	private static final String REGEX_FULL_DATE_FORMAT = "(0?[1-9]|[12][0-9]|3[01])(/|-)(0?[1-9]|1[012])(.*)(((20)\\d\\d)?)";
	private static final String REGEX_NON_WORD_CHAR = "\\W";

	LocalDate setDate;
	LocalDate now;
	int day, month, year;
	private static DateFormat DEFAULT_FORMATTER;

	/**
	 * This is the enum of the different days and the the day of the week they
	 * correspond to.
	 */
	private enum Day {
		monday(1), mon(1), tuesday(2), tue(2), tues(2), wednesday(3), weds(3), wed(
				3), thursday(4), thurs(4), thur(4), friday(5), fri(5), saturday(
				6), sat(6), sunday(7), sun(7);

		private final int dayOfWeek;

		Day(int dayOfWeek) {
			this.dayOfWeek = dayOfWeek;
		}

	}

	/**
	 * This is the enum of the different months and the month of the year they
	 * correspond to.
	 */
	private enum Month {
		janurary(1), jan(1), february(2), feb(2), march(3), april(4), may(5), june(
				6), july(7), august(8), aug(8), september(9), sep(9), sept(9), october(
				10), oct(10), november(11), nov(11), decemeber(12), dec(12);

		private final int monthOfYear;

		Month(int monthOfyear) {
			this.monthOfYear = monthOfyear;
		}
	}

	/**
	 * This is the enum of unique date types not covered in other formats.
	 */
	private enum uniqueDateType {
		today, tomorrow, week, month, year, THIS, weekend;
	}

	/**
	 * This is the constructor for this class that initializes the values.
	 */
	private LocalDate startDate;
	private LocalDate endDate;

	public DateExtractor() {
		setDate = null;
		now = LocalDate.now();
		day = now.getDayOfWeek();
		month = now.getMonthOfYear();
		year = now.getYear();
		startDate = new LocalDate();
		endDate = new LocalDate();
		DEFAULT_FORMATTER = new SimpleDateFormat(DATE_FORMAT);

	}

	private static int counter;
	private static Queue<LocalDate> dateList;

	/**
	 * This is the function to process the date and set the values.
	 * 
	 * @param commandQueue
	 *            This is the queue of date types.
	 * 
	 * @return Returns a local date type with the day,month, year set.
	 */
	public Queue<LocalDate> processDate(String[] parseString) {

		boolean monthFlag = false;
		boolean dayFlag = false;
		boolean yearFlag = false;
		boolean dateFlag = false;
		String dateCommand;
		Queue<String> dateQueue = new LinkedList<String>();
		dateList = new LinkedList<LocalDate>();
		startDate = null;
		endDate = null;
		
		int parameters;

		for (counter = 0; counter < parseString.length; counter++) {
			monthFlag = false;
			dayFlag = false;
			yearFlag = false;
			dateFlag = false;

			if (checkDateFormat(parseString[counter])) {
				dateQueue = setUpDateQueue(parseString);
				while (!dateQueue.isEmpty()) {
					dateCommand = dateQueue.poll();

					if (isInteger(dateCommand)) {
						parameters = Integer.parseInt(dateCommand);

						if (isNumberOfDaysInMonth(parameters) && !dayFlag) {
							day = parameters;
							dayFlag = true;
						}

						else if (isMonthFormatInt(parameters) && !monthFlag) {
							month = parameters;
							monthFlag = true;
						}

						else if (isYearFormat(parameters) && !yearFlag) {
							year = parameters;
							yearFlag = true;
						} else {
							System.out
									.println(ERROR_MESSAGE_NOT_NUMERICAL_DATE);

						}
					}

					else if (isDateStandardFormat(dateCommand) && !dateFlag) {
						setDate(dateCommand);
						dateFlag = true;
					}

					else if (isDateWithMonthSpelled(dateCommand) && !monthFlag) {
						parameters = getMontParameters(dateCommand);
						month = parameters;
						monthFlag = true;
					}

					else if (isDayOfWeek(dateCommand) && !dayFlag) {
						parameters = getDayParameters(dateCommand);
						setDay(parameters);
						dayFlag = true;
					}

					if (isUniqueDateType(dateCommand)) {
						if (dateQueue.size() > 0 && isUniqueDateType(dateQueue.peek())) {
							dateCommand = dateCommand + " " + dateQueue.poll();
						}
						setUniqueDate(dateCommand);
					}
				}

				if (startDate != null) {
					dateList.add(startDate);

					if (endDate != null) {
						dateList.add(endDate);
					}
				} else {

					if (!isdateValid()) {
						rectifyDate();
					}
					setDate = new LocalDate(year, month, day);
					dateList.add(setDate);

				}
			}
		}
		return dateList;

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
	 * @param command
	 *            This is the unique date type name.
	 */
	private void setUniqueDate(String command) {

		dateList = new LinkedList<LocalDate>();
		if (command.equalsIgnoreCase(uniqueDateType.today.name())) {
			startDate = now;

		} else if (command.equalsIgnoreCase(uniqueDateType.tomorrow.name())) {
			startDate = now.plusDays(1);
		} else if (command.equalsIgnoreCase(uniqueDateType.THIS.name() + " "
				+ uniqueDateType.week.name())) {
			startDate = now;
			int numberOfDaysToEndOfWeek = 7 - startDate.getDayOfWeek();
			if (numberOfDaysToEndOfWeek == 0) {
				endDate = startDate.plusDays(startDate.getDayOfWeek() + 7);
			} else {
				endDate = startDate.plusDays(numberOfDaysToEndOfWeek);
			}
		}

		else if (command.equalsIgnoreCase(uniqueDateType.THIS.name() + " "
				+ uniqueDateType.month.name())) {
			startDate = now;
			int numberOfDaysToEndOfMonth = startDate.dayOfMonth()
					.getMaximumValue() - startDate.getDayOfMonth();
			if (numberOfDaysToEndOfMonth == 0) {
				endDate = startDate.plusMonths(1);
			} else {
				endDate = startDate.plusDays(numberOfDaysToEndOfMonth);
			}
		}

		else if (command.equalsIgnoreCase(uniqueDateType.THIS.name() + " "
				+ uniqueDateType.year.name())) {
			startDate = now;
			int numberOfDaysToEndOfYear;
			if (startDate.year().isLeap()) {
				numberOfDaysToEndOfYear = 366 - startDate.getDayOfYear();
			} else {
				numberOfDaysToEndOfYear = 365 - startDate.getDayOfYear();
			}
			if (numberOfDaysToEndOfYear == 0) {
				endDate = startDate.plusYears(1);
			} else {
				endDate = startDate.plusDays(numberOfDaysToEndOfYear);
			}

		}

		else if (command.equalsIgnoreCase(uniqueDateType.THIS.name() + " "
				+ uniqueDateType.weekend.name())) {
			int numberOfDaysToEndOfWeek = 7 - now.getDayOfWeek();
			endDate = now.plusDays(numberOfDaysToEndOfWeek);
			startDate = endDate.minusDays(1);

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

		String dateString = day + "-" + month + "-" + year;
		try {
			DEFAULT_FORMATTER.parse(dateString);
			return true;
		} catch (ParseException e) {
			System.out.println("could not parse " + dateString);
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
			day = (NUMBER_NUMBER_OF_DAYS_IN_A_WEEK - now.getDayOfWeek()
					+ parameters + now.getDayOfMonth());
		} else if (parameters > now.getDayOfWeek()) {
			day = (parameters + now.getDayOfMonth() - 1);
		} else if (parameters == now.getDayOfWeek()) {
			day = (NUMBER_NUMBER_OF_DAYS_IN_A_WEEK + now.getDayOfMonth());
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

		for (Day d : Day.values()) {
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

		for (Month m : Month.values()) {
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
	private void setDate(String command) {

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

		for (uniqueDateType d : uniqueDateType.values()) {
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

		for (Day d : Day.values()) {
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
		for (Month m : Month.values()) {
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
		String dateFormat = REGEX_FULL_DATE_FORMAT;
		Pattern patternDateStandardFormat = Pattern.compile(dateFormat);
		Matcher matcherDateStandardFormat = patternDateStandardFormat
				.matcher(printString);
		if (matcherDateStandardFormat.matches()) {
			return true;
		} else {
			return false;
		}
	}

}
