/*
 * This class provides services to connect with a user's Google Calendar
 * Supported functionality:
 * 		1) Create Events
 * 		2) Retrieve Events
 * 		3) Update Events
 * 		4) Delete Events
 */

package mhs.src;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
	private static final int QUERY_DEFAULT_MAX_START_DATE_DAYS_AFTER_TODAY = 30;
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

	private DateTime queryMinStartDate;
	private DateTime queryMaxStartDate;

	/**
	 * Constructor
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	public GoogleCalendar() throws IOException, ServiceException {
		// setup the calendar service with userEmail and userPassword
		initializeCalendarService();
		// initialize default query range ( 30 days inclusive of today)
		initializeQueryDateRange(
				org.joda.time.DateTime.now().toString(),
				org.joda.time.DateTime
						.now()
						.plusDays(
								QUERY_DEFAULT_MAX_START_DATE_DAYS_AFTER_TODAY + 1)
						.toString());
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
		// initialize default query range ( 30 days inclusive of today)
		initializeQueryDateRange(
				org.joda.time.DateTime.now().toString(),
				org.joda.time.DateTime
						.now()
						.plusDays(
								QUERY_DEFAULT_MAX_START_DATE_DAYS_AFTER_TODAY + 1)
						.toString());
		// pull events from user's calendar
		pullEvents();
	}

	/**
	 * Constructor with accessToken and query start and end date
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	public GoogleCalendar(String accessToken, String minStartDateToQuery,
			String maxStartDateExclusiveToQuery) throws IOException,
			ServiceException {
		// setup the calendar service with userEmail and userPassword
		initializeCalendarService();
		// initialize query
		initializeQueryDateRange(minStartDateToQuery,
				maxStartDateExclusiveToQuery);
		// pull events from user's calendar
		pullEvents();
	}

	/**
	 * Sets a query date range from DateTime strings for querying google
	 * calendar events
	 */
	private void initializeQueryDateRange(String startMinDate,
			String startMaxDateExclusive) {
		// Ensure that startMinDate is before startMaxDateExclusive
		if (DateTime.parseDateTime(startMinDate).compareTo(
				DateTime.parseDateTime(startMaxDateExclusive)) > 0) {
			queryMaxStartDate = DateTime.parseDateTime(startMinDate);
			queryMinStartDate = DateTime.parseDateTime(startMaxDateExclusive);
		} else {
			queryMinStartDate = DateTime.parseDateTime(startMinDate);
			queryMaxStartDate = DateTime.parseDateTime(startMaxDateExclusive);
		}

		queryMinStartDate = DateTime.parseDateTime(startMinDate);
		queryMaxStartDate = DateTime.parseDateTime(startMaxDateExclusive);
		queryMinStartDate.setDateOnly(true);
		queryMaxStartDate.setDateOnly(true);
	}

	/**
	 * Returns an event with taskId matching input parameter
	 * 
	 * @param taskId
	 * @return requested event
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
	 * @return added event
	 * @throws IOException
	 * @throws ServiceException
	 */
	public CalendarEventEntry createEvent(String taskTitle,
			String taskStartStr, String taskEndStr) throws IOException,
			ServiceException {
		URL postURL = new URL(String.format(URL_CREATE_EVENT, userEmail));
		CalendarEventEntry event = constructEvent(taskTitle, taskStartStr,
				taskEndStr);
		CalendarEventEntry addedEvent = calendarService.insert(postURL, event);

		eventList.add(addedEvent);
		return addedEvent;
	}

	/**
	 * 
	 * @param taskId
	 * @param newTitle
	 * @param newStartTime
	 * @param newEndTime
	 * @return updated event
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
			if (event == null) {
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
	public void deleteEvent(String taskId) throws IOException {

		CalendarEventEntry eventToDelete = null;

		for (int i = 0; i < eventList.size(); i++) {
			String currId = eventList.get(i).getIcalUID();

			if (currId == null) {
				continue;
			}
			if (currId.equals(taskId)) {
				eventToDelete = eventList.get(i);
				try {
					eventToDelete.delete();
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				eventList.remove(i);
				break;
			}
		}
	}

	public void deleteAllEvents() throws IOException, ServiceException {
		for (int i = 0; i < eventList.size(); i++) {
			CalendarEventEntry eventToDelete = eventList.get(i);
			try {
				eventToDelete.delete();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		eventList.clear();
	}

	/**
	 * Get event list from google calendar
	 * 
	 * @return List of Calendar events
	 */
	public List<CalendarEventEntry> getEventList() {
		return eventList;
	}

	/**
	 * get user's events from calendar
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	public void pullEvents() throws IOException, ServiceException {
		URL feedUrl = new URL(URL_EVENT_FEED);
		CalendarQuery myQuery = new CalendarQuery(feedUrl);
		myQuery.setMinimumStartTime(queryMinStartDate);
		myQuery.setMaximumStartTime(queryMaxStartDate);
		/*
		 * sets max entries to retrieve (might be limited to google calendar
		 * service maximum)
		 */
		myQuery.setMaxResults(999);

		/*
		 * set updated to retrieve deleted events
		 */
		myQuery.setUpdatedMin(queryMinStartDate);
		myQuery.setUpdatedMax(queryMaxStartDate);
		myQuery.setStringCustomParameter("showdeleted", "true");

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

	/**
	 * Creates calendar event entry
	 * 
	 * @param taskToAdd
	 * @return added event
	 * @throws IOException
	 * @throws ServiceException
	 */
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

	/**
	 * returns an event with the specified title, start and end time
	 * 
	 * @param taskTitle
	 * @param taskStartStr
	 * @param taskEndStr
	 * @return
	 */
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

	/**
	 * calendarService is initialized with userEmail and userPassword
	 * 
	 * @throws AuthenticationException
	 */
	private void initializeCalendarService() throws AuthenticationException {
		calendarService = new CalendarService(APP_NAME);
		calendarService.setUserCredentials(userEmail, userPassword);
		setAuthToken();
	}

	/**
	 * CalendarService is initialized with userEmail and userPassword
	 * 
	 * @param userEmail
	 * @param userPassword
	 * @throws AuthenticationException
	 */
	public void initializeCalendarService(String userEmail, String userPassword)
			throws AuthenticationException {
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

	public String getQueryMinStartDate() {
		return queryMinStartDate.toString();

	}

	public String getQueryMaxStartDate() {
		return queryMaxStartDate.toString();
	}

	private void displayLine(String displayString) {
		System.out.println(displayString);
	}

}
