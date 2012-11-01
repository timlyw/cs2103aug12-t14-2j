package mhs.src.logic;

import java.io.IOException;
import java.util.List;

import mhs.src.storage.Task;

public class CommandRemove extends Command {

	private Task lastDeletedTask;

	/**
	 * Constructor for non index based commands
	 * 
	 * @param userCommand
	 */
	public CommandRemove(CommandInfo userCommand) {
		List<Task> resultList;
		try {
			resultList = queryTaskByName(userCommand);
			matchedTasks = resultList;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Constructor for index based commands
	 * 
	 * @param lastUsedList
	 */
	public CommandRemove(List<Task> lastUsedList) {
		matchedTasks = lastUsedList;
	}

	/**
	 * executes delete
	 */
	public String executeCommand() {
		String outputString = new String();

		if (matchedTasks.isEmpty()) {
			outputString = "No matching results found";
		}
		// if only 1 match is found then display it
		else if (matchedTasks.size() == 1) {
			try {
				lastDeletedTask = new Task();
				lastDeletedTask = matchedTasks.get(0);
				dataHandler.delete(matchedTasks.get(0).getTaskId());
				isUndoable = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			outputString = "Deleted Task - '"
					+ matchedTasks.get(0).getTaskName() + "'";
		}
		// if multiple matches are found display the list
		else {
			outputString = displayListOfTasks(matchedTasks);
			indexExpected = true;
		}
		return outputString;
	}

	/**
	 * adds previously deleted task
	 */
	public String undo() {
		if (isUndoable()) {
			try {
				dataHandler.add(lastDeletedTask);
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

	/**
	 * executes based on index only. Works when delete returned multiple matches
	 */
	public String executeByIndex(int index) {
		String outputString = new String();
		if (indexExpected & index < matchedTasks.size()) {
			try {
				System.out.println("entered");
				dataHandler.delete(matchedTasks.get(index).getTaskId());
				outputString = "Deleted "
						+ matchedTasks.get(index).getTaskName();
				lastDeletedTask = matchedTasks.get(index);
				indexExpected = false;
				isUndoable = true;
			} catch (Exception e) {

			}
		} else {
			outputString = "Invalid Command";
		}
		return outputString;
	}

	/**
	 * executes based on index and type of command. Works when there is a list
	 * present.
	 */
	public String executeByIndexAndType(int index) {
		String outputString = new String();
		if (index < matchedTasks.size()) {
			try {
				dataHandler.delete(matchedTasks.get(index).getTaskId());
				outputString = "Deleted "
						+ matchedTasks.get(index).getTaskName();
				lastDeletedTask = matchedTasks.get(index);
				isUndoable = true;
			} catch (Exception e) {

			}
		} else {
			outputString = "Invalid Command";
		}
		return outputString;
	}

}
