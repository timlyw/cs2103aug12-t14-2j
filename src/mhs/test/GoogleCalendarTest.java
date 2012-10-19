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

/**
 * This class tests the functionality of GoogleCalendar
 * for creation, retrieval, update and deletion of events
 * 
 * It also tests for exception cases of null or invalid inputs
 * 
 * @author John
 *
 */

public class GoogleCalendarTest {
	/**
	 * test account parameters
	 */
	static final String APP_NAME = "My Hot Secretary";
	static final String USER_EMAIL = "cs2103mhs@gmail.com";
	static final String USER_PASSWORD = "myhotsec2103";
	
	/**
	 * error messages
	 */
	static final String ERROR_LOGIN = "invalid login parameters or internet connection unavailable";
	static final String ERROR_ACCESS = "unable to access calendar";
	static final String ERROR_SERVICE = "unable to connect to calendar service";
	static final String ERROR_CONNECTION = "internet connection unavailable";
	static final String ERROR_NULL = "error: null parameter";
	
	/**
	 * test if the retrieved access token is not empty when email and password is valid
	 */
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
	
	/**
	 * prints out the events within the date range to console
	 */
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
			display(eventList);
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
	
	/**
	 * test creation, retrieval, update and deletion of an event 
	 */
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
			
			// test retrieve event
			String newEventId = newEvent.getIcalUID();
			CalendarEventEntry retrievedEvent = gCal.retrieveEvent(newEventId);
			String retrievedTitle = GoogleCalendar.getEventTitle(retrievedEvent);
			String retrievedStartTime = GoogleCalendar.getEventStartTime(retrievedEvent);
			assertEquals(title, retrievedTitle);
			assertTrue(GoogleCalendar.isTimeEqual(startTime, retrievedStartTime));
			
			// test update event
			String updatedTitle = "unit test update";
			String updatedStartTime = "2014-01-18T09:00:00+08:00";
			String updatedEndTime = "2014-01-18T11:00:00+08:00";
			gCal.updateEvent(newEventId, updatedTitle, updatedStartTime, updatedEndTime);
			
			CalendarEventEntry updatedEvent = gCal.retrieveEvent(newEventId);
			String retrievedUpdatedTitle = GoogleCalendar.getEventTitle(updatedEvent);
			String retrievedUpdatedStartTime = GoogleCalendar.getEventStartTime(updatedEvent);
			assertEquals(updatedTitle, retrievedUpdatedTitle);
			assertTrue(GoogleCalendar.isTimeEqual(updatedStartTime, retrievedUpdatedStartTime));
			
			// test delete event
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
	

	/**
	 * test for invalid input parameters
	 */
	@Test
	public void testInvalidInput() {
		try {
			// test createEvent
			String accessToken;
			accessToken = GoogleCalendar.retrieveAccessToken(APP_NAME,"invalid email", "invalid password");
			GoogleCalendar gCal = new GoogleCalendar(accessToken, "invalid email", APP_NAME);

			String title = "unit test create";
			String startTime = "2012-10-16T13:00:00+08:00";
			String endTime = "2012-10-16T15:00:00+08:00";
			gCal.createEvent(title, startTime, endTime);
			gCal.retrieveEvent("invalid event id");
			gCal.retrieveEvents("invalid date", "invalid date");
			gCal.retrieveEvents("2014-10-16T13:00:00+08:00", "2012-10-16T13:00:00+08:00");
			gCal.retrieveEvents("2011-500-16T13:00:00+08:00", "2012-10-16T13:00:00+08:00");
			
		} catch (AuthenticationException e) {
			display(ERROR_LOGIN);
		} catch (IOException e) {
			display(ERROR_ACCESS);
		} catch (ServiceException e) {
			display(ERROR_SERVICE);
		} catch (NullPointerException e) {
			display(ERROR_NULL);
		}
	}
	
	/**
	 * test for null parameters
	 */
	@Test
	public void testNull() {
		try {
			// test createEvent
			String accessToken;
			accessToken = GoogleCalendar.retrieveAccessToken(APP_NAME, USER_EMAIL, USER_PASSWORD);
			GoogleCalendar gCal = new GoogleCalendar(accessToken, USER_EMAIL, APP_NAME);
			gCal.createEvent(null);
			gCal.deleteEvent(null);
			
		} catch (AuthenticationException e) {
			display(ERROR_LOGIN);
		} catch (IOException e) {
			display(ERROR_ACCESS);
		} catch (ServiceException e) {
			display(ERROR_SERVICE);
		} catch (NullPointerException e) {
			display(ERROR_NULL);
		}
	}
	
	/**
	 * functions to print to console
	 */
	public void display(String displayString) {
		System.out.println(displayString);
	}
	
	public void display(int displayInt) {
		System.out.println(displayInt);
	}
	
	public void display(CalendarEventEntry calendarEvent) {
		display(calendarEvent.getTitle().getPlainText());
	}
	
	public void display(List<CalendarEventEntry> eventList) {
		for(int i = 0; i < eventList.size(); i++) {
			display(eventList.get(i));
		}
	}
}
