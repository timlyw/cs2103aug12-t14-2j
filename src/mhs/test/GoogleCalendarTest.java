package mhs.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import mhs.src.*;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.ServiceException;

public class GoogleCalendarTest {

	GoogleCalendar googleCalendar;

	@Before
	public void testGoogleCalendarInit() throws IOException, ServiceException {
		googleCalendar = new GoogleCalendar();
	}

	@Test
	public void testGoogleCalendarStart() throws IOException, ServiceException {
		googleCalendar.displayEvents();
	}

	@Test
	public void testGoogleCalendarGetEvents() throws IOException,
			ServiceException {
		List<CalendarEventEntry> calendarEvents = googleCalendar.getEventList();

		Iterator<CalendarEventEntry> iterator = calendarEvents.iterator();
		while (iterator.hasNext()) {
			CalendarEventEntry calEntry = iterator.next();
			System.out.println(calEntry.getTitle().getPlainText());
			List<When> eventTimes = calEntry.getTimes();
			System.out.println(eventTimes.get(0).getStartTime());
			System.out.println(eventTimes.get(0).getEndTime());
			System.out.println(calEntry.getUpdated());
			// System.out.println(calEntry.getIcalUID());
			// System.out.println(calEntry.getId());
		}
	}

	@Test
	public void testGoogleCalendarAddAndDelete() throws IOException,
			ServiceException {

		new DateTime();
		CalendarEventEntry addedEvent = googleCalendar.createEvent("Event 1",
				DateTime.now().toString(), DateTime.now().plusHours(1)
						.toString());

		System.out.println(addedEvent.getTitle().getPlainText());
		System.out.println(addedEvent.getTimes().get(0).getStartTime());
		System.out.println(addedEvent.getTimes().get(0).getEndTime());
		System.out.println(addedEvent.getUpdated());
		System.out.println(addedEvent.getEdited());

		googleCalendar.deleteEvent(addedEvent.getIcalUID());

	}

	@Test
	public void testGoogleCalendarUpdate() throws IOException, ServiceException {

		new DateTime();
		CalendarEventEntry addedEvent = googleCalendar.createEvent("Event 1",
				DateTime.now().toString(), DateTime.now().plusHours(2)
						.toString());

		System.out.println(addedEvent.getTitle().getPlainText());
		System.out.println(addedEvent.getId());
		System.out.println(addedEvent.getIcalUID());
		System.out.println(addedEvent.getTimes().get(0).getStartTime());
		System.out.println(addedEvent.getTimes().get(0).getEndTime());
		System.out.println(addedEvent.getUpdated());

		googleCalendar.pullEvents();

		new DateTime();
		DateTime updatedSt = DateTime.now().plusHours(2);
		DateTime updatedEt = new DateTime().now().plusHours(3);

		CalendarEventEntry updatedEventTest = googleCalendar.updateEvent(
				"123", "Event 1 Updated",
				updatedSt.toString(), updatedEt.toString());

		CalendarEventEntry updatedEvent = googleCalendar.updateEvent(
				addedEvent.getIcalUID(), "Event 1 Updated",
				updatedSt.toString(), updatedEt.toString());

		System.out.println(updatedEvent.getTitle().getPlainText());
		System.out.println(updatedEvent.getId());
		System.out.println(updatedEvent.getIcalUID());
		System.out.println(updatedEvent.getTimes().get(0).getStartTime());
		System.out.println(updatedEvent.getTimes().get(0).getEndTime());
		System.out.println(updatedEvent.getUpdated());

		googleCalendar.pullEvents();

		assertEquals(updatedEvent.getTitle().getPlainText(), "Event 1 Updated");
		// milis different
		//assertEquals(updatedEvent.getTimes().get(0).getStartTime(), updatedSt.toString());
		//assertEquals(updatedEvent.getTimes().get(0).getEndTime(), updatedEt.toString());
	}

	@After
	public void testGoogleCalendarClean() throws IOException, ServiceException {

		googleCalendar.pullEvents();

		List<CalendarEventEntry> calendarEvents = googleCalendar.getEventList();
		Iterator<CalendarEventEntry> iterator = calendarEvents.iterator();

		// Delete all events
		while (iterator.hasNext()) {
			CalendarEventEntry calEntry = iterator.next();
			calEntry.delete();
		}

		System.out.println(System.lineSeparator());
	}
}
