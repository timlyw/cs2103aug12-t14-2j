package mhs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import mhs.src.GoogleCalendar;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.util.ServiceException;

public class GoogleCalendarTest {

	private static final String TEST_EVENT_UPDATED_NAME = "Test Event Updated Name";
	private static final String TEST_EVENT_NAME = "Test Event";
	GoogleCalendar googleCalendar;

	@Before
	public void testGoogleCalendarSetup() throws IOException, ServiceException {
		googleCalendar = new GoogleCalendar();
	}

	@Test
	public void testGoogleCalendar() throws IOException, ServiceException {

		new DateTime();
		DateTime currentDateTime = DateTime.now();
		currentDateTime = currentDateTime.minusMillis(currentDateTime
				.getMillisOfSecond());

		String testStartTime = currentDateTime.toString();
		String testEndTime = currentDateTime.plusHours(1).toString();

		// Test Add
		CalendarEventEntry testEvent = googleCalendar.createEvent(
				TEST_EVENT_NAME, testStartTime, testEndTime);

		assertEquals(TEST_EVENT_NAME, testEvent.getTitle().getPlainText());
		assertEquals(testStartTime, testEvent.getTimes().get(0).getStartTime()
				.toString());
		assertEquals(testEndTime, testEvent.getTimes().get(0).getEndTime()
				.toString());

		// Test Update
		String testUpdatedStartTime = currentDateTime.plusMinutes(5).toString();
		String testUpdatedEndTime = currentDateTime.plusHours(1).plusMinutes(5)
				.toString();

		testEvent = googleCalendar.updateEvent(testEvent.getIcalUID(),
				TEST_EVENT_UPDATED_NAME, testUpdatedStartTime,
				testUpdatedEndTime);

		assertEquals(TEST_EVENT_UPDATED_NAME, testEvent.getTitle()
				.getPlainText());
		assertEquals(testUpdatedStartTime, testEvent.getTimes().get(0)
				.getStartTime().toString());
		assertEquals(testUpdatedEndTime, testEvent.getTimes().get(0)
				.getEndTime().toString());

		// Test Delete
		googleCalendar.deleteEvent(testEvent.getIcalUID());

		googleCalendar.pullEvents();
		testEvent = googleCalendar.getEvent(testEvent.getIcalUID());

		assertTrue(googleCalendar.isDeleted(testEvent));
		
	}

	@After
	public void testGoogleCalendarClean() throws IOException, ServiceException {

		googleCalendar.pullEvents();
		googleCalendar.deleteAllEvents();

		System.out.println(System.lineSeparator());
	}
}
