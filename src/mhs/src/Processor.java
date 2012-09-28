package mhs.src;

import java.io.IOException;
import java.util.List;

import javax.management.Query;
import javax.smartcardio.CommandAPDU;

import mhs.src.CommandExtractor.commands;

import org.joda.time.DateTime;

/**
 * 
 * @author Shekhar Baggavalli Raju
 * 
 *         This class processes the given user input and takes an action
 *         accordingly. It also return the necessary output to the UI
 */
public class Processor {

	private static final String MESSAGE_UNKNOWN_COMMAND = "Sorry, I didn't understand that";

	private Command previousCommand;
	private List<Task> matchedTasks;
	private CommandParser commandParser;
	private Database dataHandler;

	Processor() {
		try {
			dataHandler = new Database();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getCommandFeedback(String command) {
		return "Command feedback for " + command;
	}

	public String executeCommand(String command) throws IOException {
		String screenOutput;
		if (isInteger(command)) {
			if (validateSelectionCommand(command)) {
				screenOutput = processSelectedCommand(Integer.parseInt(command) - 1);
				// empty list if matching index found
				matchedTasks.clear();
			} else {
				screenOutput = "That is not a valid index number";
			}

		} else {
			Command userCommand = commandParser.getParsedCommand(command);
			// store last command
			previousCommand = userCommand;
			screenOutput = processCommand(userCommand);
		}
		return screenOutput;
	}

	private boolean validateSelectionCommand(String command) {
		int inputNum = Integer.parseInt(command);
		if (matchedTasks.size() >= inputNum && inputNum > 0) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	private String processSelectedCommand(int selectedIndex) throws IOException {
		String userOutputString = new String();
		switch (previousCommand.getCommandEnum()) {
		case remove:
			dataHandler.delete(matchedTasks.get(selectedIndex).getTaskId());
			userOutputString = "Deleted Task - "
					+ matchedTasks.get(selectedIndex).getTaskName();
			break;
		case edit:
			Task editedTask = createEditedTask(previousCommand,
					matchedTasks.get(selectedIndex));
			dataHandler.add(editedTask);
			// delete task
			dataHandler.delete(matchedTasks.get(selectedIndex).getTaskId());
			userOutputString = "Edited Task - "
					+ matchedTasks.get(selectedIndex).getTaskName();
			break;
		case search:
			userOutputString = "Chosen Task - "
					+ matchedTasks.get(selectedIndex).getTaskName()
					+ "\nTime: "
					+ matchedTasks.get(selectedIndex).getStartDateTime()
					+ " , " + matchedTasks.get(selectedIndex).getEndDateTime();
			break;
		default:
			userOutputString = MESSAGE_UNKNOWN_COMMAND;
		}
		return userOutputString;
	}

	private String processCommand(Command userCommand) throws IOException {
		String userOutputString = new String();
		switch (userCommand.getCommandEnum()) {
		case add:
			userOutputString = addTask(userCommand);
			break;
		case remove:
			userOutputString = removeTask(userCommand);
			break;
		case edit:
			userOutputString = editTask(userCommand);
			break;
		case search:
			userOutputString = searchTask(userCommand);
			break;
		case sync:
			break;
		case undo:
			break;
		default:
			userOutputString = MESSAGE_UNKNOWN_COMMAND;
		}
		return userOutputString;
	}

	private String editTask(Command userCommand) throws IOException {
		List<Task> resultList = queryTask(userCommand);
		matchedTasks = resultList;
		String outputString = new String();

		if (resultList.isEmpty()) {
			outputString = "No matching results found";
		}
		// if only 1 match is found then display it
		else if (resultList.size() == 1) {
			// create task
			Task editedTask = createEditedTask(userCommand, resultList.get(0));
			dataHandler.add(editedTask);
			// delete task
			dataHandler.delete(resultList.get(0).getTaskId());
			outputString = "Edited Task - " + resultList.get(0).getTaskName();
		}
		// if multiple matches are found display the list
		else {
			outputString = displayListOfTasks(resultList);
		}
		return outputString;
	}

	private Task createEditedTask(Command inputCommand, Task taskToEdit) {
		// checks kind of task :floating/timed/deadline
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
				Task floatingTaskToAdd = new FloatingTask(0,
						inputCommand.getEdittedName(), TaskCategory.FLOATING,
						DateTime.now(), null, null, false, false);
				return floatingTaskToAdd;
			case 1:
				Task deadlineTaskToAdd = new DeadlineTask(0,
						inputCommand.getEdittedName(), TaskCategory.DEADLINE,
						inputCommand.getEndDate(), DateTime.now(), null, null,
						null, false, false);
				return deadlineTaskToAdd;
			case 2:
				Task timedTaskToAdd = new TimedTask(0,
						inputCommand.getEdittedName(), TaskCategory.TIMED,
						inputCommand.getStartDate(), inputCommand.getEndDate(),
						DateTime.now(), null, null, null, false, false);
				return timedTaskToAdd;
			}
		} else {
			switch (typeCount) {
			case 1:
				Task deadlineTaskToAdd = new DeadlineTask(0,
						taskToEdit.getTaskName(), TaskCategory.DEADLINE,
						inputCommand.getEndDate(), DateTime.now(), null, null,
						null, false, false);
				return deadlineTaskToAdd;
			case 2:
				Task timedTaskToAdd = new TimedTask(0,
						taskToEdit.getTaskName(), TaskCategory.TIMED,
						inputCommand.getStartDate(), inputCommand.getEndDate(),
						DateTime.now(), null, null, null, false, false);
				return timedTaskToAdd;
			}
		}
		Task nullTask = new Task();
		return nullTask;
	}

	private String removeTask(Command userCommand) throws IOException {
		List<Task> resultList = queryTask(userCommand);
		matchedTasks = resultList;
		String outputString = new String();

		if (resultList.isEmpty()) {
			outputString = "No matching results found";
		}
		// if only 1 match is found then display it
		else if (resultList.size() == 1) {
			dataHandler.delete(resultList.get(0).getTaskId());
			outputString = "Deleted Task - " + resultList.get(0).getTaskName();
		}
		// if multiple matches are found display the list
		else {
			outputString = displayListOfTasks(resultList);
		}
		return outputString;
	}

	private String displayListOfTasks(List<Task> resultList) {
		int count = 1;
		String outputString = new String();
		for (Task selectedTask : resultList) {
			outputString += count + ". " + selectedTask.getTaskName() + "\n";
			count++;
		}
		return outputString;
	}

	private String addTask(Command userCommand) {
		Task newTask = createTaskToAdd(userCommand);
		if (newTask.getTaskName() == null) {
			return "Some error ocurred";
		} else {
			try {
				dataHandler.add(newTask);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "Task: " + newTask.getTaskName() + "was successfully added";
		}
	}

	private Task createTaskToAdd(Command inputCommand) {
		// checks kind of task :floating/timed/deadline
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
			return floatingTaskToAdd;
		case 1:
			Task deadlineTaskToAdd = new DeadlineTask(0,
					inputCommand.getTaskName(), TaskCategory.DEADLINE,
					inputCommand.getEndDate(), DateTime.now(), null, null,
					null, false, false);
			return deadlineTaskToAdd;
		case 2:
			Task timedTaskToAdd = new TimedTask(0, inputCommand.getTaskName(),
					TaskCategory.TIMED, inputCommand.getStartDate(),
					inputCommand.getEndDate(), DateTime.now(), null, null,
					null, false, false);
			return timedTaskToAdd;
		}
		Task nullTask = new Task();
		return nullTask;
	}

	private String searchTask(Command userCommand) throws IOException {
		List<Task> resultList = queryTask(userCommand);
		matchedTasks = resultList;
		String outputString = new String();

		if (resultList.isEmpty()) {
			outputString = "No matching results found";
		}
		// if only 1 match is found then display it
		else if (resultList.size() == 1) {
			outputString = "Chosen Task - " + resultList.get(0).getTaskName()
					+ "\nTime: " + matchedTasks.get(0).getStartDateTime()
					+ " , " + matchedTasks.get(0).getEndDateTime();
		}
		// if multiple matches are found display the list
		else {
			outputString = displayListOfTasks(resultList);
		}
		return outputString;

	}

	private List<Task> queryTask(Command inputCommand) throws IOException {
		boolean name, startDate, endDate;
		List<Task> queryResultList;
		name = inputCommand.getTaskName() == null ? false : true;
		startDate = inputCommand.getStartDate() == null ? false : true;
		endDate = inputCommand.getEndDate() == null ? false : true;
		if (name && startDate && endDate) {
			queryResultList = dataHandler.query(inputCommand.getTaskName(),
					TaskCategory.TIMED, inputCommand.getStartDate(),
					inputCommand.getEndDate());
		} else if (startDate && endDate && !name) {
			queryResultList = dataHandler.query(inputCommand.getStartDate(),
					inputCommand.getEndDate());
		} else if (name && !startDate && !endDate) {
			queryResultList = dataHandler.query(inputCommand.getTaskName());
		} else {
			queryResultList = dataHandler.query();
		}
		return queryResultList;
	}

}