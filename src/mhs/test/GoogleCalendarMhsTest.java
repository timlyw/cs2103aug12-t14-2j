//@author A0088015H

package mhs.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.List;

import mhs.src.storage.persistence.remote.GoogleCalendarMhs;
import mhs.src.storage.persistence.remote.MhsGoogleOAuth2;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;
import mhs.src.storage.persistence.task.TimedTask;

import org.junit.Test;
import org.joda.time.DateTime;

import com.google.api.services.calendar.model.Event;


/**
 * This class tests the functionality of GoogleCalendar
 * for creation, retrieval, update and deletion of events
 * 
 * It also tests for exception cases of null or invalid inputs
 * 
 * @author John Wong
 *
 */

public class GoogleCalendarMhsTest {
	private static final int MAX_DATE_LENGTH_COMPARE = 10;
	
	@Test 
	public void testCrudForMultipleEvents() throws Exception {
		// initialize login
		MhsGoogleOAuth2.getInstance();
		MhsGoogleOAuth2.authorizeCredentialAndStoreInCredentialStore();
		GoogleCalendarMhs gCal = new GoogleCalendarMhs(MhsGoogleOAuth2.getHttpTransport(), MhsGoogleOAuth2.getJsonFactory(), MhsGoogleOAuth2.getCredential());
		
		// test retrieve undeleted events in default calendar
		String minDate = "2012-11-01T13:00:00+08:00";
		String maxDate = "2012-11-30T13:00:00+08:00";
		List<Event> eventList = gCal.retrieveDefaultEvents(minDate, maxDate, false);
		int initialSize;
		if(eventList == null) {
			initialSize = 0;
		} else {
			initialSize = eventList.size();
		}
		
		String title = "CreateGoogleCalendarMhsTest";
		String startTime = "2012-11-16T13:00:00+08:00";
		String endTime = "2011-11-16T13:00:00+08:00";
		
		Task newTask1 = createTask(title, startTime, endTime);
		Task newTask2 = createTask(title, startTime, endTime);
		Event createdEvent1 = gCal.createEvent(newTask1);
		Event createdEvent2 = gCal.createEvent(newTask2);
		
		List<Event> updatedEventList = gCal.retrieveDefaultEvents(minDate, maxDate, false);
		int updatedSize = updatedEventList.size();
		assertEquals(initialSize + 2, updatedSize);
		
		// test retrieve deleted events in default calendar
		eventList = gCal.retrieveDefaultEvents(minDate, maxDate, true);
		initialSize = eventList.size();
		System.out.println("id " + createdEvent1.getId());
		
		gCal.deleteEvent(createdEvent1.getId());
		gCal.deleteEvent(createdEvent2.getId());
		//updatedEventList = gCal.retrieveDefaultEvents(minDate, maxDate, true);
		//updatedSize = updatedEventList.size();
		//assertEquals(initialSize + 2, updatedSize);
		
	}
	
	@Test
	public void testCrudForSingleEvent() throws Exception {
		// initialize login
		MhsGoogleOAuth2.getInstance();
		MhsGoogleOAuth2.authorizeCredentialAndStoreInCredentialStore();
		GoogleCalendarMhs gCal = new GoogleCalendarMhs(MhsGoogleOAuth2.getHttpTransport(), MhsGoogleOAuth2.getJsonFactory(), MhsGoogleOAuth2.getCredential());
		
		// test create event
		String title = "CreateGoogleCalendarMhsTest";
		String startTime = "2013-01-16T13:00:00+08:00";
		String endTime = "2013-01-16T13:00:00+08:00";
		
		Task newTask = createTask(title, startTime, endTime);
		newTask.setDone(false);
		Event createdEvent = gCal.createEvent(newTask);
		String eventId = createdEvent.getId();
		
		// test retrieve event
		Event retrievedEvent = gCal.retrieveEvent(eventId);
		assertEquals(retrievedEvent.getSummary(), title);
		assertTrue(dateStringsEqual(retrievedEvent.getStart().toString(), startTime));
		assertTrue(dateStringsEqual(retrievedEvent.getEnd().toString(), endTime));
	
		// test update event
		String updatedTitle = "UpdateGoogleCalendarMhsTest";
		String updatedStartTime = "2013-01-18T13:00:00+08:00";
		String updatedEndTime = "2013-01-19T15:00:00+08:00";	
		Task updatedTask = createTask(updatedTitle, updatedStartTime, updatedEndTime);
		updatedTask.setDone(true);
		updatedTask.setGcalTaskId(eventId);
		
		// updated event title, start time, end time, and set to completed
		Event updatedEvent = gCal.updateEvent(updatedTask);
		String updatedEventId = updatedEvent.getId();
		Event retrievedUpdatedEvent = gCal.retrieveEvent(updatedEventId);
		
		assertEquals(retrievedUpdatedEvent.getSummary(), updatedTitle);
		assertTrue(dateStringsEqual(retrievedUpdatedEvent.getStart().toString(), updatedStartTime));
		assertTrue(dateStringsEqual(retrievedUpdatedEvent.getEnd().toString(), updatedEndTime));
		
		// updated event, set to uncompleted
		updatedTask.setGcalTaskId(updatedEventId);
		updatedTask.setDone(false);
		Event eventToBeDeleted = gCal.updateEvent(updatedTask);
		String eventToBeDeletedId = eventToBeDeleted.getId();
		
		// test delete event
		gCal.deleteEvent(eventToBeDeletedId);
		Event retrievedDeletedEvent = gCal.retrieveEvent(eventToBeDeletedId);
		
		assertTrue(gCal.isDeleted(retrievedDeletedEvent));
	}
	
	private boolean dateStringsEqual(String date1, String date2) {
		String subDate2 = date2.substring(0, MAX_DATE_LENGTH_COMPARE);
		return date1.contains(subDate2);
	}
	
	private TimedTask createTask(String title, String startTime, String endTime) {
		DateTime start = DateTime.parse(startTime);
		DateTime end = DateTime.parse(endTime);
		TimedTask task = new TimedTask(1, title, TaskCategory.TIMED, start,
				end, null, null, null, null, null, false, false);
		return task;
	}
	
}
