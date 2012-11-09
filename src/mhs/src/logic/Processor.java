//@author A0088669A

package mhs.src.logic;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.storage.Database;
import mhs.src.storage.DatabaseFactory;
import mhs.src.common.HtmlCreator;
import mhs.src.common.exceptions.DatabaseAlreadyInstantiatedException;
import mhs.src.common.exceptions.DatabaseFactoryNotInstantiatedException;

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

	private static final int HELP_ADD = 1;
	private static final int HELP_EDIT = 2;
	private static final int HELP_DATE = 3;
	private static final int HELP_TIME = 4;
	private static final int HELP_NAME = 5;
	private static final int HELP_COMMAND = 6;

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
	public int LINE_HEIGHT = 20;
	private CommandInfo userCommand;
	private boolean isCommandQueried = false;
	private boolean isHelpIndexExpected = false;
	private Help help;
	private static final Logger logger = MhsLogger.getLogger();

	public String headerText = "Logged in";

	/**
	 * Default Processor Constructor
	 */
	public Processor() {
		try {
			DatabaseFactory.initializeDatabaseFactory("taskRecordFile.json",
					false);
			dataHandler = DatabaseFactory.getDatabaseInstance();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DatabaseAlreadyInstantiatedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseFactoryNotInstantiatedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		userIsLoggedIn = dataHandler.isUserGoogleCalendarAuthenticated();
		commandParser = CommandParser.getCommandParser();
		createCommand = new CommandCreator();
		userCommand = new CommandInfo();
	}

	public String getHeaderText() {
		String boldTitle = new String();
		if (dataHandler.isUserGoogleCalendarAuthenticated()) {
			headerText = dataHandler.getUserGoogleAccountName();
			headerText = "Hi " + headerText;
			boldTitle = htmlCreator.makeBold(headerText);
		} else {
			boldTitle = htmlCreator.makeBold("Please login");
		}
		return boldTitle;
	}

	public void showHome() {
		setCommand("home");
		executeCommand();
	}

	public void addStateListener(StateListener stateListener) {
		stateListeners.add(stateListener);
	}

	public void setLineLimit(int limit) {
		Command.setLineLimit(limit);
		refreshDisplay();
	}

	private void refreshDisplay() {
		if (isCommandQueried) {
			currentState = Command.refreshLastState();
		}
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
	private void processCommand(CommandInfo userCommand) throws Exception {
		logger.entering(getClass().getName(), this.getClass().getName());
		isCommandQueried = false;

		if (userCommand.getCommandEnum() == null) {
			if (isHelpIndexExpected) {
				helpByIndex(userCommand);
			} else {
				commandByIndexOnly(userCommand);
			}
		} else {
			executeNonTaskBasedCommand(userCommand);
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void executeNonTaskBasedCommand(CommandInfo userCommand)
			throws ServiceException {
		String userOutputString;
		switch (userCommand.getCommandEnum()) {
		case sync:
			userOutputString = syncGcal(userCommand);
			break;
		case login:
			userOutputString = loginUser();
			currentState = userOutputString;
			break;
		case logout:
			userOutputString = logoutUser();
			currentState = userOutputString;
			break;
		case exit:
			commandFeedback = "Exiting...";
			System.exit(0);
			break;
		case help:
			System.out.println("HELP MAIN");
			showHelp();
			break;
		default:
			isCommandQueried = true;
			commandByIndexOnly(userCommand);
			break;
		}
	}

	private void commandByIndexOnly(CommandInfo userCommand) {
		createCommand.createCommand(userCommand);
		commandFeedback = createCommand.getFeedback();
		currentState = createCommand.getState();
	}

	private void helpByIndex(CommandInfo userCommand) {
		switch (userCommand.getIndex()) {
		case HELP_ADD:
			help.HelpAdd();
			break;
		case HELP_EDIT:
			help.HelpEdit();
			break;
		case HELP_DATE:
			help.HelpDateFormat();
			break;
		case HELP_TIME:
			help.HelpTimeFormat();
			break;
		case HELP_NAME:
			help.HelpNameFormat();
			break;
		case HELP_COMMAND:
			help.HelpCommands();
			break;
		}
		currentState = help.getState();
		commandFeedback = help.getCommandFeedback();
		isHelpIndexExpected = false;
	}

	private void showHelp() {
		help = new Help();
		currentState = help.getState();
		commandFeedback = help.getCommandFeedback();
		isHelpIndexExpected = true;
	}

	/**
	 * Executes the given command after a carriage return
	 * 
	 * @param currentCommand
	 * @return
	 */
	public void executeCommand() {
		logger.entering(getClass().getName(), this.getClass().getName());
		String screenOutput = new String();
		try {
			if (usernameIsExpected) {
				username = currentCommand;
				usernameIsExpected = false;
				isPasswordExpected = true;
				screenOutput = "Enter password";
				currentState = screenOutput;
			} else if (isPasswordExpected) {
				password = currentCommand;
				isPasswordExpected = false;
				try {
					screenOutput = authenticateUser(username, password);
				} catch (AuthenticationException e) {
					screenOutput = "Login failed! Check username and password.";
				}
				currentState = screenOutput;
			} else {
				try {
					userCommand = commandParser
							.getParsedCommand(currentCommand);
					processCommand(userCommand);
					System.out.println("!entering processCommand!");

				} catch (NullPointerException e1) {
					screenOutput = "No Params specified";
					commandFeedback = screenOutput;
				}
			}
		} catch (Exception e) {
			screenOutput = "Exceptional Situation";
			e.printStackTrace();
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		updateStateListeners();
	}

	public void setCommand(String command) {
		currentCommand = command;
		if (!isPasswordExpected) {
			CommandInfo tempCommand = commandParser.getParsedCommand(command);
			String boldCommand = tempCommand.toHtmlString();
			commandFeedback = "feedback:" + boldCommand;

		} else {
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
		} catch (UnknownHostException e) {
			return "No internet connection available.";
		} catch (ServiceException e) {
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
			currentState = outputString;
		} else {
			try {
				dataHandler.syncronizeDatabases();
				outputString = "Pulling events from your Google calender.... Sync complete !";
			} catch (UnknownHostException e) {
				outputString = "No internet connection detected !";
			} catch (ServiceException e) {
				outputString = "No internet connection detected !";
			}
			commandFeedback = outputString;
			refreshDisplay();
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
			outputString = "To Sync you need to log in. \nEnter Google username . e.g: jim@gmail.com ";
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