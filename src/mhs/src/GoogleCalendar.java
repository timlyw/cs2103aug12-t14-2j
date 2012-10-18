/*
 * This class provides services to connect with a user's Google Calendar
 * Supported functionality:
 * 		1) Create Event
 * 		2) Retrieve Event(s)
 * 		3) Update Event
 * 		4) Delete Event
 */

package mhs.src;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import com.google.gdata.client.GoogleAuthTokenFactory.UserToken;
import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class GoogleCalendar {

	static final String URL_EVENT_FEED = "https://www.google.com/calendar/feeds/default/private/full";
	static final String URL_CREATE_EVENT = "http://www.google.com/calendar/feeds/%1$s/private/full";
	static final String URL_EVENT = "http://www.google.com/calendar/feeds/%1$s/private/full/%2$s";
	static final String ID_SEPARATOR = "@";
	static final int STRING_NOT_FOUND_VALUE = -1;
	static final int TIME_COMPARE_END_INDEX = 18;
	static final int MAX_RESULTS = 999;
	static final String PARAMETER_SHOW_DELETED = "showdeleted";
	static final String TRUE = "true";

	static String userEmail = null;
	static String authorizationToken = null;
	private CalendarService calendarService;

	/**
	 * Retrieves the Google Calendar access token using user's email and password
	 * 
	 * @param appName name of application
	 * @param userEmail user's Google account email 
	 * @param userPassword user's Google account password
	 * @return Google Calendar access token
	 * @throws AuthenticationException invalid login parameters or Internet connection unavailable
	 */
	public static String retrieveAccessToken(String appName, String userEmail,
			String userPassword) throws AuthenticationException {
		
		CalendarService calService = new CalendarService(appName);
		calService.setUserCredentials(userEmail, userPassword);
		UserToken userToken = getTokenFromService(calService);
		String tokenString = userToken.getValue();
		return tokenString;
	}

	/**
	 * Creates an instance of GoogleCalendar using the user's access token and email
	 * 
	 * @param accessToken retrieved from retrieveAccessToken or otherwise
	 * @param userEmail user's Google account email 
	 * @param appName name of application
	 */
	public GoogleCalendar(String accessToken, String userEmail, String appName) {
		initCalendarService(accessToken, appName);
		GoogleCalendar.userEmail = userEmail;
	}

	/**
	 * initialize an instance of CalendarService using the user's access token and email
	 * 
	 * @param accessToken retrieved from retrieveAccessToken or otherwise
	 * @param appName name of application
	 */
	public void initCalendarService(String accessToken, String appName) {
		calendarService = new CalendarService(appName);
		calendarService.setUserToken(accessToken);
	}

	/**
	 * create an event in the current user's default Google Calendar
	 * time format: YYYY-MM-DDTHH:MM:SS+HH:MM
	 * example: 29 October 2012 3pm +0800 GMT, "2012-10-29T15:00:00+08:00"
	 * 
	 * @param title name of event
	 * @param startTime start date and time of event
	 * @param endTime end date and time of event
	 * @return created event entry
	 * @throws IOException unable to write to Google Calendar
	 * @throws ServiceException Internet connection unavailable
	 */
	public CalendarEventEntry createEvent(String title, String startTime,
			String endTime) throws IOException, ServiceException {
		
		URL postURL = createPostUrl();
		CalendarEventEntry newEvent = constructEvent(title, startTime, endTime);
		CalendarEventEntry createdEvent = calendarService.insert(postURL, newEvent);
		return createdEvent;
	}

	/**
	 * create an event based on a Task object
	 * 
	 * @param newTask Task object to reference
	 * @return created event entry
	 * @throws IOException unable to write to Google Calendar
	 * @throws ServiceException Internet connection unavailable
	 * @throws UnknownHostException Internet connection unavailable
	 */
	public CalendarEventEntry createEvent(Task newTask) throws IOException,
			ServiceException, UnknownHostException {
		if (isTaskFloating(newTask)) {
			return null;
		}
		String title = newTask.getTaskName();
		String startTime = newTask.getStartDateTime().toString();
		String endTime = newTask.getEndDateTime().toString();
		CalendarEventEntry createdEvent = createEvent(title, startTime, endTime);
		return createdEvent;
	}

	/**
	 * retrieve an event from Google Calendar based on the event's ID
	 * 
	 * @param eventId Google Calendar Event ID
	 * @return retrieved event entry, null if not found
	 * @throws IOException unable to read from Google Calendar
	 */
	public CalendarEventEntry retrieveEvent(String eventId) throws IOException {
		try {
			CalendarEventEntry retrievedEvent = constructEvent(eventId);
			return retrievedEvent;
		} catch (ServiceException e) {
			return null;
		}
	}

	/**
	 * update an event's parameters based on the specified event ID
	 * 
	 * @param eventId Google Calendar event ID
	 * @param title new name of event
	 * @param startTime new start date and time of event
	 * @param endTime new end date and time of event
	 * @return updated event entry
	 * @throws IOException unable to write to Google Calendar
	 * @throws ServiceException Internet connection unavailable
	 */
	
	public CalendarEventEntry updateEvent(String eventId, String title,
			String startTime, String endTime) throws IOException,
			ServiceException {
		CalendarEventEntry eventToBeUpdated = constructEvent(eventId);
		setEventTitle(eventToBeUpdated, title);
		setEventTime(eventToBeUpdated, startTime, endTime);
		CalendarEventEntry updatedEvent = sendEditRequest(eventToBeUpdated);
		return updatedEvent;
	}

	/**
	 * sends the request to update event's parameters
	 * 
	 * @param eventToBeUpdated previously created event with updated parameters
	 * @return updated event entry
	 * @throws IOException unable to write to Google Calendar
	 * @throws ServiceException Internet connection unavailable
	 */
	public CalendarEventEntry sendEditRequest(
			CalendarEventEntry eventToBeUpdated) throws IOException,
			ServiceException {
		URL editUrl = createEditUrl(eventToBeUpdated);
		CalendarEventEntry updatedEntry = (CalendarEventEntry) calendarService
				.update(editUrl, eventToBeUpdated);
		return updatedEntry;
	}
	
	
	/**
	 * delete an event from Google Calendar
	 * 
	 * @param eventId Google Calendar event ID
	 * @throws IOException unable to write to Google Calendar
	 * @throws ServiceException Internet connection unavailable
	 */
	public void deleteEvent(String eventId) throws IOException,
			ServiceException {
		CalendarEventEntry eventToBeDeleted = constructEvent(eventId);
		eventToBeDeleted.delete();
	}

	/**
	 * @param calendarEvent
	 * @return event's title as a String
	 */
	public static String getEventTitle(CalendarEventEntry calendarEvent) {
		return calendarEvent.getTitle().getPlainText();
	}

	/**
	 * @param calendarEvent
	 * @return event's start date and time as a String
	 */
	public static String getEventStartTime(CalendarEventEntry calendarEvent) {
		return calendarEvent.getTimes().get(0).getStartTime().toString();
	}

	/**
	 * @param calendarEvent
	 * @return event's end date and time as a String
	 */
	public static String getEventEndTime(CalendarEventEntry calendarEvent) {
		return calendarEvent.getTimes().get(0).getEndTime().toString();
	}

	/**
	 * @param time1
	 * @param time2
	 * @return true if time1 and time2 are equal, false otherwise
	 */
	public static boolean isTimeEqual(String time1, String time2) {
		String timeSub1 = time1.substring(0, TIME_COMPARE_END_INDEX);
		String timeSub2 = time1.substring(0, TIME_COMPARE_END_INDEX);
		return timeSub1.equals(timeSub2);
	}

	/**
	 * get a list of events within the range specified
	 * 
	 * @param startTime start date and time of range
	 * @param endTime end date and time of range
	 * @return list of events within range
	 * @throws UnknownHostException Internet connection unavailable
	 * @throws IOException Unable to read from GoogleCalendar
	 * @throws ServiceException Internet connection unavailable
	 */
	public List<CalendarEventEntry> retrieveEvents(String startTime,
			String endTime) throws UnknownHostException, IOException,
			ServiceException {
		DateTime start = DateTime.parseDateTime(startTime);
		DateTime end = DateTime.parseDateTime(endTime);
		return retrieveEvents(start, end);
	}
	
	/**
	 * get a list of events within the range specified
	 * 
	 * @param start start date and time of range
	 * @param end end date and time of range
	 * @return list of events within range
	 * @throws IOException Unable to read from GoogleCalendar
	 * @throws ServiceException Internet connection unavailable
	 * @throws UnknownHostException Internet connection unavailable
	 */
	public List<CalendarEventEntry> retrieveEvents(DateTime start, DateTime end)
			throws IOException, ServiceException, UnknownHostException {
		CalendarQuery calendarQuery = createCalendarQuery();
		setQueryParametersForRetrieval(calendarQuery, start, end);
		CalendarEventFeed eventFeed = getEventFeed(calendarQuery);
		List<CalendarEventEntry> eventList = eventFeed.getEntries();
		return eventList;
	}

	/**
	 * @return a CalendarQuery instance
	 * @throws MalformedURLException URL specified is invalid
	 */
	private CalendarQuery createCalendarQuery() throws MalformedURLException {
		URL feedUrl = new URL(URL_EVENT_FEED);
		CalendarQuery calendarQuery = new CalendarQuery(feedUrl);
		return calendarQuery;
	}

	/**
	 * @param task
	 * @return whether task belongs to floating category
	 */
	private static boolean isTaskFloating(Task task) {
		return task.taskCategory.equals(TaskCategory.FLOATING);
	}

	/**
	 * sets the calendarQuery range to specified start and end
	 * sets the calendarQuery maximum result limit
	 * sets the calendarQuery to retrieve deleted events
	 * 
	 * @param calendarQuery
	 * @param start start date and time of query
	 * @param end end date and time of query
	 */
	private static void setQueryParametersForRetrieval(
			CalendarQuery calendarQuery, DateTime start, DateTime end) {
		setDateOfEventsRange(calendarQuery, start, end);
		setMaxResults(calendarQuery, MAX_RESULTS);
		setQueryToShowDeletedEvents(calendarQuery);
	}
	
	private static void setDateOfEventsRange(CalendarQuery calendarQuery,
			DateTime start, DateTime end) {
		calendarQuery.setMinimumStartTime(start);
		calendarQuery.setMaximumStartTime(end);
	}

	private static void setMaxResults(CalendarQuery calendarQuery,
			int maxResults) {
		calendarQuery.setMaxResults(maxResults);
	}

	private static void setQueryToShowDeletedEvents(CalendarQuery calendarQuery) {
		calendarQuery.setStringCustomParameter(PARAMETER_SHOW_DELETED, TRUE);
	}

	private CalendarEventFeed getEventFeed(CalendarQuery calendarQuery)
			throws IOException, ServiceException {
		return calendarService.query(calendarQuery, CalendarEventFeed.class);
	}

	private URL createEditUrl(CalendarEventEntry calendarEvent)
			throws MalformedURLException {
		String urlString = calendarEvent.getEditLink().getHref();
		URL editUrl = new URL(urlString);
		return editUrl;
	}

	private CalendarEventEntry constructEvent(String eventId)
			throws IOException, ServiceException {
		String refinedId = refineEventId(eventId);
		URL eventUrl = createEventUrl(refinedId);
		CalendarEventEntry constructedEvent = calendarService.getEntry(
				eventUrl, CalendarEventEntry.class);
		return constructedEvent;
	}

	private URL createEventUrl(String eventId) throws MalformedURLException {
		String urlString = String.format(URL_EVENT, userEmail, eventId);
		URL eventUrl = new URL(urlString);
		return eventUrl;
	}

	private URL createPostUrl() throws MalformedURLException {
		String urlString = String.format(URL_CREATE_EVENT, userEmail);
		URL postUrl = new URL(urlString);
		return postUrl;
	}

	private CalendarEventEntry constructEvent(String title, String startTime,
			String endTime) {
		DateTime start = DateTime.parseDateTime(startTime);
		DateTime end = DateTime.parseDateTime(endTime);
		return constructEvent(title, start, end);
	}

	private CalendarEventEntry constructEvent(String title, DateTime start,
			DateTime end) {
		CalendarEventEntry constructedEvent = new CalendarEventEntry();
		setEventTitle(constructedEvent, title);
		setEventTime(constructedEvent, start, end);

		return constructedEvent;
	}

	private void setEventTime(CalendarEventEntry calendarEvent,
			String startTime, String endTime) {
		DateTime start = DateTime.parseDateTime(startTime);
		DateTime end = DateTime.parseDateTime(endTime);
		setEventTime(calendarEvent, start, end);
	}

	private void setEventTime(CalendarEventEntry calendarEvent, DateTime start,
			DateTime end) {
		When eventTime = createEventTime(start, end);
		calendarEvent.getTimes().clear();
		calendarEvent.addTime(eventTime);
	}

	private void setEventTitle(CalendarEventEntry calendarEvent, String title) {
		PlainTextConstruct titleText = new PlainTextConstruct(title);
		calendarEvent.setTitle(titleText);
	}

	private When createEventTime(DateTime start, DateTime end) {
		When eventWhen = new When();
		eventWhen.setStartTime(start);
		eventWhen.setEndTime(end);
		return eventWhen;
	}
	
	private static UserToken getTokenFromService(CalendarService calService) {
		UserToken token = (UserToken) calService.getAuthTokenFactory().getAuthToken();
		return token;
	}

	private String refineEventId(String eventId) {
		int endIndex = eventId.indexOf(ID_SEPARATOR);
		if (endIndex == STRING_NOT_FOUND_VALUE) {
			return eventId;
		}
		String refinedId = eventId.substring(0, endIndex);
		return refinedId;
	}
}
