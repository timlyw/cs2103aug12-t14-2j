package mhs.src.logic;

import java.io.IOException;
import java.util.List;

import mhs.src.storage.Task;

public class CommandRemove extends Command {

	private Task lastDeletedTask;

	public CommandRemove(CommandInfo userCommand) {
		List<Task> resultList;
		try {
			resultList = queryTask(userCommand);
			matchedTasks = resultList;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public CommandRemove() {
		
	}
	
	
	@Override
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

	@Override
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

	@Override
	public String executeByIndex(int index) {
		String outputString = new String();
		if (indexExpected & matchedTasks.size()<=index) {
			try{
			dataHandler.delete(matchedTasks.get(index).getTaskId());
			outputString = "Deleted "+matchedTasks.get(index).getTaskName();
			lastDeletedTask = matchedTasks.get(index);
			indexExpected = false;
			isUndoable =  true;
			}
			catch(Exception e)
			{
				
			}
		} else {
				outputString = "Invalid Command";
		}
		return outputString;
	}

}
