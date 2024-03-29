//@author A0088669A

package mhs.src.logic.command;

import org.joda.time.DateTime;

import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.InvalidTaskFormatException;
import mhs.src.common.exceptions.TaskNotFoundException;
import mhs.src.logic.CommandInfo;
import mhs.src.storage.persistence.task.DeadlineTask;
import mhs.src.storage.persistence.task.FloatingTask;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;
import mhs.src.storage.persistence.task.TimedTask;

/**
 * Executed edit command
 * 
 * @author A0088669A
 * 
 */
public class CommandEdit extends Command {

	private static final String MESSAGE_TASK_NOT_EDITED = "Error occured. Task not Edited.";
	private static final String CONFIRM_TASK_EDITED = "I have edited: %1$s<br/>to: %2$s %3$s";
	protected static final String MESSAGE_MULTIPLE_MATCHES_EDIT = "Multiple matches found.<br/>Enter task index to edit.";

	private static final int FLOATING = 0;
	private static final int DEADLINE = 1;
	private static final int TIMED = 2;

	private CommandInfo tempCommandInfo;

	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * Constructor for non index based commands
	 * 
	 * @param userCommand
	 */
	public CommandEdit(CommandInfo userCommand) {
		logEnterMethod("CommandEdit");
		List<Task> resultList;
		tempCommandInfo = new CommandInfo();
		newTask = new Task();
		try {
			resultList = queryTaskByName(userCommand);
			matchedTasks = resultList;
			assert (matchedTasks != null);
			tempCommandInfo = userCommand;
		} catch (IOException e) {
			matchedTasks = null;
		}
		logExitMethod("CommandEdit");
	}

	/**
	 * Constructor for index based commands
	 * 
	 * @param lastUsedList
	 * @param changedInfo
	 */
	public CommandEdit(List<Task> lastUsedList, CommandInfo changedInfo) {
		logEnterMethod("CommandEdit-index");
		matchedTasks = lastUsedList;
		tempCommandInfo = changedInfo;
		assert (matchedTasks != null);
		logExitMethod("CommandEdit-index");
	}

	/**
	 * executes edit
	 */
	public void executeCommand() {
		logEnterMethod("executeCommand");
		String outputString = new String();
		if (matchedTasks.isEmpty()) {
			outputString = MESSAGE_NO_MATCH;
			commandFeedback = outputString;
		}
		// if only 1 match is found then edit it
		else if (matchedTasks.size() == 1) {
			storeLastTask(matchedTasks.get(0));
			newTask = createEditedTask(tempCommandInfo, lastTask);
			outputString = updateEditedTask();
			commandFeedback = outputString;
		}
		// if multiple matches are found display the list
		else {
			indexExpected = true;
			commandFeedback = MESSAGE_MULTIPLE_MATCHES_EDIT;
		}
		logExitMethod("executeCommand");
	}

	/**
	 * Updates the edited task in the database
	 * 
	 * @return
	 */
	private String updateEditedTask() {
		String outputString;
		try {
			editTask(newTask);
			String timeString = this.getTimeString(newTask);
			outputString = String.format(CONFIRM_TASK_EDITED,
					lastTask.getTaskName(), newTask.getTaskName(), timeString);
		} catch (Exception e) {
			outputString = MESSAGE_TASK_NOT_EDITED;
		}
		return outputString;
	}

	/**
	 * Edits task in database
	 * 
	 * @param newTask
	 * @throws IOException
	 * @throws ServiceException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	private void editTask(Task newTask) throws IOException, ServiceException,
			TaskNotFoundException, InvalidTaskFormatException {
		logEnterMethod("editTask");
		dataHandler.update(newTask);
		isUndoable = true;
		logExitMethod("editTask");
	}

	/**
	 * stores old task before edit
	 * 
	 * @param taskToStore
	 */
	private void storeLastTask(Task taskToStore) {
		logEnterMethod("storeLastTask");
		lastTask = taskToStore;
		logExitMethod("storeLastTask");
	}

	/**
	 * Creates an edited task based on given params
	 * 
	 * @param inputCommand
	 * @param taskToEdit
	 * @return
	 */
	private Task createEditedTask(CommandInfo inputCommand, Task taskToEdit) {
		logEnterMethod("createEditedTask");
		int taskType = decideEditedTaskType(inputCommand);
		assert (taskType >= 0 && taskType < 3);
		Task newEditedTask = new Task();
		if (inputCommand.getEdittedName() != null) {
			newEditedTask = createTaskWithNewName(inputCommand, taskToEdit,
					taskType, newEditedTask);
		} else {
			newEditedTask = createTaskWithSameName(inputCommand, taskToEdit,
					taskType, newEditedTask);
		}
		assert (newEditedTask != null);
		logExitMethod("createEditedTask");
		return newEditedTask;
	}

	/**
	 * Creates a new task depending on given params, with new name
	 * 
	 * @param inputCommand
	 * @param taskToEdit
	 * @param taskType
	 * @param newEditedTask
	 * @return
	 */
	private Task createTaskWithSameName(CommandInfo inputCommand,
			Task taskToEdit, int taskType, Task newEditedTask) {
		switch (taskType) {
		case FLOATING:
			newEditedTask = createFloatingTaskWithSameName(inputCommand,
					taskToEdit);
			break;
		case DEADLINE:
			newEditedTask = createDeadlineTaskWithSameName(inputCommand,
					taskToEdit);
			break;
		case TIMED:
			newEditedTask = createTimedTaskWithSameName(inputCommand,
					taskToEdit);
			break;
		}
		return newEditedTask;
	}

	/**
	 * Creates a task with new params , keeping the same name
	 * 
	 * @param inputCommand
	 * @param taskToEdit
	 * @param taskType
	 * @param newEditedTask
	 * @return
	 */
	private Task createTaskWithNewName(CommandInfo inputCommand,
			Task taskToEdit, int taskType, Task newEditedTask) {
		switch (taskType) {
		case FLOATING:
			newEditedTask = createFloatingTaskWithNewName(inputCommand,
					taskToEdit);
			break;
		case DEADLINE:
			newEditedTask = createDeadlineTaskWithNewName(inputCommand,
					taskToEdit);
			break;
		case TIMED:
			newEditedTask = createTimedTaskWithNewName(inputCommand, taskToEdit);
			break;
		}
		return newEditedTask;
	}

	/**
	 * Creates a timed task with old name but different date/time
	 * 
	 * @param inputCommand
	 * @param taskToEdit
	 * @return
	 */
	private Task createTimedTaskWithSameName(CommandInfo inputCommand,
			Task taskToEdit) {
		logEnterMethod("createTimedTaskWithSameName");
		Task timedTask = new TimedTask(taskToEdit.getTaskId(),
				taskToEdit.getTaskName(), TaskCategory.TIMED,
				inputCommand.getStartDate(), inputCommand.getEndDate(),
				DateTime.now(), null, null, null, null, false, false);
		assert (timedTask != null);
		logExitMethod("createTimedTaskWithSameName");
		return timedTask;
	}

	/**
	 * Creates a deadline task with old name but different date/time
	 * 
	 * @param inputCommand
	 * @param taskToEdit
	 * @return
	 */
	private Task createDeadlineTaskWithSameName(CommandInfo inputCommand,
			Task taskToEdit) {
		logEnterMethod("createDeadlineTaskWithSameName");
		Task deadlineTask = new DeadlineTask(taskToEdit.getTaskId(),
				taskToEdit.getTaskName(), TaskCategory.DEADLINE,
				inputCommand.getStartDate(), DateTime.now(), null, null, null,
				null, false, false);
		assert (deadlineTask != null);
		logExitMethod("createDeadlineTaskWithSameName");
		return deadlineTask;
	}

	/**
	 * Creates a floating task with old name but different date/time
	 * 
	 * @param inputCommand
	 * @param taskToEdit
	 * @return
	 */
	private Task createFloatingTaskWithSameName(CommandInfo inputCommand,
			Task taskToEdit) {
		logEnterMethod("createFloatingTaskWithSameName");
		Task floatingTask = new FloatingTask(taskToEdit.getTaskId(),
				taskToEdit.getTaskName(), TaskCategory.FLOATING,
				DateTime.now(), null, null, null, false, false);
		assert (floatingTask != null);
		logExitMethod("createFloatingTaskWithSameName");
		return floatingTask;
	}

	/**
	 * Creates a Timed task with new name and different date/time
	 * 
	 * @param inputCommand
	 * @param taskToEdit
	 * @return
	 */
	private Task createTimedTaskWithNewName(CommandInfo inputCommand,
			Task taskToEdit) {
		logEnterMethod("createTimedTaskWithNewName");
		Task timedTaskToAdd = new TimedTask(taskToEdit.getTaskId(),
				inputCommand.getEdittedName(), TaskCategory.TIMED,
				inputCommand.getStartDate(), inputCommand.getEndDate(),
				DateTime.now(), null, null, null, null, false, false);
		assert (timedTaskToAdd != null);
		logExitMethod("createTimedTaskWithNewName");
		return timedTaskToAdd;
	}

	/**
	 * Creates a deadline task with new name and different date/time
	 * 
	 * @param inputCommand
	 * @param taskToEdit
	 * @return
	 */
	private Task createDeadlineTaskWithNewName(CommandInfo inputCommand,
			Task taskToEdit) {
		logEnterMethod("createDeadlineTaskWithNewName");
		Task deadlineTaskToAdd = new DeadlineTask(taskToEdit.getTaskId(),
				inputCommand.getEdittedName(), TaskCategory.DEADLINE,
				inputCommand.getStartDate(), DateTime.now(), null, null, null,
				null, false, false);
		assert (deadlineTaskToAdd != null);
		logExitMethod("createDeadlineTaskWithNewName");
		return deadlineTaskToAdd;
	}

	/**
	 * Creates a floating task with new name and different date/time
	 * 
	 * @param inputCommand
	 * @param taskToEdit
	 * @return
	 */
	private Task createFloatingTaskWithNewName(CommandInfo inputCommand,
			Task taskToEdit) {
		logEnterMethod("createFloatingTaskWithNewName");
		Task floatingTaskToAdd = new FloatingTask(taskToEdit.getTaskId(),
				inputCommand.getEdittedName(), TaskCategory.FLOATING,
				DateTime.now(), null, null, null, false, false);
		assert (floatingTaskToAdd != null);
		logExitMethod("createFloatingTaskWithNewName");
		return floatingTaskToAdd;
	}

	/**
	 * Decides the type of task based on given date/time params
	 * 
	 * @param inputCommand
	 * @return
	 */
	private int decideEditedTaskType(CommandInfo inputCommand) {
		logEnterMethod("decideEditedTaskType");
		int typeCount = 0;
		if (inputCommand.getStartDate() != null) {
			typeCount++;
		}
		if (inputCommand.getEndDate() != null) {
			typeCount++;
		}
		assert (typeCount >= 0 && typeCount < 3);
		logExitMethod("decideEditedTaskType");
		return typeCount;
	}

	/**
	 * executes based on index only. Works when edit query returns multiple
	 * matches.
	 */
	public void executeByIndex(int index) {
		logEnterMethod("executeByIndex");
		String outputString = new String();
		if (indexExpected && index < matchedTasks.size() && index >= 0) {
			outputString = editByIndex(index);
		} else {
			outputString = MESSAGE_INVALID_INDEX;
		}
		commandFeedback = outputString;
		logExitMethod("executeByIndex");
	}

	private String editByIndex(int index) {
		String outputString;
		Task givenTask = matchedTasks.get(index);
		assert (givenTask != null);
		storeLastTask(givenTask);
		Task newTask = createEditedTask(tempCommandInfo, lastTask);
		try {
			editTask(newTask);
			String timeString = this.getTimeString(newTask);
			outputString = String.format(CONFIRM_TASK_EDITED,
					givenTask.getTaskName(), newTask.getTaskName(), timeString);
			indexExpected = false;
		} catch (Exception e) {
			outputString = MESSAGE_TASK_NOT_EDITED;
		}
		return outputString;
	}

	/**
	 * execute task by index and type of command
	 */
	public void executeByIndexAndType(int index) {
		logEnterMethod("executeByIndexAndType");
		String outputString = new String();
		if (index < matchedTasks.size() && index >= 0) {
			outputString = editByIndex(index);
		} else {
			outputString = MESSAGE_INVALID_INDEX;
		}
		commandFeedback = outputString;
		logExitMethod("executeByIndexAndType");
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
