package mhs.test;
import static org.junit.Assert.assertEquals;

import mhs.src.DateExtractor;
import mhs.src.TimeExtractor;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

public class CommandProcessorTest {
	DateTime now = new DateTime();

	@Before
	public void setUpEnvironment() {
		
		now = DateTime.now();
	}

	@Test
	public void testcheckTimeFormat() {

		assertEquals(TimeExtractor.checkTimeFormat("12pm"), true);
		assertEquals(TimeExtractor.checkTimeFormat("3pm"), true);
		assertEquals(TimeExtractor.checkTimeFormat("4am"), true);
		assertEquals(TimeExtractor.checkTimeFormat("12.40pm"), true);
		assertEquals(TimeExtractor.checkTimeFormat("00:00"), true);
		assertEquals(TimeExtractor.checkTimeFormat("00:12"), true);
		assertEquals(TimeExtractor.checkTimeFormat("15:13"), true);

		assertEquals(TimeExtractor.checkTimeFormat("15pm"), false);
		assertEquals(TimeExtractor.checkTimeFormat("12.60pm"), false);
		assertEquals(TimeExtractor.checkTimeFormat("00:70"), false);
		assertEquals(TimeExtractor.checkTimeFormat("400"), false);
		assertEquals(TimeExtractor.checkTimeFormat("1600"), false);
		assertEquals(TimeExtractor.checkTimeFormat("27:00"), false);
	}

	@Test
	public void testProcessTime() {

		LocalTime expectedTime = null;
		
		expectedTime = new LocalTime(12, 4 );
		assertEquals(TimeExtractor.processTime("12:04"), expectedTime);

		expectedTime = new LocalTime(12, 40 );
		assertEquals(TimeExtractor.processTime("12:40"), expectedTime);

		expectedTime = new LocalTime(0, 0 );
		assertEquals(TimeExtractor.processTime("00:00"), expectedTime);

		expectedTime = new LocalTime(0, 12 );
		assertEquals(TimeExtractor.processTime("00:12"), expectedTime);

		expectedTime = new LocalTime(15, 13 );
		assertEquals(TimeExtractor.processTime("15:13"), expectedTime);

		expectedTime = new LocalTime(15, 13 );
		assertEquals(TimeExtractor.processTime("3.13pm"), expectedTime);

		expectedTime = new LocalTime(4, 0 );
		assertEquals(TimeExtractor.processTime("4am"), expectedTime);
	}

	@Test
	public void testcheckDateFormat() {

		assertEquals(DateExtractor.checkDateFormat("10 10 2010"), true);
		assertEquals(DateExtractor.checkDateFormat("10/5/2012"), true);
		assertEquals(DateExtractor.checkDateFormat("12 jan"), true);
		assertEquals(DateExtractor.checkDateFormat("3 maRcH 2014"), true);
		assertEquals(DateExtractor.checkDateFormat("monday"), true);
		assertEquals(DateExtractor.checkDateFormat("10 5"), true);
		assertEquals(DateExtractor.checkDateFormat("5"), true);
		assertEquals(DateExtractor.checkDateFormat("5/5"), true);

	}
}