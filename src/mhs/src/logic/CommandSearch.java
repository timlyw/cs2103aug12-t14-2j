package mhs.src.logic;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.storage.persistence.task.Task;

/**
 * 
 * @author A0088669A
 * 
 */
public class CommandSearch extends Command {

	private static final String MESSAGE_SEARCH_INDEX_CANNOT = "Search does not support index commands.";

	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * Constructor for non index based commands
	 * 
	 * @param inputCommand
	 */
	public CommandSearch(CommandInfo inputCommand) {
		logEnterMethod("CommandSearch");
		List<Task> resultList;
		try {
			resultList = queryTask(inputCommand);
			matchedTasks = resultList;
			assert (matchedTasks != null);
		} catch (IOException e) {
			matchedTasks = null;
		}
		logExitMethod("CommandSearch");
	}

	/**
	 * Executes Search command
	 */
	public String executeCommand() {
		logEnterMethod("executeCommand");
		String outputString = new String();
		assert (matchedTasks != null);
		if (matchedTasks.isEmpty()) {
			outputString = MESSAGE_NO_MATCH;
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
	 * Undo is never called
	 */
	public String undo() {
		// is never called
		return MESSAGE_CANNOT_UNDO;
	}

	/**
	 * Never called
	 */
	public String executeByIndex(int index) {
		return MESSAGE_SEARCH_INDEX_CANNOT;
	}

	/**
	 * Never Called
	 */
	public String executeByIndexAndType(int index) {
		return MESSAGE_SEARCH_INDEX_CANNOT;
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
