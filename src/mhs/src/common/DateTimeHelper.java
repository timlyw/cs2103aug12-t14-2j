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

	private static DateTimeHelper instance = null;

	/**
	 * Getter for DateTimerHelper instance
	 * 
	 * @return instance of DateTimeHelper
	 */
	public static DateTimeHelper getInstance() {
		if (instance == null) {
			instance = new DateTimeHelper();
		}
		return instance;
	}

	private static final String REGEX_SPACE = " ";
	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * Method to format datetime object to a default string format.
	 * @param formatDate DateTime object to be formatted.
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
	 * @param formatDate DateTime object to be formatted.
	 * 
	 * @return Returns the formatted string.
	 */
	public String formatDateToString(DateTime formatDate) {

		logEnterMethod("formatDateToString");
		String outString;
		outString = formatDate.toString("dd MMM yyyy ");
		new DateTime();
		DateTime now = DateTime.now();
		if (formatDate.getDayOfMonth() == now.getDayOfMonth()
				&& formatDate.getMonthOfYear() == now.getMonthOfYear()
				&& formatDate.getYear() == now.getYear()) {
			outString += "Today";
		} else if (formatDate.getDayOfMonth() == now.getDayOfMonth() + 1
				&& formatDate.getMonthOfYear() == now.getMonthOfYear()
				&& formatDate.getYear() == now.getYear()) {
			outString += "Tomorrow";
		} else {
			if (formatDate.getDayOfWeek() == 1) {
				outString += "Mon";
			}
			if (formatDate.getDayOfWeek() == 2) {
				outString += "Tues";
			}
			if (formatDate.getDayOfWeek() == 3) {
				outString += "Weds";
			}
			if (formatDate.getDayOfWeek() == 4) {
				outString += "Thurs";
			}
			if (formatDate.getDayOfWeek() == 5) {
				outString += "Fri";
			}
			if (formatDate.getDayOfWeek() == 6) {
				outString += "Sat";
			}
			if (formatDate.getDayOfWeek() == 7) {
				outString += "Sun";
			}
		}
		logExitMethod("formatDateToString");

		return outString;
	}

	/**
	 * Method to format datetime object to a default time string format.
	 * @param formatDate DateTime object to be formatted.
	 * 
	 * @return Returns the formatted string.
	 */
	public String formatTimeToString(DateTime formatTime) {
		logEnterMethod("formatTimeToString");

		String outString;
		if (formatTime.getMinuteOfHour() == 0)
			outString = formatTime.toString("hh aa");
		else
			outString = formatTime.toString("hh mm aa");

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
