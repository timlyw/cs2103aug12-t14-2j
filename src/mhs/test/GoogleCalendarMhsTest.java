//@author A0088015H

package mhs.test;

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
	@Test
	public void testCrud() throws Exception {
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
		

		// test update event
		String updatedTitle = "UpdateGoogleCalendarMhsTest";
		String updatedStartTime = "2013-01-18T13:00:00+08:00";
		String updatedEndTime = "2013-01-19T15:00:00+08:00";	
		Task updatedTask = createTask(updatedTitle, updatedStartTime, updatedEndTime);
		updatedTask.setDone(true);
		updatedTask.setGcalTaskId(eventId);
		
		// updated event title, start time, end time, and set to completed
		Event updatedEvent = gCal.updateEvent(updatedTask);
		
		// updated event, set to uncompleted
		String updatedEventId = updatedEvent.getId();
		updatedTask.setGcalTaskId(updatedEventId);
		updatedTask.setDone(false);
		Event eventToBeDeleted = gCal.updateEvent(updatedTask);
		String eventToBeDeletedId = eventToBeDeleted.getId();
		
		// test delete event
		gCal.deleteEvent(eventToBeDeletedId);
		System.out.println(gCal.retrieveEvent(eventToBeDeletedId));
	}
	
	private TimedTask createTask(String title, String startTime, String endTime) {
		DateTime start = DateTime.parse(startTime);
		DateTime end = DateTime.parse(endTime);
		TimedTask task = new TimedTask(1, title, TaskCategory.TIMED, start,
				end, null, null, null, null, null, false, false);
		return task;
	}
	
}
