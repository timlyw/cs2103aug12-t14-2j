package mhs.src.logic;

import java.io.IOException;
import java.util.List;

import mhs.src.storage.Task;
import mhs.src.storage.TaskCategory;

public class CommandMark extends Command {

	Task lastTask;
	Task undoedTask;
	public CommandMark(CommandInfo inputCommand) {
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
		if(isUndoable())
		{
			undoedTask = new Task();
			undoedTask.setDone(false);
			try {
				dataHandler.update(undoedTask);
				return MESSAGE_UNDO_CONFIRM;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return MESSAGE_UNDO_FAIL;
			}
		}
		else{
			return MESSAGE_UNDO_FAIL;
		}
	}

	@Override
	public String executeByIndex(int index) {
		// TODO Auto-generated method stub
		return null;
	}

}
