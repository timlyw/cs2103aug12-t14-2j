package mhs.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import mhs.src.GoogleCalendar;

import org.junit.Before;
import org.junit.Test;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.ServiceException;

public class GoogleCalendarTester {

	@Before
	public void testGoogleCalendarInit() throws IOException, ServiceException {
		GoogleCalendar googleCalendar = new GoogleCalendar();

		List<CalendarEventEntry> calendarEvents = googleCalendar.getEventList();
		Iterator<CalendarEventEntry> iterator = calendarEvents.iterator();

		// Delete all events
		while (iterator.hasNext()) {
			CalendarEventEntry calEntry = iterator.next();
			if (!calEntry.getStatus().getValue().contains("canceled")) {
				calEntry.delete();
			}
		}

	}

	@Test
	public void testCrud() throws IOException, ServiceException {
		GoogleCalendar gCal = new GoogleCalendar();
		List<CalendarEventEntry> eventList = gCal.getEventList();
		int initialSize = eventList.size();

		// test creation of event
		String taskTitle = "test crud";
		String taskStartStr = "2012-09-21T13:00:00+08:00";
		String taskEndStr = "2012-09-21T15:00:00+08:00";
		CalendarEventEntry addedEvent = gCal.createEvent(taskTitle,
				taskStartStr, taskEndStr);
		String taskId = addedEvent.getIcalUID();
		gCal.pullEvents();

		eventList = gCal.getEventList();
		assertEquals(initialSize + 1, eventList.size());

		// test update of event
		String newTitle = "movie at Bugis";
		String newTaskStartStr = "2012-09-22T08:00:00+08:00";
		String newTaskEndStr = "2012-09-22T10:00:00+08:00";
		gCal.updateEvent(taskId, newTitle, newTaskStartStr, newTaskEndStr);
		gCal.pullEvents();
		CalendarEventEntry event = gCal.getEvent(taskId);
		assertEquals(newTitle, event.getTitle().getPlainText());

		When updatedTime = event.getTimes().get(0);
		String updatedTaskStartStr = updatedTime.getStartTime().toString();
		String updatedTaskEndStr = updatedTime.getEndTime().toString();

		boolean isStartTimeEqual = compareTimeStr(newTaskStartStr,
				updatedTaskStartStr);
		boolean isEndTimeEqual = compareTimeStr(newTaskEndStr,
				updatedTaskEndStr);

		assertTrue(isStartTimeEqual);
		assertTrue(isEndTimeEqual);

		// test delete of event
		gCal.deleteEvent(taskId);
		gCal.pullEvents();
		eventList = gCal.getEventList();
		assertEquals(initialSize, eventList.size());
	}

	public boolean compareTimeStr(String timeStr1, String timeStr2) {
		int indexOfPlus = timeStr1.indexOf("+");
		String shortTimeStr1 = timeStr1.substring(0, indexOfPlus);
		String shortTimeStr2 = timeStr2.substring(0, indexOfPlus);

		return shortTimeStr1.equals(shortTimeStr2);
	}
}
