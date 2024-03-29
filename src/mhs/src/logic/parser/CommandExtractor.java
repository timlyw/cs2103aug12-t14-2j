//@author A0086805X
package mhs.src.logic.parser;

import java.util.logging.Level;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

/**
 * This is a class that checks string if they are commands and set the
 * commands.
 * 
 */

public class CommandExtractor {

	private static final String COMMAND_ADD = "add";
	private static final String COMMAND_REMOVE = "remove";
	private static final String COMMAND_SEARCH = "search";
	private static final String COMMAND_EDIT = "edit";
	private static final String COMMAND_SYNC = "sync";
	private static final String COMMAND_UNDO = "undo";
	private static final String COMMAND_REDO = "redo";
	private static final String COMMAND_LOGIN = "login";
	private static final String COMMAND_LOGOUT = "logout";
	private static final String COMMAND_HELP = "help";
	private static final String COMMAND_MARK = "mark";
	private static final String COMMAND_UNMARK = "unmark";
	private static final String COMMAND_PREVIOUS = "previous";
	private static final String COMMAND_NEXT = "next";
	private static final String COMMAND_FLOATING = "floating";
	private static final String COMMAND_DEADLINE = "deadline";
	private static final String COMMAND_TIMED = "timed";
	private static final String COMMAND_HOME = "home";
	private static final String COMMAND_EXIT = "exit";
	private static final String COMMAND_RENAME = "rename";
	private static final String COMMAND_MINIMIZE = "hide";

	/**
	 * 
	 * These are the enum commands that are used and the different keywords the
	 * user may enter.
	 */
	enum CommandKeyWord {
				add(COMMAND_ADD), del(COMMAND_REMOVE), remove(COMMAND_REMOVE), delete(COMMAND_REMOVE), update(COMMAND_EDIT), edit(
				COMMAND_EDIT), postpone(COMMAND_EDIT), search(COMMAND_SEARCH), find(COMMAND_SEARCH), display(
				COMMAND_SEARCH), sync(COMMAND_SYNC), undo(COMMAND_UNDO), redo(COMMAND_REDO), rename(
				COMMAND_RENAME), login(COMMAND_LOGIN), signin(COMMAND_LOGIN), logout(COMMAND_LOGOUT), signout(
				COMMAND_LOGOUT), help(COMMAND_HELP), mark(COMMAND_MARK), check(COMMAND_MARK), unmark(
				COMMAND_UNMARK), p(COMMAND_PREVIOUS), n(COMMAND_NEXT), floating(COMMAND_FLOATING), deadline(
				COMMAND_DEADLINE), timed(COMMAND_TIMED), home(COMMAND_HOME), exit(
				COMMAND_EXIT), hide(COMMAND_MINIMIZE);

		
		private final String command;

		CommandKeyWord(String command) {
			this.command = command;
		}

	}

	private static final String REGEX_WHITE_SPACE = "\\s+";
	private static final Logger logger = MhsLogger.getLogger();
	private String commandString;
	private static CommandExtractor commandExtractor;

	/**
	 * Private constructor for command extractor
	 */
	private CommandExtractor() {
		logEnterMethod("CommandExtractor");
		commandString = "";
		logExitMethod("CommandExtractor");
	}

	/**
	 * Function to get a single instance of the constructor.
	 * 
	 * @return Returns a single instance of the constructor.
	 */
	public static CommandExtractor getCommandExtractor() {
		if (commandExtractor == null) {
			commandExtractor = new CommandExtractor();
		}
		return commandExtractor;
	}

	/**
	 * This is a function to check if a string is a command.
	 * 
	 * @param printString
	 *            This is the string to be checked.
	 * 
	 * @return Returns if the string is a command type.
	 */
	public boolean checkCommand(String parseString) {
		logEnterMethod("isCommand");
		commandString = null;
		if(parseString == null){
			return false;
		}
		for (CommandKeyWord c : CommandKeyWord.values()) {
			if (parseString.equalsIgnoreCase(c.name())) {
				logExitMethod("isCommand");
				return true;
			}
		}
		logExitMethod("isCommand");
		return false;
	}

	/**
	 * This is the function to set the command.
	 * 
	 * @param printString
	 *            This the command String to be set.
	 * 
	 * @return Returns the command that is set.
	 */
	public String extractCommand(String parseString) {
		logEnterMethod("extractCommand");
		try {
			String[] processArray = parseString.split(REGEX_WHITE_SPACE);
			setCommand(processArray);
		} catch (NullPointerException e) {
			logger.log(Level.FINER, e.getMessage());
			return null;
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.log(Level.FINER, e.getMessage());
			return null;
		}
		logExitMethod("extractCommand");
		return commandString;
	}

	/**
	 * Method to set the command according to what is at the first index of the array.
	 * 
	 * @param processArray
	 */
	private void setCommand(String[] processArray) {
		assert(processArray != null);
		logEnterMethod("setCommand");
		if (checkCommand(processArray[0])) {
			for (CommandKeyWord c : CommandKeyWord.values()) {
				if (processArray[0].equalsIgnoreCase(c.name())) {
					commandString = c.command;
				}
			}
		} else {
			commandString = CommandKeyWord.add.name();
		}
		logExitMethod("setCommand");
	}
	
	/**
	 * Logger enter method
	 * 
	 * @param methodName
	 */
	void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}

	/**
	 * Logger enter method
	 * 
	 * @param methodName
	 */
	void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}
}
