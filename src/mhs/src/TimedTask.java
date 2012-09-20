package mhs.src;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;

public class TimedTask extends Task {

	private DateTime startDateTime;
	private DateTime endDateTime;
	public TimedTask(){}

	public TimedTask(int taskId, String taskName, String taskCategory,
			DateTime startDt, DateTime endDt, DateTime createdDt,
			DateTime updatedDt, DateTime syncDt, String gCalTaskId,
			boolean isDone, boolean isDeleted) {
		super(taskId, taskName, taskCategory, startDt, endDt, createdDt,
				gCalTaskId, isDone, isDeleted);
		setStartDateTime(startDt);
		setEndDateTime(endDt);

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

	/*
	 * Record methods
	 */
	/**
	 * Return task properties for record file insertion
	 * 
	 * @return
	 */
	public Map<String, String> getTaskProperties() {

		Map<String, String> taskProperties = new LinkedHashMap<String, String>();

		taskProperties.put("taskId", taskId.toString());
		taskProperties.put("taskName", taskName);
		taskProperties.put("taskCategory", taskCategory.getValue());
		taskProperties.put("startDateTime", startDateTime.toString());
		taskProperties.put("endDateTime", endDateTime.toString());
		taskProperties.put("taskCreated", taskCreated.toString());
		taskProperties.put("taskUpdated", taskUpdated.toString());
		taskProperties.put("taskLastSync", taskLastSync.toString());
		taskProperties.put("gCalTaskId", gCalTaskId);
		taskProperties.put("isDone", isDone.toString());
		taskProperties.put("isDeleted", isDeleted.toString());

		return taskProperties;
	}

}