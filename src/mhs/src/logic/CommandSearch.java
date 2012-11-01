package mhs.src.logic;

import java.io.IOException;
import java.util.List;

import mhs.src.storage.Task;

public class CommandSearch extends Command {

	/**
	 * Constructor
	 * @param inputCommand
	 */
	public CommandSearch(CommandInfo inputCommand) {
		List<Task> resultList;
		try {
			resultList = queryTask(inputCommand);
			matchedTasks = resultList;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public CommandSearch(List<Task> lastUsedList) {
		matchedTasks = lastUsedList;
	}

	@Override
	public String executeCommand() {
		String outputString = new String();
		if (matchedTasks.isEmpty()) {
			outputString = "No matching results found";
		}
		// if only 1 match is found then display it
		else if (matchedTasks.size() == 1) {
			outputString = "<u>Chosen Task</u>" + htmlCreator.NEW_LINE
					+ "Task Name: "
					+ htmlCreator.makeBold(matchedTasks.get(0).getTaskName())
					+ htmlCreator.NEW_LINE + "Time: "
					+ matchedTasks.get(0).getStartDateTime()
					+ " to " + matchedTasks.get(0).getEndDateTime();
			isUndoable = false;
		}
		// if multiple matches are found display the list
		else {
			outputString = displayListOfTasks(matchedTasks);
			indexExpected = true;
		}
		return outputString;
	}

	@Override
	public String undo() {
		// is never called
		return null;
	}

	@Override
	public String executeByIndex(int index) {
		String outputString = new String();
		if (indexExpected & index < matchedTasks.size()) {
			outputString = "<u>Chosen Task</u>"
					+ htmlCreator.NEW_LINE
					+ "Task Name: "
					+ htmlCreator.makeBold(matchedTasks.get(index)
							.getTaskName()) + htmlCreator.NEW_LINE + "Time: "
					+ matchedTasks.get(index).getStartDateTime()
					+ " to "
					+ matchedTasks.get(index).getEndDateTime();
			indexExpected = false;
			isUndoable = true;
		} else {
			outputString = "Invalid Command";
		}
		return outputString;
	}

	@Override
	public String executeByIndexAndType(int index) {
		String outputString = new String();
		if (index < matchedTasks.size()) {
			outputString = "<u>Chosen Task</u>"
					+ htmlCreator.NEW_LINE
					+ "Task Name: "
					+ htmlCreator.makeBold(matchedTasks.get(index)
							.getTaskName()) + htmlCreator.NEW_LINE + "Time: "
					+ matchedTasks.get(index).getStartDateTime()
					+ " to "
					+ matchedTasks.get(index).getEndDateTime();
			isUndoable = true;
		} else {
			outputString = "Invalid Command";
		}
		return outputString;
	}

}
