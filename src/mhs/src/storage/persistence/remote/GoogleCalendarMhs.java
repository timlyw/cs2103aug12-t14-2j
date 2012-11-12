package mhs.src.storage.persistence.remote;

import java.io.IOException;
import java.util.List;

import mhs.src.storage.persistence.task.Task;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.model.Event;
import com.google.gdata.util.ResourceNotFoundException;

public class GoogleCalendarMhs {
	// title of user's competed task calendar
	private static final String COMPLETED_TASKS_CALENDAR_TITLE = "Completed Tasks (MHS)";
	
	// instance of GoogleCalendar to interface with user's default Google calendar
	private GoogleCalendar defaultCalendar;
	
	// instance of GoogleCalendar to interface with user's completed Google calendar
	private GoogleCalendar completedCalendar;

	/**
	 * constructor to create an instance of GoogleCalendarMhs
	 * 
	 * @param httpTransport
	 * @param jsonFactory
	 * @param httpRequestInitializer
	 * @throws IOException
	 */
	public GoogleCalendarMhs(HttpTransport httpTransport,
			JsonFactory jsonFactory,
			HttpRequestInitializer httpRequestInitializer) throws IOException {
		defaultCalendar = new GoogleCalendar(httpTransport, jsonFactory,
				httpRequestInitializer);
		String completedCalendarId = defaultCalendar
				.createCalendar(COMPLETED_TASKS_CALENDAR_TITLE);
		completedCalendar = new GoogleCalendar(httpTransport, jsonFactory,
				httpRequestInitializer);
		completedCalendar.setCalendarId(completedCalendarId);
	}

	/**
	 * create an event based on the parameters specified in newTask
	 * 
	 * @param newTask
	 * @return created calendar event entry
	 * @throws IOException
	 */
	public Event createEvent(Task newTask) throws IOException {
		if (newTask.isFloating()) {
			return null;
		}
		String title = newTask.getTaskName();
		String startTime = newTask.getStartDateTime().toString();
		String endTime = newTask.getEndDateTime().toString();
		Event createdEvent = null;
		if (newTask.isDone()) {
			createdEvent = completedCalendar.createEvent(title, startTime,
					endTime);
		} else {
			createdEvent = defaultCalendar.createEvent(title, startTime,
					endTime);
		}

		return createdEvent;
	}

	/**
	 * retrieve an event from user's Google calendar 
	 * 
	 * @param eventId
	 * @return
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	public Event retrieveEvent(String eventId) throws IOException,
			ResourceNotFoundException {
		Event retrievedEvent = null;
		retrievedEvent = defaultCalendar.retrieveEvent(eventId);
		if (retrievedEvent == null) {
			retrievedEvent = completedCalendar.retrieveEvent(eventId);
		}

		return retrievedEvent;
	}

	/**
	 * retrieve events from user's default Google calendar
	 * 
	 * @param minDate
	 * @param maxDate
	 * @param getDeletedEventsOnly
	 * @return retrieved event list
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	public List<Event> retrieveDefaultEvents(String minDate, String maxDate,
			boolean getDeletedEventsOnly) throws IOException,
			ResourceNotFoundException {
		if(getDeletedEventsOnly){
			return defaultCalendar.retrieveDeletedEvents(minDate, maxDate);
		}else{
			return defaultCalendar.retrieveEvents(minDate, maxDate);
		}
	}

	/**
	 * retrieve events from user's completed tasks calendar
	 * @param minDate
	 * @param maxDate
	 * @param getDeletedEventsOnly
	 * @return retrieved events
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	public List<Event> retrieveCompletedEvents(String minDate, String maxDate,
			boolean getDeletedEventsOnly) throws IOException,
			ResourceNotFoundException {
		if(getDeletedEventsOnly){
			return completedCalendar.retrieveDeletedEvents(minDate, maxDate);
		}else{
			return completedCalendar.retrieveEvents(minDate, maxDate);
		}
	}

	/**
	 * update event to parameters contained in updatedTask
	 * 
	 * @param updatedTask
	 * @return updated calendar event entry
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	public Event updateEvent(Task updatedTask) throws IOException,
			ResourceNotFoundException {
		String eventId = updatedTask.getgCalTaskId();

		deleteEvent(eventId);
		return createEvent(updatedTask);
	}

	/**
	 * delete event matching specified eventId
	 * 
	 * @param eventId
	 */
	public void deleteEvent(String eventId) {
		defaultCalendar.deleteEvent(eventId);
		completedCalendar.deleteEvent(eventId);
	}

	/**
	 * check if specified event is deleted
	 * 
	 * @param event
	 * @return 
	 */
	public boolean isDeleted(Event event) {
		return defaultCalendar.isDeleted(event);
	}

	public void deleteEvents(String startTime, String endTime) {
		try {
			defaultCalendar.deleteEvents(startTime, endTime);
			completedCalendar.deleteEvents(startTime, endTime);
		} catch (ResourceNotFoundException | IOException e) {
			// TODO Auto-generated catch block
		}
	}

	public void isEventCompleted() {

	}

}
