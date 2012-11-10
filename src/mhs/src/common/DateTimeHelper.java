package mhs.src.common;

import org.joda.time.DateTime;

public class DateTimeHelper {

	private static final String REGEX_SPACE = " ";

	public String formatDateTimeToString(DateTime formatDate) {
		String outString;
		outString = formatDateToString(formatDate) + REGEX_SPACE
				+ formatTimeToString(formatDate);
		return outString;
	}

	public String formatDateToString(DateTime formatDate) {

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
				outString += "Sub";
			}
		}
		return outString;
	}

	public String formatTimeToString(DateTime formatTime) {
		String outString;
		if (formatTime.getMinuteOfHour() == 0)
			outString = formatTime.toString("hh aa");
		else
			outString = formatTime.toString("hh mm aa");

		return outString;
	}
}
