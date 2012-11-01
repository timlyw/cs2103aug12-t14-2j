package mhs.src.logic;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;
import mhs.src.storage.DeadlineTask;
import mhs.src.storage.FloatingTask;
import mhs.src.storage.Task;
import mhs.src.storage.TaskCategory;
import mhs.src.storage.TimedTask;

public class CommandEdit extends Command {

	private Task editedTask;
	private Task oldTask;
	private CommandInfo tempCommandInfo;
	private CommandInfo updatedInfo;

	public CommandEdit(CommandInfo userCommand) {

		List<Task> resultList;
		tempCommandInfo = new CommandInfo();
		try {
			resultList = queryTaskByName(userCommand);
			matchedTasks = resultList;
			tempCommandInfo = userCommand;
		} catch (IOException e) {

		}
	}

	public CommandEdit(List<Task> lastUsedList, CommandInfo changedInfo) {
		matchedTasks = lastUsedList;
		updatedInfo = changedInfo;
	}

	@Override
	public String executeCommand() {

		String outputString = new String();
		if (matchedTasks.isEmpty()) {
			outputString = "No matching results found";
		}
		// if only 1 match is found then display it
		else if (matchedTasks.size() == 1) {
			oldTask = new Task();
			oldTask = matchedTasks.get(0);
			// create task
			editedTask = new Task();
			editedTask = createEditedTask(tempCommandInfo, oldTask);
			isUndoable = true;
			try {
				dataHandler.update(editedTask);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			outputString = "Edited Task - '" + oldTask.getTaskName() + "'";
		}
		// if multiple matches are found display the list
		else {
			outputString = displayListOfTasks(matchedTasks);
			indexExpected = true;
		}
		return outputString;
	}

	private Task createEditedTask(CommandInfo inputCommand, Task taskToEdit) {
		int typeCount = 0;
		if (inputCommand.getStartDate() != null) {
			typeCount++;
		}
		if (inputCommand.getEndDate() != null) {
			typeCount++;
		}
		if (inputCommand.getEdittedName() != null) {
			switch (typeCount) {
			case 0:
				Task floatingTaskToAdd = new FloatingTask(
						taskToEdit.getTaskId(), inputCommand.getEdittedName(),
						TaskCategory.FLOATING, DateTime.now(), null, null,
						false, false);
				return floatingTaskToAdd;
			case 1:
				Task deadlineTaskToAdd = new DeadlineTask(
						taskToEdit.getTaskId(), inputCommand.getEdittedName(),
						TaskCategory.DEADLINE, inputCommand.getStartDate(),
						DateTime.now(), null, null, null, false, false);
				return deadlineTaskToAdd;
			case 2:
				Task timedTaskToAdd = new TimedTask(taskToEdit.getTaskId(),
						inputCommand.getEdittedName(), TaskCategory.TIMED,
						inputCommand.getStartDate(), inputCommand.getEndDate(),
						DateTime.now(), null, null, null, false, false);
				return timedTaskToAdd;
			default:
				Task nullTask = new Task();
				return nullTask;
			}
		} else {
			switch (typeCount) {
			case 0:
				Task floatingTaskToAdd = new FloatingTask(
						taskToEdit.getTaskId(), inputCommand.getTaskName(),
						TaskCategory.FLOATING, DateTime.now(), null, null,
						false, false);
				return floatingTaskToAdd;
			case 1:
				System.out.println("checkpoint");
				Task deadlineTaskToAdd = new DeadlineTask(
						taskToEdit.getTaskId(), inputCommand.getTaskName(),
						TaskCategory.DEADLINE, inputCommand.getStartDate(),
						DateTime.now(), null, null, null, false, false);
				return deadlineTaskToAdd;
			case 2:
				Task timedTaskToAdd = new TimedTask(taskToEdit.getTaskId(),
						inputCommand.getTaskName(), TaskCategory.TIMED,
						inputCommand.getStartDate(), inputCommand.getEndDate(),
						DateTime.now(), null, null, null, false, false);
				return timedTaskToAdd;
			default:
				Task nullTask = new Task();
				return nullTask;
			}

		}
	}

	private Task createEditedTaskByIndex(CommandInfo inputCommand,
			Task taskToEdit) {
		int typeCount = 0;
		if (inputCommand.getStartDate() != null) {
			typeCount++;
		}
		if (inputCommand.getEndDate() != null) {
			typeCount++;
		}
		if (inputCommand.getEdittedName() != null) {
			switch (typeCount) {
			case 0:
				Task floatingTaskToAdd = new FloatingTask(
						taskToEdit.getTaskId(), inputCommand.getEdittedName(),
						TaskCategory.FLOATING, DateTime.now(), null, null,
						false, false);
				return floatingTaskToAdd;
			case 1:
				Task deadlineTaskToAdd = new DeadlineTask(
						taskToEdit.getTaskId(), inputCommand.getEdittedName(),
						TaskCategory.DEADLINE, inputCommand.getStartDate(),
						DateTime.now(), null, null, null, false, false);
				return deadlineTaskToAdd;
			case 2:
				Task timedTaskToAdd = new TimedTask(taskToEdit.getTaskId(),
						inputCommand.getEdittedName(), TaskCategory.TIMED,
						inputCommand.getStartDate(), inputCommand.getEndDate(),
						DateTime.now(), null, null, null, false, false);
				return timedTaskToAdd;
			default:
				Task nullTask = new Task();
				return nullTask;
			}
		} else {
			switch (typeCount) {
			case 0:
				Task floatingTaskToAdd = new FloatingTask(
						taskToEdit.getTaskId(), taskToEdit.getTaskName(),
						TaskCategory.FLOATING, DateTime.now(), null, null,
						false, false);
				return floatingTaskToAdd;
			case 1:
				System.out.println("checkpoint");
				Task deadlineTaskToAdd = new DeadlineTask(
						taskToEdit.getTaskId(), taskToEdit.getTaskName(),
						TaskCategory.DEADLINE, inputCommand.getStartDate(),
						DateTime.now(), null, null, null, false, false);
				return deadlineTaskToAdd;
			case 2:
				Task timedTaskToAdd = new TimedTask(taskToEdit.getTaskId(),
						taskToEdit.getTaskName(), TaskCategory.TIMED,
						inputCommand.getStartDate(), inputCommand.getEndDate(),
						DateTime.now(), null, null, null, false, false);
				return timedTaskToAdd;
			default:
				Task nullTask = new Task();
				return nullTask;
			}

		}
	}

	@Override
	public String undo() {
		if (isUndoable()) {
			try {
				dataHandler.update(oldTask);
				isUndoable = false;
				return MESSAGE_UNDO_CONFIRM;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return MESSAGE_UNDO_FAIL;
			}
		} else {
			return MESSAGE_UNDO_FAIL;
		}
	}

	@Override
	public String executeByIndex(int index) {
		String outputString = new String();
		if (indexExpected && index < matchedTasks.size()) {
			try {
				Task givenTask = matchedTasks.get(index);
				oldTask = givenTask;
				Task newTask = new Task();
				newTask = createEditedTask(tempCommandInfo, oldTask);
				isUndoable = true;
				try {
					dataHandler.update(newTask);
					outputString = "Edited Task - '" + oldTask.getTaskName()
							+ "'";
					indexExpected = false;
				} catch (Exception e) {
					outputString = "Edit/Update failed!";
				}

			} catch (Exception e) {

			}
		} else {
			outputString = "Invalid Command";
		}
		return outputString;
	}

	@Override
	public String executeByIndexAndType(int index) {
		String outputString = new String();
		if (index < matchedTasks.size()) {
			try {
				Task givenTask = matchedTasks.get(index);
				Task newTask = new Task();
				newTask = createEditedTaskByIndex(updatedInfo, givenTask);
				isUndoable = true;
				try {
					dataHandler.update(newTask);
					oldTask = givenTask;
				} catch (Exception e) {
					outputString = "Update Failed!";
				}
				outputString = "Edited Task - '" + oldTask.getTaskName() + "'";
			} catch (Exception e) {
				outputString = "Excpetional Situation.";
			}
		} else {
			outputString = "Invalid Command";
		}
		return outputString;
	}

}
