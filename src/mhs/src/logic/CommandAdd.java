package mhs.src.logic;

import java.io.IOException;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.storage.persistence.task.DeadlineTask;
import mhs.src.storage.persistence.task.FloatingTask;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;
import mhs.src.storage.persistence.task.TimedTask;

import org.joda.time.DateTime;

/**
 * Class creates Adds a Task to the database
 * 
 * @author A008866A
 * 
 */
public class CommandAdd extends Command {

	private static final String MESSAGE_INVALID_TASK = "Error occured. Empty Task. Task not Added";
	private static final String MESSAGE_TASK_NOT_ADDED = "Error occured. Task not Added.";
	private static final String MESSAGE_ADD_INDEX_CANNOT = "Add does not support index commands.";

	private static final String CONFIRM_TASK_ADDED = "A %1$s task - '%2$s' was successfully added.";

	private static final int FLOATING = 0;
	private static final int DEADLINE = 1;
	private static final int TIMED = 2;

	private Task taskToAddTask;
	private Task addedTask;

	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * 
	 * @param inputCommand
	 */
	public CommandAdd(CommandInfo inputCommand) {
		logEnterMethod("CommandAdd");
		taskToAddTask = new Task();
		int taskType = decideTaskType(inputCommand);
		// Assert that taskType has taken the right value
		assert (taskType < 3 && taskType >= 0);
		switch (taskType) {
		case FLOATING:
			taskToAddTask = createFloatingTask(inputCommand);
			break;
		case DEADLINE:
			taskToAddTask = createDeadlineTask(inputCommand);
			break;
		case TIMED:
			taskToAddTask = createTimedTask(inputCommand);
			break;
		}
		logExitMethod("CommandAdd");
	}

	/**
	 * Constructor for index command
	 */
	public CommandAdd() {
		commandFeedback = MESSAGE_ADD_INDEX_CANNOT;
	}

	/**
	 * Creates a Timed Task
	 * 
	 * @param inputCommand
	 * @return Task
	 */
	private Task createTimedTask(CommandInfo inputCommand) {
		logEnterMethod("createTimedTask");
		Task timedTaskToAdd = new TimedTask(0, inputCommand.getTaskName(),
				TaskCategory.TIMED, inputCommand.getStartDate(),
				inputCommand.getEndDate(), DateTime.now(), null, null, null,
				false, false);
		assert (timedTaskToAdd != null);
		logExitMethod("createTimedTask");
		return timedTaskToAdd;
	}

	/**
	 * Creates a Deadline Task
	 * 
	 * @param inputCommand
	 * @return Task
	 */
	private Task createDeadlineTask(CommandInfo inputCommand) {
		logEnterMethod("createDeadlineTask");
		Task deadlineTaskToAdd = new DeadlineTask(0,
				inputCommand.getTaskName(), TaskCategory.DEADLINE,
				inputCommand.getStartDate(), DateTime.now(), null, null, null,
				false, false);
		assert (deadlineTaskToAdd != null);
		logExitMethod("createDeadlineTask");
		return deadlineTaskToAdd;
	}

	/**
	 * Creates a Floating Task
	 * 
	 * @param inputCommand
	 * @return Task
	 */
	private Task createFloatingTask(CommandInfo inputCommand) {
		logEnterMethod("createFloatingTask");
		Task floatingTaskToAdd = new FloatingTask(0,
				inputCommand.getTaskName(), TaskCategory.FLOATING,
				DateTime.now(), null, null, false, false);
		assert (floatingTaskToAdd != null);
		logExitMethod("creteFloatingTask");
		return floatingTaskToAdd;
	}

	/**
	 * Decides type of task based on input date params
	 * 
	 * @param inputCommand
	 * @return int for number of date/time specified
	 */
	private int decideTaskType(CommandInfo inputCommand) {
		int typeCount = 0;
		if (inputCommand.getStartDate() != null) {
			typeCount++;
		}
		if (inputCommand.getEndDate() != null) {
			typeCount++;
		}
		return typeCount;
	}

	/**
	 * Implements the add method of the Command class. Adds the Task to the
	 * database if valid.
	 */
	public void executeCommand() {
		logEnterMethod("executeCommand");
		String outputString = new String();
		assert (taskToAddTask != null);
		if (taskToAddTask.getTaskName() == null) {
			outputString = MESSAGE_INVALID_TASK;
		} else {
			try {
				// Store last added task for undo
				addedTask = new Task();
				addedTask = dataHandler.add(taskToAddTask);
				isUndoable = true;
				outputString = String.format(CONFIRM_TASK_ADDED,
						taskToAddTask.getTaskCategory(),
						taskToAddTask.getTaskName());
			} catch (IOException e) {
				outputString = MESSAGE_TASK_NOT_ADDED;
			} catch (Exception e) {
				outputString = MESSAGE_TASK_NOT_ADDED;
			}
		}
		commandFeedback = outputString;
		logExitMethod("executeCommand");
	}

	/**
	 * Undo the add command
	 */
	public String undo() {
		logEnterMethod("undo");
		String outputString = new String();
		assert (addedTask != null);
		if (isUndoable) {
			try {
				dataHandler.delete(addedTask.getTaskId());
				isUndoable = false;
				outputString = MESSAGE_UNDO_CONFIRM;
			} catch (Exception e) {
				outputString = MESSAGE_UNDO_FAIL;
			}
		} else {
			outputString = MESSAGE_CANNOT_UNDO;
		}
		logExitMethod("undo");
		commandFeedback = outputString;
		return outputString;
	}

	/**
	 * Add does not support index commands
	 */
	public void executeByIndex(int index) {
		logEnterMethod("executeByIndex");
		logExitMethod("executeByIndex");
		commandFeedback = MESSAGE_ADD_INDEX_CANNOT;
	}

	/**
	 * Add does not support index commands
	 */
	public void executeByIndexAndType(int index) {
		logEnterMethod("executeByIndexAndType");
		logExitMethod("executeByIndexAndType");
		commandFeedback = MESSAGE_ADD_INDEX_CANNOT;
	}

	/**
	 * Logger enter method
	 * 
	 * @param methodName
	 */
	private void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}

	/**
	 * Logger exit method
	 * 
	 * @param methodName
	 */
	private void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}

}
