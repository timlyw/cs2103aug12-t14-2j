package mhs.src;

import org.joda.time.DateTime;

public class FloatingTask extends Task {

	/**
	 * Constructor with String taskCategory
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
	public FloatingTask(int taskId, String taskName, String taskCategory,
			DateTime createdDt, DateTime updatedDt, DateTime syncDt,
			String gCalTaskId, boolean isDone, boolean isDeleted) {
		super(taskId, taskName, taskCategory, createdDt, updatedDt, syncDt,
				gCalTaskId, isDone, isDeleted);
	}

	/**
	 * Constructor with TaskCategory taskCategory
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
	public FloatingTask(int taskId, String taskName, TaskCategory taskCategory,
			DateTime createdDt, DateTime updatedDt, DateTime syncDt,
			String gCalTaskId, boolean isDone, boolean isDeleted) {
		super(taskId, taskName, taskCategory, createdDt, updatedDt, syncDt,
				gCalTaskId, isDone, isDeleted);
	}
}