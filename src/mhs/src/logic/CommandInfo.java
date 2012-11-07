//@author A0086805X
package mhs.src.logic;

import java.util.logging.Logger;

import mhs.src.common.HtmlCreator;
import mhs.src.common.MhsLogger;

import org.joda.time.DateTime;

/**
 * This is the class to package the parameters into a command object.
 */
public class CommandInfo {

	// Error strings that display when parameters are not equal.
	private static final String ERROR_TASK_NAME = "Task Name Error! ";
	private static final String ERROR_EDITTED_NAME = "Editted Name Error! ";
	private static final String ERROR_START_DATE = "Start Date Error! ";
	private static final String ERROR_END_DATE = "End Date Error! ";
	private static final String ERROR_INDEX = "Index Error! ";

	/**
	 * This is the enum of the different type of commands.
	 */
	public static enum CommandKeyWords {
		add, remove, edit, search, sync, undo, login, logout, rename, redo, mark,
		help, unmark, p, n, floating, deadline, timed, home, exit;
	}

	private String taskName;
	private String edittedName;
	private CommandKeyWords commandEnum;

	private DateTime startDate;
	private DateTime endDate;
	private int index;

	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * This is the constructor to set up the command object.
	 * 
	 * @param commandInput
	 *            This is the command.
	 * @param taskNameInput
	 *            This is the name of the task.
	 * @param edittedNameInput
	 *            This is the editted name of the task.
	 * @param startDateInput
	 *            This is the start date.
	 * @param startTimeInput
	 *            This is the start time.
	 * @param endDateInput
	 *            This is the end date.
	 * @param endTimeInput
	 *            This is the end time.
	 */
	public CommandInfo(CommandKeyWords commandInput, String taskNameInput,
			String edittedNameInput, DateTime startDateInput,
			DateTime endDateInput, int indexInput) {

		logEnterMethod("CommandInfo");
		commandEnum = commandInput;
		taskName = taskNameInput;
		edittedName = edittedNameInput;
		index = indexInput;
		startDate = startDateInput;
		endDate = endDateInput;
		System.out.println(toString());
		logExitMethod("CommandInfo");

	}

	/**
	 * Default constructor setting all parameters to null.
	 */
	public CommandInfo() {
		logEnterMethod("CommandInfo");
		commandEnum = null;
		taskName = null;
		startDate = null;
		endDate = null;
		edittedName = null;
		index = 0;
		logExitMethod("CommandInfo");
	}

	/**
	 * Getter for task name.
	 * 
	 * @return Returns the task name.
	 */
	public String getTaskName() {
		logEnterMethod("getTaskName");
		logExitMethod("getTaskName");
		return taskName;
	}

	/**
	 * Getter for editted name.
	 * 
	 * @return Returns the editeed name.
	 */
	public String getEdittedName() {
		logEnterMethod("getEdittedName");
		logExitMethod("getEdittedName");
		return edittedName;
	}

	/**
	 * Getter for end date.
	 * 
	 * @return Returns the end date.
	 */
	public DateTime getEndDate() {
		logEnterMethod("getEndDate");
		logExitMethod("getEndDate");
		return endDate;
	}

	/**
	 * Getter for start date.
	 * 
	 * @return Returns the start date.
	 */
	public DateTime getStartDate() {
		logEnterMethod("getStartDate");
		logExitMethod("getStartDate");
		return startDate;
	}

	/**
	 * Getter for the command.
	 * 
	 * @return Returns the command.
	 */
	public CommandKeyWords getCommandEnum() {
		logEnterMethod("getCommandEnum");
		logExitMethod("getCommandEnum");
		return commandEnum;
	}

	/**
	 * Getter for the index.
	 * 
	 * @return Returns the index.
	 */
	public int getIndex() {
		logEnterMethod("getIndex");
		logExitMethod("getIndex");
		return index;
	}

	/**
	 * ToString function to see all the parameters initialized.
	 */
	public String toString() {
		logEnterMethod("toString");

		String outString = "";
		if (commandEnum != null)
			outString = ("Command : " + commandEnum.name());
		if (taskName != null)
			outString += (" Task name : " + taskName);
		if (edittedName != null)
			outString += (" Editted name : " + edittedName);
		if (startDate != null)
			outString += (" Start Date : " + startDate.toString("dd MMM yyyy HH:mm"));
		if (endDate != null)
			outString += (" End Date : " + endDate.toString("dd MMM yyyy HH:mm"));
		if(index != 0){
			outString += (" Index is : " + index);
		}
		logExitMethod("toString");
		return outString;
	}

	public String toHtmlString() {
		logEnterMethod("toString");
		HtmlCreator htmlCreator = new HtmlCreator(); 
		String outString = "";
		if (commandEnum != null)
			outString = ((commandEnum.name()));
		if(index != 0)
			outString += (" " + (index));
		if (taskName != null)
			outString += (" " + taskName);
		if (edittedName != null)
			outString += (" " + edittedName);
		if (startDate != null)
			outString += (" " + startDate.toString("dd MMM yyyy HH:mm"));
		if (endDate != null){
			if(startDate != null){
				outString += " -";
			}
			outString += (" " + endDate.toString("dd MMM yyyy HH:mm"));
		}
		logExitMethod("toString");
		return htmlCreator.makeBold(outString);
	}

	/**
	 * Function to check if 2 commandInfo objects are equal.
	 * 
	 * @param commandInfo1
	 * @param commandInfo2
	 * 
	 * @return Returns a true if the 2 command Info are equal.
	 */
	public boolean isEqual(CommandInfo commandInfo1, CommandInfo commandInfo2) {

		logEnterMethod("isEqual");

		if (isEqualCommandEnum(commandInfo1, commandInfo2)
				&& isEqualTaskName(commandInfo1, commandInfo2)
				&& isEqualEdittedName(commandInfo1, commandInfo2)
				&& isEqualStartDate(commandInfo1, commandInfo2)
				&& isEqualEndDate(commandInfo1, commandInfo2)
				&& isEqualIndex(commandInfo1, commandInfo2)) {
			logExitMethod("isEqual");
			return true;
		}
		logExitMethod("isEqual");
		return false;
	}

	/**
	 * Function to check if 2 Index are equal.
	 * 
	 * @param commandInfo1
	 * @param commandInfo2
	 * 
	 * @return Returns true if the 2 index are equal.
	 */
	private boolean isEqualIndex(CommandInfo commandInfo1,
			CommandInfo commandInfo2) {

		logEnterMethod("isEqualIndex");
		if (commandInfo1.index != commandInfo2.index) {
			System.out.println(ERROR_INDEX + commandInfo1.index + " "
					+ commandInfo2.index);
			logExitMethod("isEqualIndex");
			return false;
		}
		logExitMethod("isEqualIndex");
		return true;
	}

	/**
	 * Function to check if 2 EndDates are equal.
	 * 
	 * @param commandInfo1
	 * @param commandInfo2
	 * 
	 * @return Returns true if the 2 EndDates are equal.
	 */
	private boolean isEqualEndDate(CommandInfo commandInfo1,
			CommandInfo commandInfo2) {
		logEnterMethod("isEqualEndDate");
		try {
			if (!commandInfo1.endDate.equals(commandInfo2.endDate)) {
				System.out.println(ERROR_END_DATE
						+ commandInfo1.endDate.toString() + " "
						+ commandInfo2.endDate.toString());
				logExitMethod("isEqualEndDate");
				return false;
			}
		} catch (NullPointerException e) {
			if (commandInfo1.endDate != null || commandInfo2.endDate != null) {
				logExitMethod("isEqualEndDate");
				return false;
			}
		}
		logExitMethod("isEqualEndDate");
		return true;
	}

	/**
	 * Function to check if 2 StartDates are equal.
	 * 
	 * @param commandInfo1
	 * @param commandInfo2
	 * 
	 * @return Returns true if the 2 StartDates are equal.
	 */
	private boolean isEqualStartDate(CommandInfo commandInfo1,
			CommandInfo commandInfo2) {
		logEnterMethod("isEqualStartDate");
		try {
			if (!commandInfo1.startDate.equals(commandInfo2.startDate)) {
				System.out.println(ERROR_START_DATE
						+ commandInfo1.startDate.toString() + " "
						+ commandInfo2.startDate.toString());
				logExitMethod("isEqualStartDate");
				return false;
			}
		} catch (NullPointerException e) {
			if (commandInfo1.startDate != null
					|| commandInfo2.startDate != null) {
				logExitMethod("isEqualStartDate");
				return false;
			}
		}
		logExitMethod("isEqualStartDate");
		return true;
	}

	/**
	 * Function to check if 2 CommandEnum are equal.
	 * 
	 * @param commandInfo1
	 * @param commandInfo2
	 * 
	 * @return Returns true if the 2 CommandEnum are equal.
	 */
	private boolean isEqualCommandEnum(CommandInfo commandInfo1,
			CommandInfo commandInfo2) {

		logEnterMethod("isEqualCommandEnum");
		if (commandInfo1.commandEnum != commandInfo2.commandEnum) {
			System.out.println("CommanEnum Error! "
					+ commandInfo1.commandEnum.name() + " "
					+ commandInfo2.commandEnum.name());
			logExitMethod("isEqualCommandEnum");
			return false;
		}
		logExitMethod("isEqualCommandEnum");
		return true;
	}

	/**
	 * Function to check if 2 editted Names are equal.
	 * 
	 * @param commandInfo1
	 * @param commandInfo2
	 * 
	 * @return Returns true if the 2 editted Names are equal.
	 */
	private boolean isEqualEdittedName(CommandInfo commandInfo1,
			CommandInfo commandInfo2) {

		logEnterMethod("isEqualEdittedName");
		try {
			if (!commandInfo1.edittedName.equals(commandInfo2.edittedName)) {
				System.out.println(ERROR_EDITTED_NAME
						+ commandInfo1.edittedName + " "
						+ commandInfo2.edittedName);
				logExitMethod("isEqualEdittedName");
				return false;
			}
		} catch (NullPointerException e) {
			if (commandInfo1.edittedName != null
					|| commandInfo2.edittedName != null) {
				logExitMethod("isEqualEdittedName");
				return false;
			}
		}
		logExitMethod("isEqualEdittedName");
		return true;
	}

	/**
	 * Function to check if 2 task Names are equal.
	 * 
	 * @param commandInfo1
	 * @param commandInfo2
	 * 
	 * @return Returns true if the 2 task Names are equal.
	 */
	private boolean isEqualTaskName(CommandInfo commandInfo1,
			CommandInfo commandInfo2) {

		logEnterMethod("isEqualTaskName");
		try {
			if (!commandInfo1.taskName.equals(commandInfo2.taskName)) {
				System.out.println(ERROR_TASK_NAME + commandInfo1.taskName
						+ " " + commandInfo2.taskName);
				logExitMethod("isEqualTaskName");
				return false;
			}
		} catch (NullPointerException e) {
			if (commandInfo1.taskName != null || commandInfo2.taskName != null) {
				logExitMethod("isEqualTaskName");
				return false;
			}
		}
		logExitMethod("isEqualTaskName");
		return true;
	}
	
	/**
	 * Logger exit method
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