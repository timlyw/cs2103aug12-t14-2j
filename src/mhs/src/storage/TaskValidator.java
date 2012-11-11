//@author A0087048X
package mhs.src.storage;

import mhs.src.storage.persistence.task.Task;

/**
 * TaskValidator
 * 
 * Performs validation on task format and sync type check
 * 
 * Validate Task Formats: 1. Timed Task 2. Deadline Task 3. Floating Task
 * 
 * Type Check: 1. Sync Task
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

		if (task.getTaskCategory() == null || task.getTaskName() == null) {
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
	 * Checks if deadline task format is valid
	 * 
	 * @param task
	 * @param taskIsValid
	 * @return true if deadline task format is valid
	 */
	static boolean isDeadlineTaskValid(Task task, boolean taskIsValid) {
		logEnterMethod("isDeadlineTaskValid");
		assert (task != null);
		if (task.getEndDateTime() == null) {
			taskIsValid = false;
		}
		logExitMethod("isDeadlineTaskValid");
		return taskIsValid;
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

		if (task.getStartDateTime() == null || task.getEndDateTime() == null) {
			taskIsValid = false;
		}
		logExitMethod("isTimedTaskValid");
		return taskIsValid;
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
			return localTask.getGTaskId() == null
					|| localTask.getTaskLastSync() == null;
		} else {
			logExitMethod("isUnsyncedTask");
			return localTask.getgCalTaskId() == null
					|| localTask.getTaskLastSync() == null;
		}
	}

	static boolean isSyncedTask(Task localTask) {
		logEnterMethod("isSyncedTask");
		assert (localTask != null);
		logExitMethod("isSyncedTask");
		return !isUnsyncedTask(localTask);
	}

	private static void logExitMethod(String methodName) {
		Database.logger.exiting("TaskValidator", methodName);
	}

	private static void logEnterMethod(String methodName) {
		Database.logger.entering("TaskValidator", methodName);
	}

}