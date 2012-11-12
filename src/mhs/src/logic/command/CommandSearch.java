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
		List<Task> resultList;
		switch (inputCommand.getCommandEnum()) {
		case search:
			try {
				minTaskQuery = 0;
				resultList = queryTask(inputCommand);
				minTaskQuery = 1;
				matchedTasks = resultList;
				assert (matchedTasks != null);
			} catch (IOException e) {
				matchedTasks = null;
			}
			break;
		case floating:
			try {
				System.out.println("float me");
				minTaskQuery = 0;
				resultList = queryTaskByCategory(TaskCategory.FLOATING);
				System.out.println(resultList);
				minTaskQuery = 1;
				matchedTasks = resultList;
				assert (matchedTasks != null);
			} catch (IOException e) {
				matchedTasks = null;
			}
			break;
		case deadline:
			try {
				minTaskQuery = 0;
				resultList = queryTaskByCategory(TaskCategory.DEADLINE);
				minTaskQuery = 1;
				matchedTasks = resultList;
				assert (matchedTasks != null);
			} catch (IOException e) {
				matchedTasks = null;
			}
			break;
		case timed:
			try {
				minTaskQuery = 0;
				resultList = queryTaskByCategory(TaskCategory.TIMED);
				minTaskQuery = 1;
				matchedTasks = resultList;
				assert (matchedTasks != null);
			} catch (IOException e) {
				matchedTasks = null;
			}
			break;
		case home:
			try {
				minTaskQuery = 0;
				resultList = queryHome();
				minTaskQuery = 1;
				matchedTasks = resultList;
				assert (matchedTasks != null);
			} catch (IOException e) {
				matchedTasks = null;
			}
			break;
		default:
			break;
		}
		logExitMethod("CommandSearch");
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
