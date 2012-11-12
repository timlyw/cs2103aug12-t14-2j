//@author A0088015

package mhs.src.storage.persistence.remote;

import java.io.IOException;
import java.util.List;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.gdata.util.ResourceNotFoundException;

/**
 * This class provides the methods to interface with a user's Google Calendar
 * 
 * It supports single event functions such as:
 * creation, retrieval, updating and deletion
 * 
 * It also supports multiple event functions such as retrieving or deleting 
 * all events within a specified time range
 * 
 * @author John Wong
 */

public class GoogleCalendar {
	// calendar service for interfacing with user's Google Calendar
	Calendar calService = null;
	
	// calendar id to connect with
	String calendarId = null;
	
	// id for default calendar
	String DEFAULT_CALENDAR_ID = "primary";
	
	// status to check for a deleted event
	String STATUS_DELETED = "cancelled";

	/**
	 * constructor to create an instance of GoogleCalendar
	 * 
	 * @param httpTransport
	 * @param jsonFactory
	 * @param httpRequestInitializer
	 */
	public GoogleCalendar(HttpTransport httpTransport, JsonFactory jsonFactory,
			HttpRequestInitializer httpRequestInitializer) {
		initCalService(httpTransport, jsonFactory, httpRequestInitializer);
		setCalendarId(DEFAULT_CALENDAR_ID);
	}

	/**
	 * @param id
	 */
	public void setCalendarId(String id) {
		calendarId = id;
	}

	/**
	 * create an event entry in user's GoogleCalendar
	 * 
	 * @param title
	 * @param startTime
	 * @param endTime
	 * @return created event entry
	 * @throws IOException
	 */
	public Event createEvent(String title, String startTime, String endTime)
			throws IOException {
		Event event = constructEvent(title, startTime, endTime);
		Event createdEvent = calService.events().insert(calendarId, event)
				.execute();
		return createdEvent;
	}

	/**
	 * update event entry in user's Google Calendar
	 * 
	 * @param eventId
	 * @param title
	 * @param startTime
	 * @param endTime
	 * @return updated event entry
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	public Event updateEvent(String eventId, String title, String startTime,
			String endTime) throws IOException, ResourceNotFoundException {
		Event event = constructEvent(title, startTime, endTime);
		Event updatedEvent = calService.events()
				.update(calendarId, eventId, event).execute();

		return updatedEvent;
	}

	/**
	 * retrieves an event from user's google calendar
	 * 
	 * @param eventId
	 * @return retrieved event
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	public Event retrieveEvent(String eventId) throws IOException,
			ResourceNotFoundException {
		if (eventId == null) {
			return null;
		}
		try {
			Event retrievedEvent = calService.events().get(calendarId, eventId)
					.execute();
			return retrievedEvent;
		} catch (GoogleJsonResponseException e) {
			return null;
		}
	}
	
	/**
	 * retrieve list of deleted events from user's Google calendar
	 * 
	 * @param minDate
	 * @param maxDate
	 * @return list of deleted events
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	public List<Event> retrieveDeletedEvents(String minDate, String maxDate)
			throws IOException, ResourceNotFoundException {
		com.google.api.services.calendar.Calendar.Events.List calList = calService
				.events().list(calendarId);

		calList = calList.setShowDeleted(true);
		DateTime min = DateTime.parseRfc3339(minDate);
		DateTime max = DateTime.parseRfc3339(maxDate);

		calList = calList.setTimeMin(min);
		calList = calList.setTimeMax(max);

		return calList.execute().getItems();
	}

	/**
	 * retrieve list of undeleted events from user's Google calendar
	 * 
	 * @param minDate
	 * @param maxDate
	 * @return
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	public List<Event> retrieveEvents(String minDate, String maxDate)
			throws IOException, ResourceNotFoundException {
		com.google.api.services.calendar.Calendar.Events.List calList = calService
				.events().list(calendarId);

		calList = calList.setShowDeleted(false);
		DateTime min = DateTime.parseRfc3339(minDate);
		DateTime max = DateTime.parseRfc3339(maxDate);

		calList = calList.setTimeMin(min);
		calList = calList.setTimeMax(max);

		return calList.execute().getItems();
	}


	/**
	 * deletes the event with specified event id
	 * 
	 * @param eventId
	 */
	public void deleteEvent(String eventId) {
		try {
			calService.events().delete(calendarId, eventId).execute();
		} catch (IOException e) {
		}
	}
	
	/**
	 * delete all events within the time range specified by startTime and endTime
	 * 
	 * @param startTime
	 * @param endTime
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 */
	public void deleteEvents(String startTime, String endTime)
			throws IOException, ResourceNotFoundException {
		List<Event> eventList = retrieveEvents(startTime, endTime);
		for (int i = 0; i < eventList.size(); i++) {
			deleteEvent(eventList.get(i).getId());
		}
	}
	
	
	/**
	 * check if an event is deleted in user's Google Calendar
	 * 
	 * @param event
	 * @return 
	 */
	public boolean isDeleted(Event event) {
		return event.getStatus().contains(STATUS_DELETED);
	}

	/**
	 * check if calendar with calendar title is in user's Google Calendar, else 
	 * create a calendar based on specified calendar title
	 * 
	 * @param calTitle
	 * @return created calendar's id
	 * @throws IOException
	 */
	public String createCalendar(String calTitle) throws IOException {
		String calId = getCalendarId(calTitle);
		if (calId == null) {
			calId = addCalendar(calTitle);
		}

		return calId;
	}

	/**
	 * add a calendar into user's Google Calendar with specified calendar title
	 * 
	 * @param calTitle
	 * @return
	 * @throws IOException
	 */
	private String addCalendar(String calTitle) throws IOException {
		com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
		calendar.setSummary(calTitle);

		com.google.api.services.calendar.model.Calendar createdCalendar = calService
				.calendars().insert(calendar).execute();

		return createdCalendar.getId();
	}

	/**
	 * get calendar id of calendar with title matching specified calTitle
	 * 
	 * @param calTitle
	 * @return found id
	 * @throws IOException
	 */
	private String getCalendarId(String calTitle) throws IOException {
		List<CalendarListEntry> calLists = calService.calendarList().list()
				.execute().getItems();

		for (int i = 0; i < calLists.size(); i++) {
			if (calLists.get(i).getSummary().equals(calTitle)) {
				return calLists.get(i).getId();
			}
		}

		return null;
	}
	
	/**
	 * initialize calendar service
	 * 
	 * @param httpTransport
	 * @param jsonFactory
	 * @param httpRequestInitializer
	 */
	private void initCalService(HttpTransport httpTransport,
			JsonFactory jsonFactory,
			HttpRequestInitializer httpRequestInitializer) {
		calService = new Calendar(httpTransport, jsonFactory,
				httpRequestInitializer);
	}
	
	/**
	 * create an event based on specified title, start time, end time
	 * 
	 * @param title
	 * @param startTime
	 * @param endTime
	 * @return constructed event
	 */
	private Event constructEvent(String title, String startTime, String endTime) {
		Event event = new Event();
		event.setSummary(title);

		EventDateTime start = constructEventDateTime(startTime);
		event.setStart(start);

		EventDateTime end = constructEventDateTime(endTime);
		event.setEnd(end);

		return event;
	}

	/**
	 * create EventDateTime instance from specified date/time string
	 * 
	 * @param dateTimeStr
	 * @return created EventDateTime
	 */
	private EventDateTime constructEventDateTime(String dateTimeStr) {
		DateTime dateTime = DateTime.parseRfc3339(dateTimeStr);
		EventDateTime eventDateTime = new EventDateTime();
		eventDateTime.setDateTime(dateTime);
		return eventDateTime;
	}
}
