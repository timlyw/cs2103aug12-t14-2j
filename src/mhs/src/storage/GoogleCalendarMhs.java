package mhs.src.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import com.google.gdata.client.GoogleAuthTokenFactory.UserToken;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ResourceNotFoundException;
import com.google.gdata.util.ServiceException;


/**
 * This class provides services to connect with a user's Google Calendar
 * Supported functionality: 
 * 		1) Create event entry 
 * 		2) Retrieve event entry(s) 
 * 		3) Update event entry 
 * 		4) delete event entry
 * 
 * To use the above services: 
 * 		1) retrieve access token with user's email and password 
 * 		2) create an instance of this class with retrieved access token
 * 
 * @author John Wong
 */

public class GoogleCalendarMhs {
	private static final String TASK_CALENDAR_TITLE = "Completed Tasks (MHS)";
	
	private GoogleCalendar defaultCalendar;
	private GoogleCalendar taskCalendar;
	
	/**
	 * Retrieves the Google Calendar access token using user's email and
	 * password
	 * 
	 * @param appName name of application
	 * @param email user's Google account email
	 * @param userPassword user's Google account password
	 * @return Google Calendar access token
	 * @throws AuthenticationException invalid login parameters or Internet connection unavailable
	 */
	public static String retrieveUserToken(String appName, String email,
			String password) throws AuthenticationException,
			NullPointerException {

		CalendarService calService = new CalendarService(appName);
		calService.setUserCredentials(email, password);
		UserToken token = getTokenFromService(calService);
		String tokenString = token.getValue();
		return tokenString;
	}
	
	public GoogleCalendarMhs(String appName, String email, String accessToken)
			throws NullPointerException, IOException, ServiceException {
		defaultCalendar = new GoogleCalendar(appName, email, accessToken);
		String defaultCalendarId = defaultCalendar.getDefaultCalendarId(email);
		defaultCalendar.setCalendarId(defaultCalendarId);
		
		taskCalendar = new GoogleCalendar(appName, email, accessToken);
		String taskCalendarId = taskCalendar.createCalendar(TASK_CALENDAR_TITLE);
		taskCalendar.setCalendarId(taskCalendarId);
	}
	
	public CalendarEventEntry createEvent(Task newTask) throws IOException,
			ServiceException, UnknownHostException, NullPointerException {
		if (isTaskFloating(newTask)) {
			return null;
		}
		String title = newTask.getTaskName();
		String startTime = newTask.getStartDateTime().toString();
		String endTime = newTask.getEndDateTime().toString();
		CalendarEventEntry createdEvent = null;
		
		if(newTask.isDone()) {
			createdEvent = taskCalendar.createEvent(title, startTime, endTime);	
		} else {
			createdEvent = defaultCalendar.createEvent(title, startTime, endTime);
		}
		
		return createdEvent;
	}
	
	public CalendarEventEntry retrieveEvent(String eventId) throws IOException,
			NullPointerException, ResourceNotFoundException {
		CalendarEventEntry retrievedEvent = null;		
		try {
			retrievedEvent = defaultCalendar.retrieveEvent(eventId);
		} catch(ResourceNotFoundException e) {
			retrievedEvent = taskCalendar.retrieveEvent(eventId);	
		}
		
		return retrievedEvent;
	}
	
	public List<CalendarEventEntry> retrieveEvents(String startTime, String endTime)
			throws IOException, ServiceException, UnknownHostException,
			NullPointerException {
		List<CalendarEventEntry> eventList = defaultCalendar.retrieveEvents(startTime, endTime);
		List<CalendarEventEntry> taskList = taskCalendar.retrieveEvents(startTime, endTime);
		eventList.addAll(taskList);
		
		return eventList;
	}

	
	public CalendarEventEntry updateEvent(Task updatedTask) throws IOException,
			ServiceException, NullPointerException {
		String eventId = updatedTask.getgCalTaskId();
		String title = updatedTask.getTaskName();
		String startTime = updatedTask.getStartDateTime().toString();
		String endTime = updatedTask.getEndDateTime().toString();
		boolean taskIsDone = updatedTask.isDone();
		CalendarEventEntry updatedEvent = null;
		
		if(defaultCalendar.contains(eventId)) {
			if(taskIsDone) {
				defaultCalendar.deleteEvent(eventId);
				updatedEvent = taskCalendar.createEvent(title, startTime, endTime);
			} else {
				updatedEvent = defaultCalendar.updateEvent(eventId, title,
					startTime, endTime);
			}
		} else {
			if(taskIsDone) {
				updatedEvent = taskCalendar.updateEvent(eventId, title, startTime, endTime);
			} else {
				taskCalendar.deleteEvent(eventId);
				defaultCalendar.createEvent(title, startTime, endTime);
			}
		}
		
		return updatedEvent;
	}
	
	public void deleteEvent(String eventId) throws IOException,
	ServiceException, NullPointerException, ResourceNotFoundException {
		try {
			defaultCalendar.deleteEvent(eventId);
		} catch(ResourceNotFoundException e) {
			taskCalendar.deleteEvent(eventId);
		}
	}
	
	public void deleteEvents(String startTime, String endTime)
			throws UnknownHostException, NullPointerException, IOException,
			ServiceException, ResourceNotFoundException {
		
		defaultCalendar.deleteEvents(startTime, endTime);
		taskCalendar.deleteEvents(startTime, endTime);
	}
	
	public boolean isDeleted(CalendarEventEntry calendarEvent) {
		return defaultCalendar.isDeleted(calendarEvent);
	}
	
	private static boolean isTaskFloating(Task task)
			throws NullPointerException {
		return task.getTaskCategory().equals(TaskCategory.FLOATING);
	}
	
	/**
	 * get the user token of specified calendar service
	 * 
	 * @param calService service to get token from
	 * @return token of service
	 */
	private static UserToken getTokenFromService(CalendarService calService)
			throws NullPointerException {
		UserToken token = (UserToken) calService.getAuthTokenFactory()
				.getAuthToken();
		return token;
	}
}
