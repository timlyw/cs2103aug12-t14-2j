/**
 * Superclass Task
 * 
 * @author timlyw
 */

package mhs.src.storage;

import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Task {

	protected Integer taskId;
	protected String taskName;
	protected TaskCategory taskCategory;
	protected DateTime taskCreated;
	protected DateTime taskUpdated;
	protected DateTime taskLastSync;
	protected String gCalTaskId;
	protected Boolean isDone;
	protected Boolean isDeleted;

	/**
	 * Default Constructor
	 */
	public Task() {
	}

	/**
	 * Constructor with String taskCategory
	 * 
	 * @param taskId
	 * @param taskName
	 * @param taskCategory
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
	 * Constructor with enumerated type TaskCategory
	 * 
	 * @param taskId
	 * @param taskName
	 * @param taskCategory
	 * @param createdDt
	 * @param updatedDt
	 * @param syncDt
	 * @param gCalTaskId
	 * @param isDone
	 * @param isDeleted
	 */
	public Task(int taskId, String taskName, TaskCategory taskCategory,
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
	 * Returns cloned Task
	 */
	public Task clone() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(DateTime.class,
				new DateTimeTypeConverter());
		gsonBuilder.registerTypeAdapter(Task.class, new TaskTypeConverter());
		Gson gson = gsonBuilder.create();

		return gson.fromJson(gson.toJson(this), this.getClass());
	}

	public String toString() {
		String taskToString = "taskId=" + taskId + "taskName=" + taskName
				+ "taskCategory=" + taskCategory.getValue() + "taskCreated="
				+ taskCreated.toString() + "taskUpdated="
				+ taskUpdated.toString() + "taskLastSync="
				+ taskLastSync.toString() + "gCalTaskId=" + gCalTaskId
				+ "isDone=" + isDone.toString() + "isDeleted="
				+ isDeleted.toString();

		return taskToString;
	}

	public String toJson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(DateTime.class,
				new DateTimeTypeConverter());
		gsonBuilder.registerTypeAdapter(Task.class, new TaskTypeConverter());
		Gson gson = gsonBuilder.create();

		return gson.toJson(this);
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

	public DateTime getTaskUpdated() {
		return this.taskUpdated;
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

	public DateTime getEndDateTime() {
		return null;
	}

	public void setEndDateTime(DateTime dateTime) {
	}

	public DateTime getStartDateTime() {
		return null;
	}

	public void setStartDateTime(DateTime dateTime) {
	}

}
