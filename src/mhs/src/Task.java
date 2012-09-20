package mhs.src;

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

	protected Integer taskId;
	protected String taskName;
	protected TaskCategory taskCategory;
	protected DateTime taskCreated;
	protected DateTime taskUpdated;
	protected DateTime taskLastSync;
	protected String gCalTaskId;
	protected Boolean isDone;
	protected Boolean isDeleted;

	public Task() {

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
	public Task(int taskId, String taskName, String taskCategory,
			DateTime createdDt, DateTime updatedDt, DateTime syncDt,
			String gCalTaskId, boolean isDone, boolean isDeleted) {

		setTaskId(taskId);
		setTaskName(taskName);
		setTaskCategory(taskCategory);
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
		setTaskCreated(createdDt);
		setTaskUpdated(updatedDt);
		setTaskLastSync(syncDt);
		setgCalTaskId(gCalTaskId);
		setDone(isDone);
		setDeleted(isDeleted);
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

	public DateTime getEndDateTime() {
		return null;
	}

	public void setEndDateTime() {
	}

	public DateTime getStartDateTime() {
		return null;
	}

	public void setStartDateTime() {
	}

	public Map<String, String> getTaskProperties() {
		return null;
	}

}
