package mhs.src;

import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	// These are the regex for clearing.
	private static final String REGEX_NON_WORD_CHAR = "\\W";
	private static final String REGEX_SPACE = " ";

	// These are the am and pm formats used in timings.
	private static final String AM = "am";
	private static final String PM = "pm";

	// These are the regex formats of timings.
	private static final String REGEX_24_HOUR_FORMAT = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
	private static final String REGEX_12_HOUR_FORMAT_WITHOUT_MINUTES = "(1[012]|[1-9])(\\s)?(?i)(am|pm)";
	private static final String REGEX_12_HOUR_FORMAT = "(1[012]|[1-9])(.*)([0-5][0-9])(\\s)?(?i)(am|pm)";

	private LocalTime setTime = null;
	private Queue<LocalTime> timeQueue;

	/**
	 * This is the function to extract and set the time.
	 * 
	 * @param parseString
	 *            This is the string to be processed.
	 * 
	 * @return Returns a local time object with the timings set.
	 */
	public Queue<LocalTime> processTime(String[] parseString) {
		timeQueue = new LinkedList<LocalTime>();
		
		for (int i = 0; i < parseString.length; i++) {
			if (checkTimeFormat(parseString[i])) {
				if (is24HrFormat(parseString[i])) {
					process24hrFormat(parseString[i]);
				} else if (is12HrFormat(parseString[i])) {
					process12HrFormat(parseString[i]);
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
		timeArray = time.split(":");
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

		time = time.replaceAll(REGEX_NON_WORD_CHAR, REGEX_SPACE);
		String[] timeArray = new String[2];
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
			} else if (timeArray[i].contains(AM)) {
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
		timeArray[i] = timeArray[i].replace(AM, "");
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
		timeArray[i] = timeArray[i].replace(PM, "");
		hour = Integer.parseInt(timeArray[0]) + 12;
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

		String timeFormat12Hr = REGEX_12_HOUR_FORMAT;
		String timeFormat12HrWithoutMinutes = REGEX_12_HOUR_FORMAT_WITHOUT_MINUTES;

		Pattern patternTimeFormat12Hr = Pattern.compile(timeFormat12Hr);
		Pattern patternTimeFormat12HrWithoutMinutes = Pattern
				.compile(timeFormat12HrWithoutMinutes);
		Matcher matcherTimeFormat12Hr = patternTimeFormat12Hr.matcher(time);
		Matcher matcherTimeFormat12HrWithoutMinutes = patternTimeFormat12HrWithoutMinutes
				.matcher(time);

		if (matcherTimeFormat12Hr.matches()
				|| matcherTimeFormat12HrWithoutMinutes.matches()) {
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

		String timeFormat24Hr = REGEX_24_HOUR_FORMAT;
		Pattern patternTimeFormat24Hr = Pattern.compile(timeFormat24Hr);
		Matcher matcherTimeFormat24Hr = patternTimeFormat24Hr.matcher(time);

		if (matcherTimeFormat24Hr.matches()) {
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
