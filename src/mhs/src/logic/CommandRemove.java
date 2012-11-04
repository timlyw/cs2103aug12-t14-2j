package mhs.src.logic;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.storage.Task;
import mhs.src.storage.TaskNotFoundException;

public class CommandRemove extends Command {

	private static final String TASK_NOT_DELETED = "Error occured. Task not Deleted.";
	private static final String TASK_DELETED = "Deleted task - '%1$s' sucessfully.";
			
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
		} catch (IOException e) {
			matchedTasks = null;
		}
		logExitMethod("CommandRemove");
	}

	/**
	 * Constructor for index based commands
	 * Restores the previous list.
	 * @param lastUsedList
	 */
	public CommandRemove(List<Task> lastUsedList) {
		logEnterMethod("CommandRemove-Undo");
		matchedTasks = lastUsedList;
		//Matched task must not be null
		assert(matchedTasks!=null);
		logExitMethod("CommandRemove-undo");
	}

	/**
	 * executes delete
	 */
	public String executeCommand() {
		logEnterMethod("executeCommand");
		String outputString = new String();
		assert(matchedTasks!=null);
		if (matchedTasks.isEmpty()) {
			outputString = MESSAGE_NO_MATCH;
		}
		// if only 1 match is found then delete it
		else if (matchedTasks.size() == 1) {
			try {
				storeLastTask();
				deleteTask();
				outputString = String.format(TASK_DELETED, lastDeletedTask.getTaskName());
			} catch (Exception e) {
				outputString = TASK_NOT_DELETED;
			}
		}
		// if multiple matches are found display the list
		else {
			outputString = displayListOfTasks(matchedTasks);
			indexExpected = true;
		}
		logExitMethod("executeCommand");
		return outputString;
	}

	/**
	 * Deletes task 
	 * @throws TaskNotFoundException
	 * @throws IOException
	 */
	private void deleteTask() throws TaskNotFoundException, IOException {
		logEnterMethod("deleteTask");
		dataHandler.delete(matchedTasks.get(0).getTaskId());
		isUndoable = true;
		logExitMethod("deleteTask");
	}

	/**
	 * Stores last task for undo
	 */
	private void storeLastTask() {
		logEnterMethod("storeLastTask");
		lastDeletedTask = new Task();
		lastDeletedTask = matchedTasks.get(0);
		assert(lastDeletedTask!=null);
		logExitMethod("storeLastTask");
	}

	/**
	 * adds previously deleted task
	 */
	public String undo() {
		if (isUndoable()) {
			try {
				dataHandler.add(lastDeletedTask);
				isUndoable = false;
				return MESSAGE_UNDO_CONFIRM;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return MESSAGE_UNDO_FAIL;
			}

		} else {
			return MESSAGE_UNDO_FAIL;
		}
	}

	/**
	 * executes based on index only. Works when delete returned multiple matches
	 */
	public String executeByIndex(int index) {
		String outputString = new String();
		if (indexExpected & index < matchedTasks.size()) {
			try {
				System.out.println("entered");
				dataHandler.delete(matchedTasks.get(index).getTaskId());
				outputString = "Deleted task -"
						+ matchedTasks.get(index).getTaskName();
				lastDeletedTask = matchedTasks.get(index);
				indexExpected = false;
				isUndoable = true;
			} catch (Exception e) {

			}
		} else {
			outputString = "Invalid Command";
		}
		return outputString;
	}

	/**
	 * executes based on index and type of command. Works when there is a list
	 * present.
	 */
	public String executeByIndexAndType(int index) {
		String outputString = new String();
		if (index < matchedTasks.size()) {
			try {
				dataHandler.delete(matchedTasks.get(index).getTaskId());
				outputString = "Deleted task -"
						+ matchedTasks.get(index).getTaskName();
				lastDeletedTask = matchedTasks.get(index);
				isUndoable = true;
			} catch (Exception e) {

			}
		} else {
			outputString = "Invalid Command";
		}
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
