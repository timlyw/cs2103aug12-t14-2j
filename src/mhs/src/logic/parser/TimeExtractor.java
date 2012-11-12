//@author A0086805X
package mhs.src.logic.parser;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

import org.joda.time.LocalTime;

/**
 * This is the class to extract the time from the string.
 */
public class TimeExtractor {

	// These are the am and pm formats used in timings.
	private static final String AM = "am";
	private static final String PM = "pm";

	// These are the regex formats of timings.
	private static final String REGEX_24_HOUR_FORMAT = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
	private static final String REGEX_12_HOUR_FORMAT_WITHOUT_MINUTES = "(1[012]|[1-9])(\\s)?(?i)(am|pm)";
	private static final String REGEX_12_HOUR_FORMAT = "(1[012]|[1-9])(.)([0-5][0-9])(\\s)?(?i)(am|pm)";
	private static final String REGEX_PM_IGNORE_CASE = "(?i)pm";
	private static final String REGEX_AM_IGNORE_CASE = "(?i)am";
	private static final String REGEX_COLON = ":";

	// These are the regex for clearing.
	private static final String REGEX_NON_WORD_CHAR = "\\W";
	private static final String REGEX_SPACE = " ";
	private static final String REGEX_WHITE_SPACE = "\\s+";

	private LocalTime setTime;
	private Queue<LocalTime> timeQueue;
	private static TimeExtractor timeExtractor;

	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * This is the constructor for TimeExtractor.
	 */
	private TimeExtractor() {
		logEnterMethod("TimeExtractor");

		setTime = null;
		timeQueue = new LinkedList<LocalTime>();

		logExitMethod("TimeExtractor");
	}

	public static TimeExtractor getTimeExtractor() {
		if (timeExtractor == null) {
			timeExtractor = new TimeExtractor();
		}
		return timeExtractor;
	}

	/**
	 * This is the function to extract and set the time.
	 * 
	 * @param processArray
	 *            This is the string to be processed.
	 * 
	 * @return Returns a local time object with the timings set.
	 */
	public Queue<LocalTime> extractTime(String parseString) {
		logEnterMethod("processTime");
		if (parseString == null) {
			return null;
		}
		setTime = null;
		timeQueue = new LinkedList<LocalTime>();
		try {
			timeQueue = new LinkedList<LocalTime>();
			String[] processArray = parseString.split(REGEX_WHITE_SPACE);

			for (int i = 0; i < processArray.length; i++) {

				if (checkTimeFormat(processArray[i])) {
					if (is24HrFormat(processArray[i])) {
						process24hrFormat(processArray[i]);
					} else if (is12HrFormat(processArray[i])) {
						process12HrFormat(processArray[i]);
					}
					timeQueue.add(setTime);
				}
			}
		} catch (NullPointerException e) {
			logger.log(Level.FINER, e.getMessage());
			return timeQueue;
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.log(Level.FINER, e.getMessage());
			return timeQueue;
		}
		logExitMethod("processTime");
		return timeQueue;
	}

	/**
	 * This is the function to process 24hr format timing.
	 * 
	 * @param time
	 *            This is the string to process.
	 */
	private void process24hrFormat(String time) {
		logEnterMethod("process24hrFormat");
		assert(time != null);
		String[] timeArray = new String[2];
		timeArray = time.split(REGEX_COLON);
		try {
			setTime = new LocalTime(Integer.parseInt(timeArray[0]),
					Integer.parseInt(timeArray[1]));
		} catch (InvalidParameterException e) {
			logger.log(Level.FINER, e.getMessage());
		}
		logExitMethod("process24hrFormat");
	}

	/**
	 * This is the function to process 12hr format timing.
	 * 
	 * @param time
	 *            This is the string to process.
	 */
	private void process12HrFormat(String time) {
		logEnterMethod("process12HrFormat");
		assert(time != null);
		String[] timeArray = new String[2];
		time = time.replaceAll(REGEX_NON_WORD_CHAR, REGEX_SPACE);
		timeArray = time.split(REGEX_SPACE);

		int hour, minute;
		hour = extractHour(timeArray);
		minute = extractMinute(timeArray);
		try {
			setTime = new LocalTime(hour, minute);
		} catch (InvalidParameterException e) {
			logger.log(Level.FINER, e.getMessage());
		}
		logExitMethod("process12HrFormat");
	}

	/**
	 * This is the function to extract the minutes from a time array.
	 * 
	 * @param timeArray
	 *            This is an array with time format.
	 * 
	 * @return Returns the minutes.
	 */
	private int extractMinute(String[] timeArray) {
		logEnterMethod("extractMinute");
		assert(timeArray != null);
		int minute = 0;
		if (timeArray.length > 1) {
			minute = Integer.parseInt(timeArray[1]);
		}
		logExitMethod("extractMinute");
		return minute;
	}

	/**
	 * This is a function to extract the hours from a time array.
	 * 
	 * @param timeArray
	 *            This is an array with the time format.
	 * 
	 * @return Returns the hours.
	 */
	private int extractHour(String[] timeArray) {
		logEnterMethod("extractHour");
		assert(timeArray != null);
		int hour = 0;
		for (int i = 0; i < timeArray.length; i++) {
			if (timeArray[i].toLowerCase().contains(PM)) {
				hour = extractHourPM(timeArray, i);
			} else if (timeArray[i].toLowerCase().contains(AM)) {
				hour = extractHourAM(timeArray, i);
			}
		}
		logExitMethod("extractHour");
		return hour;
	}

	/**
	 * This is the function to set the hour when the time is AM.
	 * 
	 * @param timeArray
	 *            This is an array with the time format.
	 * 
	 * @param i
	 *            This is the index to where AM is.
	 * 
	 * @return Returns the hour.
	 */
	private int extractHourAM(String[] timeArray, int i) {
		logEnterMethod("extractHourAM");
		assert(timeArray != null);
		int hour;
		timeArray[i] = timeArray[i].replaceAll(REGEX_AM_IGNORE_CASE, "");
		hour = Integer.parseInt(timeArray[0]);
		if (hour == 12) {
			hour = 0;
		}
		logExitMethod("extractHourAM");
		return hour;
	}

	/**
	 * This is the function to set the hour when the time is PM.
	 * 
	 * @param timeArray
	 *            This is an array with the time format.
	 * 
	 * @param i
	 *            This is the index to where PM is.
	 * 
	 * @return Returns the appended hour.
	 */
	private int extractHourPM(String[] timeArray, int i) {
		logEnterMethod("extractHourPM");
		assert(timeArray != null);
		int hour;
		timeArray[i] = timeArray[i].replaceAll(REGEX_PM_IGNORE_CASE, "");
		hour = Integer.parseInt(timeArray[0]);
		if (hour == 12) {
			hour = 12;
		} else {
			hour += 12;
		}
		logExitMethod("extractHourPM");
		return hour;
	}

	/**
	 * This is the function check if a string is in the 12hr format.
	 * 
	 * @param time
	 *            This is the string to be checked.
	 * 
	 * @return Returns true if valid.
	 */
	private boolean is12HrFormat(String time) {
		logEnterMethod("is12HrFormat");
		if (time.matches(REGEX_12_HOUR_FORMAT)
				|| time.matches(REGEX_12_HOUR_FORMAT_WITHOUT_MINUTES)) {
			logExitMethod("is12HrFormat");
			return true;
		}
		logExitMethod("is12HrFormat");
		return false;
	}

	/**
	 * This is the format to check if a string is in the 24hr format.
	 * 
	 * @param time
	 *            This is the string to be checked.
	 * 
	 * @return Returns true if valid.
	 */
	private boolean is24HrFormat(String time) {
		logEnterMethod("is24HrFormat");

		if (time.matches(REGEX_24_HOUR_FORMAT)) {
			logExitMethod("is24HrFormat");
			return true;
		}
		logExitMethod("is24HrFormat");
		return false;
	}

	/**
	 * This is the function to check if a string is of a time format.
	 * 
	 * @param time
	 *            This is the string to be checked.
	 * 
	 * @return Returns true if valid.
	 */
	public boolean checkTimeFormat(String time) {
		logEnterMethod("checkTimeFormat");
		assert (time != null);
		if (is12HrFormat(time) || is24HrFormat(time)) {
			logExitMethod("checkTimeFormat");
			return true;
		}
		logExitMethod("checkTimeFormat");
		return false;

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
