package mhs.src.logic;

import java.io.IOException;
import java.util.List;

import mhs.src.storage.Task;
import mhs.src.storage.TaskCategory;

public class CommandMark extends Command {

	Task lastTask;

	public CommandMark(CommandInfo inputCommand) {
		List<Task> resultList;
		try {
			resultList = queryTaskByName(inputCommand);
			matchedTasks = resultList;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public CommandMark(List<Task> lastUsedList) {
		matchedTasks = lastUsedList;
	}

	@Override
	public String executeCommand() {
		String outputString = new String();
		lastTask = new Task();
		if (matchedTasks.isEmpty()) {
			outputString = "No matching results found";
		}
		// if only 1 match is found then display it
		else if (matchedTasks.size() == 1) {
			// create task
			Task editedTask = matchedTasks.get(0);
			editedTask.setDone(true);
			try {
				lastTask = editedTask;
				dataHandler.update(editedTask);
				isUndoable = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isUndoable = true;
			outputString = "Marked Task as done - '"
					+ matchedTasks.get(0).getTaskName() + "'" + "-Done? "
					+ matchedTasks.get(0).isDone();
		}
		// if multiple matches are found display the list
		else {
			outputString = displayListOfTasksCategory(matchedTasks,
					TaskCategory.FLOATING);
		}
		return outputString;
	}

	@Override
	public String undo() {
		if (isUndoable()) {
			lastTask.setDone(false);
			try {
				dataHandler.update(lastTask);
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
		if (indexExpected & index <= matchedTasks.size()) {
			Task tempTask = matchedTasks.get(index);
			tempTask.setDone(true);
			lastTask = tempTask;
			try {
				dataHandler.update(tempTask);
				outputString = "Marked Task as done - '"
						+ matchedTasks.get(index).getTaskName() + "'"
						+ "-Done? " + matchedTasks.get(index).isDone();
				indexExpected = false;
				isUndoable = true;
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
			Task tempTask = matchedTasks.get(index);
			tempTask.setDone(true);
			lastTask = tempTask;
			try {
				dataHandler.update(tempTask);
				outputString = "Marked Task as done - '"
						+ matchedTasks.get(index).getTaskName() + "'"
						+ "-Done? " + matchedTasks.get(index).isDone();
				isUndoable = true;
			} catch (Exception e) {

			}
		} else {
			outputString = "Invalid Command";
		}
		return outputString;
	}

}
