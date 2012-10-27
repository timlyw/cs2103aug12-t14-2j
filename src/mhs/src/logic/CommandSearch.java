package mhs.src.logic;

import java.io.IOException;
import java.util.List;

import mhs.src.storage.Task;

public class CommandSearch extends Command {

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

	@Override
	public String executeCommand() {
		String outputString = new String();
		if (matchedTasks.isEmpty()) {
			outputString = "No matching results found";
		}
		// if only 1 match is found then display it
		else if (matchedTasks.size() == 1) {
			outputString = "Chosen Task - " + matchedTasks.get(0).getTaskName()
					+ "\nTime: " + matchedTasks.get(0).getStartDateTime()
					+ " , " + matchedTasks.get(0).getEndDateTime();
			isUndoable = false;
		}
		// if multiple matches are found display the list
		else {
			outputString = displayListOfTasks(matchedTasks);
		}
		return outputString;
	}

	@Override
	public String undo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String executeByIndex(int index) {
		// TODO Auto-generated method stub
		return null;
	}

}
