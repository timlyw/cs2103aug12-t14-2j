package mhs.src.logic;

import java.util.Queue;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 * 
 * This is a class to parse strings and extract out appropriate parameters.
 */
public class CommandParser {

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

	private int index;
	private static CommandParser commandParser;
	private CommandParser(){
		setEnvironment();
	}
	
	public static CommandParser getCommandParser(){
		if(commandParser == null){
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
	public Command getParsedCommand(String parseString) {

		setEnvironment();

		parseString = setNameInQuotationMarks(parseString);

		setCommand(parseString);
		setTime(parseString);
		setDate(parseString);
		validateParameters(parseString);
		setName(parseString);

		return setUpCommandObject(command, taskName, edittedName, startDate,
				startTime, endDate, endTime, index);

	}

	private void validateParameters(String parseString) {
		String[] processArray = parseString.split("\\s+");
		if (processArray.length > 1) {
			if (command.equals("remove") || command.equals("edit")) {
				if (taskName == null) {
					if (isInteger(processArray[1])) {
						index = Integer.parseInt(processArray[1]);
						taskNameFlag = true;
					}
				}

			}
		}

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
		try {
			Integer.parseInt(printString);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * This is a function to set up the environment and set values to null.
	 */
	private void setEnvironment() {
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
	}

	/**
	 * This is the function to set the start dates and/or end dates.
	 * 
	 * @param parseString
	 *            Takes in a stringArray.
	 */
	private void setDate(String parseString) {

		Queue<LocalDate> dateList;
		dateList = dateParser.processDate(parseString);
		if (dateList.isEmpty()) {
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

	}

	/**
	 * This is the function to set the start times and/or end times.
	 * 
	 * @param parseString
	 *            Takes in a string array.
	 */
	private void setTime(String parseString) {
		Queue<LocalTime> timeList;
		timeList = timeParser.processTime(parseString);
		if (timeList.isEmpty()) {
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
	}

	/**
	 * This is the function to set the name and/or editted name.
	 * 
	 * @param parseString
	 *            Takes in a string array.
	 */
	private void setName(String parseString) {

		Queue<String> nameList;
		nameList = nameParser.processName(parseString);
		if (nameList.isEmpty()) {
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
	}

	/**
	 * This is a function to set the command.
	 * 
	 * @param processArray
	 *            Takes in a string array.
	 */
	private void setCommand(String parseString) {

		command = commandExtractor.setCommand(parseString);

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

		process = nameParser.processNameWithinQuotationMarks(process);
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
	private Command setUpCommandObject(String command, String taskName,
			String edittedName, LocalDate startDate, LocalTime startTime,
			LocalDate endDate, LocalTime endTime, int index) {

		Command object = new Command(command, taskName, edittedName, startDate,
				startTime, endDate, endTime, index);
		return object;
	}
}