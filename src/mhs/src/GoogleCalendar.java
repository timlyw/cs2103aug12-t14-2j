package mhs.src;

import com.google.gdata.client.calendar.*;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.*;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class GoogleCalendar {
	static final String APP_NAME = "My Hot Secretary";
	static final String TAB = "\t";
	static final String URL_SEPARATOR = "/";
	
	static final String MESSAGE_YOUR_CALENDARS = "Your calendars:";
	static final String MESSAGE_NEW_TASK_ID = "Inserted task id:";

	static final String URL_EVENT_FEED = "https://www.google.com/calendar/feeds/default/private/full";
	static final String URL_CREATE_EVENT = "http://www.google.com/calendar/feeds/%1$s/private/full";
	String _userEmail = "cs2103mhs@gmail.com";
	String _userPassword = "myhotsec2103";
	CalendarService _calendarService;
	List<CalendarEventEntry> _eventList;

    String _minStartTime = "2012-09-01T00:00:00";
    String _maxStartTime = "2012-09-29T23:59:59";
	
    // setup the calendar service with userEmail and userPassword
    // pull events from user's calendar
	public void setup() throws IOException, ServiceException {
		initializeCalendarService();
        pullEvents();
	}
	
	// returns an event with taskId matching input parameter
	public CalendarEventEntry getEvent(String taskId) throws MalformedURLException, IOException, ServiceException {
		CalendarEventEntry event;
		for(int i = 0; i < _eventList.size(); i++) {
			event = _eventList.get(i);
			if(isIdEqual(event.getId(), taskId)) {
				return event;
			}
		}
		return null;
	}
	
	// create an event in user's calendar with specified title, start and end time
	public String createEvent(String taskTitle, String taskStartStr, String taskEndStr) throws IOException, ServiceException {
		URL postURL = new URL(String.format(URL_CREATE_EVENT, _userEmail));
		CalendarEventEntry event = constructEvent(taskTitle, taskStartStr, taskEndStr);
		
		CalendarEventEntry insertedEntry = _calendarService.insert(postURL, event);
		return insertedEntry.getId();
	}
	
	public CalendarEventEntry updateEvent(String taskId, String newTitle, String newStartTime, String newEndTime) throws IOException, ServiceException {
		CalendarEventEntry event = getEvent(taskId);
		event.setTitle(new PlainTextConstruct(newTitle));
		URL editUrl = new URL(event.getEditLink().getHref());
		CalendarEventEntry updatedEntry = (CalendarEventEntry)_calendarService.update(editUrl, event);
		return updatedEntry;
	}
	
	// delete the event matching the specified taskId
	public void deleteEvent(String taskId) throws IOException, ServiceException {
		CalendarEventEntry event = getEvent(taskId);
		
		if(event != null) {
			event.delete();
		}
	}
	
	public List<CalendarEventEntry> getEventList() {
		return _eventList;
	}
	
	// get user's events from his calendar
	public void pullEvents() throws IOException, ServiceException {
		URL feedUrl = new URL(URL_EVENT_FEED);
		CalendarQuery myQuery = new CalendarQuery(feedUrl);
		myQuery.setMinimumStartTime(DateTime.parseDateTime(_minStartTime));
		myQuery.setMaximumStartTime(DateTime.parseDateTime(_maxStartTime));

		// Send the request and receive the response:
		CalendarEventFeed eventFeed = _calendarService.query(myQuery, CalendarEventFeed.class);
		_eventList = eventFeed.getEntries();
		
	}
	
	public void displayEvents() {
		for(int i = 0; i < _eventList.size(); i++) {
			//displayLine(_eventList.get(i).getId().toString());
			displayLine(_eventList.get(i).getTitle().getPlainText());
		}
	}
	
	private boolean isIdEqual(String id1, String id2) {
		String shortId1 = getIdWithoutParentUrl(id1);
		String shortId2 = getIdWithoutParentUrl(id2);
		
		return shortId1.equals(shortId2);
	}
	
	private String getIdWithoutParentUrl(String taskId) {
		int beginIndex = taskId.lastIndexOf(URL_SEPARATOR);
		String shortTaskId = taskId.substring(beginIndex);
		
		return shortTaskId;
	}
	
	// returns an event with the specified title, start and end time
	private CalendarEventEntry constructEvent(String taskTitle, String taskStartStr, String taskEndStr) {
		CalendarEventEntry event = new CalendarEventEntry();

		event.setTitle(new PlainTextConstruct(taskTitle));
		DateTime startTime = new DateTime();
		
		if(taskStartStr != null) {
			startTime = DateTime.parseDateTime(taskStartStr);
		}
		
		DateTime endTime = new DateTime();
		
		if(taskEndStr != null) {
			endTime = DateTime.parseDateTime(taskEndStr);
		}
		
		When eventTimes = new When();
		eventTimes.setStartTime(startTime);
		eventTimes.setEndTime(endTime);
		event.addTime(eventTimes);
		
		return event;
	}
	
	// _calendarService is initialized with userEmail and userPassword
	private void initializeCalendarService() throws AuthenticationException {
		_calendarService = new CalendarService(APP_NAME);
		_calendarService.setUserCredentials(_userEmail, _userPassword);
	}

	private void displayLine(String displayString) {
		System.out.println(displayString);
	}
}
