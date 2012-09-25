package mhs.src;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.gdata.data.calendar.CalendarEventEntry;

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
		super(taskId, taskName, taskCategory, startDt, endDt, createdDt,
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
		super(taskId, taskName, taskCategory, startDt, endDt, createdDt,
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
				syncDateTime, syncDateTime, syncDateTime, gCalEntry.getIcalUID(),
				false, false);
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
		String taskToString = "taskId=" + taskId + "taskName=" + taskName
				+ "taskCategory=" + taskCategory.getValue() + "startDateTime="
				+ startDateTime.toString() + "endDateTime="
				+ endDateTime.toString() + "taskCreated="
				+ taskCreated.toString() + "taskUpdated="
				+ taskUpdated.toString() + "taskLastSync="
				+ taskLastSync.toString() + "gCalTaskId=" + gCalTaskId
				+ "isDone=" + isDone.toString() + "isDeleted="
				+ isDeleted.toString();

		return taskToString;
	}

}