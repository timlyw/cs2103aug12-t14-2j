package mhs.src.logic;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.InvalidTaskFormatException;
import mhs.src.common.exceptions.TaskNotFoundException;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;

import com.google.gdata.util.ServiceException;

/**
 * Executes Unmark command
 * 
 * @author A0088669A
 * 
 */
public class CommandUnmark extends Command {

	private static final String MESSAGE_TASK_NOT_UNMARKED = "Error occured. Task not un-marked.";
	private static final String CONFIRM_TASK_UNMARKED = "Marked Task - '%1$s' as PENDING";

	Task lastTask;

	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * Default constructor for non index based commands
	 * 
	 * @param inputCommand
	 */
	public CommandUnmark(CommandInfo inputCommand) {
		logEnterMethod("CommandUnmark");
		List<Task> resultList;
		lastTask = new Task();
		try {
			resultList = queryTaskByName(inputCommand);
			matchedTasks = resultList;
			assert (matchedTasks != null);
		} catch (IOException e) {
			matchedTasks = null;
		}
		logExitMethod("CommandUnmark");
	}

	/**
	 * Constructor for index based commands
	 * 
	 * @param lastUsedList
	 */
	public CommandUnmark(List<Task> lastUsedList) {
		logEnterMethod("CommandUnmark-index");
		matchedTasks = lastUsedList;
		assert (matchedTasks != null);
		logExitMethod("CommandUnmark-index");
	}

	/**
	 * Executes mark non index based
	 */
	public String executeCommand() {
		logEnterMethod("executeCommand");
		String outputString = new String();
		assert (matchedTasks != null);
		if (matchedTasks.isEmpty()) {
			outputString = MESSAGE_NO_MATCH;
		}
		// if only 1 match is found then display it
		else if (matchedTasks.size() == 1) {
			lastTask = matchedTasks.get(0);
			Task editedTask = markPending(lastTask);
			try {
				updateTask(editedTask);
				outputString = String.format(CONFIRM_TASK_UNMARKED,
						editedTask.getTaskName());
			} catch (Exception e) {
				outputString = MESSAGE_TASK_NOT_UNMARKED;
			}
		}
		// if multiple matches are found display the list
		else {
			outputString = displayListOfTasksCategory(matchedTasks,
					TaskCategory.FLOATING);
			indexExpected = true;
		}
		logExitMethod("executeCommand");
		return outputString;
	}

	private void updateTask(Task editedTask) throws IOException,
			ServiceException, TaskNotFoundException, InvalidTaskFormatException {
		dataHandler.update(editedTask);
		isUndoable = true;
	}

	/**
	 * Mark task as pending
	 * 
	 * @return
	 */
	private Task markPending(Task unmarkedTask) {
		logEnterMethod("markPending");
		assert (unmarkedTask.isDone() == true);
		Task editedTask = unmarkedTask;
		editedTask.setDone(false);
		logExitMethod("markPending");
		return editedTask;
	}

	/**
	 * Marks a task
	 */
	public String undo() {
		logEnterMethod("undo");
		String outputString = new String();
		if (isUndoable()) {
			try {
				dataHandler.update(lastTask);
				outputString = MESSAGE_UNDO_CONFIRM;
			} catch (Exception e) {
				outputString = MESSAGE_UNDO_FAIL;
			}
		} else {
			outputString = MESSAGE_UNDO_FAIL;
		}
		logExitMethod("undo");
		return outputString;
	}

	/**
	 * Executes based on only index. Uses last task list generated by previous
	 * unmark command
	 */
	public String executeByIndex(int index) {
		logEnterMethod("executeByIndex");
		String outputString = new String();
		if (indexExpected && index < matchedTasks.size() && index >= 0) {
			assert (index >= 0 && index < matchedTasks.size());
			Task tempTask = markPending(matchedTasks.get(index));
			lastTask = tempTask;
			try {
				updateTask(tempTask);
				outputString = String.format(CONFIRM_TASK_UNMARKED,
						matchedTasks.get(index).getTaskName());
				indexExpected = false;
			} catch (Exception e) {
				outputString = MESSAGE_TASK_NOT_UNMARKED;
			}
		} else {
			outputString = MESSAGE_INVALID_INDEX;
		}
		logExitMethod("executeByIndex");
		return outputString;
	}

	/**
	 * Executes based on index and type. Uses last list generated.
	 */
	public String executeByIndexAndType(int index) {
		logEnterMethod("executeByIndexAndType");
		String outputString = new String();
		if (index < matchedTasks.size() && index >= 0) {
			assert (index >= 0 && index < matchedTasks.size());
			Task tempTask = markPending(matchedTasks.get(index));
			lastTask = tempTask;
			try {
				updateTask(tempTask);
				outputString = String.format(CONFIRM_TASK_UNMARKED,
						matchedTasks.get(index).getTaskName());
				indexExpected = false;
			} catch (Exception e) {
				outputString = MESSAGE_TASK_NOT_UNMARKED;
			}
		} else {
			outputString = MESSAGE_INVALID_INDEX;
		}
		logExitMethod("executeByIndexAndType");
		return outputString;
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
