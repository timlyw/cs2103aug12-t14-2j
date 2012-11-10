//@author A0088669A

package mhs.src.logic;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.google.gdata.util.ServiceException;

import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.InvalidTaskFormatException;
import mhs.src.common.exceptions.TaskNotFoundException;
import mhs.src.storage.persistence.task.Task;

/**
 * Executes Mark command
 * 
 * @author A0088669A
 * 
 */
public class CommandMark extends Command {

	private static final String MESSAGE_TASK_NOT_MARKED = "Error occured. Task not marked.";
	private static final String CONFIRM_TASK_MARKED = "Marked Task - '%1$s' as DONE";

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
	public void executeCommand() {
		logEnterMethod("executeCommand");
		String outputString = new String();
		assert (matchedTasks != null);
		if (matchedTasks.isEmpty()) {
			outputString = MESSAGE_NO_MATCH;
			commandFeedback = outputString;
		}
		// if only 1 match is found then display it
		else if (matchedTasks.size() == 1) {
			lastTask = matchedTasks.get(0).clone();
			Task editedTask = markDone(matchedTasks.get(0));
			try {
				updateTask(editedTask);
				newTask = editedTask;
				outputString = String.format(CONFIRM_TASK_MARKED,
						editedTask.getTaskName());
				commandFeedback = outputString;
			} catch (Exception e) {
				outputString = MESSAGE_TASK_NOT_MARKED;
			}
		}
		// if multiple matches are found display the list
		else {
			indexExpected = true;
			commandFeedback = MESSAGE_MULTIPLE_MATCHES;
		}
		logExitMethod("executeCommand");
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
		commandFeedback = outputString;
		return outputString;
	}

	/**
	 * Executes based on only index. Uses last task list generated by previous
	 * mark command
	 */
	public void executeByIndex(int index) {
		logEnterMethod("executeByIndex");
		String outputString = new String();
		if (indexExpected && index < matchedTasks.size() && index >= 0) {
			outputString = markByIndex(index);
		} else {
			outputString = MESSAGE_INVALID_INDEX;
		}
		commandFeedback = outputString;
		logExitMethod("executeByIndex");
	}

	private String markByIndex(int index) {
		String outputString;
		assert (index >= 0 && index < matchedTasks.size());
		lastTask = matchedTasks.get(index).clone();
		Task tempTask = markDone(matchedTasks.get(index));
		try {
			updateTask(tempTask);
			newTask = tempTask;
			outputString = String.format(CONFIRM_TASK_MARKED,
					matchedTasks.get(index).getTaskName());
			indexExpected = false;
		} catch (Exception e) {
			outputString = MESSAGE_TASK_NOT_MARKED;
		}
		return outputString;
	}

	/**
	 * Executes based on index and type. Uses last list generated.
	 */
	public void executeByIndexAndType(int index) {
		logEnterMethod("executeByIndexAndType");
		String outputString = new String();
		if (index < matchedTasks.size() && index >= 0) {
			outputString = markByIndex(index);
		} else {
			outputString = MESSAGE_INVALID_INDEX;
		}
		commandFeedback = outputString;
		logExitMethod("executeByIndexAndType");
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
