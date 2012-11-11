//@author A0087048X

package mhs.src.storage.persistence.task;

import mhs.src.common.MhsGson;

import org.joda.time.DateTime;

import com.google.gson.Gson;

/**
 * Task
 * 
 * Base Class for Task object Inherited classes<br>
 * 1. FloatingTask <br>
 * 2. TimedTask <br>
 * 3. DeadlineTask <br>
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */

public class Task {

	private static Gson gson = MhsGson.getInstance();

	protected Integer taskId;
	protected String taskName;
	protected TaskCategory taskCategory;
	protected DateTime taskCreated;
	protected DateTime taskUpdated;
	protected DateTime taskLastSync;
	protected Boolean isDone;
	protected Boolean isDeleted;

	/**
	 * Default Constructor
	 */
	public Task() {
	}

	/**
	 * Constructor
	 * 
	 * @param taskId
	 * @param taskName
	 * @param taskCategory
	 * @param createdDt
	 * @param updatedDt
	 * @param syncDt
	 * @param isDone
	 * @param isDeleted
	 */
	public Task(int taskId, String taskName, TaskCategory taskCategory,
			DateTime createdDt, DateTime updatedDt, DateTime syncDt,
			boolean isDone, boolean isDeleted) {
		setTaskId(taskId);
		setTaskName(taskName);
		setTaskCategory(taskCategory);
		setTaskCreated(createdDt);
		setTaskUpdated(updatedDt);
		setTaskLastSync(syncDt);
		setDone(isDone);
		setDeleted(isDeleted);
	}

	/**
	 * Returns cloned Task
	 */
	public Task clone() {
		return gson.fromJson(gson.toJson(this), this.getClass());
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
		if (taskCreated != null) {
			taskToString += "taskCreated=" + taskCreated.toString();
		}
		if (taskUpdated != null) {
			taskToString += "taskUpdated=" + taskUpdated.toString();
		}
		if (taskLastSync != null) {
			taskToString += "taskLastSync=" + taskLastSync.toString();
		}
		if (isDone != null) {
			taskToString += "isDone=" + isDone.toString();
		}
		if (isDeleted != null) {
			taskToString += "isDeleted=" + isDeleted.toString();
		}
		return taskToString;
	}

	public String toJson() {
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

	public DateTime getEndDateTime() {
		return null;
	}

	public void setEndDateTime(DateTime dateTime) {
		// do nothing
	}

	public DateTime getStartDateTime() {
		return null;
	}

	public void setStartDateTime(DateTime dateTime) {
		// do nothing
	}

	public String getgCalTaskId() {
		return null;
	}

	public void setGcalTaskId(String gCalTaskId) {
		// do nothing
	}

	public String getgCalTaskUid() {
		return null;
	}

	public void setGcalTaskUid(String gCalTaskUid) {
		// do nothing
	}

	public String getGTaskId() {
		return null;
	}

	public void setGTaskId(String gTaskId) {
		// do nothing
	}

	/**
	 * @author John Wong
	 */
	public String toHtmlString() {
		return null;
	}

	public boolean isTimed() {
		return this.getTaskCategory() == TaskCategory.TIMED;
	}

	public boolean isDeadline() {
		return this.getTaskCategory() == TaskCategory.DEADLINE;
	}

	public boolean isFloating() {
		return this.getTaskCategory() == TaskCategory.FLOATING;
	}
}
