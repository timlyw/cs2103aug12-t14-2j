package mhs.src.logic;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.google.gdata.util.ServiceException;

import mhs.src.common.MhsLogger;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;
import mhs.src.util.InvalidTaskFormatException;
import mhs.src.util.TaskNotFoundException;

/**
 * Executes Mark command
 * 
 * @author A0088669A
 * 
 */
public class CommandMark extends Command {

	private static final String MESSAGE_TASK_NOT_MARKED = "Error occured. Task not marked.";
	private static final String CONFIRM_TASK_MARKED = "Marked Task - '%1$s' as DONE";

	Task lastTask;

	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * Default constructor for non index based commands
	 * 
	 * @param inputCommand
	 */
	public CommandMark(CommandInfo inputCommand) {
		logEnterMethod("CommandMark");
		List<Task> resultList;
		lastTask = new Task();
		try {
			resultList = queryTaskByName(inputCommand);
			matchedTasks = resultList;
			assert (matchedTasks != null);
		} catch (IOException e) {
			matchedTasks = null;
		}
		logExitMethod("CommandMark");
	}

	/**
	 * Constructor for index based commands
	 * 
	 * @param lastUsedList
	 */
	public CommandMark(List<Task> lastUsedList) {
		logEnterMethod("CommandMark-index");
		matchedTasks = lastUsedList;
		assert (matchedTasks != null);
		logExitMethod("CommandMark-index");
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
			Task editedTask = markDone(lastTask);
			try {
				updateTask(editedTask);
				outputString = String.format(CONFIRM_TASK_MARKED,
						editedTask.getTaskName());
			} catch (Exception e) {
				outputString = MESSAGE_TASK_NOT_MARKED;
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
	 * Mark task as done
	 * 
	 * @return
	 */
	private Task markDone(Task unmarkedTask) {
		logEnterMethod("markDone");
		assert (unmarkedTask.isDone() == false);
		Task editedTask = unmarkedTask;
		editedTask.setDone(true);
		logExitMethod("markDone");
		return editedTask;
	}

	/**
	 * Unmarks a task
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
	 * mark command
	 */
	public String executeByIndex(int index) {
		logEnterMethod("executeByIndex");
		String outputString = new String();
		if (indexExpected && index < matchedTasks.size() && index >= 0) {
			assert (index >= 0 && index < matchedTasks.size());
			Task tempTask = markDone(matchedTasks.get(index));
			lastTask = tempTask;
			try {
				updateTask(tempTask);
				outputString = String.format(CONFIRM_TASK_MARKED, matchedTasks
						.get(index).getTaskName());
				indexExpected = false;
			} catch (Exception e) {
				outputString = MESSAGE_TASK_NOT_MARKED;
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
			Task tempTask = markDone(matchedTasks.get(index));
			lastTask = tempTask;
			try {
				updateTask(tempTask);
				outputString = String.format(CONFIRM_TASK_MARKED, matchedTasks
						.get(index).getTaskName());
				indexExpected = false;
			} catch (Exception e) {
				outputString = MESSAGE_TASK_NOT_MARKED;
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
