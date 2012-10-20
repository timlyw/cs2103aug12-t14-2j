/**
 * Floating Task - inherits from superclass Task
 * 
 * @author timlyw
 */

package mhs.src.storage;

import org.joda.time.DateTime;

public class FloatingTask extends Task {

	/**
	 * Constructor with String taskCategory
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
	public FloatingTask(int taskId, String taskName, String taskCategory,
			DateTime createdDt, DateTime updatedDt, DateTime syncDt,
			boolean isDone, boolean isDeleted) {
		super(taskId, taskName, taskCategory, createdDt, updatedDt, syncDt,
				null, isDone, isDeleted);
	}

	/**
	 * Constructor with TaskCategory taskCategory
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
	public FloatingTask(int taskId, String taskName, TaskCategory taskCategory,
			DateTime createdDt, DateTime updatedDt, DateTime syncDt,
			boolean isDone, boolean isDeleted) {
		super(taskId, taskName, taskCategory, createdDt, updatedDt, syncDt,
				null, isDone, isDeleted);
	}
}