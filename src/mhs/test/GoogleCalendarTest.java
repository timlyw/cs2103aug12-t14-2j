package mhs.test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.Test;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import mhs.src.GoogleCalendar;
import static org.junit.Assert.*;

public class GoogleCalendarTest {
	static final String APP_NAME = "My Hot Secretary";
	static final String USER_EMAIL = "cs2103mhs@gmail.com";
	static final String USER_PASSWORD = "myhotsec2103";
	static final String ERROR_LOGIN = "invalid login parameters or internet connection unavailable";
	static final String ERROR_ACCESS = "unable to access calendar";
	static final String ERROR_SERVICE = "unable to connect to calendar service";
	static final String ERROR_CONNECTION = "internet connection unavailable";
	
	
	@Test
	public void testRetrieveAccessToken() {
		try {
			String accessToken;
			accessToken = GoogleCalendar.retrieveAccessToken(APP_NAME, USER_EMAIL, USER_PASSWORD);
			assertTrue(accessToken.length() > 0);
		} catch (AuthenticationException e) {
			display(ERROR_LOGIN);
		}
	}
	

	@Test
	public void testGetEvents() {
		try {	
			String accessToken;
			accessToken = GoogleCalendar.retrieveAccessToken(APP_NAME, USER_EMAIL, USER_PASSWORD);
			GoogleCalendar gCal = new GoogleCalendar(accessToken, USER_EMAIL, APP_NAME);

			String startTime = "2014-02-01T01:00:00+08:00";
			String endTime = "2014-02-29T23:00:00+08:00";
			List<CalendarEventEntry> eventList = gCal.retrieveEvents(startTime, endTime);
			display(eventList.size());
			for(int i = 0; i < eventList.size(); i++) {
				display(eventList.get(i).getTitle().getPlainText());
			}
			
		} catch (AuthenticationException e) {
			display(ERROR_LOGIN);
		} catch (UnknownHostException e) {
			display(ERROR_CONNECTION);
		} catch (IOException e) {
			display(ERROR_ACCESS);
		} catch (ServiceException e) {
			display(ERROR_SERVICE);
		}
	}
	
	@Test
	public void testCrud() {
		try {
			// test createEvent
			String accessToken;
			accessToken = GoogleCalendar.retrieveAccessToken(APP_NAME, USER_EMAIL, USER_PASSWORD);
			GoogleCalendar gCal = new GoogleCalendar(accessToken, USER_EMAIL, APP_NAME);
			
			String title = "unit test create";
			String startTime = "2012-10-16T13:00:00+08:00";
			String endTime = "2012-10-16T15:00:00+08:00";
			CalendarEventEntry newEvent = gCal.createEvent(title, startTime, endTime);
			
			String newEventId = newEvent.getIcalUID();
			CalendarEventEntry retrievedEvent = gCal.retrieveEvent(newEventId);
			String retrievedTitle = GoogleCalendar.getEventTitle(retrievedEvent);
			String retrievedStartTime = GoogleCalendar.getEventStartTime(retrievedEvent);
			assertEquals(title, retrievedTitle);
			assertTrue(GoogleCalendar.isTimeEqual(startTime, retrievedStartTime));
			
			
			String updatedTitle = "unit test update";
			String updatedStartTime = "2014-01-18T09:00:00+08:00";
			String updatedEndTime = "2014-01-18T11:00:00+08:00";
			gCal.updateEvent(newEventId, updatedTitle, updatedStartTime, updatedEndTime);
			
			CalendarEventEntry updatedEvent = gCal.retrieveEvent(newEventId);
			String retrievedUpdatedTitle = GoogleCalendar.getEventTitle(updatedEvent);
			String retrievedUpdatedStartTime = GoogleCalendar.getEventStartTime(updatedEvent);
			assertEquals(updatedTitle, retrievedUpdatedTitle);
			assertTrue(GoogleCalendar.isTimeEqual(updatedStartTime, retrievedUpdatedStartTime));
			
			gCal.deleteEvent(newEventId);
			CalendarEventEntry deletedEvent = gCal.retrieveEvent(newEventId);
			assertEquals(null, deletedEvent);
			
		} catch (AuthenticationException e) {
			display(ERROR_LOGIN);
		} catch (IOException e) {
			display(ERROR_ACCESS);
		} catch (ServiceException e) {
			display(ERROR_SERVICE);
		}
	}
	
	public void display(String displayString) {
		System.out.println(displayString);
	}
	
	public void display(int displayInt) {
		System.out.println(displayInt);
	}
}
