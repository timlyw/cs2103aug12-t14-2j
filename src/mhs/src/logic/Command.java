package mhs.src.logic;

import java.io.IOException;
import java.util.List;

import com.google.gdata.util.ServiceException;
import mhs.src.ui.HtmlCreator;
import mhs.src.storage.Database;
import mhs.src.storage.Task;
import mhs.src.storage.TaskCategory;

public abstract class Command {

	protected static final String MESSAGE_UNDO_FAIL = "Cannot Undo";
	protected static final String MESSAGE_UNDO_CONFIRM = "Undo Successful";
	protected boolean isUndoable;
	protected List<Task> matchedTasks;
	protected Database dataHandler;
	protected boolean indexExpected;
	private HtmlCreator htmlCreator;

	public Command() {
		indexExpected = false;
		isUndoable = false;
		htmlCreator = new HtmlCreator();
		try {
			dataHandler = new Database();
		} catch (IOException | ServiceException e1) {

		}
	}

	abstract public String executeCommand();

	abstract public String undo();

	public boolean isUndoable() {
		return isUndoable;
	}

	abstract public String executeByIndex(int index);

	abstract public String executeByIndexAndType(int index);

	protected List<Task> queryTask(CommandInfo inputCommand) throws IOException {
		boolean name, startDate, endDate;
		List<Task> queryResultList;
		name = inputCommand.getTaskName() == null ? false : true;
		System.out.println("name=(" + inputCommand.getTaskName() + ")" + name);
		startDate = inputCommand.getStartDate() == null ? false : true;
		System.out.println("START:("+inputCommand.getStartDate()+")"+startDate);
		endDate = inputCommand.getEndDate() == null ? false : true;
		System.out.println("END:("+inputCommand.getEndDate()+")"+endDate);
		if (name && startDate && endDate) {
			queryResultList = dataHandler.query(inputCommand.getTaskName(),
					inputCommand.getStartDate(), inputCommand.getEndDate(),
					true);
		} else if (startDate && endDate && !name) {
			queryResultList = dataHandler.query(inputCommand.getStartDate(),
					inputCommand.getEndDate(), true);
		} else if (name && !startDate && !endDate) {
			queryResultList = dataHandler.query(inputCommand.getTaskName(),
					true);
		} else if (name && startDate && !endDate) {
			queryResultList = dataHandler.query(inputCommand.getTaskName(),
					inputCommand.getStartDate(), inputCommand.getStartDate().plusDays(1)
							.toDateMidnight().toDateTime(), true);
		} else if (!name && startDate && !endDate) {
			queryResultList = dataHandler.query(inputCommand.getStartDate(),
					inputCommand.getStartDate().plusDays(1).toDateMidnight().toDateTime(),
					true);
		} else {
			queryResultList = dataHandler.query(true);
		}
		System.out.println(displayListOfTasks(queryResultList));
		return queryResultList;
	}

	protected String displayListOfTasks(List<Task> resultList) {
		int count = 1;
		String outputString = new String();
		for (Task selectedTask : resultList) {
			if (selectedTask.getTaskCategory() == TaskCategory.FLOATING) {
				outputString += count + ". " + selectedTask.getTaskName() + "-"
						+ selectedTask.getTaskCategory() + "("
						+ selectedTask.isDone() + ")" + htmlCreator.NEW_LINE;
			} else {
				outputString += count + ". " + selectedTask.getTaskName() + "-"
						+ selectedTask.getTaskCategory() + htmlCreator.NEW_LINE;
			}
			count++;
		}
		return outputString;
	}

	protected String displayListOfTasksCategory(List<Task> resultList,
			TaskCategory category) {
		int count = 1;
		String outputString = new String();
		for (Task selectedTask : resultList) {
			if (selectedTask.getTaskCategory() == category) {
				outputString += count + ". " + selectedTask.getTaskName() + "-"
						+ selectedTask.getTaskCategory() + "("
						+ selectedTask.isDone() + ")\n";
			}
			count++;
		}
		return outputString;
	}

}
