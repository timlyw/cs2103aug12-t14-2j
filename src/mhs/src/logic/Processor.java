package mhs.src.logic;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
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
	private CommandInfo userCommand;
	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * Default Processor Constructor
	 */
	public Processor() {
		try {
			dataHandler = new Database();
			commandParser = CommandParser.getCommandParser();
			userIsLoggedIn = dataHandler.isUserGoogleCalendarAuthenticated();
			createCommand = new CommandCreator();
			userCommand = new CommandInfo();
			showHome();
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

	private void showHome() {
		setCommand("display today");
		executeCommand();
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
		logger.entering(getClass().getName(), this.getClass().getName());
		String userOutputString = new String();
		if (userCommand.getCommandEnum() != null) {
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
		} else {
			userOutputString = createCommand.createCommand(userCommand);
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return userOutputString;
	}

	/**
	 * Executes the given command after a carriage return
	 * 
	 * @param currentCommand
	 * @return
	 */
	public void executeCommand() {
		logger.entering(getClass().getName(), this.getClass().getName());
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
				try {
					userCommand = commandParser
							.getParsedCommand(currentCommand);
					screenOutput = processCommand(userCommand);

				} catch (NullPointerException e1) {
					screenOutput = "Empty Command - Blank Input";
				}
			}
		} catch (Exception e) {
			screenOutput = "Exceptional Situation";
			e.printStackTrace();
		}
		commandFeedback = "Completed";
		currentState = screenOutput;
		logger.exiting(getClass().getName(), this.getClass().getName());
		updateStateListeners();
	}

	public void setCommand(String command) {
		if(!isPasswordExpected)
		{
		currentCommand = command;
		String boldCommand = htmlCreator.makeBold(currentCommand);
		commandFeedback = "feedback for " + boldCommand;
		}
		else{
			commandFeedback = "Please ensure that CAPS lock is not on..";
		}
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
		logger.entering(getClass().getName(), this.getClass().getName());
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
		logger.exiting(getClass().getName(), this.getClass().getName());
		return outputString;
	}

	/**
	 * Begins the login process by prompting for username
	 * 
	 * @return confirmation
	 */
	private String loginUser() {
		logger.entering(getClass().getName(), this.getClass().getName());
		String outputString;
		if (!userIsLoggedIn) {
			outputString = "To Sync you need to log in. \nEnter Google username . e.g: tom.sawyer@gmail.com ";
			usernameIsExpected = true;
		} else {
			outputString = "You are already logged in!";
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return outputString;
	}

	/**
	 * User logout method
	 * 
	 * @return confirmation
	 */
	private String logoutUser() {
		logger.entering(getClass().getName(), this.getClass().getName());
		String outputString;
		if (!userIsLoggedIn) {
			outputString = "You are not logged in! Cannot logout";
		} else {
			try {
				System.out.println("inside logout");
				dataHandler.logOutUserGoogleAccount();
				userIsLoggedIn = false;
				outputString = "You have successfully logged out !";
			} catch (IOException e) {
				outputString = "Some error occurred during logout!";
			} catch (Exception e1) {
				outputString = "Some error occurred during logout!";
				e1.printStackTrace();
			}
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return outputString;
	}

}