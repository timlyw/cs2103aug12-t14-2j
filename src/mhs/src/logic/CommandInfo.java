//@author A0086805X
package mhs.src.logic;

import java.util.logging.Logger;

import mhs.src.common.DateTimeHelper;
import mhs.src.common.HtmlCreator;
import mhs.src.common.MhsLogger;

import org.joda.time.DateTime;

/**
 * This is the class to package the parameters into a command object.
 */
public class CommandInfo {

	//colors used in html strings
	private static final String COLOR_BLUE = "blue";	
	private static final String COLOR_GREEN = "green";
	private static final String COLOR_RED = "red";

	//Strings needed for sentance stucture
	private static final String STRING_OR = " or ";
	private static final String STRING_TO_BE_CHANGED = " to be changed.";
	private static final String STRING_AND = " and ";
	private static final String STRING_FOLLOWED_BY = " followed by ";
	private static final String STRING_ENTER = "Enter ";

	//Strings for feedback on the parameters needed
	private static final String FEEDBACK_DATE_RANGE = "date range";
	private static final String FEEDBACK_EDITTED_NAME = "editted name";
	private static final String FEEDBACK_EDIT_PARAMETERS = "new task name or new date time";
	private static final String FEEDBACK_TIME_OPTIONAL = "time if needed :)";
	private static final String FEEDBACK_TASKNAME = "task name";
	private static final String FEEDBACK_TASKNAME_INDEX = "task name / index";
	
	//regex for characters
	private static final String REGEX_SPACE = " ";
	private static final String REGEX_DASH = " -";
	
	private static final String KEYWORD_INDEX = " Index is : ";
	private static final String KEYWORD_END_DATE = " End Date : ";
	private static final String KEYWORD_STARTDATE = " Start Date : ";
	private static final String KEYWORD_EDITTED_NAME = " Editted name : ";
	private static final String KEYWORD_TASK_NAME = " Task name : ";
	private static final String KEYWORD_COMMAND = "Command : ";
	// Strings for feedback ont the commands given
	private static final String COMMAND_FEEDBACK_SEARCH = "Display : ";
	private static final String COMMAND_FEEDBACK_UNMARK = "Unmark : ";
	private static final String COMMAND_FEEDBACK_MARK = "Mark : ";
	private static final String COMMAND_FEEDBACK_EXIT = "Exit program";
	private static final String COMMAND_FEEDBACK_LOGOUT = "Logout from Google Calendar ";
	private static final String COMMAND_FEEDBACK_LOGIN = "Login to Google Calendar ";
	private static final String COMMAND_FEEDBACK_SYNC = "Sync tasks with Google Calendar";
	private static final String COMMAND_FEEDBACK_REMOVE = "Delete : ";
	private static final String COMMAND_FEEDBACK_RENAME = "Rename : ";
	private static final String COMMAND_FEEDBACK_TIMED = "Display all timed tasks";
	private static final String COMMAND_FEEDBACK_DEADLINE = "Display all deadline tasks";
	private static final String COMMAND_FEEDBACK_FLOATING = "Display all floating tasks";
	private static final String COMMAND_FEEDBACK_HOME = "Display home page";
	private static final String COMMAND_FEEDBACK_EDIT = "Edit : ";
	private static final String COMMAND_FEEDBACK_ADD = "Add : ";

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
		add, remove, edit, search, sync, undo, login, logout, rename, redo, mark, help, unmark, previous, next, floating, deadline, timed, home, exit;
	}

	private String taskName;
	private String edittedName;
	private CommandKeyWords commandEnum;

	private DateTime startDate;
	private DateTime endDate;
	private int index;

	private static final Logger logger = MhsLogger.getLogger();
	private static HtmlCreator htmlCreator = new HtmlCreator();

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
		clearParameters();
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
	 * Default constrcutor resetting all parameters.
	 */
	public CommandInfo(){
		logEnterMethod("CommandInfo");
		clearParameters();
		logExitMethod("CommandInfo");

	}
	
	/**
	 * Method to set all parameters to null.
	 */
	public void clearParameters() {
		logEnterMethod("clearParameters");
		commandEnum = null;
		taskName = null;
		startDate = null;
		endDate = null;
		edittedName = null;
		index = 0;
		logExitMethod("clearParameters");
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
	 * ToString function to see all the parameters initialised.
	 */
	public String toString() {
		logEnterMethod("toString");

		DateTimeHelper dateTimeFormatter = new DateTimeHelper();
		String outString = "";
		if (commandEnum != null)
			outString = (KEYWORD_COMMAND + commandEnum.name());
		if (taskName != null)
			outString += (KEYWORD_TASK_NAME + taskName);
		if (edittedName != null)
			outString += (KEYWORD_EDITTED_NAME + edittedName);
		if (startDate != null)
			outString += (KEYWORD_STARTDATE + dateTimeFormatter
					.formatDateTimeToString(startDate));
		if (endDate != null)
			outString += (KEYWORD_END_DATE + dateTimeFormatter
					.formatDateTimeToString(endDate));
		if (index != 0) {
			outString += (KEYWORD_INDEX + index);
		}
		logExitMethod("toString");
		return outString;
	}

	/**
	 * ToString function to display for command feedback
	 * @return
	 */
	public String toHtmlString() {
		logEnterMethod("toHtmlString");
		String outString = "";
		DateTimeHelper dateTimeFormatter = new DateTimeHelper();
		outString = getCommandEnumFeedback();
		if (index != 0){
			outString += (" at index" + (index));
		}
		if (taskName != null){
			outString += (REGEX_SPACE + taskName);
		}
		if (edittedName != null){
			outString += (REGEX_SPACE + edittedName);
		}
		if (startDate != null){
			outString += (REGEX_SPACE + dateTimeFormatter
					.formatDateTimeToString(startDate));
		}
		if (endDate != null) {
			if (startDate != null) {
				outString += REGEX_DASH;
			}
			outString += (REGEX_SPACE + dateTimeFormatter
					.formatDateTimeToString(endDate));
		}
		logExitMethod("toHtmlString");
		return (outString);
	}

	/**
	 * Method to get the command feedback for commands
	 * @param outString
	 * @return Returns a html string.
	 */
	private String getCommandEnumFeedback() {
		logEnterMethod("getCommandEnumFeedback");
		String outString = "";
		if (commandEnum != null) {
			switch (commandEnum) {
			case add:
				outString = getAddFeedback();
				break;
			case edit:
				outString = getEditFeedback();
				break;
			case home:
				outString = getFeedbackHome();
				break;
			case floating:
				outString = getFeedBackFloating();
				break;
			case deadline:
				outString = getFeedBackDeadline();
				break;
			case timed:
				outString = getFeedbackTimed();
				break;
			case rename:
				outString = getRenameFeedback();
				break;
			case remove:
				outString = getRemoveFeedback();
				break;
			case sync:
				outString = getFeedbackSync();
				break;
			case login:
				outString = getFeedbackLogin();
				break;
			case logout:
				outString = getFeedbackLogout();
				break;
			case exit:
				outString = getFeedBackExit();
				break;
			case mark:
				outString = getMarkFeedback();
				break;
			case unmark:
				outString = getUnmarkFeedBack();
				break;
			case search:
				outString = getSearchFeedback();
				break;
			default:
				outString = (commandEnum.name());
				break;
			}
			logExitMethod("getCommandEnumFeedback");
		}
		return outString;
	}

	/**
	 * Method to get feedback for search command.
	 * @return Returns a html string.
	 */
	private String getSearchFeedback() {
		logEnterMethod("getSearchFeedback");
		String outString;
		if (taskName == null && edittedName == null
				&& startDate == null && endDate == null) {
			outString = getSearchParametersFeedback();
		} else
			outString = COMMAND_FEEDBACK_SEARCH;
		logExitMethod("getSearchFeedback");
		return outString;
	}

	/**
	 * Method to get feedback for parameters needed for searching.
	 * @return Returns a html string.
	 */
	private String getSearchParametersFeedback() {
		logEnterMethod("getSearchParametersFeedback");
		String outString;
		outString = STRING_ENTER
				+ htmlCreator.color(FEEDBACK_TASKNAME, COLOR_BLUE) + STRING_OR
				+ htmlCreator.color(FEEDBACK_DATE_RANGE, COLOR_BLUE);
		logExitMethod("getSearchParametersFeedback");
		return outString;
	}

	/**
	 * Method to get feedback for unmark command.
	 * @return Returns a string.
	 */
	private String getUnmarkFeedBack() {
		logEnterMethod("getUnmarkFeedBack");
		String outString;
		if (taskName == null && edittedName == null
				&& startDate == null && endDate == null) {
			outString = getRemoveParametersFeedBack();
		} else
			outString = COMMAND_FEEDBACK_UNMARK;
		logExitMethod("getUnmarkFeedBack");
		return outString;
	}

	/**
	 * Method to get feedback for mark command.
	 * @return Returns a string.
	 */
	private String getMarkFeedback() {
		logEnterMethod("getMarkFeedback");
		String outString;
		if (taskName == null && edittedName == null
				&& startDate == null && endDate == null) {
			outString = getRemoveParametersFeedBack();
		} else
			outString = COMMAND_FEEDBACK_MARK;
		logExitMethod("getMarkFeedback");
		return outString;
	}

	/**
	 * Method to get feedback for exit command.
	 * @return Returns a string.
	 */
	private String getFeedBackExit() {
		logEnterMethod("getFeedBackExit");
		String outString;
		outString = COMMAND_FEEDBACK_EXIT;
		logExitMethod("getFeedBackExit");
		return outString;
	}
	
	/**
	 * Method to get feedback for lgout command.
	 * @return Returns a string.
	 */
	private String getFeedbackLogout() {
		logEnterMethod("getFeedbackLogout");
		String outString;
		outString = COMMAND_FEEDBACK_LOGOUT;
		logExitMethod("getFeedbackLogout");
		return outString;
	}

	/**
	 * Method to get feedback for login command.
	 * @return Returns a string.
	 */
	private String getFeedbackLogin() {
		logEnterMethod("getFeedbackLogin");
		String outString;
		outString = COMMAND_FEEDBACK_LOGIN;
		logExitMethod("getFeedbackLogin");
		return outString;
	}

	/**
	 * Method to get feedback for sync command.
	 * @return Returns a string.
	 */
	private String getFeedbackSync() {
		logEnterMethod("getFeedbackSync");
		String outString;
		outString = COMMAND_FEEDBACK_SYNC;
		logExitMethod("getFeedbackSync");
		return outString;
	}

	/**
	 * Method to get feedback for remove command.
	 * @return Returns a html string.
	 */
	private String getRemoveFeedback() {
		logEnterMethod("getRemoveFeedback");
		String outString;
		if (taskName == null && edittedName == null
				&& startDate == null && endDate == null) {
			outString = getRemoveParametersFeedBack();
		} else
			outString = COMMAND_FEEDBACK_REMOVE;
		logExitMethod("getRemoveFeedback");
		return outString;
	}

	/**
	 * Method to get feedback for remove command parameters.
	 * @return Returns a html string.
	 */
	private String getRemoveParametersFeedBack() {
		logEnterMethod("getRemoveParametersFeedBack");
		String outString;
		outString = STRING_ENTER
				+ htmlCreator.color(FEEDBACK_TASKNAME_INDEX, COLOR_RED);
		logExitMethod("getRemoveParametersFeedBack");
		return outString;
	}

	/**
	 * Method to get feedback for rename command.
	 * @return Returns a html string.
	 */
	private String getRenameFeedback() {
		logEnterMethod("getRenameFeedback");
		String outString;
		if (taskName == null && edittedName == null
				&& startDate == null && endDate == null) {
			outString = getRenameParametersFeedback();
		} else
			outString = COMMAND_FEEDBACK_RENAME;
		logExitMethod("getRenameFeedback");
		return outString;
	}

	/**
	 * Method to get feedback for rename command parameters.
	 * @return Returns a string.
	 */
	private String getRenameParametersFeedback() {
		logEnterMethod("getRenameParametersFeedback");
		String outString;
		outString = STRING_ENTER
				+ htmlCreator.color(FEEDBACK_TASKNAME_INDEX, COLOR_RED)
				+ STRING_AND
				+ htmlCreator.color(FEEDBACK_EDITTED_NAME, COLOR_GREEN);
		logExitMethod("getRenameParametersFeedback");
		return outString;
	}

	/**
	 * Method to get feedback for timed command.
	 * @return Returns a string.
	 */
	private String getFeedbackTimed() {
		logEnterMethod("getFeedbackTimed");
		String outString;
		outString = COMMAND_FEEDBACK_TIMED;
		logExitMethod("getFeedbackTimed");
		return outString;
	}

	/**
	 * Method to get feedback for deadline command.
	 * @return Returns a string.
	 */
	private String getFeedBackDeadline() {
		logEnterMethod("getFeedBackDeadline");
		String outString;
		outString = COMMAND_FEEDBACK_DEADLINE;
		logExitMethod("getFeedBackDeadline");
		return outString;
	}

	/**
	 * Method to get feedback for floating command.
	 * @return Returns a string.
	 */
	private String getFeedBackFloating() {
		logEnterMethod("getFeedBackFloating");
		String outString;
		outString = COMMAND_FEEDBACK_FLOATING;
		logExitMethod("getFeedBackFloating");
		return outString;
	}

	/**
	 * Method to get feedback for home command.
	 * @return Returns a string.
	 */
	private String getFeedbackHome() {
		logEnterMethod("getFeedbackHome");
		String outString;
		outString = COMMAND_FEEDBACK_HOME;
		logExitMethod("getFeedbackHome");
		return outString;
	}

	/**
	 * Method to get feedback for add command.
	 * @return Returns a string.
	 */
	private String getAddFeedback() {
		logEnterMethod("getAddFeedback");
		String outString;
		if (taskName == null && edittedName == null
				&& startDate == null && endDate == null) {
			outString = getAddParametersFeedback();
		} else
			outString = COMMAND_FEEDBACK_ADD;
		logExitMethod("getAddFeedback");
		return outString;
	}

	/**
	 * Method to get feedback for add command parameters.
	 * @return Returns a string.
	 */
	private String getAddParametersFeedback() {
		logEnterMethod("getAddParametersFeedback");
		String outString;
		outString = STRING_ENTER
				+ htmlCreator.color(FEEDBACK_TASKNAME, COLOR_RED) + STRING_AND
				+ htmlCreator.color(FEEDBACK_TIME_OPTIONAL, COLOR_GREEN);
		logExitMethod("getAddParametersFeedback");
		return outString;
	}

	/**
	 * Method to get feedback for edit command.
	 * @return Returns a string.
	 */
	private String getEditFeedback() {
		logEnterMethod("getEditFeedback");
		String outString;
		if (taskName == null && edittedName == null
				&& startDate == null && endDate == null) {
			outString = getEditParametersFeedback();
		} else
			outString = COMMAND_FEEDBACK_EDIT;
		logExitMethod("getEditFeedback");
		return outString;
	}

	/**
	 * Method to get feedback for edit command parameters.
	 * @return Returns a html string.
	 */
	private String getEditParametersFeedback() {
		logEnterMethod("getEditParametersFeedback");
		String outString;
		outString = STRING_ENTER
				+ htmlCreator.color(FEEDBACK_TASKNAME_INDEX, COLOR_RED)
				+ STRING_FOLLOWED_BY
				+ htmlCreator.color(
						FEEDBACK_EDIT_PARAMETERS, COLOR_RED)
				+ STRING_TO_BE_CHANGED;
		logExitMethod("getEditParametersFeedback");
		return outString;
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
			System.out.println(ERROR_INDEX + commandInfo1.index + REGEX_SPACE
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
				printErrorEndDate(commandInfo1, commandInfo2);
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
	 * Prints the differences in the 2 end dates.
	 * 
	 * @param commandInfo1
	 * @param commandInfo2
	 */
	private void printErrorEndDate(CommandInfo commandInfo1,
			CommandInfo commandInfo2) {
		logEnterMethod("printErrorEndDate");
		System.out.println(ERROR_END_DATE + commandInfo1.endDate.toString()
				+ REGEX_SPACE + commandInfo2.endDate.toString());
		logExitMethod("printErrorEndDate");

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
				printErrorStartDate(commandInfo1, commandInfo2);
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
	 * Prints the differences in the 2 start dates.
	 * 
	 * @param commandInfo1
	 * @param commandInfo2
	 */
	private void printErrorStartDate(CommandInfo commandInfo1,
			CommandInfo commandInfo2) {
		logEnterMethod("printErrorStartDate");
		System.out.println(ERROR_START_DATE + commandInfo1.startDate.toString()
				+ REGEX_SPACE + commandInfo2.startDate.toString());
		logExitMethod("printErrorStartDate");
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
			printErrorCommand(commandInfo1, commandInfo2);
			logExitMethod("isEqualCommandEnum");
			return false;
		}
		logExitMethod("isEqualCommandEnum");
		return true;
	}

	/**
	 * Print the differences in the 2 commands
	 * 
	 * @param commandInfo1
	 * @param commandInfo2
	 */
	private void printErrorCommand(CommandInfo commandInfo1,
			CommandInfo commandInfo2) {
		logEnterMethod("printErrorCommand");
		System.out.println("CommanEnum Error! "
				+ commandInfo1.commandEnum.name() + REGEX_SPACE
				+ commandInfo2.commandEnum.name());
		logExitMethod("printErrorCommand");

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
				printErrorEdittedName(commandInfo1, commandInfo2);
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
	 * Prints the error in the 2 editted names.
	 * 
	 * @param commandInfo1
	 * @param commandInfo2
	 */
	private void printErrorEdittedName(CommandInfo commandInfo1,
			CommandInfo commandInfo2) {
		logEnterMethod("printErrorEdittedName");
		System.out.println(ERROR_EDITTED_NAME + commandInfo1.edittedName
				+ REGEX_SPACE + commandInfo2.edittedName);
		logExitMethod("printErrorEdittedName");

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
				printErrorTaskName(commandInfo1, commandInfo2);
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
	 * Prints the error in the 2 task names.
	 * 
	 * @param commandInfo1
	 * @param commandInfo2
	 */
	private void printErrorTaskName(CommandInfo commandInfo1,
			CommandInfo commandInfo2) {
		logEnterMethod("printErrorTaskName");
		System.out.println(ERROR_TASK_NAME + commandInfo1.taskName
				+ REGEX_SPACE + commandInfo2.taskName);
		logExitMethod("printErrorTaskName");

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