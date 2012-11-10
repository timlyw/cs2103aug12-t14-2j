package mhs.src.storage.persistence.remote;

import java.io.IOException;
import java.util.List;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.gdata.util.ResourceNotFoundException;

public class GoogleCalendar {
	Calendar calService = null;
	String calendarId = null;
	String DEFAULT_CALENDAR_ID = "primary";
	String STATUS_DELETED = "cancelled";
	
	public GoogleCalendar(HttpTransport httpTransport, JsonFactory jsonFactory, HttpRequestInitializer httpRequestInitializer) {
		initCalService(httpTransport, jsonFactory, httpRequestInitializer);
		setCalendarId(DEFAULT_CALENDAR_ID);
	}
	
	private void initCalService(HttpTransport httpTransport, JsonFactory jsonFactory, HttpRequestInitializer httpRequestInitializer) {
		calService = new Calendar(httpTransport, jsonFactory, httpRequestInitializer);
	}
	
	public void setCalendarId(String id) {
		calendarId = id;
	}

	public Event createEvent(String title, String startTime, String endTime) throws IOException {
		Event event = constructEvent(title, startTime, endTime);
		Event createdEvent = calService.events().insert(calendarId, event).execute();
		return createdEvent;
	}
	
	public Event updateEvent(String eventId, String title, String startTime, String endTime) throws IOException, ResourceNotFoundException {
		Event event = constructEvent(title, startTime, endTime);
		Event updatedEvent = calService.events().update(calendarId, eventId, event).execute();
		
		return updatedEvent;
	}
	
	public Event retrieveEvent(String eventId) throws IOException, ResourceNotFoundException {
		if(eventId == null) {
			return null;
		}
		Event retrievedEvent = calService.events().get(calendarId, eventId).execute();
		return retrievedEvent;
	}
	
	public void deleteEvent(String eventId) throws IOException, ResourceNotFoundException {
		calService.events().delete(calendarId, eventId).execute();
	}

	public List<Event> retrieveEvents(String minDate, String maxDate) throws IOException, ResourceNotFoundException {
		com.google.api.services.calendar.Calendar.Events.List calList = calService.events().list(calendarId);
		
		calList = calList.setShowDeleted(true);
		DateTime min = DateTime.parseRfc3339(minDate);
		DateTime max = DateTime.parseRfc3339(maxDate);
		
		calList = calList.setTimeMin(min);
		calList = calList.setTimeMax(max);	
		
		return calList.execute().getItems();
	}
	
	public boolean isDeleted(Event event) {
		return event.getStatus().contains(STATUS_DELETED);
	}
	
	private Event constructEvent(String title, String startTime, String endTime) {
		Event event = new Event();
		event.setSummary(title);
		
		EventDateTime start = constructEventDateTime(startTime);
		event.setStart(start);
		
		EventDateTime end = constructEventDateTime(endTime);
		event.setEnd(end);
		
		return event;
	}
	
	private EventDateTime constructEventDateTime(String dateTimeStr) {
		DateTime dateTime = DateTime.parseRfc3339(dateTimeStr);
		EventDateTime eventDateTime = new EventDateTime();
		eventDateTime.setDateTime(dateTime);
		return eventDateTime;
	}
	
	public String createCalendar(String calTitle) throws IOException {
		String calId = getCalendarId(calTitle);
		if(calId == null) {
			calId = addCalendar(calTitle);
		}
		
		return calId;
	}
	
	private String addCalendar(String calTitle) throws IOException {
		com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
		calendar.setSummary(calTitle);
		
		com.google.api.services.calendar.model.Calendar createdCalendar = calService.calendars().insert(calendar).execute();
		
		return createdCalendar.getId();
	}
	
	private String getCalendarId(String calTitle) throws IOException {
		List<CalendarListEntry> calLists = calService.calendarList().list().execute().getItems();
		
		for(int i = 0; i < calLists.size(); i++) {
			System.out.println("calendar: " + calLists.get(i).getSummary());
			if(calLists.get(i).getSummary().equals(calTitle)) {
				return calLists.get(i).getId();
			}
		}
		
		return null;
	}
	
	public void deleteEvents(String startTime, String endTime) throws IOException, ResourceNotFoundException {
		List<Event> eventList = retrieveEvents(startTime, endTime);
		for(int i = 0; i < eventList.size(); i++) {
			deleteEvent(eventList.get(i).getId());
		}
	}
}
