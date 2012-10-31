package mhs.src.logic;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import mhs.src.storage.Database;
import mhs.src.ui.HtmlCreator;
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

	private Database dataHandler;
	private CommandParser commandParser;
	private boolean usernameIsExpected = false;
	private String username;
	private String password;
	private boolean userIsLoggedIn;
	private CommandCreator createCommand;

	private ArrayList<StateListener> stateListeners = new ArrayList<StateListener>();
	private String commandFeedback = null;
	private HtmlCreator htmlCreator = new HtmlCreator();
	private String currentCommand = null;
	private String currentState = null;
	private boolean isPasswordExpected = false;
	public int LINE_HEIGHT = 30;

	public Processor() {
		try {
			dataHandler = new Database();
			commandParser = CommandParser.getCommandParser();
			userIsLoggedIn = dataHandler.isUserGoogleCalendarAuthenticated();
			createCommand = new CommandCreator();
		} catch (UnknownHostException e) {
			// no internet
			e.printStackTrace();
		} catch (AuthenticationException e) {
			e.printStackTrace();
			// auth excep
		} catch (ServiceException e) {
			// service exception(wrong with google API)
			e.printStackTrace();
		} catch (IOException e) {
			//
			e.printStackTrace();
		}
	}

	public void addStateListener(StateListener stateListener) {
		stateListeners.add(stateListener);
	}

	public void setLineLimit(int limit) {
		updateStateListeners();
	}

	public String getCommandFeedback() {
		return commandFeedback;
	}

	public boolean passwordExpected() {
		return isPasswordExpected;
	}

	public String getState() {
		return currentState;
	}

	public void updateStateListeners() {
		for (int i = 0; i < stateListeners.size(); i++) {
			StateListener stateListener = stateListeners.get(i);
			stateListener.stateChanged();
		}
	}

	/**
	 * Checks if the input field needs to be masked for password
	 * 
	 * @return whether password is expected
	 */
	public boolean isPasswordExpected() {
		return isPasswordExpected;
	}

	/**
	 * Performs given operation on a task
	 * 
	 * @param userCommand
	 * @return confirmation string
	 * @throws Exception
	 */
	private String processCommand(CommandInfo userCommand) throws Exception {
		String userOutputString = new String();
		switch (userCommand.getCommandEnum()) {
		case sync:
			userOutputString = syncGcal(userCommand);
			break;
		case login:
			userOutputString = loginUser();
			break;
		case logout:
			userOutputString = logoutUser();
			break;
		default:
			userOutputString = createCommand.createCommand(userCommand);
			break;

		}
		return userOutputString;
	}

	/**
	 * Executes the given command after a carriage return
	 * 
	 * @param currentCommand
	 * @return
	 */
	public void executeCommand() {
		String screenOutput = null;
		try {
			if (usernameIsExpected) {
				username = currentCommand;
				usernameIsExpected = false;
				isPasswordExpected = true;
				screenOutput = "Enter password";
			} else if (isPasswordExpected) {
				password = currentCommand;
				isPasswordExpected = false;
				try {
					screenOutput = authenticateUser(username, password);
				} catch (AuthenticationException e) {
					screenOutput = "Login failed! Check username and password.";
					// login failed scenario goes here
					// e.printStackTrace();
				}
			} else {
				CommandInfo userCommand = commandParser
						.getParsedCommand(currentCommand);
				// store last command
				screenOutput = processCommand(userCommand);
			}
		} catch (Exception e) {
			screenOutput = "Exceptional Situation";
			e.printStackTrace();
		}
		commandFeedback = "Completed";
		currentState = screenOutput;
		updateStateListeners();
	}


	public void setCommand(String command) {
		currentCommand = command;
		String boldCommand = htmlCreator.makeBold(currentCommand);
		commandFeedback = "feedback for " + boldCommand;
		updateStateListeners();
	}

	/**
	 * Authenticates GCAL account for syncing
	 * 
	 * @param userName
	 * @param password
	 * @return confirmation string
	 * @throws IOException
	 * @throws ServiceException
	 */
	private String authenticateUser(String userName, String password)
			throws IOException, ServiceException {
		try {
			dataHandler.loginUserGoogleAccount(userName, password);
			userIsLoggedIn = true;
			return "You have successfully logged in! Your tasks will now be synced with Google Calender.";
		} catch (AuthenticationException e) {
			return "Login unsuccessful! Please check username and password.";
		}
	}

	/**
	 * Prompts user for username
	 * 
	 * @param inputCommand
	 * @return String asking for username
	 * @throws ServiceException
	 */
	private String syncGcal(CommandInfo inputCommand) throws ServiceException {
		String outputString = new String();
		if (!userIsLoggedIn) {
			outputString = loginUser();
		} else {
			try {
				dataHandler.syncronizeDatabases();
				outputString = "Pulling events from your Google calender.... Sync complete !";
			} catch (UnknownHostException e) {
				outputString = "No internet connection detected !";
			} catch (ServiceException e) {
				outputString = "No internet connection detected !";
			}
		}
		return outputString;
	}

	/**
	 * Begins the login process by prompting for username
	 * 
	 * @return confirmation
	 */
	private String loginUser() {
		String outputString;
		if (!userIsLoggedIn) {
			outputString = "To Sync you need to log in. \nEnter Google username . e.g: tom.sawyer@gmail.com ";
			usernameIsExpected = true;
		} else {
			outputString = "You are already loggen in!";
		}
		return outputString;
	}

	/**
	 * User logout method
	 * 
	 * @return confirmation
	 */
	private String logoutUser() {
		String outputString;
		try {
			dataHandler.logOutUserGoogleAccount();
			userIsLoggedIn = false;
			outputString = "You have successfully logged out !";
		} catch (IOException e) {
			outputString = "Some error occurred during logout!";
		}
		return outputString;
	}

	/**
	 * Undo's the last change to database
	 * 
	 * @return
	 * @throws Exception
	 */

}