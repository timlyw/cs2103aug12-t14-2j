//@author A0088669A

package mhs.src.logic;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Logger;

import mhs.src.common.FileHandler;
import mhs.src.common.HtmlCreator;
import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.DatabaseAlreadyInstantiatedException;
import mhs.src.common.exceptions.DatabaseFactoryNotInstantiatedException;
import mhs.src.storage.Database;
import mhs.src.storage.DatabaseFactory;

import org.joda.time.DateTime;

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

	private static final String DATE_TIME_FORMAT = "dd-MM-yy HH-mm";
	private static final String TEST_FILE_CLOSE_HTML = "</body></html>";
	private static final String TEST_FILE_START_HTML = "<html><body>";
	private static final String MESSAGE_LOGOUT_FAIL = "Some error occurred during logout!";
	private static final String MESSAGE_LOGOUT_SUCCESS = "You have successfully logged out !";
	private static final String MESSAGE_LOGOUT_FAIL_NOT_LOGGED_IN = "You are not logged in! Cannot logout";
	private static final String MESSAGE_NOT_LOGGED_IN = "You are already logged in!";
	private static final String MESSAGE_SYNC_NOT_LOGGED_IN = "To Sync you need to log in. \nEnter Google username . e.g: jim@gmail.com ";
	private static final String MESSAGE_SYNCING = "Pulling events from your Google calender.... Please refresh !";
	private static final String MESSAGE_NO_INTERNET = "No internet connection available.";
	private static final String MESSAGE_LOGIN_FAIL = "Login unsuccessful! Please check username and password.";
	private static final String MESSAGE_LOGIN_SUCCESS = "You have successfully logged in! Your tasks will now be synced with Google Calender.";
	private static final String MESSAGE_PASSWORD_FEEDBACK = "Please ensure that CAPS lock is not on..";
	private static final String MESSAGE_ERROR = "Some Error Occurred";
	private static final String MESSAGE_NO_PARAMS = "No Params specified";
	private static final String MESSAGE_NULL_INPUT = "Null Input";
	private static final String MESSAGE_ENTER_PASSWORD = "Enter password";
	private static final String COMMAND_HOME = "home";
	private static final String MESSAGE_DEFAULT_LOGIN_DISPLAY_TEXT = "Please login";
	private static final String MESSAGE_DATABASE_FACTORY_NOT_INSTANTIATED = "Database Factory not instantiated";
	private static final String MESSAGE_DATABASE_GIVEN_WRONG_ARGUMENTS = "Database given wrong arguments";
	private static final String MESSAGE_DATABASE_NOT_INSTANTIATED = "Database not instantiated";
	private static final String MESSAGE_FILE_I_O_ERROR = "File I/O Error";
	private static final String MESSAGE_INVALID_INDEX = "Invalid Index";
	private static final String MESSAGE_HI_USERNAME = "Hi %1$s";
	private static final String MESSAGE_FEEDBACK = "feedback: %1$s";

	private static final String FILE_FEEDBACK = "SystemTestFiles/feedback-%1$s.html";
	private static final String FILE_STATE = "SystemTestFiles/state-%1$s.html";

	private static final int HELP_ADD = 1;
	private static final int HELP_EDIT = 2;
	private static final int HELP_SEARCH = 3;
	private static final int HELP_DATE = 4;
	private static final int HELP_TIME = 5;
	private static final int HELP_NAME = 6;
	private static final int HELP_COMMAND = 7;

	private static boolean DEBUG = false;

	private static Processor processor;
	private Database dataHandler;
	private CommandParser commandParser;
	private CommandCreator commandCreator;

	private boolean usernameIsExpected = false;
	private boolean isPasswordExpected = false;
	private String username;
	private String password;
	private boolean userIsLoggedIn;

	private FileHandler feedbackFile;
	private FileHandler stateFile;

	private String userInputString;

	private String commandFeedback;
	private String currentState;

	private ArrayList<StateListener> stateListeners = new ArrayList<StateListener>();
	private HtmlCreator htmlCreator = new HtmlCreator();
	public int LINE_HEIGHT = 20;
	private CommandInfo userCommand;
	private boolean isCommandQueried = false;
	private boolean isHelpIndexExpected = false;
	private Help help;

	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * Default Processor Constructor. Instantiated all required objects.
	 */
	private Processor() {
		logEnterMethod("Processor");
		initializeDatabse();
		userIsLoggedIn = dataHandler.isUserGoogleCalendarAuthenticated();
		commandParser = CommandParser.getCommandParser();
		commandCreator = CommandCreator.getCommandCreator();
		userCommand = new CommandInfo();
		logExitMethod("Processor");
	}

	/**
	 * Initializes Database.
	 */
	private void initializeDatabse() {
		try {
			DatabaseFactory.initializeDatabaseFactory("taskRecordFile.json",
					false);
			dataHandler = DatabaseFactory.getDatabaseInstance();
		} catch (IOException e1) {
			commandFeedback = MESSAGE_FILE_I_O_ERROR;
		} catch (DatabaseAlreadyInstantiatedException e1) {
			commandFeedback = MESSAGE_DATABASE_NOT_INSTANTIATED;
		} catch (IllegalArgumentException e) {
			commandFeedback = MESSAGE_DATABASE_GIVEN_WRONG_ARGUMENTS;
		} catch (DatabaseFactoryNotInstantiatedException e) {
			commandFeedback = MESSAGE_DATABASE_FACTORY_NOT_INSTANTIATED;
		}
	}

	/**
	 * Getter method to get instant of Processor. Processor is singleton.
	 * 
	 * @return
	 */
	public static Processor getProcessor() {
		if (processor == null) {
			processor = new Processor();
		}
		return processor;
	}

	/**
	 * Processor for System Tests.
	 * 
	 * @param testDb
	 */
	private Processor(String testDb) {
		initializeDatabaseTestMode(testDb);
		userIsLoggedIn = dataHandler.isUserGoogleCalendarAuthenticated();
		commandParser = CommandParser.getCommandParser();
		commandCreator = CommandCreator.getCommandCreator();
		userCommand = new CommandInfo();
		initiateFile();
	}

	/**
	 * Initates out files for system testing
	 */
	private void initiateFile() {
		feedbackFile = new FileHandler(String.format(FILE_FEEDBACK, DateTime
				.now().toString(DATE_TIME_FORMAT)));
		stateFile = new FileHandler(String.format(FILE_STATE, DateTime.now()
				.toString(DATE_TIME_FORMAT)));
		feedbackFile.writeToFile(TEST_FILE_START_HTML);
		stateFile.writeToFile(TEST_FILE_START_HTML);
	}

	/**
	 * Initializes Database for System Testing.
	 * 
	 * @param testDb
	 */
	private void initializeDatabaseTestMode(String testDb) {
		try {
			DatabaseFactory.initializeDatabaseFactory(testDb, false);
			dataHandler = DatabaseFactory.getDatabaseInstance();
		} catch (IOException e1) {
			commandFeedback = MESSAGE_FILE_I_O_ERROR;
		} catch (DatabaseAlreadyInstantiatedException e1) {
			commandFeedback = MESSAGE_DATABASE_NOT_INSTANTIATED;
		} catch (IllegalArgumentException e) {
			commandFeedback = MESSAGE_DATABASE_GIVEN_WRONG_ARGUMENTS;
		} catch (DatabaseFactoryNotInstantiatedException e) {
			commandFeedback = MESSAGE_DATABASE_FACTORY_NOT_INSTANTIATED;
		}
	}

	/**
	 * Gets instant for Processor - DEBUG
	 * 
	 * @param testDb
	 * @return
	 */
	public static Processor getProcessor(String testDb) {
		processor = new Processor(testDb);
		return processor;
	}

	/**
	 * Returns text to be displayed in the Login Notification area.
	 * 
	 * @return String
	 */
	public String getLoginDisplayFieldText() {
		logEnterMethod("getLoginDisplayFieldText");
		String boldTitle = new String();
		if (dataHandler.isUserGoogleCalendarAuthenticated()) {
			boldTitle = displayTextIfLoggedIn();
		} else {
			boldTitle = htmlCreator
					.makeBold(MESSAGE_DEFAULT_LOGIN_DISPLAY_TEXT);
		}
		logExitMethod("getLoginDisplayFieldText");
		return boldTitle;
	}

	/**
	 * Returns the username to show, if logged in.
	 * 
	 * @return String
	 */
	private String displayTextIfLoggedIn() {
		String userGreetString;
		userGreetString = dataHandler.getAuthenticatedUserGoogleAccountName();
		userGreetString = String.format(MESSAGE_HI_USERNAME, userGreetString);
		userGreetString = htmlCreator.makeBold(userGreetString);
		return userGreetString;
	}

	/**
	 * Sets the userInput to 'home' . Called at the beginning of the program.
	 */
	public void showHome() {
		logEnterMethod("showHome");
		setCommand(COMMAND_HOME);
		executeCommand();
		logExitMethod("showHome");
	}

	/**
	 * Adds state listeners.
	 * 
	 * @param stateListener
	 */
	public void addStateListener(StateListener stateListener) {
		stateListeners.add(stateListener);
	}

	/**
	 * Set line limit for UI. Called when window is resized.
	 * 
	 * @param limit
	 */
	public void setLineLimit(int limit) {
		logEnterMethod("setLineLimit");
		Command.setLineLimit(limit);
		refreshDisplay();
		logExitMethod("setLineLimit");
	}

	/**
	 * Refreshes display with Last list, if the last command did a query.
	 */
	private void refreshDisplay() {
		logEnterMethod("refreshDisplay");
		if (isCommandQueried) {
			currentState = Command.refreshLastState();
		}
		updateStateListeners();
		logExitMethod("refreshDisplay");
	}

	/**
	 * Returns the Command Feedback, to be displayed in the feedback box.
	 * 
	 * @return
	 */
	public String getCommandFeedback() {
		return commandFeedback;
	}

	/**
	 * Informs the UI, that the next text should be encrypted.
	 * 
	 * @return
	 */
	public boolean passwordExpected() {
		logEnterMethod("passwordExpected");
		logExitMethod("passwordExpected");
		return isPasswordExpected;
	}

	/**
	 * Returns the user the Current State, to be displayed in the state box.
	 * 
	 * @return
	 */
	public String getState() {
		return currentState;
	}

	/**
	 * Updates the state listeners to take note of any changes.
	 */
	public void updateStateListeners() {
		for (int i = 0; i < stateListeners.size(); i++) {
			StateListener stateListener = stateListeners.get(i);
			stateListener.stateChanged();
		}
	}

	/**
	 * Executes the given command after a carriage return
	 * 
	 * @param userInputString
	 * @return
	 */
	public void executeCommand() {
		logEnterMethod("executeCommand");
		if (userInputString == null) {
			commandFeedback = MESSAGE_NULL_INPUT;
			return;
		}
		executeByInputType();
		writeToFileIfInDebugMode();
		updateStateListeners();
		logExitMethod("executeCommand");
	}

	/**
	 * Checks the if input is username/password/command and executes
	 * accordingly.
	 */
	private void executeByInputType() {
		try {
			if (usernameIsExpected) {
				setUsername();
			} else if (isPasswordExpected) {
				setPassword();
			} else {
				parseAndExecute();
			}
		} catch (Exception e) {
			commandFeedback = MESSAGE_ERROR;
		}
	}

	/**
	 * Writes UI out to File during System testing
	 */
	private void writeToFileIfInDebugMode() {
		if (DEBUG) {
			feedbackFile.writeToFile(commandFeedback + HtmlCreator.NEW_LINE);
			stateFile.writeToFile(currentState + HtmlCreator.NEW_LINE);
		}
	}

	/**
	 * Parses the input for command params and executes accordingly.
	 * 
	 * @throws Exception
	 */
	private void parseAndExecute() throws Exception {
		logEnterMethod("parseAndExecute");
		try {
			userCommand = commandParser.getParsedCommand(userInputString);
			processCommand(userCommand);
		} catch (NullPointerException e1) {
			commandFeedback = MESSAGE_NO_PARAMS;
		}
		logExitMethod("parseAndExecute");
	}

	/**
	 * If input Type is password, it tries to authenticate user
	 * 
	 * @throws IOException
	 * @throws ServiceException
	 */
	private void setPassword() throws IOException, ServiceException {
		logEnterMethod("setPassword");
		String screenOutput;
		password = userInputString;
		isPasswordExpected = false;
		try {
			screenOutput = authenticateUser(username, password);
		} catch (AuthenticationException e) {
			screenOutput = "Login failed! Check username and password.";
		}
		currentState = screenOutput;
		logExitMethod("setPassword");
	}

	/**
	 * Stores username
	 */
	private void setUsername() {
		String screenOutput;
		username = userInputString;
		usernameIsExpected = false;
		isPasswordExpected = true;
		screenOutput = MESSAGE_ENTER_PASSWORD;
		currentState = screenOutput;
	}

	/**
	 * Performs given operation on a task
	 * 
	 * @param userCommand
	 * @return confirmation string
	 * @throws Exception
	 */
	private void processCommand(CommandInfo userCommand) throws Exception {
		logEnterMethod("processCommand");
		setCommandQueriedFlagToFalse();
		if (userCommand == null) {
			commandFeedback = MESSAGE_ERROR;
			return;
		}
		if (userCommand.getCommandEnum() == null) {
			if (isHelpIndexExpected) {
				helpByIndex(userCommand);
			} else {
				executeNonIndexCommand(userCommand);
			}
		} else {
			disableHelpIndex();
			executeNonTaskBasedCommand(userCommand);
		}
		logExitMethod("processCommand");
	}

	/**
	 * Sets Queried flag so that UI doesnt refresh a task list when resized
	 */
	private void setCommandQueriedFlagToFalse() {
		isCommandQueried = false;
	}

	/**
	 * Help index flag is disabled.
	 */
	private void disableHelpIndex() {
		if (isHelpIndexExpected) {
			isHelpIndexExpected = false;
		}
	}

	/**
	 * Execute Non Index based Command
	 * 
	 * @param userCommand
	 * @throws ServiceException
	 */
	private void executeNonTaskBasedCommand(CommandInfo userCommand)
			throws ServiceException {
		logEnterMethod("executeNonTaskBasedCommand");
		switch (userCommand.getCommandEnum()) {
		case sync:
			syncGcal(userCommand);
			break;
		case login:
			currentState = loginUser();
			break;
		case logout:
			currentState = logoutUser();
			break;
		case exit:
			exitProgram();
			break;
		case help:
			showHelp();
			break;
		default:
			isCommandQueried = true;
			executeNonIndexCommand(userCommand);
			break;
		}
		logExitMethod("executeNonTaskBasedCommand");
	}

	/**
	 * Closes the program. Writes closing HTML tags if in DEBUG mode
	 */
	private void exitProgram() {
		if (DEBUG) {
			closeFileHtml();
		}
		System.exit(0);
	}

	/**
	 * Adds closing HTML tags to file.
	 */
	private void closeFileHtml() {
		feedbackFile.writeToFile(TEST_FILE_CLOSE_HTML);
		stateFile.writeToFile(TEST_FILE_CLOSE_HTML);
	}

	/**
	 * Execute Command by index
	 * 
	 * @param userCommand
	 */
	private void executeNonIndexCommand(CommandInfo userCommand) {
		logEnterMethod("commandByIndexOnly");
		commandCreator.createCommand(userCommand);
		commandFeedback = commandCreator.getFeedback();
		currentState = commandCreator.getState();
		logExitMethod("commandByIndexOnly");
	}

	/**
	 * Executes Help index commands
	 * 
	 * @param userCommand
	 */
	private void helpByIndex(CommandInfo userCommand) {
		logEnterMethod("helpByIndex");
		if (userCommand.getIndex() > 0 && userCommand.getIndex() < 8) {
			switch (userCommand.getIndex()) {
			case HELP_ADD:
				help.HelpAdd();
				break;
			case HELP_EDIT:
				help.HelpEdit();
				break;
			case HELP_SEARCH:
				help.HelpSearch();
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
		} else {
			commandFeedback = MESSAGE_INVALID_INDEX;
		}
		logExitMethod("helpByIndex");
	}

	/**
	 * Show main help page
	 */
	private void showHelp() {
		logEnterMethod("showHelp");
		help = new Help();
		currentState = help.getState();
		commandFeedback = help.getCommandFeedback();
		isHelpIndexExpected = true;
		logExitMethod("showHelp");
	}

	/**
	 * Set CommandFeedback
	 * 
	 * @param command
	 */
	public void setCommand(String command) {
		userInputString = command;
		if (!isPasswordExpected) {
			CommandInfo tempCommand = commandParser.getParsedCommand(command);
			String feedbackString = tempCommand.toHtmlString();
			commandFeedback = String.format(MESSAGE_FEEDBACK, feedbackString);
		} else {
			commandFeedback = MESSAGE_PASSWORD_FEEDBACK;
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
		logEnterMethod("authenticateUser");
		String output;
		try {
			dataHandler.loginUserGoogleAccount(userName);
			userIsLoggedIn = true;
			output = MESSAGE_LOGIN_SUCCESS;
		} catch (AuthenticationException e) {
			output = MESSAGE_LOGIN_FAIL;
		} catch (UnknownHostException e) {
			output = MESSAGE_NO_INTERNET;
		} catch (ServiceException e) {
			output = MESSAGE_LOGIN_FAIL;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			output = MESSAGE_LOGIN_FAIL;
			e.printStackTrace();
		}
		logExitMethod("authenticateUser");
		return output;
	}

	/**
	 * Prompts user for username
	 * 
	 * @param inputCommand
	 * @return String asking for username
	 * @throws ServiceException
	 */
	private String syncGcal(CommandInfo inputCommand) throws ServiceException {
		logEnterMethod("syncGcal");
		String outputString = new String();
		if (!userIsLoggedIn) {
			outputString = loginUser();
			currentState = outputString;
		} else {
			try {
				dataHandler.syncronizeDatabases();
				outputString = MESSAGE_SYNCING;
			} catch (UnknownHostException e) {
				outputString = MESSAGE_NO_INTERNET;
			} catch (ServiceException e) {
				outputString = MESSAGE_NO_INTERNET;
			}
			commandFeedback = outputString;
			refreshDisplay();
		}
		logExitMethod("syncGcal");
		return outputString;
	}

	/**
	 * Begins the login process by prompting for username
	 * 
	 * @return confirmation
	 */
	private String loginUser() {
		logEnterMethod("loginUser");
		String outputString;
		if (!userIsLoggedIn) {
			outputString = MESSAGE_SYNC_NOT_LOGGED_IN;
			usernameIsExpected = true;
		} else {
			outputString = MESSAGE_NOT_LOGGED_IN;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		logExitMethod("loginUser");
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
			outputString = MESSAGE_LOGOUT_FAIL_NOT_LOGGED_IN;
		} else {
			try {
				dataHandler.logOutUserGoogleAccount();
				userIsLoggedIn = false;
				outputString = MESSAGE_LOGOUT_SUCCESS;
			} catch (IOException e) {
				outputString = MESSAGE_LOGOUT_FAIL;
			} catch (Exception e1) {
				outputString = MESSAGE_LOGOUT_FAIL;
			}
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return outputString;
	}

	/**
	 * Sets debug mode
	 */
	public void setDebugMode() {
		DEBUG = true;
	}

	/**
	 * Clears database
	 */
	public void clearDatabase() {
		try {
			dataHandler.clearDatabase();
		} catch (IOException e) {
		} catch (ServiceException e) {
		}
	}

	/**
	 * Logger enter method
	 * 
	 * @param methodName
	 */
	private void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}

	/**
	 * Logger exit method
	 * 
	 * @param methodName
	 */
	private void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}
}