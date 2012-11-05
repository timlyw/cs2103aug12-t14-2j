//@author A0086805X
package mhs.src.logic;

import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 * This is the class that validates the parameters according to what the commands were given
 * and ensure that the dates given are correct. 
 *
 */
public class CommandValidator {

	private String taskName;
	private String edittedName;
	private CommandInfo.CommandKeyWords commandEnum;

	private DateTime startDate;
	private DateTime endDate;
	private DateTime now;

	private static final Logger logger = MhsLogger.getLogger();
	private int index;
	private CommandInfo command;

	/**
	 * This is the method to validate commandInfo parameters and sets up CommandInfo.
	 *  
	 * @param commandInput
	 * @param taskNameInput
	 * @param edittedNameInput
	 * @param startDateInput
	 * @param startTimeInput
	 * @param endDateInput
	 * @param endTimeInput
	 * @param index
	 * 
	 * @return Returns CommandInfo.
	 */
	public CommandInfo validateCommand(String commandInput, String taskNameInput,
			String edittedNameInput, LocalDate startDateInput,
			LocalTime startTimeInput, LocalDate endDateInput,
			LocalTime endTimeInput, int index) {
		
		logEnterMethod("validateCommand");

		now = DateTime.now();
		for (CommandInfo.CommandKeyWords c : CommandInfo.CommandKeyWords.values()) {
			if (commandInput == c.name()) {
				commandEnum = c;
			}
		}
		taskName = taskNameInput;
		edittedName = edittedNameInput;

		validateStartDate(startDateInput, startTimeInput);
		validateEndDate(endDateInput, endTimeInput);
		validateTiming(startDateInput, startTimeInput, endDateInput,
				endTimeInput);	
		validateStartDateIsBeforeEndDate();

		checkParameters();
		command = new CommandInfo(commandEnum, taskName, edittedName, startDate,
				endDate, index);

		logExitMethod("validateCommand");
		return command;
	}

	/**
	 * This is a method to validate if just a timing without a date was entered. 
	 * 
	 * @param startDateInput
	 * @param startTimeInput
	 * @param endDateInput
	 * @param endTimeInput
	 */
	private void validateTiming(LocalDate startDateInput,
			LocalTime startTimeInput, LocalDate endDateInput,
			LocalTime endTimeInput) {
		
		logEnterMethod("validateTiming");
		validateNoStartDateStartTime(startDateInput, startTimeInput);
		validateNoEndDateEndTime(endDateInput, endTimeInput);
		logExitMethod("validateTiming");
	}

	/**
	 * This is a method to validate inputs with an endDate.
	 * 
	 * @param endDateInput
	 * @param endTimeInput
	 */
	private void validateEndDate(LocalDate endDateInput, LocalTime endTimeInput) {
		
		logEnterMethod("validateEndDate");
		validateNoEndDateNoEndTime(endDateInput, endTimeInput);
		validateEndDateAndEndTime(endDateInput, endTimeInput);
		validateEndDateNoEndTime(endDateInput, endTimeInput);
		logExitMethod("validateEndDate");
	}

	/**
	 * This is a method to validate inputs with a startDate.
	 * 
	 * @param startDateInput
	 * @param startTimeInput
	 */
	private void validateStartDate(LocalDate startDateInput,
			LocalTime startTimeInput) {
		
		logEnterMethod("validateStartDate");
		validateNoStartDateNoStartTime(startDateInput, startTimeInput);
		validateStartDateStartTime(startDateInput, startTimeInput);
		validateStartDateNoStartTime(startDateInput, startTimeInput);
		
		logExitMethod("validateStartDate");
	}

	/**
	 * This is a method to ensure that startDate is always before endDate.
	 */
	private void validateStartDateIsBeforeEndDate() {
		
		logEnterMethod("validateStartDateIsBeforeEndDate");
		if (startDate != null && endDate != null) {
			if (startDate.isAfter(endDate)) {
				DateTime temp = new DateTime();
				temp = endDate;
				endDate = startDate;
				startDate = temp;
			}
		}
		logExitMethod("validateStartDateIsBeforeEndDate");
	}

	/**
	 * This is a method to validate an input with no end date and with end time.
	 * 
	 * @param endDateInput
	 * @param endTimeInput
	 */
	private void validateNoEndDateEndTime(LocalDate endDateInput,
			LocalTime endTimeInput) {
		
		logEnterMethod("validateNoEndDateEndTime");
		if (endDateInput == null && endTimeInput != null) {
			endDateInput = LocalDate.now();
			endDate = endDateInput.toDateTime(endTimeInput);
			if (endDate.isBefore(now)) {
				endDate = endDate.plusDays(1);
			}
		}
		logExitMethod("validateNoEndDateEndTime");
	}

	/**
	 * This is a method to validate an input with no start date and with start time.
	 * 
	 * @param startDateInput
	 * @param startTimeInput
	 */
	private void validateNoStartDateStartTime(LocalDate startDateInput,
			LocalTime startTimeInput) {
		
		logEnterMethod("validateNoStartDateStartTime");
		if (startDateInput == null && startTimeInput != null) {
			startDateInput = LocalDate.now();
			startDate = startDateInput.toDateTime(startTimeInput);
			if (startDate.isBefore(now)) {
				startDate = startDate.plusDays(1);
			}
		}
		
		logExitMethod("validateNoStartDateStartTime");
	}

	/**
	 * This is a method to validate an input with an end date and no end time.
	 * 
	 * @param endDateInput
	 * @param endTimeInput
	 */
	private void validateEndDateNoEndTime(LocalDate endDateInput,
			LocalTime endTimeInput) {
		
		logEnterMethod("validateEndDateNoEndTime");
		if (endDateInput != null && endTimeInput == null) {
			endDate = endDateInput.toDateTimeAtStartOfDay();
		}
		logExitMethod("validateEndDateNoEndTime");
	}

	/**
	 * This is a method to validate an input with an end date and end time.
	 * 
	 * @param endDateInput
	 * @param endTimeInput
	 */
	private void validateEndDateAndEndTime(LocalDate endDateInput,
			LocalTime endTimeInput) {
		
		logEnterMethod("validateEndDateAndEndTime");
		if (endDateInput != null && endTimeInput != null) {
			endDate = endDateInput.toDateTime(endTimeInput);
		}
		logExitMethod("validateEndDateAndEndTime");
	}

	/**
	 * This is a method to validate an input with no end date and no end time
	 * 
	 * @param endDateInput
	 * @param endTimeInput
	 */
	private void validateNoEndDateNoEndTime(LocalDate endDateInput,
			LocalTime endTimeInput) {
		
		logEnterMethod("validateNoEndDateNoEndTime");
		if (endDateInput == null && endTimeInput == null) {
			endDate = null;
		}
		logExitMethod("validateNoEndDateNoEndTime");
	}

	/**
	 * This is a method to validate an input with no start date and no start time.
	 * 
	 * @param startDateInput
	 * @param startTimeInput
	 */
	private void validateStartDateNoStartTime(LocalDate startDateInput,
			LocalTime startTimeInput) {
		
		logEnterMethod("validateStartDateNoStartTime");
		if (startDateInput != null && startTimeInput == null) {
			startDate = startDateInput.toDateTimeAtStartOfDay();
		}
		logExitMethod("validateStartDateNoStartTime");
	}

	/**
	 * This is a method to validate an input with start date and start time.
	 * 
	 * @param startDateInput
	 * @param startTimeInput
	 */
	private void validateStartDateStartTime(LocalDate startDateInput,
			LocalTime startTimeInput) {
		
		logEnterMethod("validateStartDateStartTime");
		if (startDateInput != null && startTimeInput != null) {
			startDate = startDateInput.toDateTime(startTimeInput);
		}
		logExitMethod("validateStartDateStartTime");
	}

	/**
	 * This is a method to validate an input with no start date and no start time.
	 * 
	 * @param startDateInput
	 * @param startTimeInput
	 */
	private void validateNoStartDateNoStartTime(LocalDate startDateInput,
			LocalTime startTimeInput) {
		
		logEnterMethod("validateNoStartDateNoStartTime");
		if ((startDateInput == null && startTimeInput == null)) {
			startDate = null;
		}
		logExitMethod("validateNoStartDateNoStartTime");
	}

	/**
	 * This is a method to ensure the parameters parsed match the commands given. 
	 */
	private void checkParameters() {
		logEnterMethod("validateNoStartDateNoStartTime");
		if (commandEnum == CommandInfo.CommandKeyWords.add) {
			if(taskName == null && startDate != null){
				commandEnum = CommandInfo.CommandKeyWords.search;
			}
			else if(taskName == null){
				enforceTaskName();
			}

		}
		if (commandEnum == CommandInfo.CommandKeyWords.mark ||
			commandEnum == CommandInfo.CommandKeyWords.unmark ||
			commandEnum == CommandInfo.CommandKeyWords.remove){
			clearParameterExceptNameAndIndex();		
		}
		
		if (commandEnum == CommandInfo.CommandKeyWords.help ||
			commandEnum == CommandInfo.CommandKeyWords.login ||
			commandEnum == CommandInfo.CommandKeyWords.logout ||
			commandEnum == CommandInfo.CommandKeyWords.redo ||
			commandEnum == CommandInfo.CommandKeyWords.sync ||
			commandEnum == CommandInfo.CommandKeyWords.home ||
			commandEnum == CommandInfo.CommandKeyWords.exit){
			clearAllParameters();
		}
		
	
		if (commandEnum == CommandInfo.CommandKeyWords.rename) {
			startDate = null;
			endDate = null;		
		}
		if (commandEnum == CommandInfo.CommandKeyWords.search) {
			enforceDateRange();
			edittedName = null;
		}

		logExitMethod("validateNoStartDateNoStartTime");
	}

	/**
	 * This is a method to enforce a date range
	 */
	private void enforceDateRange() {
		logEnterMethod("enforceDateRange");
		if(endDate == null && startDate != null){
			endDate = startDate.plusDays(1);
		}
		logExitMethod("enforceDateRange");
	}
	

	/**
	 * This is a method to clear everthing except the command name and index
	 */
	private void clearParameterExceptNameAndIndex() {
		logEnterMethod("clearParameterExceptNameAndIndex");
		edittedName = null;
		startDate = null;
		endDate = null;
		logExitMethod("clearParameterExceptNameAndIndex");
	}


	/**
	 * This is the method to enforce a default task name.
	 */
	private void enforceTaskName() {
		logEnterMethod("enforceTaskName");
		taskName = "Default task";
		logExitMethod("enforceTaskName");
	}


	/**
	 * This is a method to clear all the parameters except the command. 
	 */
	private void clearAllParameters() {
		logEnterMethod("clearAllParameters");
		taskName = null;
		clearParameterExceptNameAndIndex();
		index = 0;
		logExitMethod("clearAllParameters");
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
