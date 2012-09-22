import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


public class Date {
	//add today tomorrow
		private int Day;
		private int Month;
		private int Year;

		private static DateFormat DEFAULT_FORMATTER = new SimpleDateFormat(
				"dd-MM-yyyy");
		private DateTimeZone zone = DateTimeZone.forID("Singapore");
		private DateTime dt = new DateTime(zone);

		public Date(boolean currentTime) {
			if (currentTime) {
				Day = dt.getDayOfMonth();
				Month = dt.getMonthOfYear();
				Year = dt.getYear();

			} else {
				Day = 0;
				Month = 0;
				Year = 0;

			}
		}

		public boolean DateValidator() {

			DEFAULT_FORMATTER.setLenient(false);

			String dateString = Day + "-" + Month +"-" + Year;
			try {
				System.out.println("arg: " + dateString + " date: "
						+ DEFAULT_FORMATTER.parse(dateString));
				return true;
			} catch (ParseException e) {
				System.out.println("could not parse " + dateString);
			}
			return false;
		}

		public void setDay(int day) {
			Day = day;
			System.out.println("day " + day + " set");
		}

		public void setMonth(int month) {
			Month = month;
			System.out.println("month " + month + " set");
		}

		public void setYear(int year) {
			Year = year;
			System.out.println("year " + year + " set");
		}

		public int getCurrentYear() {

			return dt.getYear();
		}

		public int getCurrentMonth() {

			return dt.getMonthOfYear();
		}

		public int getCurrentDayOfWeek() {
			return dt.getDayOfWeek();
		}

		public int getCurrentDayOfMonth() {
			return dt.getDayOfMonth();
		}

		public void printString() {
			System.out.println(Day + " " + Month + " " + Year);
		}
		public int getDay(){
			return Day;
		}

		public int getMonth(){
			return Month;
		}

		public int getYear(){
			return Year;
		}

}
