package mhs.src.logic;

import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import mhs.src.common.MhsLogger;
import mhs.src.common.HtmlCreator;
import mhs.src.common.exceptions.DatabaseFactoryNotInstantiatedException;
import mhs.src.storage.Database;
import mhs.src.storage.DatabaseFactory;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;

public abstract class Command {

	protected static final String MESSAGE_UNDO_FAIL = "Undo Failed";
	protected static final String MESSAGE_UNDO_CONFIRM = "Undo Successful";
	protected static final String MESSAGE_CANNOT_UNDO = "Sorry Cannot Undo last command";
	protected static final String MESSAGE_NO_MATCH = "No matching results found";
	protected static final String MESSAGE_INVALID_INDEX = "Invalid Index.";
	protected static final String MESSAGE_MULTIPLE_MATCHES = "Multiple matches found.";

	protected boolean isUndoable;
	protected static List<Task> matchedTasks;
	protected static Database dataHandler;
	protected boolean indexExpected;
	protected static HtmlCreator htmlCreator;
	private static final Logger logger = MhsLogger.getLogger();
	private static Stack<Integer> indexDisplayedStack = new Stack<Integer>();
	private static int firstIndexDisplayed = 0;
	private static int lastIndexDisplayed = 0;
	private static int lineLimit = 0;
	private static CommandInfo lastQueryCommandInfo;
	protected String commandFeedback;
	protected static String currentState;
	protected static int minTaskQuery = 1;

	public Command() {
		indexExpected = false;
		isUndoable = false;
		htmlCreator = new HtmlCreator();

		try {
			dataHandler = DatabaseFactory.getDatabaseInstance();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseFactoryNotInstantiatedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	abstract public void executeCommand();

	abstract public String undo();

	public boolean isUndoable() {
		return isUndoable;
	}

	abstract public void executeByIndex(int index);

	abstract public void executeByIndexAndType(int index);

	/**
	 * Queries task based on task name/start time/end time
	 * 
	 * @param inputCommand
	 * @return List of matched tasks
	 * @throws IOException
	 */
	protected static List<Task> queryTask(CommandInfo inputCommand)
			throws IOException {

		boolean name, startDate, endDate;
		List<Task> queryResultList;
		
		name = inputCommand.getTaskName() == null ? false : true;
		startDate = inputCommand.getStartDate() == null ? false : true;
		endDate = inputCommand.getEndDate() == null ? false : true;
		if (name && startDate && endDate) {
			queryResultList = dataHandler.query(inputCommand.getTaskName(),
					inputCommand.getStartDate(), inputCommand.getEndDate(),
					true);
		} else if (startDate && endDate && !name) {
			queryResultList = dataHandler.query(inputCommand.getStartDate(),
					inputCommand.getEndDate(), true);
		} else if (name && !startDate && !endDate) {
			queryResultList = dataHandler.query(inputCommand.getTaskName(),
					true);
		} else if (name && startDate && !endDate) {
			queryResultList = dataHandler.query(inputCommand.getTaskName(),
					inputCommand.getStartDate(), inputCommand.getStartDate()
							.plusDays(1).toDateMidnight().toDateTime(), true);
		} else if (!name && startDate && !endDate) {
			queryResultList = dataHandler.query(inputCommand.getStartDate(),
					inputCommand.getStartDate().plusDays(1).toDateMidnight()
							.toDateTime(), true);
		} else {
			queryResultList = dataHandler.query(true);
		}
		if (queryResultList.size() > minTaskQuery) {
			lastQueryCommandInfo = inputCommand;
		}
		return queryResultList;
	}

	/**
	 * Queries tasks exclusively by name
	 * 
	 * @param inputCommand
	 * @return matched Tasks
	 * @throws IOException
	 */
	protected List<Task> queryTaskByName(CommandInfo inputCommand)
			throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());
		boolean name;
		List<Task> queryResultList;
		name = inputCommand.getTaskName() == null ? false : true;
		if (name) {
			queryResultList = dataHandler.query(inputCommand.getTaskName(),
					true);
		} else {
			queryResultList = null;
		}
		if (queryResultList.size() > minTaskQuery) {
			lastQueryCommandInfo = inputCommand;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return queryResultList;
	}
	
	protected List<Task> queryTaskByCategory(CommandInfo inputCommand, TaskCategory taskCategory)
			throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());
		boolean name;
		List<Task> queryResultList;
		name = inputCommand.getTaskName() == null ? false : true;
		if (name) {
			queryResultList = dataHandler.query(inputCommand.getTaskName(),
					true);
		} else {
			queryResultList = null;
		}
		if (queryResultList.size() > minTaskQuery) {
			lastQueryCommandInfo = inputCommand;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return queryResultList;
	}

	/*
	 * 
	 * Displays list of all kinds of tasks
	 */
	protected static String displayListOfTasks(List<Task> resultList) {
		String outputString = new String();

		outputString = createTaskListHtml(resultList, lineLimit - 2);
		return outputString;
	}

	/**
	 * Displays tasks by category
	 * 
	 * @param resultList
	 * @param category
	 * @return
	 */
	protected String displayListOfTasksCategory(List<Task> resultList,
			TaskCategory category) {
		logger.entering(getClass().getName(), this.getClass().getName());
		int count = 1;
		String outputString = new String();
		for (Task selectedTask : resultList) {
			if (selectedTask.getTaskCategory() == category) {
				outputString += count
						+ ". "
						+ htmlCreator.makeBold(selectedTask.getTaskName() + "-"
								+ selectedTask.getTaskCategory() + "("
								+ selectedTask.isDone() + ")")
						+ HtmlCreator.NEW_LINE;
			}
			count++;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return outputString;
	}

	public static void displayNext() {
		if(lastIndexDisplayed + 1 < matchedTasks.size()) {
			indexDisplayedStack.add(firstIndexDisplayed);
			firstIndexDisplayed = lastIndexDisplayed + 1;
		}
	}

	public static void displayPrev() {
		if(indexDisplayedStack.empty()) {
			firstIndexDisplayed = 0;
			return;
		}
		firstIndexDisplayed = indexDisplayedStack.pop();
	}

	public static void resetDisplayIndex() {
		firstIndexDisplayed = 0;
		indexDisplayedStack.clear();
	}

	public static String createTaskListHtml(List<Task> taskList, int limit) {
		String taskListHtml = "";

		if (taskList.size() == 0) {
			return "No tasks to display";
		}

		if(firstIndexDisplayed > taskList.size()) {
			firstIndexDisplayed = taskList.size() - 2;
		}
		
		int lineCount = 0;
		DateTime prevTaskDateTime = null;

		for (int i = firstIndexDisplayed; i < taskList.size()
				&& lineCount < limit; i++) {
			Task task = taskList.get(i);
			DateTime currTaskDateTime = null;

			if (task.isTimed()) {
				currTaskDateTime = task.getStartDateTime();
			} else if (task.isDeadline()) {
				currTaskDateTime = task.getEndDateTime();
			} else if (task.isFloating()) {
				if (i == firstIndexDisplayed) {
					taskListHtml += htmlCreator.color("Floating Tasks:",
							HtmlCreator.LIGHT_BLUE) + HtmlCreator.NEW_LINE;
				}

				currTaskDateTime = null;
			} else {
				continue;
			}

			if (!dateIsEqual(prevTaskDateTime, currTaskDateTime)
					&& currTaskDateTime != null) {
				if (i > firstIndexDisplayed) {
					taskListHtml += HtmlCreator.NEW_LINE;
				}
				String dateString = getDateString(currTaskDateTime);
				dateString = htmlCreator.color(dateString, HtmlCreator.BLUE);
				taskListHtml += dateString + HtmlCreator.NEW_LINE;
			}

			prevTaskDateTime = currTaskDateTime;
			String indexString = htmlCreator.color(Integer.toString(i + 1)
					+ ". ", HtmlCreator.GRAY);
			taskListHtml += indexString + task.toHtmlString()
					+ HtmlCreator.NEW_LINE;
			lastIndexDisplayed = i;
			
			lineCount = HtmlCreator.countNewLine(taskListHtml);
			System.out.println(lineCount);
		}

		String pageInstruction = "";
		if(lastIndexDisplayed + 1 < taskList.size()) {
			pageInstruction = "n: next page";
		}
		if(firstIndexDisplayed > 0) {
			pageInstruction = "p: previous page";
		}
		if(lastIndexDisplayed + 1 < taskList.size() && firstIndexDisplayed > 0) {
			pageInstruction = "n: next page | p: previous page";
		}
			
		String pagination = "[Task " + Integer.toString(firstIndexDisplayed + 1) + " - " + Integer.toString(lastIndexDisplayed + 1) + " of " + Integer.toString(taskList.size()) + "] " + pageInstruction;
		pagination = htmlCreator.color(pagination, HtmlCreator.GRAY);
		taskListHtml += HtmlCreator.NEW_LINE;
		taskListHtml += pagination;

		return taskListHtml;
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
		return date.toString("dd MMM yy");
	}

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

	public String getCurrentState() {
		String state = refreshLastState();
		return state;
	}

	protected static String refreshLastState() {
		String lastStateString = new String();
		List<Task> resultList;
		try {
			resultList = queryTask(lastQueryCommandInfo);
			matchedTasks = resultList;
			System.out.println("matched task " + matchedTasks.size());

			lastStateString = displayListOfTasks(resultList);
			System.out.println(lastStateString);
			return lastStateString;
		} catch (IOException e) {
			return "Read/Write error";
		}
	}

}
