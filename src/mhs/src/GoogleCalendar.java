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

/**
 * This class provides services to connect with a user's Google Calendar
 * Supported functionality: 1) Create event entry 2) Retrieve event entry(s) 3)
 * Update event entry 4) delete event entry
 * 
 * To use the above services: 1) retrieve access token with user's email and
 * password 2) create an instance of this class with retrieved access token
 * 
 * @author John Wong
 */

public class GoogleCalendar {
	// URL constants for communicating with Google Calendar
	private static final String URL_EVENT_FEED = "https://www.google.com/calendar/feeds/default/private/full";
	private static final String URL_CREATE_EVENT = "http://www.google.com/calendar/feeds/%1$s/private/full";
	private static final String URL_EVENT = "http://www.google.com/calendar/feeds/%1$s/private/full/%2$s";

	// id format: id@google.com
	private static final String ID_SEPARATOR = "@";

	// return value of indexOf operator on a String
	private static final int STRING_NOT_FOUND_VALUE = -1;

	// returned String format of a DateTime object may differ after this index
	private static final int TIME_COMPARE_END_INDEX = 18;

	// used to set the maximum results of a Google Calendar query
	private static final int MAX_RESULTS = 999;

	// used to set a Google Calendar query to show deleted events
	private static final String PARAMETER_SHOW_DELETED = "showdeleted";
	// used to check if an event is deleted
	private static final String PARAMETER_IS_DELETED = "canceled";
	private static final String TRUE = "true";

	// user's Google account email
	private String userEmail = null;

	// access token used to authorize communication with Google Calendar
	private static String authorizationToken = null;

	// calendarService used to interface with Google Calendar
	private CalendarService calendarService;

	/**
	 * Retrieves the Google Calendar access token using user's email and
	 * password
	 * 
	 * @param appName
	 *            name of application
	 * @param email
	 *            user's Google account email
	 * @param userPassword
	 *            user's Google account password
	 * @return Google Calendar access token
	 * @throws AuthenticationException
	 *             invalid login parameters or Internet connection unavailable
	 */
	public static String retrieveAccessToken(String appName, String email,
			String password) throws AuthenticationException,
			NullPointerException {

		CalendarService calService = new CalendarService(appName);
		calService.setUserCredentials(email, password);
		UserToken userToken = getTokenFromService(calService);
		String tokenString = userToken.getValue();
		return tokenString;
	}

	/**
	 * Creates an instance of GoogleCalendar using the user's access token and
	 * email
	 * 
	 * @param accessToken
	 *            retrieved from retrieveAccessToken or otherwise
	 * @param email
	 *            user's Google account email
	 * @param appName
	 *            name of application
	 */
	public GoogleCalendar(String accessToken, String email, String appName)
			throws NullPointerException {
		initCalendarService(accessToken, appName);
		userEmail = email;
	}

	/**
	 * initialize an instance of CalendarService using the user's access token
	 * and email
	 * 
	 * @param accessToken
	 *            retrieved from retrieveAccessToken or otherwise
	 * @param appName
	 *            name of application
	 */
	public void initCalendarService(String accessToken, String appName)
			throws NullPointerException {
		calendarService = new CalendarService(appName);
		calendarService.setUserToken(accessToken);
	}

	/**
	 * create an event in the current user's default Google Calendar time
	 * format: YYYY-MM-DDTHH:MM:SS+HH:MM example: 29 October 2012 3pm +0800 GMT,
	 * "2012-10-29T15:00:00+08:00"
	 * 
	 * @param title
	 *            name of event
	 * @param startTime
	 *            start date and time of event
	 * @param endTime
	 *            end date and time of event
	 * @return created event entry
	 * @throws IOException
	 *             unable to write to Google Calendar
	 * @throws ServiceException
	 *             Internet connection unavailable
	 */
	public CalendarEventEntry createEvent(String title, String startTime,
			String endTime) throws IOException, ServiceException,
			NullPointerException {

		URL postURL = createPostUrl();
		CalendarEventEntry newEvent = constructEvent(title, startTime, endTime);
		CalendarEventEntry createdEvent = calendarService.insert(postURL,
				newEvent);
		return createdEvent;
	}

	/**
	 * create an event based on a Task object
	 * 
	 * @param newTask
	 *            Task object to reference
	 * @return created event entry
	 * @throws IOException
	 *             unable to write to Google Calendar
	 * @throws ServiceException
	 *             Internet connection unavailable
	 * @throws UnknownHostException
	 *             Internet connection unavailable
	 */
	public CalendarEventEntry createEvent(Task newTask) throws IOException,
			ServiceException, UnknownHostException, NullPointerException {
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
	 * @param eventId
	 *            Google Calendar Event ID
	 * @return retrieved event entry, null if not found
	 * @throws IOException
	 *             unable to read from Google Calendar
	 */
	public CalendarEventEntry retrieveEvent(String eventId) throws IOException,
			NullPointerException {
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
	 * @param updatedTask
	 *            task with updated parameters
	 * @return updated event entry
	 * @throws IOException
	 *             unable to write to Google Calendar
	 * @throws ServiceException
	 *             Internet connection unavailable
	 */
	public CalendarEventEntry updateEvent(Task updatedTask) throws IOException,
			ServiceException, NullPointerException {
		if (isTaskFloating(updatedTask)) {
			return null;
		}
		String eventId = updatedTask.getgCalTaskId();
		String title = updatedTask.getTaskName();
		String startTime = updatedTask.getStartDateTime().toString();
		String endTime = updatedTask.getEndDateTime().toString();
		CalendarEventEntry updatedEvent = updateEvent(eventId, title,
				startTime, endTime);
		return updatedEvent;
	}

	/**
	 * update an event's parameters based on the specified event ID
	 * 
	 * @param eventId
	 *            Google Calendar event ID
	 * @param title
	 *            new name of event
	 * @param startTime
	 *            new start date and time of event
	 * @param endTime
	 *            new end date and time of event
	 * @return updated event entry
	 * @throws IOException
	 *             unable to write to Google Calendar
	 * @throws ServiceException
	 *             Internet connection unavailable
	 */
	public CalendarEventEntry updateEvent(String eventId, String title,
			String startTime, String endTime) throws IOException,
			ServiceException, NullPointerException {
		CalendarEventEntry eventToBeUpdated = constructEvent(eventId);
		setEventTitle(eventToBeUpdated, title);
		setEventTime(eventToBeUpdated, startTime, endTime);
		CalendarEventEntry updatedEvent = sendEditRequest(eventToBeUpdated);
		return updatedEvent;
	}

	/**
	 * sends the request to update event's parameters
	 * 
	 * @param eventToBeUpdated
	 *            previously created event with updated parameters
	 * @return updated event entry
	 * @throws IOException
	 *             unable to write to Google Calendar
	 * @throws ServiceException
	 *             Internet connection unavailable
	 */
	public CalendarEventEntry sendEditRequest(
			CalendarEventEntry eventToBeUpdated) throws IOException,
			ServiceException, NullPointerException {
		URL editUrl = createEditUrl(eventToBeUpdated);
		CalendarEventEntry updatedEntry = (CalendarEventEntry) calendarService
				.update(editUrl, eventToBeUpdated);
		return updatedEntry;
	}

	/**
	 * delete an event from Google Calendar
	 * 
	 * @param eventId
	 *            Google Calendar event ID
	 * @throws IOException
	 *             unable to write to Google Calendar
	 * @throws ServiceException
	 *             Internet connection unavailable
	 */
	public void deleteEvent(String eventId) throws IOException,
			ServiceException, NullPointerException {
		CalendarEventEntry eventToBeDeleted = constructEvent(eventId);
		eventToBeDeleted.delete();
	}

	/**
	 * TODO Delete all events from Google Calendar
	 * 
	 * @param eventId
	 *            Google Calendar event ID
	 * @throws IOException
	 *             unable to write to Google Calendar
	 * @throws ServiceException
	 *             Internet connection unavailable
	 */
	public void deleteAllEvents() throws IOException, ServiceException,
			NullPointerException {
	}

	/**
	 * @param calendarEvent
	 * @return event's title as a String
	 */
	public static String getEventTitle(CalendarEventEntry calendarEvent)
			throws NullPointerException {
		return calendarEvent.getTitle().getPlainText();
	}

	/**
	 * @param calendarEvent
	 * @return event's start date and time as a String
	 */
	public static String getEventStartTime(CalendarEventEntry calendarEvent)
			throws NullPointerException {
		return calendarEvent.getTimes().get(0).getStartTime().toString();
	}

	/**
	 * @param calendarEvent
	 * @return event's end date and time as a String
	 */
	public static String getEventEndTime(CalendarEventEntry calendarEvent)
			throws NullPointerException {
		return calendarEvent.getTimes().get(0).getEndTime().toString();
	}

	/**
	 * @param time1
	 * @param time2
	 * @return true if time1 and time2 are equal, false otherwise
	 */
	public static boolean isTimeEqual(String time1, String time2)
			throws NullPointerException {
		String timeSub1 = time1.substring(0, TIME_COMPARE_END_INDEX);
		String timeSub2 = time1.substring(0, TIME_COMPARE_END_INDEX);
		return timeSub1.equals(timeSub2);
	}

	/**
	 * get a list of events within the range specified
	 * 
	 * @param startTime
	 *            start date and time of range
	 * @param endTime
	 *            end date and time of range
	 * @return list of events within range
	 * @throws UnknownHostException
	 *             Internet connection unavailable
	 * @throws IOException
	 *             Unable to read from GoogleCalendar
	 * @throws ServiceException
	 *             Internet connection unavailable
	 */
	public List<CalendarEventEntry> retrieveEvents(String startTime,
			String endTime) throws UnknownHostException, IOException,
			ServiceException, NullPointerException {
		DateTime start = DateTime.parseDateTime(startTime);
		DateTime end = DateTime.parseDateTime(endTime);
		return retrieveEvents(start, end);
	}

	/**
	 * get a list of events within the range specified
	 * 
	 * @param start
	 *            start date and time of range
	 * @param end
	 *            end date and time of range
	 * @return list of events within range
	 * @throws IOException
	 *             Unable to read from GoogleCalendar
	 * @throws ServiceException
	 *             Internet connection unavailable
	 * @throws UnknownHostException
	 *             Internet connection unavailable
	 */
	public List<CalendarEventEntry> retrieveEvents(DateTime start, DateTime end)
			throws IOException, ServiceException, UnknownHostException,
			NullPointerException {
		CalendarQuery calendarQuery = createCalendarQuery();
		setQueryParametersForRetrieval(calendarQuery, start, end);
		CalendarEventFeed eventFeed = getEventFeed(calendarQuery);
		List<CalendarEventEntry> eventList = eventFeed.getEntries();
		return eventList;
	}

	/**
	 * TODO
	 * 
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 * @throws UnknownHostException
	 * @throws NullPointerException
	 */
	public List<CalendarEventEntry> retrieveAllEvents() throws IOException,
			ServiceException, UnknownHostException, NullPointerException {
		CalendarQuery calendarQuery = createCalendarQuery();
		CalendarEventFeed eventFeed = getEventFeed(calendarQuery);
		List<CalendarEventEntry> eventList = eventFeed.getEntries();
		return eventList;
	}

	/**
	 * check if an event has been deleted from user's Google Calendar
	 * 
	 * @param calendarEvent
	 *            event entry to be checked
	 * @return if event has been deleted
	 */
	public boolean isDeleted(CalendarEventEntry calendarEvent) {
		if (calendarEvent.getStatus().getValue().contains(PARAMETER_IS_DELETED)) {
			return true;
		}
		return false;
	}

	/**
	 * @return a CalendarQuery instance
	 * @throws MalformedURLException
	 *             URL specified is invalid
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
	private static boolean isTaskFloating(Task task)
			throws NullPointerException {
		return task.taskCategory.equals(TaskCategory.FLOATING);
	}

	/**
	 * set the calendarQuery range to specified start and end date set the
	 * calendarQuery maximum result limit set the calendarQuery to retrieve
	 * deleted events
	 * 
	 * @param calendarQuery
	 *            query to be set
	 * @param start
	 *            start date and time of query
	 * @param end
	 *            end date and time of query
	 */
	private static void setQueryParametersForRetrieval(
			CalendarQuery calendarQuery, DateTime start, DateTime end)
			throws NullPointerException {
		setDateRangeOfQuery(calendarQuery, start, end);
		setMaxResults(calendarQuery, MAX_RESULTS);
		setQueryToShowDeletedEvents(calendarQuery);
	}

	/**
	 * set the calendarQuery range to specified start and end date
	 * 
	 * @param calendarQuery
	 *            query to be set
	 * @param start
	 *            start date of query range
	 * @param end
	 *            end date of query range
	 */
	private static void setDateRangeOfQuery(CalendarQuery calendarQuery,
			DateTime start, DateTime end) throws NullPointerException {
		calendarQuery.setMinimumStartTime(start);
		calendarQuery.setMaximumStartTime(end);
	}

	/**
	 * set the calendarQuery maximum result limit
	 * 
	 * @param calendarQuery
	 *            query to be set
	 * @param maxResults
	 *            max result limit
	 */
	private static void setMaxResults(CalendarQuery calendarQuery,
			int maxResults) throws NullPointerException {
		calendarQuery.setMaxResults(maxResults);
	}

	/**
	 * set the calendarQuery to retrieve deleted events
	 * 
	 * @param calendarQuery
	 *            query to be set
	 */
	private static void setQueryToShowDeletedEvents(CalendarQuery calendarQuery)
			throws NullPointerException {
		calendarQuery.setStringCustomParameter(PARAMETER_SHOW_DELETED, TRUE);
	}

	/**
	 * gets the event feed based on the specified calendar query
	 * 
	 * @param calendarQuery
	 *            specifies query parameters
	 * @return event feed of query
	 * @throws IOException
	 *             unable to read from Google Calendar
	 * @throws ServiceException
	 *             Internet connection unavailable
	 */
	private CalendarEventFeed getEventFeed(CalendarQuery calendarQuery)
			throws IOException, ServiceException, NullPointerException {
		return calendarService.query(calendarQuery, CalendarEventFeed.class);
	}

	/**
	 * create a URL that can be used to edit specified event
	 * 
	 * @param calendarEvent
	 *            event to get edit URL of
	 * @return Google Calendar edit URL
	 * @throws MalformedURLException
	 *             invalid URL format
	 */
	private URL createEditUrl(CalendarEventEntry calendarEvent)
			throws MalformedURLException, NullPointerException {
		String urlString = calendarEvent.getEditLink().getHref();
		URL editUrl = new URL(urlString);
		return editUrl;
	}

	/**
	 * construct a CalendarEventEntry based on specified eventId
	 * 
	 * @param eventId
	 * @return constructed event
	 * @throws IOException
	 *             unable to read from Google Calendar
	 * @throws ServiceException
	 *             Internet connection unavailable
	 */
	private CalendarEventEntry constructEvent(String eventId)
			throws IOException, ServiceException, NullPointerException {
		String refinedId = refineEventId(eventId);
		URL eventUrl = createEventUrl(refinedId);
		CalendarEventEntry constructedEvent = calendarService.getEntry(
				eventUrl, CalendarEventEntry.class);
		return constructedEvent;
	}

	/**
	 * create an event's URL based on specified event id
	 * 
	 * @param eventId
	 * @return event's URL
	 * @throws MalformedURLException
	 *             invalid URL format
	 */
	private URL createEventUrl(String eventId) throws MalformedURLException,
			NullPointerException {
		String urlString = String.format(URL_EVENT, userEmail, eventId);
		URL eventUrl = new URL(urlString);
		return eventUrl;
	}

	/**
	 * create a URL that can be used to post an event
	 * 
	 * @return Google Calendar post URL
	 * @throws MalformedURLException
	 *             invalid URL format
	 */
	private URL createPostUrl() throws MalformedURLException {
		String urlString = String.format(URL_CREATE_EVENT, userEmail);
		URL postUrl = new URL(urlString);
		return postUrl;
	}

	/**
	 * creates a CalendarEventEntry object based on specified parameters
	 * 
	 * @param title
	 *            name of event
	 * @param startTime
	 *            start date and time of event
	 * @param endTime
	 *            end date and time of event
	 * @return constructed event
	 */
	private CalendarEventEntry constructEvent(String title, String startTime,
			String endTime) throws NullPointerException {
		DateTime start = DateTime.parseDateTime(startTime);
		DateTime end = DateTime.parseDateTime(endTime);
		return constructEvent(title, start, end);
	}

	/**
	 * creates a CalendarEventEntry object based on specified parameters
	 * 
	 * @param title
	 *            name of event
	 * @param start
	 *            start date and time of event
	 * @param end
	 *            end date and time of event
	 * @return constructed event
	 */
	private CalendarEventEntry constructEvent(String title, DateTime start,
			DateTime end) throws NullPointerException {
		CalendarEventEntry constructedEvent = new CalendarEventEntry();
		setEventTitle(constructedEvent, title);
		setEventTime(constructedEvent, start, end);

		return constructedEvent;
	}

	/**
	 * set the event entry's start and end dateTime
	 * 
	 * @param calendarEvent
	 *            event to be set
	 * @param startTime
	 *            start date and time of event
	 * @param endTime
	 *            end date and time of event
	 */
	private void setEventTime(CalendarEventEntry calendarEvent,
			String startTime, String endTime) throws NullPointerException {
		DateTime start = DateTime.parseDateTime(startTime);
		DateTime end = DateTime.parseDateTime(endTime);
		setEventTime(calendarEvent, start, end);
	}

	/**
	 * set the event entry's start and end dateTime
	 * 
	 * @param calendarEvent
	 *            event to be set
	 * @param start
	 *            start date and time of event
	 * @param end
	 *            end date and time of event
	 */
	private void setEventTime(CalendarEventEntry calendarEvent, DateTime start,
			DateTime end) throws NullPointerException {
		When eventTime = createEventTime(start, end);
		calendarEvent.getTimes().clear();
		calendarEvent.addTime(eventTime);
	}

	/**
	 * set the event entry's title
	 * 
	 * @param calendarEvent
	 *            event to be set
	 * @param title
	 *            name of event
	 */
	private void setEventTitle(CalendarEventEntry calendarEvent, String title)
			throws NullPointerException {
		PlainTextConstruct titleText = new PlainTextConstruct(title);
		calendarEvent.setTitle(titleText);
	}

	/**
	 * create a When object based on specified parameters
	 * 
	 * @param start
	 *            start date and time of event
	 * @param end
	 *            end date and time of event
	 * @return constructed When object
	 */
	private When createEventTime(DateTime start, DateTime end)
			throws NullPointerException {
		When eventWhen = new When();
		eventWhen.setStartTime(start);
		eventWhen.setEndTime(end);
		return eventWhen;
	}

	/**
	 * get the user token of specified calendar service
	 * 
	 * @param calService
	 *            service to get token from
	 * @return token of service
	 */
	private static UserToken getTokenFromService(CalendarService calService)
			throws NullPointerException {
		UserToken token = (UserToken) calService.getAuthTokenFactory()
				.getAuthToken();
		return token;
	}

	/**
	 * if format of id is id@google.com, retrieves the id
	 * 
	 * @param eventId
	 *            unrefined event id
	 * @return refined event id
	 */
	private String refineEventId(String eventId) throws NullPointerException {
		int endIndex = eventId.indexOf(ID_SEPARATOR);
		if (endIndex == STRING_NOT_FOUND_VALUE) {
			return eventId;
		}
		String refinedId = eventId.substring(0, endIndex);
		return refinedId;
	}

	public String getAuthToken() {
		return authorizationToken;
	}

	public String getUserEmail() {
		return userEmail;
	}
}
