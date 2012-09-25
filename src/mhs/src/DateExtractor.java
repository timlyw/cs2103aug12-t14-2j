package mhs.src;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;

public class DateExtractor {

	private static Date newDate;

	static LocalDate startDate = null;

	private enum Day {
		monday(1), mon(1), tuesday(2), tue(2), tues(2), wednesday(3), weds(3), wed(
				3), thursday(4), thurs(4), thur(4), friday(5), fri(5), saturday(
				6), sat(6), sunday(7), sun(7);

		private final int dayOfWeek;

		Day(int dayOfWeek) {
			this.dayOfWeek = dayOfWeek;
		}

	}

	private enum Month {
		janurary(1), jan(1), february(2), feb(2), march(3), april(4), may(5), june(
				6), july(7), august(8), aug(8), september(9), sep(9), sept(9), october(
				10), oct(10), november(11), nov(11), decemeber(12), dec(12);

		private final int monthOfYear;

		Month(int monthOfyear) {
			this.monthOfYear = monthOfyear;
		}
	}

	public DateExtractor() {
		newDate = new Date(true);
	}

	public LocalDate processDate(Queue<String> commandQueue) {

		boolean monthFlag = false;
		boolean dayFlag = false;
		boolean yearFlag = false;
		boolean dateFlag = false;


		String command;
		int parameters;

		while (!commandQueue.isEmpty()) {
			command = commandQueue.poll();

			if (isInteger(command)) {
				parameters = Integer.parseInt(command);

				if (isNumberOfDaysInMonth(parameters) && !dayFlag) {
					newDate.setDay(parameters);
					dayFlag = true;
				}

				else if (isMonthFormatInt(parameters) && !monthFlag) {
					newDate.setMonth(parameters);
					monthFlag = true;
				}

				else if (isYearFormat(parameters) && !yearFlag) {
					newDate.setYear(parameters);
					yearFlag = true;
				} else {
					System.out.println("error not numerical date!");

				}
			}

			else if (isDateStandardFormat(command) && !dateFlag) {
				setDate(command);

				dateFlag = true;
			}

			else if (isDateWithMonthSpelled(command) && !monthFlag) {
				parameters = getMontParameters(command);
				newDate.setMonth(parameters);
				monthFlag = true;
			}

			else if (isDayOfWeek(command) && !dayFlag) {
				parameters = getDayParameters(command);
				setDay(parameters);
				dayFlag = true;
			}

		}
		if (newDate.DateValidator()) {
			startDate = new LocalDate(newDate.getYear(), newDate.getMonth(),
					newDate.getDay());

		} else {
			System.out.println("Error! date does not exist!");
		}
		return startDate;

	}

	private static void setDay(int parameters) {

		if (parameters < newDate.getCurrentDayOfWeek()) {
			newDate.setDay((7 - newDate.getCurrentDayOfWeek() + parameters + newDate
					.getCurrentDayOfMonth()));
		} else if (parameters > newDate.getCurrentDayOfWeek()) {
			newDate.setDay(parameters + newDate.getCurrentDayOfMonth() - 1);
		} else if (parameters == newDate.getCurrentDayOfWeek()) {
			newDate.setDay(7 + newDate.getCurrentDayOfMonth());
		}
	}

	private static int getDayParameters(String command) {

		int dayOfWeek = 0;

		for (Day d : Day.values()) {
			if (command.equals(d.name())) {
				dayOfWeek = d.dayOfWeek;
			}
		}
		return dayOfWeek;
	}

	private static int getMontParameters(String command) {

		for (Month m : Month.values()) {
			if (command.equals(m.name())) {
				return m.monthOfYear;
			}
		}
		return 0;
	}

	private static void setDate(String command) {

		int[] dateParameters = new int[3];
		String[] dateArray = command.split("\\W");

		for (int i = 0; i < dateArray.length; i++) {
			dateParameters[i] = Integer.parseInt(dateArray[i]);
		}

		newDate.setDay(dateParameters[0]);
		newDate.setMonth(dateParameters[1]);
		if (dateParameters[2] != 0) {
			newDate.setYear(dateParameters[2]);
		}
	}

	private static boolean isNumberOfDaysInMonth(int number) {
		if (number > 0 && number < 32) {
			return true;
		}
		return false;
	}

	private static boolean isMonthFormatInt(int number) {
		if (number > 0 && number < 13) {
			return true;
		}
		return false;
	}

	private static boolean isYearFormat(int number) {

		int year = newDate.getCurrentYear();
		if (number >= year && number < 9999) {
			return true;
		}
		return false;
	}

	public static boolean checkDateFormat(String printString) {
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

		return false;
	}


	private static boolean isInteger(String printString) {
		try {
			Integer.parseInt(printString);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isDayOfWeek(String printString) {

		for (Day d : Day.values()) {
			if (printString.equals(d.name())) {
				return true;
			}
		}
		return false;

	}

	private static boolean isDateWithMonthSpelled(String printString) {
		for (Month m : Month.values()) {
			if (printString.equals(m.name())) {
				return true;
			}
		}
		return false;

	}

	private static boolean isDateStandardFormat(String printString) {
		String dateFormat = "(0?[1-9]|[12][0-9]|3[01])(/|-)(0?[1-9]|1[012])(.*)(((20)\\d\\d)?)";
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
