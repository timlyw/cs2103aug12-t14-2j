//@author A0088669A

package mhs.src.logic;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.TaskNotFoundException;
import mhs.src.storage.persistence.task.Task;

/**
 * Executes Delete Command
 * 
 * @author A0088669A
 * 
 */
public class CommandRemove extends Command {

	private static final String MESSAGE_TASK_NOT_DELETED = "Error occured. Task not Deleted.";
	private static final String CONFIRM_TASK_DELETED = "I have deleted - '%1$s'.";

	private Task lastDeletedTask;

	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * Constructor for non index based commands
	 * 
	 * @param userCommand
	 */
	public CommandRemove(CommandInfo userCommand) {
		logEnterMethod("CommandRemove");
		List<Task> resultList;
		try {
			resultList = queryTaskByName(userCommand);
			matchedTasks = resultList;
			assert (matchedTasks != null);
		} catch (IOException e) {
			matchedTasks = null;
		}
		logExitMethod("CommandRemove");
	}

	/**
	 * Constructor for index based commands Restores the previous list.
	 * 
	 * @param lastUsedList
	 */
	public CommandRemove(List<Task> lastUsedList) {
		logEnterMethod("CommandRemove-index");
		matchedTasks = lastUsedList;
		// Matched task must not be null
		assert (matchedTasks != null);
		logExitMethod("CommandRemove-index");
	}

	/**
	 * executes delete
	 */
	public void executeCommand() {
		logEnterMethod("executeCommand");
		String outputString = new String();
		assert (matchedTasks != null);
		if (matchedTasks.isEmpty()) {
			outputString = MESSAGE_NO_MATCH;
			commandFeedback = outputString;
		}
		// if only 1 match is found then delete it
		else if (matchedTasks.size() == 1) {
			try {
				storeLastTask(matchedTasks.get(0));
				deleteTask(matchedTasks.get(0));
				outputString = String.format(CONFIRM_TASK_DELETED,
						lastDeletedTask.getTaskName());
				commandFeedback = outputString;
			} catch (Exception e) {
				commandFeedback = MESSAGE_TASK_NOT_DELETED;
			}
		}
		// if multiple matches are found display the list
		else {
			indexExpected = true;
			commandFeedback = MESSAGE_MULTIPLE_MATCHES;
		}
		logExitMethod("executeCommand");
	}

	/**
	 * Deletes task
	 * 
	 * @throws TaskNotFoundException
	 * @throws IOException
	 */
	private void deleteTask(Task taskToDelete) throws TaskNotFoundException,
			IOException {
		logEnterMethod("deleteTask");
		dataHandler.delete(taskToDelete.getTaskId());
		newTask = null;
		isUndoable = true;
		logExitMethod("deleteTask");
	}

	/**
	 * Stores last task for undo
	 */
	private void storeLastTask(Task taskToStore) {
		logEnterMethod("storeLastTask");
		lastDeletedTask = new Task();
		lastDeletedTask = taskToStore;
		lastTask = lastDeletedTask;
		assert (lastDeletedTask != null);
		logExitMethod("storeLastTask");
	}

	/**
	 * executes based on index only. Works when delete returned multiple matches
	 */
	public void executeByIndex(int index) {
		logEnterMethod("executeByIndex");
		String outputString = new String();
		if (indexExpected && index < matchedTasks.size() && index >= 0) {
			outputString = deleteByIndex(index);
		} else {
			outputString = MESSAGE_INVALID_INDEX;
		}
		commandFeedback = outputString;
		logExitMethod("executeByIndex");
	}

	/**
	 * Deletes by index
	 * 
	 * @param index
	 * @return
	 */
	private String deleteByIndex(int index) {
		String outputString;
		assert (index >= 0 && index < matchedTasks.size());
		try {
			assert (matchedTasks.get(index) != null);
			storeLastTask(matchedTasks.get(index));
			deleteTask(matchedTasks.get(index));
			indexExpected = false;
			outputString = String.format(CONFIRM_TASK_DELETED, matchedTasks
					.get(index).getTaskName());
		} catch (Exception e) {
			outputString = MESSAGE_TASK_NOT_DELETED;
		}
		return outputString;
	}

	/**
	 * executes based on index and type of command. Works when there is a list
	 * present.
	 */
	public void executeByIndexAndType(int index) {
		logEnterMethod("executeByIndexAndType");
		String outputString = new String();
		if (index < matchedTasks.size() && index >= 0) {
			outputString = deleteByIndex(index);
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
