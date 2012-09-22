import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;


public class TimeExtractor {

	static DateTimeFormatter parseTime = null;
	private static DateTimeZone zone = DateTimeZone.forID("Singapore");
	private static DateTime startTime = new DateTime(zone);

	public static DateTime processTime(String time) {
		if (is24HrFormat(time)) {
			process24hrFormat(time);
		} else if (is12HrFormat(time)) {
			process12HrFormat(time);
		}
		return startTime;

	}

	private static void process24hrFormat(String time) {

	}

	private static void process12HrFormat(String time) {


	}

	private static boolean is12HrFormat(String time) {
		return false;
	}

	private static boolean is24HrFormat(String time) {


		return false;
	}

	public static boolean checkTimeFormat(String time) {

		if (is12HrFormat(time) || is24HrFormat(time)) {
			return true;
		}
		return false;

	}
}
