//@author A0087048X
package mhs.src.storage.persistence.task;

import java.util.logging.Logger;

import mhs.src.common.HtmlCreator;
import mhs.src.common.MhsLogger;

import org.joda.time.DateTime;

import com.google.api.services.calendar.model.Event;

/**
 * TimedTask
 * 
 * Timed Task object
 * 
 * - inherits from base class Task<br>
 * - Task with startDateTime and endDateTime<br>
 * - syncs with google calendar
 * 
 * @author Timothy Lim Yi Wen A0087048X
 * 
 */
public class TimedTask extends Task {

	private DateTime startDateTime;
	private DateTime endDateTime;
	private String gCalTaskId;
	private String gCalTaskUid;

	static final Logger logger = MhsLogger.getLogger();

	/**
	 * Full Constructor with TaskCategory taskCategory
	 * 
	 * @param taskId
	 * @param taskName
	 * @param taskCategory
	 * @param startDt
	 * @param endDt
	 * @param createdDt
	 * @param updatedDt
	 * @param syncDt
	 * @param gCalTaskId
	 * @param gCalTaskUid
	 * @param isDone
	 * @param isDeleted
	 */
	public TimedTask(int taskId, String taskName, TaskCategory taskCategory,
			DateTime startDt, DateTime endDt, DateTime createdDt,
			DateTime updatedDt, DateTime syncDt, String gCalTaskId,
			String gCalTaskUid, boolean isDone, boolean isDeleted) {
		super(taskId, taskName, taskCategory, createdDt, updatedDt, syncDt,
				isDone, isDeleted);
		logEnterMethod("TimedTask");
		setGcalTaskId(gCalTaskId);
		setGcalTaskUid(gCalTaskUid);
		setStartDateTime(startDt);
		setEndDateTime(endDt);
		logExitMethod("TimedTask");

	}

	/**
	 * Construct synced TimedTask from Google CalendarEventEntry
	 * 
	 * @param taskId
	 * @param gCalEntry
	 */
	public TimedTask(int taskId, Event gCalEntry, DateTime syncDateTime) {
		super(taskId, gCalEntry.getSummary(), TaskCategory.TIMED, syncDateTime,
				syncDateTime, syncDateTime, false, false);
		logEnterMethod("TimedTask");
		this.gCalTaskId = gCalEntry.getId().toString();
		this.gCalTaskUid = gCalEntry.getICalUID().toString();
		this.startDateTime = new DateTime(gCalEntry.getStart().getDateTime()
				.getValue());
		this.endDateTime = new DateTime(gCalEntry.getEnd().getDateTime()
				.getValue());
		logExitMethod("TimedTask");
	}

	public DateTime getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(DateTime startDateTime) {
		this.startDateTime = startDateTime;
	}

	public DateTime getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(DateTime endDateTime) {
		this.endDateTime = endDateTime;
	}

	public String getgCalTaskId() {
		return gCalTaskId;
	}

	public void setGcalTaskId(String gCalTaskId) {
		this.gCalTaskId = gCalTaskId;
	}

	public String getgCalTaskUid() {
		return gCalTaskUid;
	}

	public void setGcalTaskUid(String gCalTaskUid) {
		this.gCalTaskUid = gCalTaskUid;
	}

	public String toString() {
		logEnterMethod("toString");
		String taskToString = "";
		if (taskId != null) {
			taskToString += "taskId=" + taskId;
		}
		if (taskName != null) {
			taskToString += "taskName=" + taskName;
		}
		if (taskCategory != null) {
			taskToString += "taskCategory=" + taskCategory.getValue();
		}
		if (startDateTime != null) {
			taskToString += "startDateTime=" + startDateTime.toString();
		}
		if (endDateTime != null) {
			taskToString += "endDateTime=" + endDateTime.toString();
		}
		if (taskCreated != null) {
			taskToString += "taskCreated=" + taskCreated.toString();
		}
		if (taskUpdated != null) {
			taskToString += "taskUpdated=" + taskUpdated.toString();
		}
		if (taskLastSync != null) {
			taskToString += "taskLastSync=" + taskLastSync.toString();
		}
		if (gCalTaskId != null) {
			taskToString += "gCalTaskId=" + gCalTaskId;
		}
		if (gCalTaskUid != null) {
			taskToString += "gCalTaskUid=" + gCalTaskUid;
		}
		if (isDone != null) {
			taskToString += "isDone=" + isDone.toString();
		}
		if (isDeleted != null) {
			taskToString += "isDeleted=" + isDeleted.toString();
		}
		logExitMethod("toString");
		return taskToString;
	}

	/**
	 * Logger Methods
	 */

	/**
	 * Logger trace method entry
	 * 
	 * @param methodName
	 */
	void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}

	/**
	 * Logger trace method exit
	 * 
	 * @param methodName
	 */
	void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}

	/**
	 * @author John Wong
	 */
	public String toHtmlString() {
		String dateString = "";
		String endDateString = "";
		if (dateIsEqual(startDateTime, endDateTime)) {
			dateString = getDateString(startDateTime);
		} else {
			endDateString = " " + getDateString(endDateTime) + ", ";
		}

		HtmlCreator htmlCreator = new HtmlCreator();
		dateString = htmlCreator.color(dateString, HtmlCreator.BLUE);
		String timeString = getTimeString(startDateTime) + " - "
				+ endDateString + getTimeString(endDateTime);

		String boldTaskName = taskName;
		String htmlString = timeString + ": " + boldTaskName;

		if (isDone()) {
			htmlString = htmlCreator.color(htmlString + " [completed]",
					HtmlCreator.LIGHT_GRAY);
		}

		return htmlString;
	}

	private boolean dateIsEqual(DateTime date1, DateTime date2) {
		if (date1.getDayOfYear() == date2.getDayOfYear()
				&& date1.getYear() == date2.getYear()) {
			return true;
		}
		return false;
	}

	private String getDateString(DateTime date) {
		return date.toString("dd MMM yy");
	}

	private String getTimeString(DateTime date) {
		String timeString = "";

		if (date.getMinuteOfHour() == 0) {
			timeString = date.toString("h aa");
		} else {
			timeString = date.toString("h.mm aa");
		}

		timeString = timeString.toLowerCase();

		return timeString;
	}

}