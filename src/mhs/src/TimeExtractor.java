package mhs.src;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

public class TimeExtractor {

	private static final String AM = "am";

	private static final String PM = "pm";

	private static final String REGEX_24_HOUR_FORMAT = "([01]?[0-9]|2[0-3]):[0-5][0-9]";

	private static final String REGEX_12_HOUR_FORMAT_WITHOUT_MINUTES = "(1[012]|[1-9])(\\s)?(?i)(am|pm)";

	private static final String REGEX_12_HOUR_FORMAT = "(1[012]|[1-9])(.*)([0-5][0-9])(\\s)?(?i)(am|pm)";

	static DateTimeFormatter parseTime = null;

	static DateTimeFieldType[] types = {
	            DateTimeFieldType.hourOfDay(),
	            DateTimeFieldType.minuteOfHour(),
	   };
		
		private static LocalTime startTime = null;
		
	public static LocalTime processTime(String time) {
		if (is24HrFormat(time)) {
			process24hrFormat(time);
		} else if (is12HrFormat(time)) {
			process12HrFormat(time);
		}
		return startTime;
	}

	private static void process24hrFormat(String time) {

		String[] timeArray = new String[2];
		timeArray = time.split(":");
		startTime = new LocalTime(Integer.parseInt(timeArray[0]),Integer.parseInt(timeArray[1]));

	}

	private static void process12HrFormat(String time) {

		time = time.replaceAll("\\W", " ");

		String[] timeArray = new String[2];
		timeArray = time.split(" ");

		int hour=0, minute=0;
		for(int i =0 ; i<timeArray.length; i ++){
			if(timeArray[i].contains(PM)){
				timeArray[i] = timeArray[i].replace(PM, "");
				hour = Integer.parseInt(timeArray[0]) + 12;
				}
			else if(timeArray[i].contains(AM)){
				timeArray[i] = timeArray[i].replace(AM, "");
				hour = Integer.parseInt(timeArray[0]);
			}
		}
		if(timeArray.length > 1){
			minute = Integer.parseInt(timeArray[1]);
		}
		
		startTime = new LocalTime(hour, minute);

	}

	private static boolean is12HrFormat(String time) {

		String timeFormat12Hr = REGEX_12_HOUR_FORMAT;
		String timeFormat12HrWithoutMinutes = REGEX_12_HOUR_FORMAT_WITHOUT_MINUTES;
		
		Pattern patternTimeFormat12Hr = Pattern.compile(timeFormat12Hr);
		Pattern patternTimeFormat12HrWithoutMinutes = Pattern.compile(timeFormat12HrWithoutMinutes);
		Matcher matcherTimeFormat12Hr = patternTimeFormat12Hr.matcher(time);
		Matcher matcherTimeFormat12HrWithoutMinutes = patternTimeFormat12HrWithoutMinutes.matcher(time);
		if (matcherTimeFormat12Hr.matches() || matcherTimeFormat12HrWithoutMinutes.matches()) {
			return true;
		}
		return false;
	}

	private static boolean is24HrFormat(String time) {

		String timeFormat24Hr = REGEX_24_HOUR_FORMAT;
		Pattern patternTimeFormat24Hr = Pattern.compile(timeFormat24Hr);
		Matcher matcherTimeFormat24Hr = patternTimeFormat24Hr.matcher(time);

		if (matcherTimeFormat24Hr.matches()) {
			return true;
		}
		return false;
	}

	public static boolean checkTimeFormat(String time) {

		if (is12HrFormat(time) || is24HrFormat(time)) {
			return true;
		}
		return false;

	}
}
