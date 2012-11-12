//@author A0086805X
package mhs.src.logic.parser;

import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.logic.CommandInfo;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 * This is the class that validates the parameters according to what the
 * commands were given and ensure that the dates given are correct.
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
	private boolean searchStartDateFlag;

	/**
	 * This is the method to validate commandInfo parameters and sets up
	 * CommandInfo.
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
	public CommandInfo validateCommand(String commandInput,
			String taskNameInput, String edittedNameInput,
			LocalDate startDateInput, LocalTime startTimeInput,
			LocalDate endDateInput, LocalTime endTimeInput, int indexInput) {

		logEnterMethod("validateCommand");

		searchStartDateFlag = false;

		taskName = null;
		edittedName = null;
		startDate = null;
		endDate = null;
		index = 0;
		
		now = DateTime.now();
		setCommandEnum(commandInput);
		taskName = taskNameInput;
		edittedName = edittedNameInput;
		index = indexInput;

		validateDateTime(startDateInput, startTimeInput, endDateInput,
				endTimeInput);

		checkParameters();
		command = new CommandInfo(commandEnum, taskName, edittedName,
				startDate, endDate, index);

		logExitMethod("validateCommand");
		return command;
	}

	/**
	 * Method to validate and default all date time parameters.
	 * 
	 * @param startDateInput
	 * @param startTimeInput
	 * @param endDateInput
	 * @param endTimeInput
	 */
	private void validateDateTime(LocalDate startDateInput,
			LocalTime startTimeInput, LocalDate endDateInput,
			LocalTime endTimeInput) {

		validateStartDate(startDateInput, startTimeInput);
		validateEndDate(endDateInput, endTimeInput);
		validateTiming(startDateInput, startTimeInput, endDateInput,
				endTimeInput);
		validateStartDateIsBeforeEndDate();
		logExitMethod("validateDateTime");

	}

	/**
	 * Method to set the command enumeration.
	 * 
	 * @param commandInput
	 */
	private void setCommandEnum(String commandInput) {
		logEnterMethod("setCommandEnum");
		for (CommandInfo.CommandKeyWords c : CommandInfo.CommandKeyWords
				.values()) {
			if (commandInput == c.name()) {
				commandEnum = c;
			}
		}
		logExitMethod("setCommandEnum");
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
		validateNoEndDateEndTime(startDateInput, endDateInput, endTimeInput);
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
	private void validateNoEndDateEndTime(LocalDate startDateInput,
			LocalDate endDateInput, LocalTime endTimeInput) {

		logEnterMethod("validateNoEndDateEndTime");
		if (endDateInput == null && endTimeInput != null) {
			if (startDateInput != null) {
				endDateInput = startDateInput;
			}
			else{
				endDateInput = LocalDate.now();
			}
			endDate = endDateInput.toDateTime(endTimeInput);

			if (endDate.isBefore(now)) {
				endDate = endDate.plusDays(1);
			}
		}
		logExitMethod("validateNoEndDateEndTime");
	}

	/**
	 * This is a method to validate an input with no start date and with start
	 * time.
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
			endTimeInput = new LocalTime(23, 59);
			endDate = endDateInput.toDateTime(endTimeInput);
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
	 * This is a method to validate an input with no start date and no start
	 * time.
	 * 
	 * @param startDateInput
	 * @param startTimeInput
	 */
	private void validateStartDateNoStartTime(LocalDate startDateInput,
			LocalTime startTimeInput) {

		logEnterMethod("validateStartDateNoStartTime");
		if (startDateInput != null && startTimeInput == null) {
			startTimeInput = new LocalTime(23, 59);
			startDate = startDateInput.toDateTime(startTimeInput);
			searchStartDateFlag = true;
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
	 * This is a method to validate an input with no start date and no start
	 * time.
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
	 * This is a method to ensure the parameters parsed match the commands
	 * given.
	 */
	private void checkParameters() {
		logEnterMethod("checkParameters");
		validateAddParameters();
		validateCommandsWithNameAndIndexParameters();
		validateCommandWithNoParameters();
		validateCommandsWithNoDateTIme();
		validateSearchParameters();
		logExitMethod("checkParameters");
	}

	/**
	 * Method to ensure search parameters
	 */
	private void validateSearchParameters() {
		logEnterMethod("validateSearchParameters");
		if (commandEnum == CommandInfo.CommandKeyWords.search) {
			setStartDateToStartOfDay();
			enforceDateRange();
			edittedName = null;
		}
		logExitMethod("validateSearchParameters");
	}

	/**
	 * Method that sets the start date to start of date if a start time was not
	 * originally input.
	 */
	private void setStartDateToStartOfDay() {
		logEnterMethod("setStartDateToStartOfDay");
		if (searchStartDateFlag == true) {
			startDate = startDate.minusHours(23).minusMinutes(59);
		}
		logExitMethod("setStartDateToStartOfDay");
	}

	/**
	 * Method that removes the start date and end date parameters for rename.
	 */
	private void validateCommandsWithNoDateTIme() {
		logEnterMethod("validateCommandsWithNoDateTIme");
		if (commandEnum == CommandInfo.CommandKeyWords.rename) {
			startDate = null;
			endDate = null;
		}
		logExitMethod("validateCommandsWithNoDateTIme");
	}

	/**
	 * Method to validate commands that expect no parameters.
	 */
	private void validateCommandWithNoParameters() {
		logEnterMethod("validateCommandWithNoParameters");
		if (isCommandWithNoParameters()) {
			clearAllParameters();
		}
		logExitMethod("validateCommandWithNoParameters");
	}

	/**
	 * Method that checks if the command should have no other parameters.
	 * 
	 * @return
	 */
	private boolean isCommandWithNoParameters() {
		logEnterMethod("isCommandWithNoParameters");
		logExitMethod("isCommandWithNoParameters");
		return commandEnum == CommandInfo.CommandKeyWords.help
				|| commandEnum == CommandInfo.CommandKeyWords.login
				|| commandEnum == CommandInfo.CommandKeyWords.logout
				|| commandEnum == CommandInfo.CommandKeyWords.redo
				|| commandEnum == CommandInfo.CommandKeyWords.sync
				|| commandEnum == CommandInfo.CommandKeyWords.home
				|| commandEnum == CommandInfo.CommandKeyWords.previous
				|| commandEnum == CommandInfo.CommandKeyWords.next
				|| commandEnum == CommandInfo.CommandKeyWords.floating
				|| commandEnum == CommandInfo.CommandKeyWords.deadline
				|| commandEnum == CommandInfo.CommandKeyWords.timed
				|| commandEnum == CommandInfo.CommandKeyWords.exit
				|| commandEnum == CommandInfo.CommandKeyWords.hide;
	}

	/**
	 * Method for commands that expect no parameter other than name or index.
	 */
	private void validateCommandsWithNameAndIndexParameters() {
		logEnterMethod("validateCommandsWithNameAndIndexParameters");
		if (isCommandWithNameOrIndexParameters()) {
			clearParameterExceptNameAndIndex();
		}
		logExitMethod("validateCommandsWithNameAndIndexParameters");
	}

	/**
	 * Method that checks if the command should only have a name or index
	 * parameter.
	 * 
	 * @return
	 */
	private boolean isCommandWithNameOrIndexParameters() {
		logEnterMethod("isCommandWithNameOrIndexParameters");
		logExitMethod("isCommandWithNameOrIndexParameters");
		return commandEnum == CommandInfo.CommandKeyWords.mark
				|| commandEnum == CommandInfo.CommandKeyWords.unmark
				|| commandEnum == CommandInfo.CommandKeyWords.remove;
	}

	/**
	 * Method to check that the parameters for add is correct.
	 */
	private void validateAddParameters() {
		logEnterMethod("validateAddParameters");
		if (commandEnum == CommandInfo.CommandKeyWords.add) {
			appendEdittedNameToTaskName();
			changeCommandToSearch();
		}
		logExitMethod("validateAddParameters");
	}

	/**
	 * Method to change default command to search if only a date time was input.
	 */
	private void changeCommandToSearch() {
		logEnterMethod("changeCommandToSearch");
		if (taskName == null && startDate != null) {
			commandEnum = CommandInfo.CommandKeyWords.search;
		}
		logExitMethod("changeCommandToSearch");
	}

	/**
	 * Method to append the editted name to the start name.
	 */
	private void appendEdittedNameToTaskName() {
		logEnterMethod("appendEdittedNameToTaskName");
		if (edittedName != null) {
			taskName = taskName + " " + edittedName;
			edittedName = null;
		}
		logExitMethod("appendEdittedNameToTaskName");
	}

	/**
	 * This is a method to enforce a date range
	 */
	private void enforceDateRange() {
		logEnterMethod("enforceDateRange");
		if (endDate == null && startDate != null) {
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
