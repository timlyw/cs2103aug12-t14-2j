package mhs.src;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;

public class Task {

	enum TaskCategory {
		FLOATING("FLOATING"), TIMED("TIMED"), DEADLINE("DEADLINE");
		private final String value;

		private TaskCategory(String category) {
			this.value = category;
		}

		public String getValue() {
			return value;
		}
	}

	private Integer taskId;
	private String taskName;
	private TaskCategory taskCategory;
	private DateTime startDateTime;
	private DateTime endDateTime;
	private DateTime taskCreated;
	private DateTime taskUpdated;
	private DateTime taskLastSync;
	private String gCalTaskId;
	private Boolean isDone;
	private Boolean isDeleted;

	/**
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
	public Task(int taskId, String taskName, String taskCategory,
			DateTime startDt, DateTime endDt, DateTime createdDt,
			DateTime updatedDt, DateTime syncDt, String gCalTaskId,
			boolean isDone, boolean isDeleted) {
				
		setTaskId(taskId);
		setTaskName(taskName);
		setTaskCategory(taskCategory);
		setStartDateTime(startDt);
		setEndDateTime(endDt);
		setTaskCreated(createdDt);
		setTaskUpdated(updatedDt);
		setTaskLastSync(syncDt);
		setgCalTaskId(gCalTaskId);
		setDone(isDone);
		setDeleted(isDeleted);
	}

	/**
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
	public Task(int taskId, String taskName, TaskCategory taskCategory,
			DateTime startDt, DateTime endDt, DateTime createdDt,
			DateTime updatedDt, DateTime syncDt, String gCalTaskId,
			boolean isDone, boolean isDeleted) {

		setTaskId(taskId);
		setTaskName(taskName);
		setStartDateTime(startDt);
		setEndDateTime(endDt);
		setTaskCreated(createdDt);
		setTaskUpdated(updatedDt);
		setTaskLastSync(syncDt);
		setgCalTaskId(gCalTaskId);
		setDone(isDone);
		setDeleted(isDeleted);
	}

	/*
	 * Display Methods
	 */
	public String toString() {
		// TODO
		return null;
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

	/*
	 * Getters and Setters
	 */

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
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

	public DateTime getTaskUpdated() {
		return taskUpdated;
	}

	public void setTaskUpdated(DateTime taskUpdated) {
		this.taskUpdated = taskUpdated;
	}

	public DateTime getTaskLastSync() {
		return taskLastSync;
	}

	public void setTaskLastSync(DateTime taskLastSync) {
		this.taskLastSync = taskLastSync;
	}

	public boolean isDone() {
		return isDone;
	}

	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public DateTime getTaskCreated() {
		return taskCreated;
	}

	public void setTaskCreated(DateTime taskCreated) {
		this.taskCreated = taskCreated;
	}

	public String getgCalTaskId() {
		return gCalTaskId;
	}

	public void setgCalTaskId(String gCalTaskId) {
		this.gCalTaskId = gCalTaskId;
	}

	public TaskCategory getTaskCategory() {
		return taskCategory;
	}

	public void setTaskCategory(String taskCategory) {
		switch (taskCategory.toLowerCase()) {
		case "timed":
			this.taskCategory = TaskCategory.TIMED;
			break;
		case "deadline":
			this.taskCategory = TaskCategory.DEADLINE;
			break;
		case "floating":
			this.taskCategory = TaskCategory.FLOATING;
			break;
		default:
			this.taskCategory = TaskCategory.FLOATING;
			break;
		}
	}

	public void setTaskCategory(TaskCategory taskCategory) {
		this.taskCategory = taskCategory;
	}

}
