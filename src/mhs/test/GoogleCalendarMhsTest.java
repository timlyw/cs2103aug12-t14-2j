////@author A0088015H
//
//package mhs.test;
//
//import java.io.IOException;
//import java.net.UnknownHostException;
//import java.util.List;
//
//import org.joda.time.DateTime;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//
//import com.google.gdata.data.calendar.CalendarEventEntry;
//import com.google.gdata.util.AuthenticationException;
//import com.google.gdata.util.ServiceException;
//
//import mhs.src.storage.persistence.remote.GoogleCalendar;
//import mhs.src.storage.persistence.remote.GoogleCalendarMhs;
//import mhs.src.storage.persistence.task.TaskCategory;
//import mhs.src.storage.persistence.task.TimedTask;
//import static org.junit.Assert.*;
//
///**
// * This class tests the functionality of GoogleCalendar
// * for creation, retrieval, update and deletion of events
// * 
// * It also tests for exception cases of null or invalid inputs
// * 
// * @author John Wong
// *
// */
//
//public class GoogleCalendarMhsTest {
//	/**
//	 * test account parameters
//	 */
//	static final String APP_NAME = "My Hot Secretary";
//	static final String USER_EMAIL = "cs2103mhs@gmail.com";
//	static final String USER_PASSWORD = "myhotsec2103";
//	
//	
//	@Rule
//    public ExpectedException thrown= ExpectedException.none();
//	
//	/**
//	 * test if the retrieved access token is not empty when email and password is valid
//	 * @throws NullPointerException 
//	 * @throws AuthenticationException 
//	 * @throws UnknownHostException 
//	 */
//	@Test
//	public void testRetrieveAccessToken() throws AuthenticationException, NullPointerException, UnknownHostException {
//		String accessToken;
//		accessToken = GoogleCalendarMhs.retrieveUserToken(APP_NAME, USER_EMAIL, USER_PASSWORD);
//		assertTrue(accessToken.length() > 0);
//	}
//	
//	@Test
//	public void testMoveEvent() throws NullPointerException, IOException, ServiceException {
//		String accessToken;
//		accessToken = GoogleCalendarMhs.retrieveUserToken(APP_NAME, USER_EMAIL, USER_PASSWORD);
//		GoogleCalendarMhs gCal = new GoogleCalendarMhs(APP_NAME, USER_EMAIL, accessToken);
//
//		String startTime = "2013-07-01T13:00:00+08:00";
//		String endTime = "2013-07-30T15:00:00+08:00";
//		List<CalendarEventEntry> taskList = gCal.retrieveEvents(startTime, endTime);
//		display(taskList);
//		for(int i = 0; i < taskList.size(); i++) {
//			String isDeleted = Boolean.toString(gCal.isDeleted(taskList.get(i)));
//			display(isDeleted);
//		}
//	}
//
//	/**
//	 * prints out the events within the date range to console
//	 * @throws NullPointerException 
//	 * @throws ServiceException 
//	 * @throws IOException 
//	 */
//	@Test
//	public void testCrudForEvents() throws NullPointerException, IOException, ServiceException {
//		String accessToken;
//		accessToken = GoogleCalendarMhs.retrieveUserToken(APP_NAME, USER_EMAIL, USER_PASSWORD);
//		GoogleCalendarMhs gCal = new GoogleCalendarMhs(APP_NAME, USER_EMAIL, accessToken);
//
//		String title1 = "mhs test event 1";
//		String startTime1 = "2013-01-16T13:00:00+08:00";
//		String endTime1 = "2013-01-16T15:00:00+08:00";
//		
//		String title2 = "mhs test event 2";
//		String startTime2 = "2013-01-18T09:00:00+08:00";
//		String endTime2 = "2013-01-18T20:00:00+08:00";
//	
//		List<CalendarEventEntry> initialList = gCal.retrieveEvents(startTime1, endTime2);
//
//		TimedTask newTask1 = createTask(title1, startTime1, endTime1);
//		newTask1.setDone(false);
//		gCal.createEvent(newTask1);
//		
//		TimedTask newTask2 = createTask(title2, startTime2, endTime2);
//		newTask2.setDone(true);
//		gCal.createEvent(newTask2);
//		
//		List<CalendarEventEntry> newList = gCal.retrieveEvents(startTime1, endTime2);
//		assertTrue(newList.size() == initialList.size() + 2);
//		
//		gCal.deleteEvents(startTime1, endTime2);
//	}
//	
//	/**
//	 * Test the creation, retrieval, update and delete methods for GoogleCalendarMhs
//	 * 
//	 * 1) create a task "mhs test time calendar" on 12 Jan 2013, 11pm to 13 Jan 2012, 1am
//	 * 2) retrieve the task using GoogleCalendarMhs and check whether the retrieved parameters
//	 * 	  match the parameters used for creation
//	 * 3) update the task title, start and end time
//	 * 4) retrieve the task again and check if the parameters match the parameters used to 
//	 *    update the task
//	 * 5) delete the task and try to retrieve it with GoogleCalendarMhs
//	 * 6) check that the retrieve method for the task now returns null
//	 * 
//	 * @throws NullPointerException
//	 * @throws IOException
//	 * @throws ServiceException
//	 */
//	@Test
//	public void testCrudForSingleEvent() throws NullPointerException, IOException, ServiceException {
//		// test createEvent
//		String accessToken;
//		accessToken = GoogleCalendarMhs.retrieveUserToken(APP_NAME, USER_EMAIL, USER_PASSWORD);
//		GoogleCalendarMhs gCal = new GoogleCalendarMhs(APP_NAME, USER_EMAIL, accessToken);
//		
//		String title = "mhs test time calendar";
//		String startTime = "2013-01-12T23:00:00+08:00";
//		String endTime = "2013-01-13T01:00:00+08:00";
//		
//		TimedTask newTask = createTask(title, startTime, endTime);
//		newTask.setDone(false);
//		CalendarEventEntry createdTask = gCal.createEvent(newTask);
//		String createdTaskId = createdTask.getIcalUID();
//		
//		// test retrieveEvent
//		CalendarEventEntry retrievedTask = gCal.retrieveEvent(createdTaskId);
//		String retrievedTitle = GoogleCalendar.getEventTitle(retrievedTask);
//		String retrievedStartTime = GoogleCalendar.getEventStartTime(retrievedTask);
//		String retrievedEndTime = GoogleCalendar.getEventEndTime(retrievedTask);
//		assertEquals(title, retrievedTitle);
//		assertTrue(GoogleCalendar.isTimeEqual(startTime, retrievedStartTime));
//		assertTrue(GoogleCalendar.isTimeEqual(endTime, retrievedEndTime));	
//		assertFalse(gCal.isEventCompleted(retrievedTask));
//		
//		
//		// test updateEvent
//		String updatedTitle = "mhs test default calendar update";
//		String updatedStartTime = "2013-01-17T08:00:00+08:00";
//		String updatedEndTime = "2013-01-17T09:00:00+08:00";
//		
//		TimedTask updatedTask = createTask(updatedTitle, updatedStartTime, updatedEndTime);
//		updatedTask.setDone(true);
//		updatedTask.setgCalTaskId(retrievedTask.getIcalUID());
//		CalendarEventEntry updatedEvent = gCal.updateEvent(updatedTask);
//		String updatedEventId = updatedEvent.getIcalUID();
//		
//		CalendarEventEntry retrievedUpdatedTask = gCal.retrieveEvent(updatedEventId);
//		String retrievedUpdatedTitle = GoogleCalendar.getEventTitle(retrievedUpdatedTask);
//		String retrievedUpdatedStartTime = GoogleCalendar.getEventStartTime(retrievedUpdatedTask);
//		String retrievedUpdatedEndTime = GoogleCalendar.getEventEndTime(retrievedUpdatedTask);
//		
//		assertEquals(updatedTitle, retrievedUpdatedTitle);
//		assertTrue(GoogleCalendar.isTimeEqual(updatedStartTime, retrievedUpdatedStartTime));
//		assertTrue(GoogleCalendar.isTimeEqual(updatedEndTime, retrievedUpdatedEndTime));
//		assertTrue(gCal.isEventCompleted(retrievedUpdatedTask));
//		
//		
//		// test delete event
//		gCal.deleteEvent(updatedEventId);
//		CalendarEventEntry deletedTask = gCal.retrieveEvent(updatedEventId);
//		assertEquals(null, deletedTask);
//	}
//	
//	/**
//	 * creates a basic TimedTask for testing purposes
//	 * @param title
//	 * @param startTime
//	 * @param endTime
//	 * @return created task
//	 */
//	private TimedTask createTask(String title, String startTime, String endTime) {
//		DateTime start = DateTime.parse(startTime);
//		DateTime end = DateTime.parse(endTime);
//
//		TimedTask task = new TimedTask(1, title, TaskCategory.TIMED, start, end,
//				null, null, null, null, false, false);
//		return task;
//	}
//
//	/**
//	 * test for invalid login parameters
//	 * @throws NullPointerException 
//	 * @throws ServiceException 
//	 * @throws IOException 
//	 * @throws AuthenticationException 
//	 */
//	@Test(expected=AuthenticationException.class)
//	public void testInvalidLoginParameters() throws NullPointerException, IOException, AuthenticationException {
//		GoogleCalendarMhs.retrieveUserToken(APP_NAME,"invalid email", "invalid password");
//	}
//	
//	/**
//	 * functions to print to console
//	 */
//	public void display(String displayString) {
//		System.out.println(displayString);
//	}
//	
//	public void display(int displayInt) {
//		System.out.println(displayInt);
//	}
//	
//	public void display(CalendarEventEntry calendarEvent) {
//		String title = calendarEvent.getTitle().getPlainText();
//		String id = calendarEvent.getIcalUID();
//		display(title + " " + id);
//	}
//	
//	public void display(List<CalendarEventEntry> eventList) {
//		for(int i = 0; i < eventList.size(); i++) {
//			display(eventList.get(i));
//		}
//	}
//}
