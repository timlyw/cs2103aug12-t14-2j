//@A0086805X

package mhs.src.logic;

import java.util.logging.Logger;

import mhs.src.common.HtmlCreator;
import mhs.src.common.MhsLogger;

public class Help {

	//Strings used for the command list help.
	private static final String HELP_COMMAND_FEEDBACK = "How would u like to command me?  :)";
	private static final String HELP_COMMAND_LIST_MINIMIZE = "minimize - minimize program";
	private static final String HELP_COMMAND_LIST_EXIT = "exit - exit the program";
	private static final String HELP_COMMAND_LIST_TIMED = "timed - Searcb for timed tasks";
	private static final String HELP_COMMAND_LIST_DEADLINE = "deadline - Search for deadline tasks";
	private static final String HELP_COMMAND_LIST_FLOATING = "floating - Search for floating tasks";
	private static final String HELP_COMMAND_LIST_PREVIOUS = "p - Scroll to previos page";
	private static final String HELP_COMMAND_LIST_NEXT = "n - Scroll to next page";
	private static final String HELP_COMMAND_LIST_UNMARK = "unmark - Unmark a done task";
	private static final String HELP_COMMAND_LIST_MARK = "mark - Mark a task done";
	private static final String HELP_COMMAND_LIST_HELP = "help - Get help";
	private static final String HELP_COMMAND_LIST_LOGOUT = "logout, signout - Logout to google calendar";
	private static final String HELP_COMMAND_LIST_LOGIN = "login, signin - Login to google calendar";
	private static final String HELP_COMMAND_LIST_RENAME = "rename - Rename a task";
	private static final String HELP_COMMAND_LIST_REDO = "redo - Redo last command";
	private static final String HELP_COMMAND_LIST_UNDO = "undo - Undo last command";
	private static final String HELP_COMMAND_LIST_SYNC = "sync - Sync tasks with google calendar";
	private static final String HELP_COMMAND_LIST_SEARCH = "search, find, display - Searching for tasks";
	private static final String HELP_COMMAND_LIST_REMOVE = "remove, delete - Deleting a task";
	private static final String HELP_COMMAND_LIST_ADD = "Commands add - Adding a task";
	
	//Strings used for name format help.
	private static final String HELP_NAME_FORMAT_FEEDBACK = "You may address me by these names. :)";
	private static final String STRING_NAME_FORMAT = "Name retrival may confict with date time parameters entered, for more acurate task names, enter the name withing quotation marks.";
	
	//Strings used for time format help.
	private static final String HELP_TIME_FORMAT_FEEDBACK = "I will be able to find you at these times.  :)";
	private static final String EXAMPLES_TIME_FORMAT_3 = "23:50";
	private static final String EXAMPLES_TIME_FORMAT_2 = "4am";
	private static final String EXAMPLES_TIME_FORMAT_1 = "2pm";
	private static final String STRING_TIME_DEFAULTING = "Times that are not set are defaulted to 23:59";
	private static final String STRING_TIME_FORMAT = "Full support for 12hrs and 24hrs timing";
	
	//Strings used for date format help.
	private static final String HELP_DATE_FORMAT_FEEDBACK = "These are the dates you can find me.  :)";
	private static final String EXAMPLE_DATE_FORMAT_9 = "5/11/2012";
	private static final String EXAMPLE_DATE_FORMAT_8 = "5/11";
	private static final String EXAMPLE_DATE_FORMAT_7 = "this weekend";
	private static final String EXAMPLE_DATE_FORMAT_6 = "this year";
	private static final String EXAMPLE_DATE_FORMAT_5 = "this month";
	private static final String EXAMPLE_DATE_FORMAT_4 = "this week";
	private static final String EXAMPLE_DATE_FOMAT_3 = "tomorrow";
	private static final String EXAMPLE_DATE_FORMAT_2 = "today";
	private static final String EXAMPLE_DATE_FORMAT_1 = "10 nov 2012";
	private static final String STRING_DATE_DEFAULTING = "Dates that are not set are defaulted to todays date ";
	private static final String STRING_DATE_FORMATS_SUPPORT = "Full support for all months and days in both words and numbers";
	
	//Strings used for search help.
	private static final String HELP_SEARCH_FEEDBACK = "You looking for something? :?";
	private static final String EXAMPLE_SEARCH_DATE_RANGE_2 = "search 7/11 to 10/11/2012";
	private static final String EXAMPLE_SEARCH_DATE_RANGE = "display this weekend";
	private static final String EXAMPLE_SEARCH_TASK = "find laundry";
	private static final String STRING_SEARCH_TIMED = " to search for timed tasks.";
	private static final String COMMAND_TIMED = "timed";
	private static final String STRING_SEARCH_DEADLINE = " to search for deadline tasks.";
	private static final String COMMAND_DEADLINE = "deadline";
	private static final String STRING_SEARCH_FLOATING = " to search for floating tasks.";
	private static final String COMMAND_FLOATING = "floating";
	private static final String STRING_SEARCH_DATE_RANGE = " to search for tasks in a date range.";
	private static final String STRING_SEARCH_FOR_TASKS = " to search for a task.";
	private static final String COMMAND_SEARCH = "Search";
	private static final String COMMAND_KEYWORDS_SEARCH = "Commands : Search, find, display";
	
	//Strings used for edit help.
	private static final String HELP_EDIT_FEEDBACK = "You like these new changes?";
	private static final String EXAMPLE_EDIT_TIME_RANGE = "update holiday this weekend";
	private static final String EXAMPLE_EDIT_TIME = "postpone assignment submission to 23 10";
	private static final String EXAMPLE_EDIT_NAME = "Edit 5 to homework";
	private static final String STRING_EXAMPLES = "Examples";
	private static final String STRING_EDIT_DATE_RANGE = " for editting a task to a timed task with the new date time range specified.";
	private static final String STRING_CHANGE_DATE_TIME = " for editting a task to a deadline task with the new date time specified.";
	private static final String COMMAND_EDIT = "Edit";
	private static final String STRING_EDIT_NAME = " for editting a task to a floating task with editted name as the new task name.";
	private static final String PARAMETERS_EDITTED_NAME = "editted name";
	private static final String PARAMETERS_NAME_INDEX = "task name / Index";
	private static final String COMMAND_EDIT_KEYWORDS = "Commands : Update, edit, postpone";
	
	//Strings used for add help.
	private static final String HELP_ADD_FEEDBACK = "Please fill me up! :D";
	private static final String EXAMPLE_ADD_TIMED = "Add honeymoon 3pm sunday to 23:00 23/11";
	private static final String EXAPLE_ADD_DEADLINE = "Add private meeting at 5pm today";
	private static final String EXAMPLE_ADD_FLOATING = "Add laundry";
	private static final String STRING_ADD_TIMED = " for adding a timed task.";
	private static final String STRING_TO = " to ";
	private static final String PARAMETER_TIME = "time";
	private static final String STRING_ADD_DEADLINE = " for adding a deadline task.";
	private static final String STRING_AND_OR = " and / or ";
	private static final String PARAMETER_DATE = "date";
	private static final String STRING_WITH = " with ";
	private static final String STRING_ADD_FLOATING = " for adding a floating task.";
	private static final String COLOR_RED = "red";
	private static final String PARAMETER_TASK_NAME = "task name";
	private static final String STRING_A = " a ";
	private static final String COLOR_GREEN = "green";
	private static final String COMMAND_ADD = "Add";
	private static final String STRING_ENTER = "Enter ";
	
	//Strings used for the help index. 
	private static final String HELP_INDEX_FEEDBACK = "How would you like me to assist you?";
	private static final String COMMANDS_ADD_KEYWORDS = "Commands : add";
	private static final String HELP_COMMAND_LIST = "7. Supported Command List";
	private static final String HELP_NAME_FORMATS = "6. Supported Name Formats";
	private static final String HELP_TIME_FORMATS = "5. Supported Time Formats";
	private static final String HELP_DATE_FORMATS = "4. Supported Date Formats";
	private static final String HELP_SEARCH = "3. Search Help";
	private static final String HELP_EDIT = "2. Edit Help";
	private static final String HELP_ADD = "1. Add Help";
	
	private String screenState;
	private String feedback;
	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * Constructor to call for the help index.
	 */
	public Help() {
		logEnterMethod("Help");

		screenState = HELP_ADD + HtmlCreator.NEW_LINE + HELP_EDIT
				+ HtmlCreator.NEW_LINE + HELP_SEARCH
				+ HtmlCreator.NEW_LINE + HELP_DATE_FORMATS
				+ HtmlCreator.NEW_LINE + HELP_TIME_FORMATS
				+ HtmlCreator.NEW_LINE + HELP_NAME_FORMATS
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST;

		feedback = HELP_INDEX_FEEDBACK;
		logExitMethod("Help");

	}

	/**
	 * Method to get help for adding.
	 */
	public void HelpAdd() {
		logEnterMethod("HelpAdd");
		HtmlCreator htmlCreator = new HtmlCreator();
		
		screenState = COMMANDS_ADD_KEYWORDS + HtmlCreator.NEW_LINE
				+ HtmlCreator.NEW_LINE + STRING_ENTER
				+ htmlCreator.color(COMMAND_ADD, COLOR_GREEN) + STRING_A
				+ htmlCreator.color(PARAMETER_TASK_NAME, COLOR_RED)
				+ STRING_ADD_FLOATING + HtmlCreator.NEW_LINE
				+ STRING_ENTER + htmlCreator.color(COMMAND_ADD, COLOR_GREEN) + STRING_A
				+ htmlCreator.color(PARAMETER_TASK_NAME, COLOR_RED) + STRING_WITH
				+ htmlCreator.color(PARAMETER_DATE, COLOR_RED) + STRING_AND_OR
				+ htmlCreator.color(PARAMETER_TIME, COLOR_RED)
				+ STRING_ADD_DEADLINE + HtmlCreator.NEW_LINE
				+ STRING_ENTER + htmlCreator.color(COMMAND_ADD, COLOR_GREEN) + STRING_A
				+ htmlCreator.color(PARAMETER_TASK_NAME, COLOR_RED) + STRING_WITH
				+ htmlCreator.color(PARAMETER_DATE, COLOR_RED) + STRING_AND_OR
				+ htmlCreator.color(PARAMETER_TIME, COLOR_RED)
				+ htmlCreator.color(STRING_TO, COLOR_GREEN)
				+ htmlCreator.color(PARAMETER_DATE, COLOR_RED) + STRING_AND_OR
				+ htmlCreator.color(PARAMETER_TIME, COLOR_RED)
				+ STRING_ADD_TIMED + HtmlCreator.NEW_LINE
				+ HtmlCreator.NEW_LINE + STRING_EXAMPLES + HtmlCreator.NEW_LINE
				+ EXAMPLE_ADD_FLOATING + HtmlCreator.NEW_LINE
				+ EXAPLE_ADD_DEADLINE + HtmlCreator.NEW_LINE
				+ EXAMPLE_ADD_TIMED;
		
		feedback = HELP_ADD_FEEDBACK;
		logExitMethod("HelpAdd");

	}

	/**
	 * Method to get help for editting. 
	 */
	public void HelpEdit() {
		logEnterMethod("HelpEdit");
		HtmlCreator htmlCreator = new HtmlCreator();

		screenState = COMMAND_EDIT_KEYWORDS
				+ HtmlCreator.NEW_LINE
				+ HtmlCreator.NEW_LINE
				+ STRING_ENTER
				+ htmlCreator.color(COMMAND_EDIT, COLOR_GREEN)
				+ STRING_A
				+ htmlCreator.color(PARAMETERS_NAME_INDEX, COLOR_RED)
				+ htmlCreator.color(STRING_TO, COLOR_RED)
				+ htmlCreator.color(PARAMETERS_EDITTED_NAME, COLOR_RED)
				+ STRING_EDIT_NAME
				+ HtmlCreator.NEW_LINE
				+ STRING_ENTER
				+ htmlCreator.color(COMMAND_EDIT, COLOR_GREEN)
				+ STRING_A
				+ htmlCreator.color(PARAMETERS_NAME_INDEX, COLOR_RED)
				+ htmlCreator.color(STRING_TO, COLOR_GREEN)
				+ htmlCreator.color(PARAMETER_DATE, COLOR_RED)
				+ STRING_AND_OR
				+ htmlCreator.color(PARAMETER_TIME, COLOR_RED)
				+ STRING_CHANGE_DATE_TIME
				+ HtmlCreator.NEW_LINE
				+ STRING_ENTER
				+ HtmlCreator.NEW_LINE
				+ STRING_ENTER
				+ htmlCreator.color(COMMAND_EDIT, COLOR_GREEN)
				+ STRING_A
				+ htmlCreator.color(PARAMETERS_NAME_INDEX, COLOR_RED)
				+ htmlCreator.color(STRING_TO, COLOR_GREEN)
				+ htmlCreator.color(PARAMETER_DATE, COLOR_RED)
				+ STRING_AND_OR
				+ htmlCreator.color(PARAMETER_TIME, COLOR_RED)
				+ htmlCreator.color(STRING_TO, COLOR_GREEN)
				+ htmlCreator.color(PARAMETER_DATE, COLOR_RED)
				+ STRING_AND_OR
				+ htmlCreator.color(PARAMETER_TIME, COLOR_RED)
				+ STRING_EDIT_DATE_RANGE
				+ HtmlCreator.NEW_LINE + HtmlCreator.NEW_LINE + STRING_EXAMPLES
				+ HtmlCreator.NEW_LINE + EXAMPLE_EDIT_NAME
				+ HtmlCreator.NEW_LINE
				+ EXAMPLE_EDIT_TIME
				+ HtmlCreator.NEW_LINE + EXAMPLE_EDIT_TIME_RANGE;

		feedback = HELP_EDIT_FEEDBACK;
		logExitMethod("HelpEdit");

	}

	/**
	 * Method to get help for searching.
	 */
	public void HelpSearch() {
		logEnterMethod("HelpSearch");
		HtmlCreator htmlCreator = new HtmlCreator();

		screenState = COMMAND_KEYWORDS_SEARCH + HtmlCreator.NEW_LINE
				+ HtmlCreator.NEW_LINE + STRING_ENTER
				+ htmlCreator.color(COMMAND_SEARCH, COLOR_GREEN) + STRING_A
				+ htmlCreator.color(PARAMETER_TASK_NAME, COLOR_RED)
				+ STRING_SEARCH_FOR_TASKS + HtmlCreator.NEW_LINE
				+ STRING_ENTER + htmlCreator.color("search", COLOR_GREEN) + STRING_A
				+ htmlCreator.color(PARAMETER_DATE, COLOR_RED) + STRING_AND_OR
				+ htmlCreator.color(PARAMETER_TIME, COLOR_RED)
				+ htmlCreator.color(STRING_TO, COLOR_GREEN)
				+ htmlCreator.color(PARAMETER_DATE, COLOR_RED) + STRING_AND_OR
				+ htmlCreator.color(PARAMETER_TIME, COLOR_RED)
				+ STRING_SEARCH_DATE_RANGE
				+ HtmlCreator.NEW_LINE + STRING_ENTER
				+ htmlCreator.color(COMMAND_FLOATING, COLOR_GREEN)
				+ STRING_SEARCH_FLOATING + HtmlCreator.NEW_LINE
				+ STRING_ENTER + htmlCreator.color(COMMAND_DEADLINE, COLOR_GREEN)
				+ STRING_SEARCH_DEADLINE + HtmlCreator.NEW_LINE
				+ STRING_ENTER + htmlCreator.color(COMMAND_TIMED, COLOR_GREEN)
				+ STRING_SEARCH_TIMED + HtmlCreator.NEW_LINE
				+ HtmlCreator.NEW_LINE + STRING_EXAMPLES + HtmlCreator.NEW_LINE
				+ EXAMPLE_SEARCH_TASK + HtmlCreator.NEW_LINE
				+ EXAMPLE_SEARCH_DATE_RANGE + HtmlCreator.NEW_LINE
				+ EXAMPLE_SEARCH_DATE_RANGE_2 + HtmlCreator.NEW_LINE
				+ COMMAND_FLOATING;

		feedback = HELP_SEARCH_FEEDBACK;
		logExitMethod("HelpSearch");
	}

	/**
	 * Method to get help for supported date formats.
	 */
	public void HelpDateFormat() {
		logEnterMethod("HelpDateFormat");

		screenState = STRING_DATE_FORMATS_SUPPORT
				+ HtmlCreator.NEW_LINE
				+ STRING_DATE_DEFAULTING
				+ HtmlCreator.NEW_LINE
				+ HtmlCreator.NEW_LINE
				+ STRING_EXAMPLES
				+ HtmlCreator.NEW_LINE
				+ EXAMPLE_DATE_FORMAT_1
				+ HtmlCreator.NEW_LINE
				+ EXAMPLE_DATE_FORMAT_2
				+ HtmlCreator.NEW_LINE
				+ EXAMPLE_DATE_FOMAT_3
				+ HtmlCreator.NEW_LINE
				+ EXAMPLE_DATE_FORMAT_4
				+ HtmlCreator.NEW_LINE
				+ EXAMPLE_DATE_FORMAT_5
				+ HtmlCreator.NEW_LINE
				+ EXAMPLE_DATE_FORMAT_6
				+ HtmlCreator.NEW_LINE
				+ EXAMPLE_DATE_FORMAT_7
				+ HtmlCreator.NEW_LINE
				+ EXAMPLE_DATE_FORMAT_8 + HtmlCreator.NEW_LINE + EXAMPLE_DATE_FORMAT_9;

		feedback = HELP_DATE_FORMAT_FEEDBACK;
		logExitMethod("HelpDateFormat");

	}

	/**
	 * Method to get help for supported time formats. 
	 */
	public void HelpTimeFormat() {
		
		logEnterMethod("HelpTimeFormat");
		screenState = STRING_TIME_FORMAT
				+ HtmlCreator.NEW_LINE
				+ STRING_TIME_DEFAULTING
				+ HtmlCreator.NEW_LINE + HtmlCreator.NEW_LINE + STRING_EXAMPLES
				+ HtmlCreator.NEW_LINE + EXAMPLES_TIME_FORMAT_1 + HtmlCreator.NEW_LINE + EXAMPLES_TIME_FORMAT_2
				+ HtmlCreator.NEW_LINE + EXAMPLES_TIME_FORMAT_3;

		feedback = HELP_TIME_FORMAT_FEEDBACK;
		logExitMethod("HelpTimeFormat");

	}

	/**
	 * Method to get help for the supported name fomats. 
	 */
	public void HelpNameFormat() {
		logEnterMethod("HelpNameFormat");
		screenState = STRING_NAME_FORMAT;
		feedback = HELP_NAME_FORMAT_FEEDBACK;
		logExitMethod("HelpNameFormat");

	}

	/**
	 * Method to get help for the supported commands. 
	 */
	public void HelpCommands() {

		logEnterMethod("HelpCommands");

		screenState = HELP_COMMAND_LIST_ADD 
				+ HtmlCreator.NEW_LINE+ HELP_COMMAND_LIST_REMOVE
				+ HtmlCreator.NEW_LINE+ HELP_COMMAND_LIST_SEARCH
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_UNDO
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_REDO
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_RENAME
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_LOGIN
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_LOGOUT
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_SYNC
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_MARK
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_UNMARK
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_FLOATING
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_DEADLINE
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_TIMED	
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_HELP	
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_NEXT
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_PREVIOUS
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_EXIT
				+ HtmlCreator.NEW_LINE + HELP_COMMAND_LIST_MINIMIZE;

		feedback = HELP_COMMAND_FEEDBACK;
		logExitMethod("HelpCommands");

	}

	/**
	 * Getter method to get the command feedback
	 * @return Returns the string for the feedback that is shown to user. 
	 */
	public String getCommandFeedback() {
		logEnterMethod("getCommandFeedback");
		logExitMethod("getCommandFeedback");
		return feedback;
	}

	/**
	 * Getter method to get the state.
	 * @return Returns the state that is shown to user. 
	 */
	public String getState() {
		logEnterMethod("getState");
		logExitMethod("getState");
		return screenState;
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
