package mhs.src.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

import com.google.gdata.client.GoogleAuthTokenFactory.UserToken;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ResourceNotFoundException;
import com.google.gdata.util.ServiceException;


/**
 * This class provides an interface between MHS tasks and GoogleCalendar
 * 
 * Supported functionality: 
 * 		1) Create event entry 
 * 		2) Retrieve event entry(s) 
 * 		3) Update event entry 
 * 		4) delete event entry(s)
 * 
 * This class is also used to handle two calendars: user's default calendar and
 * a second calendar with title "Completed Tasks (MHS)"
 * 
 * When a task is done, it is moved to the second calendar
 * 
 * To use the above services: 
 * 		1) retrieve access token with user's email and password 
 * 		2) create an instance of this class with retrieved access token
 * 
 * @author John Wong
 */

public class GoogleCalendarMhs {
	
	// name of calendar where completed tasks are stored
	private static final String TASK_CALENDAR_TITLE = "Completed Tasks (MHS)";
	
	// user's default calendar
	private GoogleCalendar defaultCalendar;
	
	// calendar used to store user's completed tasks
	private GoogleCalendar taskCalendar;
	
	// logger used to log function calls
	private final Logger logger = MhsLogger.getLogger();
	
	// class name used for logging
	private static final String CLASS_NAME = "UserInterface";
	
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
			NullPointerException, UnknownHostException {

		CalendarService calService = new CalendarService(appName);
		calService.setUserCredentials(email, password);
		UserToken token = getTokenFromService(calService);
		String tokenString = token.getValue();
		
		return tokenString;
	}
	
	/**
	 * @param appName
	 * @param email
	 * @param accessToken retrieved from retrieveUserToken
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws ServiceException
	 */
	public GoogleCalendarMhs(String appName, String email, String accessToken)
			throws NullPointerException, IOException, ServiceException, UnknownHostException {
		startLog("constructor");
		defaultCalendar = new GoogleCalendar(appName, email, accessToken);
		String defaultCalendarId = defaultCalendar.getDefaultCalendarId(email);
		defaultCalendar.setCalendarId(defaultCalendarId);
		
		taskCalendar = new GoogleCalendar(appName, email, accessToken);
		String taskCalendarId = taskCalendar.createCalendar(TASK_CALENDAR_TITLE);
		taskCalendar.setCalendarId(taskCalendarId);
		endLog("constructor");
	}
	
	/**
	 * creates an event in user's default calendar, if the task is already done
	 * event is created in user's completed tasks calendar instead
	 * 
	 * @param newTask
	 * @return created event entry
	 * @throws IOException
	 * @throws ServiceException
	 * @throws UnknownHostException
	 * @throws NullPointerException
	 */
	public CalendarEventEntry createEvent(Task newTask) throws IOException,
			ServiceException, UnknownHostException, NullPointerException {
		startLog("createEvent");
		if (isTaskFloating(newTask)) {
			endLog("exit createEvent as task is floating");
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
		
		endLog("createEvent");
		return createdEvent;
	}
	
	/**
	 * checks default and completed task calendar for event and returns it if found
	 * 
	 * @param eventId
	 * @return calendar event entry if found, null otherwise
	 * @throws IOException
	 * @throws NullPointerException
	 * @throws ResourceNotFoundException
	 */
	public CalendarEventEntry retrieveEvent(String eventId) throws IOException,
			NullPointerException, ResourceNotFoundException, UnknownHostException {
		startLog("retrieveEvent");
		CalendarEventEntry retrievedEvent = null;		
		retrievedEvent = defaultCalendar.retrieveEvent(eventId);
		if(retrievedEvent == null) {
			retrievedEvent = taskCalendar.retrieveEvent(eventId);
		}
		endLog("retrieveEvent");
		return retrievedEvent;
	}
	
	/**
	 * retrieve events falling within the specified start and end time,
	 * deleted events are returned as well
	 * 
	 * @param startTime
	 * @param endTime
	 * @return List of events matching time criteria
	 * @throws IOException
	 * @throws ServiceException
	 * @throws UnknownHostException
	 * @throws NullPointerException
	 */
	public List<CalendarEventEntry> retrieveEvents(String startTime, String endTime)
			throws IOException, ServiceException, UnknownHostException,
			NullPointerException {
		startLog("retrieveEvents");
		List<CalendarEventEntry> eventList = defaultCalendar.retrieveEvents(startTime, endTime);
		List<CalendarEventEntry> taskList = taskCalendar.retrieveEvents(startTime, endTime);
		eventList.addAll(taskList);
		endLog("retrieveEvents");
		
		return eventList;
	}
	
	/**
	 * update an event with existing task ID to new title, start time, end time
	 * updates the event's calendar depending on whether it is completed or not
	 * 
	 * @param updatedTask
	 * @return updated calendar event entry
	 * @throws IOException
	 * @throws ServiceException
	 * @throws NullPointerException
	 */
	public CalendarEventEntry updateEvent(Task updatedTask) throws IOException,
			ServiceException, NullPointerException, UnknownHostException {
		startLog("updateEvent");
		CalendarEventEntry updatedEvent = null;
		String eventId = updatedTask.getgCalTaskId();
		
		if(defaultCalendar.contains(eventId)) {
			updatedEvent = updateTaskInDefaultCalendar(updatedTask);
		} else {
			updatedEvent = updateTaskInTaskCalendar(updatedTask);
		}
		endLog("updateEvent");
		return updatedEvent;
	}
	
	/**
	 * deletes event in default calendar or task calendar depending on which 
	 * calendar the task is in
	 * 
	 * @param eventId
	 * @throws IOException
	 * @throws ServiceException
	 * @throws NullPointerException
	 * @throws ResourceNotFoundException
	 */
	public void deleteEvent(String eventId) throws IOException,
			ServiceException, NullPointerException, ResourceNotFoundException,
			UnknownHostException {
	startLog("deleteEvent");
		try {
			defaultCalendar.deleteEvent(eventId);
		} catch(ResourceNotFoundException e) {
			taskCalendar.deleteEvent(eventId);
		}
		endLog("deleteEvent");
	}
	
	/**
	 * deletes all events within the specified time range
	 * 
	 * @param startTime
	 * @param endTime
	 * @throws UnknownHostException
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws ResourceNotFoundException
	 */
	public void deleteEvents(String startTime, String endTime)
			throws UnknownHostException, NullPointerException, IOException,
			ServiceException, ResourceNotFoundException {
		startLog("deleteEvents");
		defaultCalendar.deleteEvents(startTime, endTime);
		taskCalendar.deleteEvents(startTime, endTime);
		endLog("deleteEvents");
	}
	
	/**
	 * checks if an event has been marked as deleted
	 * 
	 * @param calendarEvent
	 * @return is event deleted
	 */
	public boolean isDeleted(CalendarEventEntry calendarEvent) {
		return defaultCalendar.isDeleted(calendarEvent);
	}
	
	/**
	 * checks if an event is in the completed tasks calendar
	 * 
	 * @param calendarEvent
	 * @return is event in the completed tasks calendar
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws ServiceException
	 */
	public boolean isEventCompleted(CalendarEventEntry calendarEvent)
			throws NullPointerException, IOException, ServiceException,
			UnknownHostException {
	try {
			return taskCalendar.contains(calendarEvent.getIcalUID());
		} catch(ResourceNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * checks if task is of a floating type category
	 * 
	 * @param task
	 * @return is task floating
	 * @throws NullPointerException
	 */
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
	

	/**
	 * @param updatedTask
	 * @return updated calendar event entry
	 * @throws NullPointerException
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private CalendarEventEntry updateTaskInDefaultCalendar(Task updatedTask)
			throws NullPointerException, ResourceNotFoundException,
			IOException, ServiceException, UnknownHostException {
	boolean taskIsDone = updatedTask.isDone();
		
		String eventId = updatedTask.getgCalTaskId();
		String title = updatedTask.getTaskName();
		String startTime = updatedTask.getStartDateTime().toString();
		String endTime = updatedTask.getEndDateTime().toString();

		CalendarEventEntry updatedEvent = null;
		
		if(taskIsDone) {
			defaultCalendar.deleteEvent(eventId);
			updatedEvent = taskCalendar.createEvent(title, startTime, endTime);
		} else {
			updatedEvent = defaultCalendar.updateEvent(eventId, title,
				startTime, endTime);
		}
		
		return updatedEvent;
	}
	
	/**
	 * @param updatedTask
	 * @return updated calendar event entry
	 * @throws NullPointerException
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 * @throws ServiceException
	 */
	private CalendarEventEntry updateTaskInTaskCalendar(Task updatedTask)
			throws NullPointerException, ResourceNotFoundException,
			IOException, ServiceException, UnknownHostException {
	boolean taskIsDone = updatedTask.isDone();
		
		String eventId = updatedTask.getgCalTaskId();
		String title = updatedTask.getTaskName();
		String startTime = updatedTask.getStartDateTime().toString();
		String endTime = updatedTask.getEndDateTime().toString();

		CalendarEventEntry updatedEvent = null;
		
		if(taskIsDone) {
			updatedEvent = taskCalendar.updateEvent(eventId, title, startTime, endTime);
		} else {
			taskCalendar.deleteEvent(eventId);
			updatedEvent = defaultCalendar.createEvent(title, startTime, endTime);
		}
		
		return updatedEvent;
	}
	
	private void startLog(String methodName) {
		logger.entering(CLASS_NAME, methodName);
	}
	
	private void endLog(String methodName) {
		logger.entering(CLASS_NAME, methodName);
	}
	
}
