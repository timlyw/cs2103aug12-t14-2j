//@author A0086805X
package mhs.src.logic;

import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.storage.Database;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 * 
 * This is a class to parse strings and extract out appropriate parameters.
 */
public class CommandParser {

	//These are the regex strings
	private static final String REGEX_WHITE_SPACES = "\\s+";
	private static final String REGEX_LEFT_BRACER = "\\<";
	private static final String REGEX_RIGHT_BRACER = "\\>";
	
	//These are the commands that allow indexing
	private static final String COMMAND_UNMARK = "unmark";
	private static final String COMMAND_RENAME = "rename";
	private static final String COMMAND_MARK = "mark";
	private static final String COMMAND_EDIT = "edit";
	private static final String COMMAND_REMOVE = "remove";
	
	private DateExtractor dateParser;
	private TimeExtractor timeParser;
	private CommandExtractor commandExtractor;
	private NameExtractor nameParser;

	private boolean taskNameFlag;
	private boolean timeFlag;
	private boolean dateFlag;

	private String command;
	private String taskName;
	private String edittedName;

	private LocalDate startDate;
	private LocalDate endDate;
	private LocalTime startTime;
	private LocalTime endTime;

	private static final Logger logger = MhsLogger.getLogger();

	private int index;
	private static CommandParser commandParser;

	/**
	 * This is the private constructor for commandParser
	 */
	private CommandParser() {
		logEnterMethod("CommandParser");
		setEnvironment();
		logExitMethod("CommandParser");
	}

	/**
	 * This is the method to get a single instance of commandParser.
	 * 
	 * @return Returns a single instance of commandParser.
	 */
	public static CommandParser getCommandParser() {

		if (commandParser == null) {
			commandParser = new CommandParser();
		}

		return commandParser;
	}

	/**
	 * This is the function to take in a string and return a command object with
	 * parameters set.
	 * 
	 * @param parseString
	 *            The String that needs to be parsed.
	 * 
	 * @return Returns a commandObject that has all the arguments set
	 */
	public CommandInfo getParsedCommand(String parseString) {
		logEnterMethod("getParsedCommand");
		
		assert (parseString != null);
		parseString = setEnvironment(parseString);
		
		setCommand(parseString);
		getIndexAtFirstLocation(parseString);
		parseString = setNameInQuotationMarks(parseString);
		setTime(parseString);
		setDate(parseString);
		setIndex(parseString);
		setName(parseString);
		
		logExitMethod("getParsedCommand");
		return setUpCommandObject(command, taskName, edittedName, startDate,
				startTime, endDate, endTime, index);

	}

	/**
	 * Method to get a single index at the first location of the string.
	 * 
	 * @param parseString
	 * 			The string that needs to be parsed.
	 */
	private void getIndexAtFirstLocation(String parseString) {
		logEnterMethod("getIndexAtFirstLocation");
		try {
			String[] processArray = parseString.split(REGEX_WHITE_SPACES);
			if (processArray.length > 0) {
				if (isInteger(processArray[0])) {
					index = Integer.parseInt(processArray[0]);
					command = null;
				}
			}
		} catch (NullPointerException e) {
			logger.log(Level.FINER, e.getMessage());
			return;
		}catch(ArrayIndexOutOfBoundsException e){
			logger.log(Level.FINER, e.getMessage());
			return;
		}
		logExitMethod("getIndexAtFirstLocation");
	}

	/**
	 * Method to get an index at the second position of the string.
	 * 
	 * @param parseString
	 * 			The String that needs to be parsed.
	 */
	private void setIndex(String parseString) {
		logEnterMethod("setIndex");
		try {
			String[] processArray = parseString.split(REGEX_WHITE_SPACES);

			if (processArray.length > 1) {

				if (command.equals(COMMAND_REMOVE)
						|| command.equals(COMMAND_EDIT)
						|| command.equals(COMMAND_MARK)
						|| command.equals(COMMAND_RENAME)
						|| command.equals(COMMAND_UNMARK)) {
					if (taskName == null) {
						if (isInteger(processArray[1])) {
							index = Integer.parseInt(processArray[1]);
							taskNameFlag = true;
						}
					}

				}
			}
		} catch (NullPointerException e) {
			logger.log(Level.FINER, e.getMessage());
			return;
		}catch(ArrayIndexOutOfBoundsException e){
			logger.log(Level.FINER, e.getMessage());
			return;
		}

		logExitMethod("setIndex");
	}

	/**
	 * This is the function to check if the string is an integer.
	 * 
	 * @param printString
	 *            This is the string to be checked.
	 * 
	 * @return Returns true if the string is an int.
	 */
	private boolean isInteger(String printString) {
		logEnterMethod("isInteger");
		try {
			Integer.parseInt(printString);
			logExitMethod("isInteger");
			return true;
		} catch (NumberFormatException e) {
			logger.log(Level.FINER, e.getMessage());
			logExitMethod("isInteger");
			return false;
		}

	}

	/**
	 * This is a function to set up the environment and regex away html tags.
	 */
	private String setEnvironment(String parseString) {
		logEnterMethod("setEnvironment");
		
		setEnvironment();
		parseString = removeHtmlBrackets(parseString);
		
		logExitMethod("setEnvironment");
		return parseString;
		
	}

	/**
	 * This is a function to regex away html tags.
	 */
	private String removeHtmlBrackets(String parseString) {
		logEnterMethod("removeHtmlBrackets");
		parseString = parseString.replaceAll(REGEX_LEFT_BRACER,"");
		parseString = parseString.replaceAll(REGEX_RIGHT_BRACER,"");
		logExitMethod("removeHtmlBrackets");
		return parseString;
	}

	/**
	 * This is a function to set up the environment clearing all parameters and resetting flags.
	 */
	private void setEnvironment() {
		logEnterMethod("setEnvironment");
		dateParser = DateExtractor.getDateExtractor();
		timeParser = TimeExtractor.getTimeExtractor();
		commandExtractor = CommandExtractor.getCommandExtractor();
		nameParser = NameExtractor.getNameExtractor();

		taskNameFlag = false;
		timeFlag = false;
		dateFlag = false;

		command = null;
		taskName = null;
		edittedName = null;

		startDate = null;
		endDate = null;
		startTime = null;
		endTime = null;
		index = 0;

		logExitMethod("setEnvironment");

	}

	/**
	 * This is the function to set the start dates and/or end dates.
	 * 
	 * @param parseString
	 *            Takes in a stringArray.
	 */
	private void setDate(String parseString) {
		logEnterMethod("setDate");
		Queue<LocalDate> dateList;
		dateList = dateParser.processDate(parseString);
		try {
			if (dateList.isEmpty()) {
				logExitMethod("setDate");
				return;
			}
			while (!dateList.isEmpty()) {
				if (!dateFlag) {
					startDate = dateList.poll();
					dateFlag = true;
				} else if (dateFlag) {
					endDate = dateList.poll();
					break;
				}
			}
		} catch (NullPointerException e) {
			logger.log(Level.FINER, e.getMessage());
			return;
		}
		logExitMethod("setDate");
	}

	/**
	 * This is the function to set the start times and/or end times.
	 * 
	 * @param parseString
	 *            Takes in a string array.
	 */
	private void setTime(String parseString) {
		logEnterMethod("setTime");
		Queue<LocalTime> timeList;
		timeList = timeParser.processTime(parseString);
		if (timeList.isEmpty()) {
			logExitMethod("setTime");
			return;
		}
		while (!timeList.isEmpty()) {
			if (!timeFlag) {
				startTime = timeList.poll();
				timeFlag = true;
			} else if (timeFlag) {
				endTime = timeList.poll();
			}
		}
		logExitMethod("setTime");
	}

	/**
	 * This is the function to set the name and/or editted name.
	 * 
	 * @param parseString
	 *            Takes in a string array.
	 */
	private void setName(String parseString) {
		logEnterMethod("setName");
		Queue<String> nameList;
		nameList = nameParser.processName(parseString);
		try {
			if (nameList.isEmpty()) {
				logExitMethod("setName");
				return;
			}
			for (int i = 0; i < 2; i++) {
				if (!taskNameFlag) {
					taskName = nameList.poll();
					taskNameFlag = true;
				} else if (taskNameFlag && !nameList.isEmpty()) {
					edittedName = nameList.poll();
				}
			}
		} catch (NullPointerException e) {
			logger.log(Level.FINER, e.getMessage());
			return;
		}
		logExitMethod("setName");
	}

	/**
	 * This is a function to set the command.
	 * 
	 * @param processArray
	 *            Takes in a string array.
	 */
	private void setCommand(String parseString) {
		logEnterMethod("setCommand");
		command = commandExtractor.setCommand(parseString);
		logExitMethod("setCommand");

	}

	/**
	 * This is a function to set names that are in quotation marks and remove
	 * them from the string.
	 * 
	 * @param process
	 *            This is the string that is input by the user.
	 * 
	 * @return Returns the string with the quotation marks removed.
	 */
	private String setNameInQuotationMarks(String process) {
		logEnterMethod("setNameInQuotationMarks");
		process = nameParser.processQuotationMarks(process);
		logExitMethod("setNameInQuotationMarks");
		return process;

	}

	/**
	 * This is the function to set up the command object with all the parameters
	 * extracted.
	 * 
	 * @param command
	 *            This is the command the user wants to perform.
	 * 
	 * @param taskName
	 *            This is the name of the task.
	 * 
	 * @param edittedName
	 *            This is the name that the user wants to change the task to.
	 * 
	 * @param startDate
	 *            This is the start date of the task.
	 * 
	 * @param startTime
	 *            This is the start time of the task.
	 * 
	 * @param endDate
	 *            This is the end date of the task.
	 * 
	 * @param endTime
	 *            This is the end time of the task.
	 * 
	 * @return Returns an object with all the parameters packaged together.
	 */
	private CommandInfo setUpCommandObject(String command, String taskName,
			String edittedName, LocalDate startDate, LocalTime startTime,
			LocalDate endDate, LocalTime endTime, int index) {
		
		logEnterMethod("setUpCommandObject");
		CommandValidator commandValidator = new CommandValidator();
		CommandInfo object = commandValidator.validateCommand(command,
				taskName, edittedName, startDate, startTime, endDate, endTime,
				index);
		logExitMethod("setUpCommandObject");
		return object;
	}
	
	/**
	 * Logger enter method
	 * 
	 * @param methodName
	 */
	void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}

	/**
	 * Logger enter method
	 * 
	 * @param methodName
	 */
	void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}
}