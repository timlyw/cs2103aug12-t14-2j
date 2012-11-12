//@author A0086805X

package mhs.src.common;

import java.util.logging.Logger;

import org.joda.time.DateTime;

/**
 * DateTimeHelper
 * 
 * Provides DateTime helper functions
 * 
 */
public class DateTimeHelper {

	// Strings related to formatting.
	private static final String FORMAT_DATE = "dd MMM yyyy ";
	private static final String FORMAT_TIME = "hh mm aa";
	private static final String FORMAT_TIME_NO_MINUTES = "hh aa";

	// Strings related to days.
	private static final String DAY_SUNDAY = "Sun";
	private static final String DAY_SATURDAY = "Sat";
	private static final String DAY_FRIDAY = "Fri";
	private static final String DAY_THURSDAY = "Thurs";
	private static final String DAY_WEDSDAY = "Weds";
	private static final String DAY_TUESDAY = "Tues";
	private static final String DAY_MONDAY = "Mon";
	private static final String DAY_TOMORROW = "Tomorrow";
	private static final String DAY_TODAY = "Today";

	private static final String REGEX_SPACE = " ";
	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * Method to format datetime object to a default string format.
	 * 
	 * @param formatDate
	 *            DateTime object to be formatted.
	 * 
	 * @return Returns the formatted string.
	 */
	public String formatDateTimeToString(DateTime formatDate) {
		logEnterMethod("formatDateTimeToString");

		String outString;
		outString = formatDateToString(formatDate) + REGEX_SPACE
				+ formatTimeToString(formatDate);
		logExitMethod("formatDateTimeToString");

		return outString;
	}

	/**
	 * Method to format datetime object to a default date string format.
	 * 
	 * @param formatDate
	 *            DateTime object to be formatted.
	 * 
	 * @return Returns the formatted string.
	 */
	public String formatDateToString(DateTime formatDate) {

		logEnterMethod("formatDateToString");
		String outString;
		outString = formatDate.toString(FORMAT_DATE);
		new DateTime();
		DateTime now = DateTime.now();
		if (formatDate.getDayOfMonth() == now.getDayOfMonth()
				&& formatDate.getMonthOfYear() == now.getMonthOfYear()
				&& formatDate.getYear() == now.getYear()) {
			outString += DAY_TODAY;
		} else if (formatDate.getDayOfMonth() == now.getDayOfMonth() + 1
				&& formatDate.getMonthOfYear() == now.getMonthOfYear()
				&& formatDate.getYear() == now.getYear()) {
			outString += DAY_TOMORROW;
		} else {
			if (formatDate.getDayOfWeek() == 1) {
				outString += DAY_MONDAY;
			}
			if (formatDate.getDayOfWeek() == 2) {
				outString += DAY_TUESDAY;
			}
			if (formatDate.getDayOfWeek() == 3) {
				outString += DAY_WEDSDAY;
			}
			if (formatDate.getDayOfWeek() == 4) {
				outString += DAY_THURSDAY;
			}
			if (formatDate.getDayOfWeek() == 5) {
				outString += DAY_FRIDAY;
			}
			if (formatDate.getDayOfWeek() == 6) {
				outString += DAY_SATURDAY;
			}
			if (formatDate.getDayOfWeek() == 7) {
				outString += DAY_SUNDAY;
			}
		}
		logExitMethod("formatDateToString");

		return outString;
	}

	/**
	 * Method to format datetime object to a default time string format.
	 * 
	 * @param formatDate
	 *            DateTime object to be formatted.
	 * 
	 * @return Returns the formatted string.
	 */
	public String formatTimeToString(DateTime formatTime) {
		logEnterMethod("formatTimeToString");

		String outString;
		if (formatTime.getMinuteOfHour() == 0)
			outString = formatTime.toString(FORMAT_TIME_NO_MINUTES);
		else
			outString = formatTime.toString(FORMAT_TIME);

		logExitMethod("formatTimeToString");

		return outString;
	}

	/**
	 * Logger exit method
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
