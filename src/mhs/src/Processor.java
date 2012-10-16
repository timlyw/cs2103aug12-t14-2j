package mhs.src;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import org.joda.time.DateTime;

import com.google.gdata.client.GoogleService.InvalidCredentialsException;
import com.google.gdata.util.AuthenticationException;
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
	private boolean usernameIsExpected = false;
	private boolean passwordIsExpected = false;
	private String username;
	private String password;

	/**
	 * constructor to initialize sync with Gcal
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

	/**
	 * Checks if the input field needs to be masked for password
	 * 
	 * @return whether password is expected
	 */
	public boolean isPasswordExpected() {
		return passwordIsExpected;
	}

	/**
	 * Method that handles the live command feedback mechanism
	 * 
	 * @param command
	 * @return Command Feedback string
	 */
	public String getCommandFeedback(String command) {
		String screenOutput = null;
		try {
			if (isInteger(command) && matchedTasks.size() > 0) {
				if (validateSelectionCommand(command)) {
					screenOutput = "Command-"
							+ previousCommand.getCommandEnum()
							+ " will be performed on Task number -" + command;
					// empty list if matching index found
				} else {
					screenOutput = "That is not a valid index number";
				}

			} else {
				Command userCommand = commandParser.getParsedCommand(command);
				// store last command
				screenOutput = "Command-" + userCommand.getCommandEnum()
						+ " will be performed on event -"
						+ userCommand.getTaskName();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return screenOutput;
	}

	/**
	 * Executes the given command after a carriage return
	 * 
	 * @param command
	 * @return
	 */
	public String executeCommand(String command) {
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
				if (usernameIsExpected) {
					username = command;
					usernameIsExpected = false;
					passwordIsExpected = true;
					screenOutput = "Enter password";
				} else if (passwordIsExpected) {
					password = command;
					passwordIsExpected = false;
					try{
						screenOutput = authenticateUser(username, password);
					} catch (AuthenticationException e){
						System.out.println("Login Failed!");
						// TODO - login failed scenario goes here
						// e.printStackTrace();
					}
				} else {
					Command userCommand = commandParser
							.getParsedCommand(command);
					// store last command
					previousCommand = userCommand;
					screenOutput = processCommand(userCommand);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return screenOutput;
	}

	/**
	 * Validates if the given selection index number is less than the total
	 * matched entries
	 * 
	 * @param command
	 * @return boolean
	 */
	private boolean validateSelectionCommand(String command) {
		int inputNum = Integer.parseInt(command);
		if (matchedTasks.size() >= inputNum && inputNum > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Authenticates GCAL account for syncing
	 * 
	 * @param userName
	 * @param password
	 * @return confirmation string
	 * @throws AuthenticationException
	 * @throws IOException
	 */
	private String authenticateUser(String userName, String password)
			throws AuthenticationException, IOException {
		try{
			dataHandler.authenticateUserGoogleAccount(userName, password);
			return "You have successfully logged in! Your tasks will now be synced with Google Calender.";
		}catch (AuthenticationException e){
			throw e;
		}
	}

	/**
	 * Checks if integer is entered
	 * 
	 * @param input
	 * @return boolean
	 */
	private static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/**
	 * Process the given type of command on task selected from list
	 * @param selectedIndex
	 * @return confirmation string
	 * @throws Exception
	 */
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

	/**
	 * Performs given operation on a task
	 * @param userCommand
	 * @return confirmation string
	 * @throws Exception
	 */
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
			userOutputString = displayTask(userCommand);
			break;
		case sync:
			userOutputString = loginUser(userCommand);
			break;
		case undo:
			userOutputString = undoTask();
			break;
		default:
			userOutputString = MESSAGE_UNKNOWN_COMMAND;
		}
		return userOutputString;
	}

	/**
	 * Prompts user for username
	 * @param inputCommand
	 * @return String asking for username
	 */
	private String loginUser(Command inputCommand) {
		String outputString = new String();
		outputString = "Enter Google username . e.g: tom.sawyer@gmail.com ";
		usernameIsExpected = true;
		return outputString;
	}

	/**
	 * Undo's the ;ast change to database
	 * @return
	 */
	private String undoTask() {
		String outputString = new String();

		return outputString;
	}

	/**
	 * edits a given task, or else returns a list of matched tasks to given search string
	 * @param userCommand
	 * @return confirmations string
	 * @throws Exception
	 */
	private String editTask(Command userCommand) throws Exception {
		List<Task> resultList = queryTasksByTaskName(userCommand);
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
			outputString = "Edited Task - '" + resultList.get(0).getTaskName()
					+ "'";
		}
		// if multiple matches are found display the list
		else {
			outputString = displayListOfTasks(resultList);
		}
		return outputString;
	}

	/**
	 * create edited task based on command given
	 * @param inputCommand
	 * @param taskToEdit
	 * @return new edited Task to be updated in the DB
	 */
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

	/**
	 * remove the given task from the DB or else show all matched entries to search string
	 * @param userCommand
	 * @return confirmation string
	 * @throws Exception
	 */
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
			outputString = "Deleted Task - '" + resultList.get(0).getTaskName()
					+ "'";
		}
		// if multiple matches are found display the list
		else {
			outputString = displayListOfTasks(resultList);
		}
		return outputString;
	}

	/**
	 * Show all matched tasks in an appended string
	 * @param resultList
	 * @return appended string of tasks
	 */
	private String displayListOfTasks(List<Task> resultList) {
		int count = 1;
		String outputString = new String();
		for (Task selectedTask : resultList) {
			outputString += count + ". " + selectedTask.getTaskName() + "-"
					+ selectedTask.getTaskCategory() + "\n";
			/*
			 * + "#" + selectedTask.getStartDateTime() + "/" +
			 * selectedTask.getEndDateTime() + "\n";
			 */
			count++;
		}
		return outputString;
	}

	/**
	 * Adds a task
	 * @param userCommand
	 * @return confirmation string
	 * @throws Exception
	 */
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
			return "A " + newTask.getTaskCategory() + " task - '"
					+ newTask.getTaskName() + "' was successfully added";
		}
	}

	/**
	 * Create a new task to add. Checks the kind of task to create based on input command.
	 * @param inputCommand
	 * @return new task
	 */
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

	/**
	 * Displays a given task
	 * @param userCommand
	 * @return String to be displayed
	 * @throws IOException
	 */
	private String displayTask(Command userCommand) throws IOException {
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

	/**
	 * Queries keywords & time to get matched tasks
	 * @param inputCommand
	 * @return matched list of tasks
	 * @throws IOException
	 */
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

	/**
	 * query tasks by name
	 * @param inputCommand
	 * @return
	 * @throws IOException
	 */
	private List<Task> queryTasksByTaskName(Command inputCommand) throws IOException {
		List<Task> queryResultList;
		queryResultList = dataHandler.query(inputCommand.getTaskName());
		return queryResultList;
	}

	/**
	 * execute the given command in the database
	 * @param commandType
	 * @param taskToExecute
	 * @return if executed
	 * @throws Exception
	 */
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