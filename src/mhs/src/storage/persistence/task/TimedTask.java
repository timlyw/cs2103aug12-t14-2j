//@author A0087048X
package mhs.src.storage.persistence.task;

import mhs.src.common.HtmlCreator;

import org.joda.time.DateTime;

import com.google.gdata.data.calendar.CalendarEventEntry;

/**
 * TimedTask
 * 
 * Timed Task object
 *  
 * - inherits from base class Task 
 * - Task with startDateTime and endDateTime
 * - syncs with google calendar
 * 
 * @author Timothy Lim Yi Wen A0087048X
 * 
 */
public class TimedTask extends Task {

	private DateTime startDateTime;
	private DateTime endDateTime;

	/**
	 * Constructor with TaskCategory taskCategory
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
	 * @param isDone
	 * @param isDeleted
	 */
	public TimedTask(int taskId, String taskName, TaskCategory taskCategory,
			DateTime startDt, DateTime endDt, DateTime createdDt,
			DateTime updatedDt, DateTime syncDt, String gCalTaskId,
			boolean isDone, boolean isDeleted) {
		super(taskId, taskName, taskCategory, createdDt, updatedDt, syncDt,
				gCalTaskId, isDone, isDeleted);
		setStartDateTime(startDt);
		setEndDateTime(endDt);

	}

	/**
	 * Constructor with String taskCategory
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
	 * @param isDone
	 * @param isDeleted
	 */
	public TimedTask(int taskId, String taskName, String taskCategory,

	DateTime startDt, DateTime endDt, DateTime createdDt, DateTime updatedDt,
			DateTime syncDt, String gCalTaskId, boolean isDone,
			boolean isDeleted) {
		super(taskId, taskName, taskCategory, createdDt, updatedDt, syncDt,
				gCalTaskId, isDone, isDeleted);
		setStartDateTime(startDt);
		setEndDateTime(endDt);
	}

	/**
	 * Constructor from Google CalendarEventEntry
	 * 
	 * @param taskId
	 * @param gCalEntry
	 */
	public TimedTask(int taskId, CalendarEventEntry gCalEntry,
			DateTime syncDateTime) {
		super(taskId, gCalEntry.getTitle().getPlainText(), TaskCategory.TIMED,
				syncDateTime, syncDateTime, syncDateTime, gCalEntry
						.getIcalUID(), false, false);
		setStartDateTime(new DateTime(gCalEntry.getTimes().get(0)
				.getStartTime().toString()));
		setEndDateTime(new DateTime(gCalEntry.getTimes().get(0).getEndTime()
				.toString()));

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

	public String toString() {
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
		if (isDone != null) {
			taskToString += "isDone=" + isDone.toString();
		}
		if (isDeleted != null) {
			taskToString += "isDeleted=" + isDeleted.toString();
		}
		return taskToString;
	}

	/**
	 * @author John Wong
	 */
	public String toHtmlString() {
		String dateString = "";
		if(dateIsEqual(startDateTime, endDateTime)) {
			dateString = getDateString(startDateTime);
		}
		
		HtmlCreator htmlCreator = new HtmlCreator();

		dateString = htmlCreator.color(dateString, HtmlCreator.BLUE);
		String timeString = getTimeString(startDateTime) + " - " + getTimeString(endDateTime);
		
		String boldTaskName = htmlCreator.makeBold(taskName);
		String htmlString = timeString + ": " + boldTaskName;
		
		return htmlString;
	}
	
	private boolean dateIsEqual(DateTime date1, DateTime date2) {
		if(date1.getDayOfYear() == date2.getDayOfYear() && date1.getYear() == date2.getYear()) {
			return true;
		}
		return false;
	}
	
	private String getDateString(DateTime date) {
		return date.toString("dd MMM yy");
	}
	
	private String getTimeString(DateTime date) {
		String timeString = "";
		
		if(date.getMinuteOfHour() == 0) {
			timeString = date.toString("h aa");
		} else {
			timeString = date.toString("h.mm aa");
		}
		
		timeString = timeString.toLowerCase();
		
		return timeString;
	}
	
	
}