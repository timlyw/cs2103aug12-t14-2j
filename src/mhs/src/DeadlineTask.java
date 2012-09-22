package mhs.src;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;

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
	 * Copy Constructor
	 * @param sourceTask
	 */
	public DeadlineTask(DeadlineTask sourceTask) {
	    this(sourceTask.getTaskId(), sourceTask.getTaskName(), sourceTask
				.getTaskCategory(), sourceTask.getEndDateTime(), sourceTask.getTaskCreated(), sourceTask
				.getTaskUpdated(), sourceTask.getTaskLastSync(), sourceTask
				.getgCalTaskId(), sourceTask.isDone(), sourceTask.isDeleted());
	}

	public DateTime getStartDateTime() {
		return null;
	}

	public DateTime getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(DateTime endDateTime) {
		this.endDateTime = endDateTime;
	}

	public String toString() {
		String taskToString = "taskId=" + taskId + "taskName=" + taskName
				+ "taskCategory=" + taskCategory.getValue() + "endDateTime="
				+ endDateTime.toString() + "taskCreated="
				+ taskCreated.toString() + "taskUpdated="
				+ taskUpdated.toString() + "taskLastSync="
				+ taskLastSync.toString() + "gCalTaskId=" + gCalTaskId
				+ "isDone=" + isDone.toString() + "isDeleted="
				+ isDeleted.toString();

		return taskToString;
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