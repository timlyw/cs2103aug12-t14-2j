package mhs.src.logic;

import java.io.IOException;

import mhs.src.storage.DeadlineTask;
import mhs.src.storage.FloatingTask;
import mhs.src.storage.Task;
import mhs.src.storage.TaskCategory;
import mhs.src.storage.TimedTask;

import org.joda.time.DateTime;

public class CommandAdd extends Command {

	private Task taskToAddTask;
	private Task addedTask;

	public CommandAdd(CommandInfo inputCommand) {
		taskToAddTask = new Task();
		int typeCount = 0;
		if (inputCommand.getStartDate() != null) {
			typeCount++;
		}
		if (inputCommand.getEndDate() != null) {
			typeCount++;
		}
		switch (typeCount) {
		case 0:
			Task floatingTaskToAdd = new FloatingTask(0,
					inputCommand.getTaskName(), TaskCategory.FLOATING,
					DateTime.now(), null, null, false, false);
			taskToAddTask = floatingTaskToAdd;
			break;
		case 1:
			Task deadlineTaskToAdd = new DeadlineTask(0,
					inputCommand.getTaskName(), TaskCategory.DEADLINE,
					inputCommand.getStartDate(), DateTime.now(), null, null,
					null, false, false);
			taskToAddTask = deadlineTaskToAdd;
			break;
		case 2:
			System.out.println(inputCommand.getTaskName()+"/"+inputCommand.getStartDate()+"/"+inputCommand.getEndDate());
			Task timedTaskToAdd = new TimedTask(0, inputCommand.getTaskName(),
					TaskCategory.TIMED, inputCommand.getStartDate(),
					inputCommand.getEndDate(), DateTime.now(), null, null,
					null, false, false);
			System.out.println(timedTaskToAdd.getTaskName()+"/"+timedTaskToAdd.getStartDateTime()+"/"+timedTaskToAdd.getEndDateTime());
			taskToAddTask = timedTaskToAdd;
			break;
		default:
			Task nullTask = new Task();
			taskToAddTask = nullTask;
		}
	}

	@Override
	public String executeCommand() {
		if (taskToAddTask.getTaskName() == null) {
			return "Some error ocurred";
		} else {
			try {
				addedTask = new Task();
				addedTask = dataHandler.add(taskToAddTask);
				isUndoable = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "A " + taskToAddTask.getTaskCategory() + " task - '"
					+ taskToAddTask.getTaskName() + "' was successfully added";
		}
	}

	@Override
	public String undo() {
		if (isUndoable) {
			try {
				dataHandler.delete(addedTask.getTaskId());
				isUndoable = false;
				return MESSAGE_UNDO_CONFIRM;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} else {
			return MESSAGE_UNDO_FAIL;
		}
	}

	@Override
	public String executeByIndex(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String executeByIndexAndType(int index) {
		// TODO Auto-generated method stub
		return null;
	}

}
