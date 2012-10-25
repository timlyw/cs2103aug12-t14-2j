package mhs.src.logic;

import java.util.LinkedList;
import java.util.Queue;

import org.joda.time.LocalTime;

/**
 * 
 * @author Cheong Kahou
 *A0086805X
 */

/**
 * This is the class to extract the time from the string.
 */
public class TimeExtractor {

	private static final String REGEX_COLON = ":";
	// These are the am and pm formats used in timings.
	private static final String AM = "am";
	private static final String PM = "pm";

	// These are the regex formats of timings.
	private static final String REGEX_24_HOUR_FORMAT = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
	private static final String REGEX_12_HOUR_FORMAT_WITHOUT_MINUTES = "(1[012]|[1-9])(\\s)?(?i)(am|pm)";
	private static final String REGEX_12_HOUR_FORMAT = "(1[012]|[1-9])(.)([0-5][0-9])(\\s)?(?i)(am|pm)";
	private static final String REGEX_PM_IGNORE_CASE = "(?i)pm";
	private static final String REGEX_AM_IGNORE_CASE = "(?i)am";
	
	// These are the regex for clearing.
	private static final String REGEX_NON_WORD_CHAR = "\\W";
	private static final String REGEX_SPACE = " ";
	private static final String REGEX_WHITE_SPACE = "\\s+";

	private LocalTime setTime;
	private Queue<LocalTime> timeQueue;
	private static TimeExtractor timeExtractor;
	
	private TimeExtractor(){
		setTime = null;
		timeQueue = new LinkedList<LocalTime>();
	}

	public static TimeExtractor getTimeExtractor(){
		if(timeExtractor == null){
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
	public Queue<LocalTime> processTime(String parseString) {
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

		return timeQueue;
	}


	/**
	 * This is the function to process 24hr format timing.
	 * 
	 * @param time
	 *            This is the string to process.
	 */
	private void process24hrFormat(String time) {

		String[] timeArray = new String[2];
		timeArray = time.split(REGEX_COLON);
		setTime = new LocalTime(Integer.parseInt(timeArray[0]),
				Integer.parseInt(timeArray[1]));

	}

	/**
	 * This is the function to process 12hr format timing.
	 * 
	 * @param time
	 *            This is the string to process.
	 */
	private void process12HrFormat(String time) {

		String[] timeArray = new String[2];
		time = time.replaceAll(REGEX_NON_WORD_CHAR, REGEX_SPACE);
		timeArray = time.split(REGEX_SPACE);

		int hour, minute;
		hour = extractHour(timeArray);
		minute = extractMinute(timeArray);

		setTime = new LocalTime(hour, minute);

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
		int minute = 0;
		if (timeArray.length > 1) {
			minute = Integer.parseInt(timeArray[1]);
		}
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
		int hour = 0;
		for (int i = 0; i < timeArray.length; i++) {
			if (timeArray[i].contains(PM)) {
				hour = extractHourPM(timeArray, i);
			} else if (timeArray[i].toLowerCase().contains(AM)) {
				hour = extractHourAM(timeArray, i);
			}
		}
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
		int hour;
		timeArray[i] = timeArray[i].replaceAll(REGEX_AM_IGNORE_CASE, "");
		hour = Integer.parseInt(timeArray[0]);
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
		int hour;
		timeArray[i] = timeArray[i].replaceAll(REGEX_PM_IGNORE_CASE, "");
		hour = Integer.parseInt(timeArray[0]);
		if (hour == 12) {
			hour = 0;
		} else {
			hour += 12;
		}
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

		if (time.matches(REGEX_12_HOUR_FORMAT) ||time.matches(REGEX_12_HOUR_FORMAT_WITHOUT_MINUTES)) {
			return true;
		}

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

		if (time.matches(REGEX_24_HOUR_FORMAT)) {
			return true;
		}
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

		if (is12HrFormat(time) || is24HrFormat(time)) {
			return true;
		}
		return false;

	}
}
