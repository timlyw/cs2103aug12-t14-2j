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
	private static final Logger logger = MhsLogger.getLogger();

	public String headerText = "My Hot Secretary ";

	/**
	 * Default Processor Constructor
	 */
	public Processor() {
		try {
			DatabaseFactory.getDatabaseFactory("taskRecordFile.json", true);
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
		System.out.println(userIsLoggedIn);
		commandParser = CommandParser.getCommandParser();
		createCommand = new CommandCreator();
		userCommand = new CommandInfo();
	}

	public String getHeaderText() {
		String boldTitle = htmlCreator.makeBold(headerText);
		return boldTitle;
	}

	public void showHome() {
		setCommand("display today");
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
		currentState = Command.refreshLastState();
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
		if (userCommand.getCommandEnum() == null) {
			userOutputString = createCommand.createCommand(userCommand);
		} else {
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
				System.exit(0);
				break;
			case help:
				break;
			default:
				System.out.println("user command " + userCommand);
				
				userOutputString = createCommand.createCommand(userCommand);
				commandFeedback = createCommand.getFeedback();
				currentState = createCommand.getState();
				break;
			}

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
					screenOutput = processCommand(userCommand);

				} catch (NullPointerException e1) {
					screenOutput = "No Params specified";
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
			// TODO
			return "Login unsuccessful! Please check username and password.";
		} catch (UnknownHostException e) {
			// TODO
			return "No internet connection available.";
		} catch (ServiceException e) {
			// TODO
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