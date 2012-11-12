//@author A0088669A

package mhs.src.logic.command;

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

import org.joda.time.DateTime;

import com.google.gdata.util.ServiceException;

/**
 * Executes the Rename command
 * 
 * @author A0088669A
 * 
 */
public class CommandRename extends Command {

	private static final String MESSAGE_NO_EDITED_NAME = "No new name specified.";
	private static final String MESSAGE_TASK_NOT_RENAMED = "Error occured. Task not Re-named.";
	private static final String CONFIRM_TASK_RENAMED = "I have renamed '%1$s'<br/>to '%2$s'";

	private CommandInfo tempCommandInfo;

	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * Constructor for non index commands
	 * 
	 * @param userCommand
	 */
	public CommandRename(CommandInfo userCommand) {
		logEnterMethod("CommandRename");
		List<Task> resultList;
		lastTask = new Task();
		newTask = new Task();
		tempCommandInfo = new CommandInfo();
		try {
			resultList = queryTaskByName(userCommand);
			matchedTasks = resultList;
			assert (matchedTasks != null);
			tempCommandInfo = userCommand;
		} catch (IOException e) {
			matchedTasks = null;
		}
		logExitMethod("CommandRename");
	}

	/**
	 * Constructor for index based commands
	 * 
	 * @param lastUsedList
	 * @param changedInfo
	 */
	public CommandRename(List<Task> lastUsedList, CommandInfo changedInfo) {
		logEnterMethod("CommandRename-index");
		matchedTasks = lastUsedList;
		assert (matchedTasks != null);
		tempCommandInfo = changedInfo;
		logExitMethod("CommandRename-index");
	}

	/**
	 * executes the rename command
	 */
	public void executeCommand() {
		logEnterMethod("executeCommand");
		String outputString = new String();
		assert (matchedTasks != null);
		if (tempCommandInfo.getEdittedName() == null) {
			commandFeedback = MESSAGE_NO_EDITED_NAME;
		}
		assert (tempCommandInfo.getEdittedName() != null);
		if (matchedTasks.isEmpty()) {
			outputString = MESSAGE_NO_MATCH;
			commandFeedback = outputString;
		}
		// if only 1 match is found then display it
		else if (matchedTasks.size() == 1) {
			storeLastTask(matchedTasks.get(0));
			newTask = createRenamedTask(tempCommandInfo, matchedTasks.get(0));
			System.out.println(lastTask.getTaskName() + "/"
					+ newTask.getTaskName());
			try {
				updateTask(newTask);
				outputString = String.format(CONFIRM_TASK_RENAMED,
						lastTask.getTaskName(), newTask.getTaskName());
			} catch (Exception e) {
				outputString = MESSAGE_TASK_NOT_RENAMED;
			}
			commandFeedback = outputString;
		}
		// if multiple matches are found display the list
		else {
			indexExpected = true;
			commandFeedback = MESSAGE_MULTIPLE_MATCHES;
		}
		logExitMethod("excuteCommand");
	}

	/**
	 * update task in database
	 * 
	 * @param taskToUpdate
	 * @throws IOException
	 * @throws ServiceException
	 * @throws TaskNotFoundException
	 * @throws InvalidTaskFormatException
	 */
	private void updateTask(Task taskToUpdate) throws IOException,
			ServiceException, TaskNotFoundException, InvalidTaskFormatException {
		logEnterMethod("updateTask");
		assert (taskToUpdate != null);
		dataHandler.update(taskToUpdate);
		newTask = taskToUpdate;
		isUndoable = true;
		logExitMethod("updateTask");
	}

	/**
	 * Stores the last task for undo
	 * 
	 * @param taskToStore
	 */
	private void storeLastTask(Task taskToStore) {
		logEnterMethod("storeLastTask");
		assert (taskToStore != null);
		lastTask = taskToStore;
		logExitMethod("storeLastTask");
	}

	/**
	 * Returns a renamed task
	 * 
	 * @param inputCommand
	 * @param taskToEdit
	 * @return
	 */
	private Task createRenamedTask(CommandInfo inputCommand, Task taskToEdit) {
		logEnterMethod("createRenamedTask");
		assert (inputCommand.getEdittedName() != null);
		Task newEditedTask = new Task();
		if (inputCommand.getEdittedName() != null) {
			switch (taskToEdit.getTaskCategory()) {
			case FLOATING:
				newEditedTask = createFloatingTaskWithNewName(inputCommand,
						taskToEdit);
				break;
			case DEADLINE:
				newEditedTask = createDeadlineTaskWithNewName(inputCommand,
						taskToEdit);
				break;
			case TIMED:
				newEditedTask = createTimedTaskWithNewName(inputCommand,
						taskToEdit);
				break;
			}
		}
		logExitMethod("createRenamedTask");
		return newEditedTask;
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
				taskToEdit.getStartDateTime(), taskToEdit.getEndDateTime(),
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
				taskToEdit.getStartDateTime(), DateTime.now(), null, null,
				null, null, false, false);
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
	 * Execute rename by index when last rename query returned multiple matches.
	 */
	public void executeByIndex(int index) {
		logEnterMethod("executeByIndex");
		String outputString = new String();
		if (indexExpected && index < matchedTasks.size() && index >= 0) {
			outputString = renameByIndex(index);
		} else {
			outputString = MESSAGE_INVALID_INDEX;
		}
		commandFeedback = outputString;
		logExitMethod("executeByIndex");
	}

	private String renameByIndex(int index) {
		String outputString;
		Task givenTask = matchedTasks.get(index);
		assert (givenTask != null);
		storeLastTask(givenTask);
		Task newTask = createRenamedTask(tempCommandInfo, lastTask);
		try {
			updateTask(newTask);
			outputString = String.format(CONFIRM_TASK_RENAMED,
					givenTask.getTaskName(), newTask.getTaskName());
			indexExpected = false;
		} catch (Exception e) {
			outputString = MESSAGE_TASK_NOT_RENAMED;
		}
		return outputString;
	}

	@Override
	public void executeByIndexAndType(int index) {
		logEnterMethod("executeByIndexAndType");
		String outputString = new String();
		if (index < matchedTasks.size() && index >= 0) {
			outputString = renameByIndex(index);
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
