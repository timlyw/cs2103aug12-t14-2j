//@author A0087048X
package mhs.src.storage;

import mhs.src.storage.persistence.task.Task;

/**
 * TaskValidator
 * 
 * Performs validation on task format and sync type check
 * 
 * Validate Task Formats: <br>
 * 1. Timed Task <br>
 * 2. Deadline Task<br>
 * 3. Floating Task<br>
 * 
 * Sync Task Type Check Support for: <br>
 * 1. Google Calendar<br>
 * 2. Google Tasks
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */

class TaskValidator {

	/**
	 * Checks if Task format is valid for given type
	 * 
	 * @param task
	 * @return boolean
	 */
	static boolean isTaskValid(Task task) {
		logEnterMethod("isTaskValid");
		assert (task != null);

		if (isTaskNameAndCategoryNull(task)) {
			logExitMethod("isTaskValid");
			return false;
		}
		boolean taskIsValid = true;
		switch (task.getTaskCategory()) {
		case FLOATING:
			break;
		case TIMED:
			taskIsValid = isTimedTaskValid(task, taskIsValid);
			break;
		case DEADLINE:
			taskIsValid = isDeadlineTaskValid(task, taskIsValid);
			break;
		default:
			taskIsValid = false;
			break;
		}

		logExitMethod("isTaskValid");
		return taskIsValid;
	}

	/**
	 * Checks if task name and cateogry are null
	 * 
	 * @param task
	 * @return
	 */
	protected static boolean isTaskNameAndCategoryNull(Task task) {
		logEnterMethod("isTaskNameAndCategoryNull");
		logExitMethod("isTaskNameAndCategoryNull");
		return task.getTaskCategory() == null || task.getTaskName() == null;
	}

	/**
	 * Checks if deadline task format is valid
	 * 
	 * @param task
	 * @param taskIsValid
	 * @return true if deadline task format is valid
	 */
	static boolean isDeadlineTaskValid(Task task, boolean taskIsValid) {
		logEnterMethod("isDeadlineTaskValid");
		assert (task != null);
		if (isDeadlineEndDateTimesNull(task)) {
			taskIsValid = false;
		}
		logExitMethod("isDeadlineTaskValid");
		return taskIsValid;
	}

	/**
	 * Checks if deadline task date times are null
	 * 
	 * @param task
	 * @return
	 */
	protected static boolean isDeadlineEndDateTimesNull(Task task) {
		logEnterMethod("isDeadlineEndDateTimesNull");
		logExitMethod("isDeadlineEndDateTimesNull");
		return task.getEndDateTime() == null;
	}

	/**
	 * Checks if timed task is valid
	 * 
	 * @param task
	 * @param taskIsValid
	 * @return
	 */
	static boolean isTimedTaskValid(Task task, boolean taskIsValid) {
		logEnterMethod("isTimedTaskValid");
		assert (task != null);
		if (isTimedTaskStartAndEndDateTimesNull(task)) {
			taskIsValid = false;
		}
		logExitMethod("isTimedTaskValid");
		return taskIsValid;
	}

	/**
	 * Checks if timed task datetimes are null
	 * 
	 * @param task
	 * @return
	 */
	protected static boolean isTimedTaskStartAndEndDateTimesNull(Task task) {
		logEnterMethod("isTimedTaskStartAndEndDateTimesNull");
		logExitMethod("isTimedTaskStartAndEndDateTimesNull");
		return task.getStartDateTime() == null
				|| isDeadlineEndDateTimesNull(task);
	}

	/**
	 * Checks whether task is unsynced
	 * 
	 * @param localTask
	 * @return true if task is unsynced
	 */
	static boolean isUnsyncedTask(Task localTask) {
		logEnterMethod("isUnsyncedTask");
		assert (localTask != null);
		if (localTask.isFloating()) {
			logExitMethod("isUnsyncedTask");
			return isFloatingTaskSynced(localTask);
		} else {
			logExitMethod("isUnsyncedTask");
			return isTimedOrDeadlineTaskSynced(localTask);
		}
	}

	/**
	 * Checks if timed / deadline task is synced
	 * 
	 * @param localTask
	 * @return
	 */
	protected static boolean isTimedOrDeadlineTaskSynced(Task localTask) {
		logEnterMethod("isTimedOrDeadlineTaskSynced");
		logExitMethod("isTimedOrDeadlineTaskSynced");
		return localTask.getgCalTaskId() == null
				|| localTask.getTaskLastSync() == null;
	}

	/**
	 * Checks if floating task is synced
	 * 
	 * @param localTask
	 * @return
	 */
	protected static boolean isFloatingTaskSynced(Task localTask) {
		logEnterMethod("isFloatingTaskSynced");
		logExitMethod("isFloatingTaskSynced");
		return localTask.getGTaskId() == null
				|| localTask.getTaskLastSync() == null;
	}

	/**
	 * Checks if task is synced
	 * 
	 * @param localTask
	 * @return
	 */
	static boolean isSyncedTask(Task localTask) {
		logEnterMethod("isSyncedTask");
		assert (localTask != null);
		logExitMethod("isSyncedTask");
		return !isUnsyncedTask(localTask);
	}

	/**
	 * Log Methods
	 */

	/**
	 * Log trace exit method
	 * 
	 * @param methodName
	 */
	private static void logExitMethod(String methodName) {
		Database.logger.exiting("TaskValidator", methodName);
	}

	/**
	 * Log trace entry method
	 * 
	 * @param methodName
	 */
	private static void logEnterMethod(String methodName) {
		Database.logger.entering("TaskValidator", methodName);
	}

}