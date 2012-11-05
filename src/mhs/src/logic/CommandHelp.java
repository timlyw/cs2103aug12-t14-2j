package mhs.src.logic;

import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

public class CommandHelp extends Command {

	private static final Logger logger = MhsLogger.getLogger();

	public CommandHelp() {

	}

	@Override
	public String executeCommand() {
		logEnterMethod("executeCommand");
		String helpString = new String();
		helpString = htmlCreator.makeBold("Help") + htmlCreator.NEW_LINE
				+ htmlCreator.makeBold("Add") + htmlCreator.NEW_LINE
				+ "[add] <task name> <start date/time> to <end date/time>"
				+ htmlCreator.NEW_LINE + "add movie 4pm 5pm";
		logExitMethod("executeCommand");
		return helpString;
	}

	@Override
	public String undo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String executeByIndex(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String executeByIndexAndType(int index) {
		// TODO Auto-generated method stub
		return null;
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
