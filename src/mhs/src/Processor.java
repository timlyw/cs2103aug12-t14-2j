package mhs.src;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import org.joda.time.DateTime;

import com.google.gdata.util.ServiceException;

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

	// private Stack<Task> taskLog;

	/**
	 * constructor to initialize sync
	 */
	Processor() {
		try {
			try {
				dataHandler = new Database();
				commandParser = new CommandParser();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getCommandFeedback(String command) {
		String screenOutput = null;
		try {
			// *********add && matchedTasks.size()>0
			if (isInteger(command)) {
				if (validateSelectionCommand(command)) {
					screenOutput = "Command-"+previousCommand.getCommandEnum()+" will be performed on Task number -"+command;
					// empty list if matching index found
				} else {
					screenOutput = "That is not a valid index number";
				}

			} else {
				Command userCommand = commandParser.getParsedCommand(command);
				// store last command
				screenOutput="Command-"+userCommand.getCommandEnum()+" will be performed on event -"+userCommand.getTaskName();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return screenOutput;
	}

	public String executeCommand(String command) throws IOException {
		String screenOutput = null;
		try {
			// *********add && matchedTasks.size()>0
			if (isInteger(command)) {
				if (validateSelectionCommand(command)) {
					screenOutput = processSelectedCommand(Integer
							.parseInt(command) - 1);
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	private String processSelectedCommand(int selectedIndex) throws Exception {
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
			dataHandler.update(editedTask);
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

	private String processCommand(Command userCommand) throws Exception {
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
			userOutputString = undoTask();
			break;
		default:
			userOutputString = MESSAGE_UNKNOWN_COMMAND;
		}
		return userOutputString;
	}

	private String undoTask() {
		String outputString = new String();

		return outputString;
	}

	private String editTask(Command userCommand) throws Exception {
		List<Task> resultList = queryTask(userCommand);
		matchedTasks = resultList;
		String outputString = new String();
		// outputString =
		// userCommand.getCommandEnum()+"??"+userCommand.getTaskName()+"??"+userCommand.getEdittedName()+"??"+userCommand.getStartDate()+"??"+userCommand.getEndDate();
		// return outputString;
		if (resultList.isEmpty()) {
			outputString = "No matching results found";
		}
		// if only 1 match is found then display it
		else if (resultList.size() == 1) {
			// create task
			Task editedTask = createEditedTask(userCommand, resultList.get(0));
			executeTask("edit", editedTask);
			outputString = "Edited Task";
		}
		// if multiple matches are found display the list
		else {
			outputString = displayListOfTasks(resultList);
		}
		return outputString;
	}

	public Task createEditedTask(Command inputCommand, Task taskToEdit) {
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

	private String removeTask(Command userCommand) throws Exception {
		List<Task> resultList = queryTask(userCommand);
		matchedTasks = resultList;
		String outputString = new String();

		if (resultList.isEmpty()) {
			outputString = "No matching results found";
		}
		// if only 1 match is found then display it
		else if (resultList.size() == 1) {
			executeTask("remove", resultList.get(0));
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
			outputString += count + ". " + selectedTask.getTaskName() + "-"
					+ selectedTask.getTaskCategory() + "#"
					+ selectedTask.getStartDateTime() + "/"
					+ selectedTask.getEndDateTime() + "\n";
			count++;
		}
		return outputString;
	}

	private String addTask(Command userCommand) throws Exception {
		Task newTask = createTaskToAdd(userCommand);
		if (newTask.getTaskName() == null) {
			return "Some error ocurred";
		} else {
			try {
				executeTask("add", newTask);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "Task: " + newTask.getTaskName() + "@"
					+ userCommand.getStartDate() + "@"
					+ newTask.getEndDateTime() + "@"
					+ newTask.getTaskCategory() + "#"
					+ newTask.getStartDateTime() + " to "
					+ newTask.getEndDateTime() + "was successfully added";
		}
	}

	public Task createTaskToAdd(Command inputCommand) {
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
					inputCommand.getStartDate(), DateTime.now(), null, null,
					null, false, false);
			return deadlineTaskToAdd;
		case 2:
			Task timedTaskToAdd = new TimedTask(0, inputCommand.getTaskName(),
					TaskCategory.TIMED, inputCommand.getStartDate(),
					inputCommand.getEndDate(), DateTime.now(), null, null,
					null, false, false);
			return timedTaskToAdd;
		default:
			Task nullTask = new Task();
			return nullTask;
		}
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

	private boolean executeTask(String commandType, Task taskToExecute)
			throws Exception {
		switch (commandType) {
		case "add":
			dataHandler.add(taskToExecute);
			break;
		case "remove":
			dataHandler.delete(taskToExecute.getTaskId());
			break;
		case "edit":
			dataHandler.update(taskToExecute);
			break;
		default:
			return false;
		}
		return true;
	}
}