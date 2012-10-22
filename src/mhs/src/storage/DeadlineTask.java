/**
 * Deadline Task - inherits from superclass Task
 * 
 * @author timlyw
 */

package mhs.src.storage;

import org.joda.time.DateTime;

import com.google.gdata.data.calendar.CalendarEventEntry;

public class DeadlineTask extends Task {

	private DateTime endDateTime;

	/**
	 * Constructor with String taskCategory
	 * 
	 * @param taskId
	 * @param taskName
	 * @param taskCategory
	 * @param endDt
	 * @param createdDt
	 * @param updatedDt
	 * @param syncDt
	 * @param gCalTaskId
	 * @param isDone
	 * @param isDeleted
	 */
	public DeadlineTask(int taskId, String taskName, String taskCategory,
			DateTime endDt, DateTime createdDt, DateTime updatedDt,
			DateTime syncDt, String gCalTaskId, boolean isDone,
			boolean isDeleted) {
		super(taskId, taskName, taskCategory, createdDt, updatedDt, syncDt,
				gCalTaskId, isDone, isDeleted);
		setEndDateTime(endDt);
	}

	/**
	 * Constructor with TaskCategory taskCategory
	 * 
	 * @param taskId
	 * @param taskName
	 * @param taskCategory
	 * @param endDt
	 * @param createdDt
	 * @param updatedDt
	 * @param syncDt
	 * @param gCalTaskId
	 * @param isDone
	 * @param isDeleted
	 */
	public DeadlineTask(int taskId, String taskName, TaskCategory taskCategory,
			DateTime endDt, DateTime createdDt, DateTime updatedDt,
			DateTime syncDt, String gCalTaskId, boolean isDone,
			boolean isDeleted) {
		super(taskId, taskName, taskCategory, createdDt, updatedDt, syncDt,
				gCalTaskId, isDone, isDeleted);
		setEndDateTime(endDt);
	}

	/**
	 * Constructor from Google CalendarEventEntry
	 * 
	 * @param taskId
	 * @param gCalEntry
	 */
	public DeadlineTask(int taskId, CalendarEventEntry gCalEntry,
			DateTime syncDateTime) {
		super(taskId, gCalEntry.getTitle().getPlainText(),
				TaskCategory.DEADLINE, syncDateTime, syncDateTime,
				syncDateTime, gCalEntry.getIcalUID(), false, false);
		setEndDateTime(new DateTime(gCalEntry.getTimes().get(0).getEndTime()
				.toString()));
	}

	/**
	 * Return endDateTime for startDateTime
	 */
	public DateTime getStartDateTime() {
		return endDateTime;
	}

	public void setStartDateTime(DateTime endDateTime) {
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
}