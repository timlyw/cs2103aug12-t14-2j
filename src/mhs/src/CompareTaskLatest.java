package mhs.src;

import java.util.Comparator;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

public class CompareTaskLatest implements Comparator<DateTime> {
	//TODO use jodatime datetime comparator
	@Override
	public int compare(DateTime o1, DateTime o2) {
		// TODO Auto-generated method stub
		if (o1.isAfter(o2)) {
			return -1;
		} else if (o1.isBefore(o2)) {
			return 1;
		}
		return 0;
	}
}