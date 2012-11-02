package mhs.test;

import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import mhs.src.storage.GoogleCalendarMhs;
import mhs.src.storage.TaskCategory;
import mhs.src.storage.TimedTask;
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

public class GoogleCalendarMhsTest {
	/**
	 * test account parameters
	 */
	static final String APP_NAME = "My Hot Secretary";
	static final String USER_EMAIL = "cs2103mhs@gmail.com";
	static final String USER_PASSWORD = "myhotsec2103";
	
	
	@Rule
    public ExpectedException thrown= ExpectedException.none();
	
	/**
	 * test if the retrieved access token is not empty when email and password is valid
	 * @throws NullPointerException 
	 * @throws AuthenticationException 
	 */
	@Test
	public void testRetrieveAccessToken() throws AuthenticationException, NullPointerException {
		String accessToken;
		accessToken = GoogleCalendarMhs.retrieveUserToken(APP_NAME, USER_EMAIL, USER_PASSWORD);
		assertTrue(accessToken.length() > 0);
	}
	
	
	/**
	 * prints out the events within the date range to console
	 * @throws NullPointerException 
	 * @throws ServiceException 
	 * @throws IOException 
	 */
	@Test
	public void testGetEvents() throws NullPointerException, IOException, ServiceException {
		String accessToken;
		accessToken = GoogleCalendarMhs.retrieveUserToken(APP_NAME, USER_EMAIL, USER_PASSWORD);
		GoogleCalendarMhs gCal = new GoogleCalendarMhs(APP_NAME, USER_EMAIL, accessToken);

		String startTime = "2014-02-01T01:00:00+08:00";
		String endTime = "2014-02-29T23:00:00+08:00";
		List<CalendarEventEntry> eventList = gCal.retrieveEvents(startTime, endTime);
		assertTrue(eventList.size() > 0);
	}
	
	@Test
	public void testCrudTaskCalendar() throws NullPointerException, IOException, ServiceException {
		// test createEvent
		String accessToken;
		accessToken = GoogleCalendarMhs.retrieveUserToken(APP_NAME, USER_EMAIL, USER_PASSWORD);
		GoogleCalendarMhs gCal = new GoogleCalendarMhs(APP_NAME, USER_EMAIL, accessToken);
		
		String title = "mhs calendar test";
		String startTime = "2013-01-16T13:00:00+08:00";
		String endTime = "2013-01-16T15:00:00+08:00";
		
		DateTime start = DateTime.parse(startTime);
		DateTime end = DateTime.parse(endTime);

		TimedTask newTask = new TimedTask(1, title, TaskCategory.TIMED, start, end,
				null, null, null, null, false, false);
		
		newTask.setDone(false);
		gCal.createEvent(newTask);
		
		
	}
	
	

	/**
	 * test for invalid login parameters
	 * @throws NullPointerException 
	 * @throws ServiceException 
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	@Test(expected=AuthenticationException.class)
	public void testInvalidLoginParameters() throws NullPointerException, IOException, AuthenticationException {
		GoogleCalendarMhs.retrieveUserToken(APP_NAME,"invalid email", "invalid password");
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
