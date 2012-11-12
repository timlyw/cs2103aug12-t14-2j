//@author A0088669A

package mhs.src.logic.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.gdata.util.ServiceException;

import mhs.src.common.DateTimeHelper;
import mhs.src.common.HtmlCreator;
import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.DatabaseFactoryNotInstantiatedException;
import mhs.src.common.exceptions.InvalidTaskFormatException;
import mhs.src.common.exceptions.TaskNotFoundException;
import mhs.src.logic.CommandInfo;
import mhs.src.storage.Database;
import mhs.src.storage.DatabaseFactory;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;

/**
 * Parent class of all task based commands
 * 
 * @author Shekhar Baggavalli Raju
 * 
 */
public abstract class Command {

	private static final String MESSAGE_NO_TASKS_TO_DISPLAY = "No tasks to display";
	private static final String MESSAGE_SHOWING_HOME = "Showing home";
	private static final String MESSAGE_SHOWING_RESULTS = "Showing results ";
	protected static HtmlCreator htmlCreator = new HtmlCreator();
	private static final String MESSAGE_ERROR_IO = "Read/Write error";
	private static final String MESSAGE_DATABASE_FACTORY_NOT_INITIALIZED = "Database Factory not instantiated";
	private static final String MESSAGE_DATABASE_ILLEGAL_ARGUMENT = "Database given wrong arguments";
	protected static final String MESSAGE_UNDO_FAIL = "Undo Failed";
	protected static final String MESSAGE_UNDO_CONFIRM = "Undo Successful";
	protected static final String MESSAGE_REDO_FAIL = "Redo Failed";
	protected static final String MESSAGE_REDO_CONFIRM = "Redo Successful";
	protected static final String MESSAGE_CANNOT_UNDO = "Sorry Cannot Undo last command";
	protected static final String MESSAGE_NO_MATCH = htmlCreator.color(
			"No matching results found!", HtmlCreator.RED);
	protected static final String MESSAGE_INVALID_INDEX = "Invalid Index.";
	protected static final String MESSAGE_MULTIPLE_MATCHES = "Multiple matches found.";

	private static final int QUERY_BY_COMMANDINFO = 0;
	private static final int QUERY_BY_NAME = 1;
	private static final int QUERY_BY_CATEGORY = 2;
	private static final int QUERY_HOME = 3;

	private static final String CONNECTOR_TIMED = " from %1$s to %2$s";
	private static final String CONNECTOR_DEADLINE = " due %1$s";

	protected boolean isUndoable;
	public static List<Task> matchedTasks = new ArrayList<Task>();
	protected static Database dataHandler;
	protected boolean indexExpected;
	private static Stack<Integer> indexDisplayedStack = new Stack<Integer>();
	private static int firstIndexDisplayed = 0;
	private static int lastIndexDisplayed = 0;
	private static int lineLimit = 0;
	private static int lastQueryType = 0;
	private static CommandInfo lastQueryCommandInfo;
	private static TaskCategory lastQueryCategory;
	protected Task lastTask;
	protected Task newTask;
	protected String commandFeedback;
	protected static String currentState;
	protected DateTimeHelper dateTimeHelper;
	protected static int minTaskQuery = 1;

	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * Default Constructor for Commands
	 */
	public Command() {
		dateTimeHelper = new DateTimeHelper();
		indexExpected = false;
		isUndoable = false;
		htmlCreator = new HtmlCreator();
		lastTask = new Task();
		newTask = new Task();
		instantiateDatabase();
	}

	/**
	 * Creates an instance of database
	 */
	private void instantiateDatabase() {
		try {
			dataHandler = DatabaseFactory.getDatabaseInstance();
			commandFeedback = MESSAGE_DATABASE_ILLEGAL_ARGUMENT;
		} catch (DatabaseFactoryNotInstantiatedException e) {
			commandFeedback = MESSAGE_DATABASE_FACTORY_NOT_INITIALIZED;
		}
	}

	/**
	 * Abstract method, to execute given command
	 */
	abstract public void executeCommand();

	/**
	 * Abstract method to execute by index
	 * 
	 * @param index
	 */
	abstract public void executeByIndex(int index);

	/**
	 * Abstract method to execute By Index and Type
	 * 
	 * @param index
	 */
	abstract public void executeByIndexAndType(int index);

	/**
	 * adds previously deleted task
	 */
	public String undo() {
		logEnterMethod("undo");
		String outputString = new String();
		if (isUndoable()) {
			outputString = performUndo();
		} else {
			outputString = MESSAGE_CANNOT_UNDO;
		}
		commandFeedback = outputString;
		logExitMethod("undo");
		return outputString;
	}

	/**
	 * Executes undo
	 * 
	 * @return
	 */
	private String performUndo() {
		String outputString;
		try {
			if (lastTask == null) {
				dataHandler.delete(newTask.getTaskId());
			} else if (newTask == null) {
				lastTask = dataHandler.add(lastTask);
			} else {
				dataHandler.update(lastTask);
			}
			isUndoable = false;
			outputString = MESSAGE_UNDO_CONFIRM;
		} catch (Exception e) {
			outputString = MESSAGE_UNDO_FAIL;
		}
		return outputString;
	}

	/**
	 * Executes Redo
	 * 
	 * @return
	 */
	public String redo() {
		logEnterMethod("redo");
		String outputString = new String();
		try {
			executeRedo();
			outputString = MESSAGE_REDO_CONFIRM;
		} catch (Exception e) {
			outputString = MESSAGE_REDO_FAIL;
		}

		commandFeedback = outputString;
		logExitMethod("redo");
		return outputString;
	}

	/**
	 * Performs the redo
	 * 
	 * @throws InvalidTaskFormatException
	 * @throws IOException
	 * @throws TaskNotFoundException
	 * @throws ServiceException
	 */
	private void executeRedo() throws InvalidTaskFormatException, IOException,
			TaskNotFoundException, ServiceException {
		if (lastTask == null) {
			dataHandler.add(newTask);
		} else if (newTask == null) {
			dataHandler.delete(lastTask.getTaskId());
		} else {
			dataHandler.update(newTask);
		}
		isUndoable = true;
	}

	/**
	 * Checks if the commands is undoable
	 * 
	 * @return
	 */
	public boolean isUndoable() {
		return isUndoable;
	}

	/**
	 * Queries task in database
	 * 
	 * @param inputCommand
	 * @return List of matched tasks
	 * @throws IOException
	 */
	protected static List<Task> queryTask(CommandInfo inputCommand)
			throws IOException {
		if (inputCommand == null) {
			return null;
		}
		boolean name, startDate, endDate;
		lastQueryType = QUERY_BY_COMMANDINFO;
		List<Task> queryResultList;
		name = isTaskNameInitialized(inputCommand);
		startDate = isStartDateInitialized(inputCommand);
		endDate = isEndDateInitialized(inputCommand);
		queryResultList = queryByParams(inputCommand, name, startDate, endDate);
		if (queryResultList.size() > minTaskQuery) {
			lastQueryCommandInfo = inputCommand;
		}
		return queryResultList;
	}

	/**
	 * Queries task based on name & startDate & end date
	 * 
	 * @param inputCommand
	 * @param name
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws IOException
	 */
	private static List<Task> queryByParams(CommandInfo inputCommand,
			boolean name, boolean startDate, boolean endDate)
			throws IOException {
		List<Task> queryResultList;
		if (name && startDate && endDate) {
			queryResultList = dataHandler.query(inputCommand.getTaskName(),
					inputCommand.getStartDate(), inputCommand.getEndDate(),
					true);
		} else if (!name && startDate && endDate) {
			queryResultList = dataHandler.query(inputCommand.getStartDate(),
					inputCommand.getEndDate(), false, true);
			if (queryResultList == null) {
			} else {
			}
		} else if (name && !startDate && !endDate) {
			queryResultList = dataHandler.query(inputCommand.getTaskName(),
					true);
		} else {
			queryResultList = dataHandler.query(true);
		}
		return queryResultList;
	}

	/**
	 * Checks if CommandInfo contains end date
	 * 
	 * @param inputCommand
	 * @return
	 */
	private static boolean isEndDateInitialized(CommandInfo inputCommand) {
		boolean endDate;
		if (inputCommand.getEndDate() == null) {
			endDate = false;
		} else {
			endDate = true;
		}
		return endDate;
	}

	/**
	 * Checks if Command Info contains start date
	 * 
	 * @param inputCommand
	 * @return
	 */
	private static boolean isStartDateInitialized(CommandInfo inputCommand) {
		boolean startDate;
		if (inputCommand.getStartDate() == null) {
			startDate = false;
		} else {
			startDate = true;
		}
		return startDate;
	}

	/**
	 * Checks if command info contains name
	 * 
	 * @param inputCommand
	 * @return
	 */
	private static boolean isTaskNameInitialized(CommandInfo inputCommand) {
		boolean name;
		if (inputCommand == null) {
			return false;
		}
		if (inputCommand.getTaskName() == null) {
			name = false;
		} else {
			name = true;
		}
		return name;
	}

	/**
	 * Queries tasks exclusively by name
	 * 
	 * @param inputCommand
	 * @return matched Tasks
	 * @throws IOException
	 */
	protected static List<Task> queryTaskByName(CommandInfo inputCommand)
			throws IOException {
		boolean name;
		lastQueryType = QUERY_BY_NAME;
		List<Task> queryResultList;
		name = isTaskNameInitialized(inputCommand);
		if (name) {
			queryResultList = dataHandler.query(inputCommand.getTaskName(),
					true);
		} else {
			queryResultList = null;
		}
		if (queryResultList.size() > minTaskQuery) {
			lastQueryCommandInfo = inputCommand;
		}
		return queryResultList;
	}

	/**
	 * Query By Task Category
	 * 
	 * @param taskCategory
	 * @return
	 * @throws IOException
	 */
	protected static List<Task> queryTaskByCategory(TaskCategory taskCategory)
			throws IOException {
		List<Task> queryResultList;
		lastQueryType = QUERY_BY_CATEGORY;
		switch (taskCategory) {
		case FLOATING:
			queryResultList = dataHandler.query(TaskCategory.FLOATING, false);
			break;
		case DEADLINE:
			queryResultList = dataHandler.query(TaskCategory.DEADLINE, true);
			break;
		case TIMED:
			queryResultList = dataHandler.query(TaskCategory.TIMED, true);
			break;
		default:
			queryResultList = dataHandler.query(true);
		}
		if (queryResultList.size() > minTaskQuery) {
			lastQueryCategory = taskCategory;
		}
		return queryResultList;
	}

	/**
	 * Query Home
	 * 
	 * @return
	 * @throws IOException
	 */
	protected static List<Task> queryHome() throws IOException {
		List<Task> queryResultList;
		lastQueryType = QUERY_HOME;
		queryResultList = dataHandler.query(DateTime.now().toDateMidnight()
				.toDateTime(), DateTime.now().plusDays(2).toDateMidnight()
				.toDateTime(), true, true);
		return queryResultList;
	}

	/*
	 * Displays list of all kinds of tasks
	 */
	protected static String displayListOfTasks(List<Task> resultList) {
		String outputString = new String();
		outputString = createTaskListHtml(resultList, lineLimit - 2);
		return outputString;
	}

	/**
	 * Display next page
	 */
	public static void displayNext() {
		if (lastIndexDisplayed + 1 < matchedTasks.size()) {
			indexDisplayedStack.add(firstIndexDisplayed);
			firstIndexDisplayed = lastIndexDisplayed + 1;
		}
	}

	/**
	 * Display previous page
	 */
	public static void displayPrev() {
		if (indexDisplayedStack.empty()) {
			firstIndexDisplayed = 0;
			return;
		}
		firstIndexDisplayed = indexDisplayedStack.pop();
	}

	/**
	 * Reset Display Index i.e send to first page
	 */
	public static void resetDisplayIndex() {
		firstIndexDisplayed = 0;
		indexDisplayedStack.clear();
	}

	/**
	 * Convert List into Pretty HTML List
	 * 
	 * @param taskList
	 * @param limit
	 * @return
	 */
	public static String createTaskListHtml(List<Task> taskList, int limit) {
		if (taskList == null) {
			return MESSAGE_NO_TASKS_TO_DISPLAY;
		}
		if (taskList.size() == 0) {
			return MESSAGE_NO_TASKS_TO_DISPLAY;
		}
		limit = setBounds(limit, taskList);
		String taskListHtml = getTasksHtml(taskList, limit);
		String pagination = createPagination(taskList);
		taskListHtml += HtmlCreator.NEW_LINE;
		taskListHtml += pagination;
		return taskListHtml;
	}

	private static String getTasksHtml(List<Task> taskList, int limit) {
		String taskListHtml = "";
		int lineCount = 0;
		DateTime prevTaskDateTime = null;
		for (int i = firstIndexDisplayed; i < taskList.size()
				&& lineCount < limit; i++) {
			Task task = taskList.get(i);
			DateTime currTaskDateTime = getCurrTaskDateTime(task);
			taskListHtml += getFloatingHeader(task, currTaskDateTime, i);
			taskListHtml += getDateHeader(prevTaskDateTime, currTaskDateTime, i);

			prevTaskDateTime = currTaskDateTime;
			String indexString = createIndexString(i);
			taskListHtml += indexString + task.toHtmlString()
					+ HtmlCreator.NEW_LINE;
			lastIndexDisplayed = i;

			lineCount = HtmlCreator.countNewLine(taskListHtml);
		}
		return taskListHtml;
	}

	private static String createIndexString(int index) {
		String indexString = htmlCreator.color(Integer.toString(index + 1)
				+ ". ", HtmlCreator.GRAY);
		return indexString;
	}

	private static String createPagination(List<Task> taskList) {
		String pageInstruction = "";
		if (lastIndexDisplayed + 1 < taskList.size()) {
			pageInstruction = "n: next page";
		}
		if (firstIndexDisplayed > 0) {
			pageInstruction = "p: previous page";
		}
		if (lastIndexDisplayed + 1 < taskList.size() && firstIndexDisplayed > 0) {
			pageInstruction = "n: next page | p: previous page";
		}

		String pagination = "[Task "
				+ Integer.toString(firstIndexDisplayed + 1) + " - "
				+ Integer.toString(lastIndexDisplayed + 1) + " of "
				+ Integer.toString(taskList.size()) + "] " + pageInstruction;
		pagination = htmlCreator.color(pagination, HtmlCreator.GRAY);
		return pagination;
	}

	private static String getDateHeader(DateTime prevTaskDateTime,
			DateTime currTaskDateTime, int index) {
		String dateHeader = "";
		if (!dateIsEqual(prevTaskDateTime, currTaskDateTime)
				&& currTaskDateTime != null) {
			if (index > firstIndexDisplayed) {
				dateHeader += HtmlCreator.NEW_LINE;
			}
			String dateString = getDateString(currTaskDateTime);
			dateString = htmlCreator.color(dateString, HtmlCreator.BLUE);
			dateString = htmlCreator.largeFont(dateString);
			dateHeader += dateString + HtmlCreator.NEW_LINE;
		}

		return dateHeader;
	}

	private static DateTime getCurrTaskDateTime(Task task) {
		DateTime currTaskDateTime = null;
		if (task.isTimed()) {
			currTaskDateTime = task.getStartDateTime();
		} else if (task.isDeadline()) {
			currTaskDateTime = task.getEndDateTime();
		} else if (task.isFloating()) {
			currTaskDateTime = null;
		}

		return currTaskDateTime;
	}

	private static String getFloatingHeader(Task task,
			DateTime currTaskDateTime, int index) {
		String taskListHtml = "";
		if (task.isFloating() && index == firstIndexDisplayed) {
			String floatingHtml = htmlCreator.largeFont("TASKS");
			taskListHtml += htmlCreator.color(floatingHtml,
					HtmlCreator.LIGHT_BLUE) + HtmlCreator.NEW_LINE;
		}

		return taskListHtml;
	}

	private static int setBounds(int limit, List<Task> taskList) {
		if (limit < 0) {
			limit = 1;
		}

		if (firstIndexDisplayed > taskList.size()) {
			firstIndexDisplayed = taskList.size() - 1;
		}

		return limit;
	}

	private static boolean dateIsEqual(DateTime date1, DateTime date2) {
		if (date1 == null || date2 == null) {
			return false;
		}

		if (date1.getDayOfYear() == date2.getDayOfYear()
				&& date1.getYear() == date2.getYear()) {
			return true;
		}
		return false;
	}

	private static String getDateString(DateTime date) {
		if (dateIsEqual(date, DateTime.now())) {
			return "TODAY";
		}

		if (dateIsEqual(date, DateTime.now().plusDays(1))) {
			return "TOMORROW";
		}

		return date.toString("dd MMM yy");
	}

	/**
	 * Sets line limit of display
	 * 
	 * @param limit
	 */
	public static void setLineLimit(int limit) {
		lineLimit = limit;
		currentState = refreshLastState();
	}

	/**
	 * Returns the command feedback
	 * 
	 * @return
	 */
	public String getCommandFeedback() {
		return commandFeedback;
	}

	/**
	 * Returns the Screen State
	 * 
	 * @return
	 */
	public String getCurrentState() {
		String state = refreshLastState();
		return state;
	}

	/**
	 * Refresh Last list
	 * 
	 * @return
	 */
	public static String refreshLastState() {
		String lastStateString = new String();
		List<Task> resultList;
		try {
			switch (lastQueryType) {
			case QUERY_BY_COMMANDINFO:
				resultList = queryTask(lastQueryCommandInfo);
				lastStateString = populateStateForQueryByCommandInfo(lastStateString);
				break;
			case QUERY_BY_NAME:
				resultList = queryTask(lastQueryCommandInfo);
				lastStateString = populateStateForQueryByName();
				break;
			case QUERY_BY_CATEGORY:
				lastStateString = populateStateForQueryByCategory();
				resultList = queryTaskByCategory(lastQueryCategory);
				break;
			default:
				lastStateString = populateStateForHome();
				resultList = queryHome();
			}
			matchedTasks = resultList;
			lastStateString = populateFormattedState(lastStateString,
					resultList);
			return lastStateString;
		} catch (IOException e) {
			return MESSAGE_ERROR_IO;
		}
	}

	/**
	 * Formats and returns the final screen state
	 * 
	 * @param lastStateString
	 * @param resultList
	 * @return
	 */
	private static String populateFormattedState(String lastStateString,
			List<Task> resultList) {
		lastStateString = lastStateString.toUpperCase();
		lastStateString = htmlCreator.color(lastStateString, HtmlCreator.GRAY);
		lastStateString = htmlCreator.smallFont(lastStateString);
		lastStateString += displayListOfTasks(resultList);
		return lastStateString;
	}

	/**
	 * Populates the state for last query - home
	 * 
	 * @return
	 */
	private static String populateStateForHome() {
		String lastStateString;
		lastStateString = MESSAGE_SHOWING_HOME + HtmlCreator.NEW_LINE;
		return lastStateString;
	}

	/**
	 * Populates state for last query - by category
	 * 
	 * @return
	 */
	private static String populateStateForQueryByCategory() {
		String lastStateString;
		lastStateString = MESSAGE_SHOWING_RESULTS + lastQueryCategory
				+ HtmlCreator.NEW_LINE;
		return lastStateString;
	}

	/**
	 * Populates state for last query - by name
	 * 
	 * @return
	 */
	private static String populateStateForQueryByName() {
		String lastStateString;
		lastStateString = MESSAGE_SHOWING_RESULTS
				+ lastQueryCommandInfo.getTaskName() + HtmlCreator.NEW_LINE;
		return lastStateString;
	}

	/**
	 * Populates state for last query by Command Info params
	 * 
	 * @param lastStateString
	 * @return
	 */
	private static String populateStateForQueryByCommandInfo(
			String lastStateString) {
		boolean name = isTaskNameInitialized(lastQueryCommandInfo);
		boolean startDate = isStartDateInitialized(lastQueryCommandInfo);
		boolean endDate = isEndDateInitialized(lastQueryCommandInfo);
		lastStateString = getFormattedSearchParams(lastStateString, name,
				startDate, endDate);
		return lastStateString;
	}

	/**
	 * Formats the name/date/time for page heading
	 * 
	 * @param lastStateString
	 * @param name
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private static String getFormattedSearchParams(String lastStateString,
			boolean name, boolean startDate, boolean endDate) {
		DateTimeHelper helper = new DateTimeHelper();
		lastStateString = MESSAGE_SHOWING_RESULTS;
		if (name)
			lastStateString += lastQueryCommandInfo.getTaskName() + " ";
		if (startDate) {
			lastStateString += helper
					.formatDateTimeToString(lastQueryCommandInfo.getStartDate())
					+ " - ";
		}
		if (endDate) {
			lastStateString += helper
					.formatDateTimeToString(lastQueryCommandInfo.getEndDate());
		}
		lastStateString += HtmlCreator.NEW_LINE;
		return lastStateString;
	}

	/**
	 * Converts Task's date time to pretty string
	 * 
	 * @param task
	 * @return
	 */
	protected String getTimeString(Task task) {
		String timeString = "";
		if (task.isDeadline()) {
			String dueTime = dateTimeHelper.formatDateTimeToString(task
					.getStartDateTime());
			timeString = String.format(CONNECTOR_DEADLINE, dueTime);
		} else if (task.isTimed()) {
			String startTime = dateTimeHelper.formatDateTimeToString(task
					.getStartDateTime());
			String endTime = dateTimeHelper.formatDateTimeToString(task
					.getEndDateTime());
			timeString = String.format(CONNECTOR_TIMED, startTime, endTime);
		}
		return timeString;
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
