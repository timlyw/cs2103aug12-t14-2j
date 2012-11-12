//@author A0088669A

package mhs.src.logic.command;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.logic.CommandInfo;
import mhs.src.storage.persistence.task.TaskCategory;
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
		switch (inputCommand.getCommandEnum()) {
		case search:
			executeGenericSearch(inputCommand);
			break;
		case floating:
			executeFloatingSearch();
			break;
		case deadline:
			executeDeadlineSearch();
			break;
		case timed:
			executeTimedSearch();
			break;
		case home:
			executeHomeSearch();
			break;
		default:
			break;
		}
		logExitMethod("CommandSearch");
	}

	/**
	 * Queries home and populates matchedtasks
	 */
	private void executeHomeSearch() {
		List<Task> resultList;
		try {
			minTaskQuery = 0;
			resultList = queryHome();
			minTaskQuery = 1;
			matchedTasks = resultList;
			assert (matchedTasks != null);
		} catch (IOException e) {
			matchedTasks = null;
		}
	}

	/**
	 * Queries Timed Tasks and populates matchedtasks
	 */
	private void executeTimedSearch() {
		List<Task> resultList;
		try {
			minTaskQuery = 0;
			resultList = queryTaskByCategory(TaskCategory.TIMED);
			minTaskQuery = 1;
			matchedTasks = resultList;
			assert (matchedTasks != null);
		} catch (IOException e) {
			matchedTasks = null;
		}
	}

	/**
	 * Queries deadline and populates matchedtasks
	 */
	private void executeDeadlineSearch() {
		List<Task> resultList;
		try {
			minTaskQuery = 0;
			resultList = queryTaskByCategory(TaskCategory.DEADLINE);
			minTaskQuery = 1;
			matchedTasks = resultList;
			assert (matchedTasks != null);
		} catch (IOException e) {
			matchedTasks = null;
		}
	}

	/**
	 * Queries floating and populates matchestasks
	 */
	private void executeFloatingSearch() {
		List<Task> resultList;
		try {
			minTaskQuery = 0;
			resultList = queryTaskByCategory(TaskCategory.FLOATING);
			minTaskQuery = 1;
			matchedTasks = resultList;
			assert (matchedTasks != null);
		} catch (IOException e) {
			matchedTasks = null;
		}
	}

	/**
	 * Queries based on inputcommand and populates matchedtasks
	 * 
	 * @param inputCommand
	 */
	private void executeGenericSearch(CommandInfo inputCommand) {
		List<Task> resultList;
		try {
			minTaskQuery = 0;
			resultList = queryTask(inputCommand);
			minTaskQuery = 1;
			matchedTasks = resultList;
			assert (matchedTasks != null);
		} catch (IOException e) {
			matchedTasks = null;
		}
	}

	/**
	 * Constructor for index commands
	 */
	public CommandSearch() {
		commandFeedback = MESSAGE_SEARCH_INDEX_CANNOT;
	}

	/**
	 * Executes Search command
	 */
	public void executeCommand() {
		logEnterMethod("executeCommand");
		String outputString = new String();
		assert (matchedTasks != null);
		if (matchedTasks.isEmpty()) {
			outputString = MESSAGE_NO_MATCH;
			commandFeedback = outputString;
		}
		// if multiple matches are found display the list
		else {
			outputString = displayListOfTasks(matchedTasks);
			commandFeedback = MESSAGE_MULTIPLE_MATCHES;
		}
		logExitMethod("executeCommand");
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
	public void executeByIndex(int index) {
		commandFeedback = MESSAGE_SEARCH_INDEX_CANNOT;
	}

	/**
	 * Never Called
	 */
	public void executeByIndexAndType(int index) {
		commandFeedback = MESSAGE_SEARCH_INDEX_CANNOT;
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
