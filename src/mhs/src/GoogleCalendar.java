package mhs.src;

import com.google.gdata.client.GoogleAuthTokenFactory.UserToken;
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

	static String userEmail = "cs2103mhs@gmail.com";
	static String userPassword = "myhotsec2103";

	static String authToken;
	private CalendarService calendarService;
	private List<CalendarEventEntry> eventList;

	String minStartTime = "2012-09-01T00:00:00";
	String maxStartTime = "2012-09-29T23:59:59";

	/**
	 * Constructor
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	public GoogleCalendar() throws IOException, ServiceException {
		// setup the calendar service with userEmail and userPassword
		initializeCalendarService();
		// pull events from user's calendar
		pullEvents();
	}

	/**
	 * Constructor with accessToken provided
	 * 
	 * @param accessToken
	 * @throws IOException
	 * @throws ServiceException
	 */
	public GoogleCalendar(String accessToken) throws IOException,
			ServiceException {
		// setup the calendar service with userEmail and userPassword
		initializeCalendarServiceWithAuthToken(accessToken);
		// pull events from user's calendar
		pullEvents();
	}

	/**
	 * Returns an event with taskId matching input parameter
	 * 
	 * @param taskId
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ServiceException
	 */
	public CalendarEventEntry getEvent(String taskId)
			throws MalformedURLException, IOException, ServiceException {
		if (taskId == null) {
			return null;
		}
		CalendarEventEntry event;
		for (int i = 0; i < eventList.size(); i++) {
			event = eventList.get(i);
			String currId = event.getIcalUID();
			if (currId != null && currId.equals(taskId)) {
				return event;
			}
		}
		return null;
	}

	/**
	 * Create an event in user's calendar with specified title, start and end
	 * time
	 * 
	 * @param taskTitle
	 * @param taskStartStr
	 * @param taskEndStr
	 * @param taskUpdated
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	public CalendarEventEntry createEvent(String taskTitle,
			String taskStartStr, String taskEndStr) throws IOException,
			ServiceException {
		URL postURL = new URL(String.format(URL_CREATE_EVENT, userEmail));
		CalendarEventEntry event = constructEvent(taskTitle, taskStartStr,
				taskEndStr);

		// add event to list
		eventList.add(event);

		return calendarService.insert(postURL, event);
	}

	/**
	 * 
	 * @param taskId
	 * @param newTitle
	 * @param newStartTime
	 * @param newEndTime
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	public CalendarEventEntry updateEvent(String taskId, String newTitle,
			String newStartTime, String newEndTime) throws IOException,
			ServiceException {

		CalendarEventEntry event = getEvent(taskId);

		// TODO refine null event as exception handling
		// Pull events again to double-check
		if (event == null) {
			// try pulling events
			pullEvents();
			event = getEvent(taskId);
			if(event == null){
				return null;	
			}
		}

		event.setTitle(new PlainTextConstruct(newTitle));

		When eventUpdatedTimes = new When();
		DateTime updatedStartTime = DateTime.parseDateTime(newStartTime);
		DateTime updatedEndTime = DateTime.parseDateTime(newEndTime);
		eventUpdatedTimes.setStartTime(updatedStartTime);
		eventUpdatedTimes.setEndTime(updatedEndTime);
		event.getTimes().clear();
		event.addTime(eventUpdatedTimes);

		URL editUrl = new URL(event.getEditLink().getHref());
		CalendarEventEntry updatedEntry = (CalendarEventEntry) calendarService
				.update(editUrl, event, "*");
				
		// update event in list
		// remove existing event
		for (int i = 0; i < eventList.size(); i++) {
			if (eventList.get(i).getIcalUID() != null
					&& updatedEntry.getIcalUID().equals(
							eventList.get(i).getIcalUID())) {
				eventList.remove(i);
				break;
			}
		}
		
		// add updated event to list
		eventList.add(updatedEntry);
		return updatedEntry;
	}

	/**
	 * delete the event matching the specified taskId
	 * 
	 * @param taskId
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void deleteEvent(String taskId) throws IOException, ServiceException {

		CalendarEventEntry eventToDelete = null;

		for (int i = 0; i < eventList.size(); i++) {
			String currId = eventList.get(i).getIcalUID();
			if (currId != null && currId.equals(taskId)) {
				eventToDelete.delete();
				eventList.remove(i);
				break;
			}
		}
	}

	/**
	 * Get event list from google calendar
	 * 
	 * @return
	 */
	public List<CalendarEventEntry> getEventList() {
		return eventList;
	}

	// TODO!!! make auto-pull logic
	/**
	 * get user's events from calendar
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void pullEvents() throws IOException, ServiceException {
		URL feedUrl = new URL(URL_EVENT_FEED);
		CalendarQuery myQuery = new CalendarQuery(feedUrl);
		myQuery.setMinimumStartTime(DateTime.parseDateTime(minStartTime));
		myQuery.setMaximumStartTime(DateTime.parseDateTime(maxStartTime));

		// Send the request and receive the response:
		CalendarEventFeed eventFeed = calendarService.query(myQuery,
				CalendarEventFeed.class);
		eventList = eventFeed.getEntries();
	}

	/**
	 * Display Events
	 */
	public void displayEvents() {
		for (int i = 0; i < eventList.size(); i++) {
			displayLine(eventList.get(i).getTitle().getPlainText());
		}
	}

	public CalendarEventEntry createEvent(Task taskToAdd) throws IOException,
			ServiceException {
		if (taskToAdd.taskCategory.equals(TaskCategory.FLOATING)) {
			return null;
		}
		URL postURL = new URL(String.format(URL_CREATE_EVENT, userEmail));
		CalendarEventEntry event = constructEvent(taskToAdd.getTaskName(),
				taskToAdd.getStartDateTime().toString(), taskToAdd
						.getEndDateTime().toString());
		pullEvents();
		return calendarService.insert(postURL, event);
	}

	// returns an event with the specified title, start and end time
	private CalendarEventEntry constructEvent(String taskTitle,
			String taskStartStr, String taskEndStr) {
		CalendarEventEntry event = new CalendarEventEntry();

		event.setTitle(new PlainTextConstruct(taskTitle));
		DateTime startTime = new DateTime();

		if (taskStartStr != null) {
			startTime = DateTime.parseDateTime(taskStartStr);
		}

		DateTime endTime = new DateTime();

		if (taskEndStr != null) {
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
		calendarService = new CalendarService(APP_NAME);
		calendarService.setUserCredentials(userEmail, userPassword);
		setAuthToken();
	}
	
	public void initializeCalendarService(String userEmail, String userPassword) throws AuthenticationException {
		calendarService = new CalendarService(APP_NAME);
		calendarService.setUserCredentials(userEmail, userPassword);
		setAuthToken();
	}


	/**
	 * Initializes google service with auth token (valid after first credential
	 * setting)
	 * 
	 * @param accessToken
	 */
	private void initializeCalendarServiceWithAuthToken(String accessToken) {
		calendarService = new CalendarService(APP_NAME);
		calendarService.setUserToken(accessToken);
	}

	private void setAuthToken() {
		UserToken auth_token = (UserToken) calendarService
				.getAuthTokenFactory().getAuthToken();
		authToken = auth_token.getValue();
	}

	public String getAuthToken() {
		if (authToken != null) {
			return authToken;
		}
		return null;
	}

	private void displayLine(String displayString) {
		System.out.println(displayString);
	}

}
