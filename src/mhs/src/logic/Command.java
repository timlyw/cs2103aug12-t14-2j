package mhs.src.logic;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import mhs.src.common.MhsLogger;
import com.google.gdata.util.ServiceException;
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
	protected boolean isUndoable;
	protected List<Task> matchedTasks;
	protected Database dataHandler;
	protected boolean indexExpected;
	protected HtmlCreator htmlCreator;
	private static final Logger logger = MhsLogger.getLogger();
	private Task lastTask = null;

	public Command() {
		indexExpected = false;
		isUndoable = false;
		htmlCreator = new HtmlCreator();

		try {
			dataHandler = DatabaseFactory.getDatabaseInstance();
		} catch (IOException | ServiceException e1) {

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseFactoryNotInstantiatedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	abstract public String executeCommand();

	abstract public String undo();

	public boolean isUndoable() {
		return isUndoable;
	}

	abstract public String executeByIndex(int index);

	abstract public String executeByIndexAndType(int index);

	/**
	 * Queries task based on task name/start time/end time
	 * 
	 * @param inputCommand
	 * @return List of matched tasks
	 * @throws IOException
	 */
	protected List<Task> queryTask(CommandInfo inputCommand) throws IOException {
		logger.entering(getClass().getName(), this.getClass().getName());
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
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.exiting(getClass().getName(), this.getClass().getName());
		return queryResultList;
	}

	/*
	 * 
	 * Displays list of all kinds of tasks
	 */
	protected String displayListOfTasks(List<Task> resultList) {
		logger.entering(getClass().getName(), this.getClass().getName());
		int count = 1;
		String outputString = new String();

		for (Task selectedTask : resultList) {
			if (selectedTask.getTaskCategory() == TaskCategory.FLOATING) {
				if (selectedTask.isDone()) {
					outputString += count
							+ ". "
							+ htmlCreator.makeBold(selectedTask.getTaskName()
									+ "-" + selectedTask.getTaskCategory()
									+ " [DONE]") + htmlCreator.NEW_LINE;
				} else {
					outputString += count
							+ ". "
							+ htmlCreator.makeBold(selectedTask.getTaskName()
									+ "-" + selectedTask.getTaskCategory()
									+ " [PENDING]") + htmlCreator.NEW_LINE;
				}
			} else {
				outputString += count + ". " + selectedTask.getTaskName() + "-"
						+ selectedTask.getTaskCategory() + htmlCreator.NEW_LINE;
			}
			count++;
		}

		// outputString =
		// htmlCreator.createTaskListHtml(resultList,resultList.size());
		logger.exiting(getClass().getName(), this.getClass().getName());
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
						+ htmlCreator.NEW_LINE;
			}
			count++;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return outputString;
	}
	
	public Task getLastTaskDisplayed() {
		return lastTask;
	}
	
	public String createTaskListHtml(List<Task> taskList, int limit) {
		String taskListHtml = "";
		
		DateTime prevTaskDateTime = null;
		
		int lineCount = 0;
		
		for(int i = 0; i < taskList.size() && lineCount < limit; i++) {
			Task task = taskList.get(i);
			DateTime currTaskDateTime = null;
			
			if(isTimed(task)) {
				currTaskDateTime = task.getStartDateTime();
			} else if(isDeadline(task)) {
				currTaskDateTime = task.getEndDateTime();
			} else if(isFloating(task)) {
				if(i == 0) {
					taskListHtml += htmlCreator.color("Floating Tasks:", HtmlCreator.LIGHT_BLUE) + htmlCreator.NEW_LINE;
					lineCount += 2;
				}
				
				currTaskDateTime = null;
			} else {
				continue;
			}
			
			if(!dateIsEqual(prevTaskDateTime, currTaskDateTime) && currTaskDateTime != null) {
				if(i > 0) {
					taskListHtml += htmlCreator.NEW_LINE;
					lineCount++;
				}
				String dateString = getDateString(currTaskDateTime);
				dateString = htmlCreator.color(dateString, HtmlCreator.BLUE);
				taskListHtml +=  dateString + htmlCreator.NEW_LINE;
			}
			
			prevTaskDateTime = currTaskDateTime;
			String indexString = htmlCreator.color(Integer.toString(i + 1) + ". ", HtmlCreator.GRAY);
			taskListHtml += indexString + task.toHtmlString() + htmlCreator.NEW_LINE;
			lineCount += 2;
			lastTask = task;
		}
		
		return taskListHtml;
	}
	
	public boolean isTimed(Task task) {
		if(task.getTaskCategory() == TaskCategory.TIMED) {
			return true;
		} 
		return false;
	}
	
	public boolean isDeadline(Task task) {
		if(task.getTaskCategory() == TaskCategory.DEADLINE) {
			return true;
		} 
		return false;
	}
	
	public boolean isFloating(Task task) {
		if(task.getTaskCategory() == TaskCategory.FLOATING) {
			return true;
		} 
		return false;
	}
	
	private boolean dateIsEqual(DateTime date1, DateTime date2) {
		if(date1 == null || date2 == null) {
			return false;
		}
		
		if(date1.getDayOfYear() == date2.getDayOfYear() && date1.getYear() == date2.getYear()) {
			return true;
		}
		return false;
	}
	
	private String getDateString(DateTime date) {
		return date.toString("dd MMM yy");
	}

}
