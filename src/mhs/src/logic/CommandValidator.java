package mhs.src.logic;

import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

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

	public CommandInfo validateCommand(String commandInput, String taskNameInput,
			String edittedNameInput, LocalDate startDateInput,
			LocalTime startTimeInput, LocalDate endDateInput,
			LocalTime endTimeInput, int index) {
		logger.entering(getClass().getName(), this.getClass().getName());
		now = DateTime.now();
		for (CommandInfo.CommandKeyWords c : CommandInfo.CommandKeyWords.values()) {
			if (commandInput == c.name()) {
				commandEnum = c;
			}
		}
		taskName = taskNameInput;
		edittedName = edittedNameInput;

		// no start date no start time
		if ((startDateInput == null && startTimeInput == null)) {
			startDate = null;
		}
		// both start date and start time
		if (startDateInput != null && startTimeInput != null) {
			startDate = startDateInput.toDateTime(startTimeInput);
		}
		// only start date
		if (startDateInput != null && startTimeInput == null) {
			startDate = startDateInput.toDateTimeAtStartOfDay();
		}
		// only start time
		if (startDateInput == null && startTimeInput != null) {
			startDateInput = LocalDate.now();
			startDate = startDateInput.toDateTime(startTimeInput);
			if (startDate.isBefore(now)) {
				startDate = startDate.plusDays(1);
			}
		}
		// only end time
		if (endDateInput == null && endTimeInput != null) {
			endDateInput = LocalDate.now();
			endDate = endDateInput.toDateTime(endTimeInput);
			if (endDate.isBefore(now)) {
				endDate = endDate.plusDays(1);
			}
		}
		// no end date no end time
		if (endDateInput == null && endTimeInput == null) {
			endDate = null;
		}
		// both end date and end time
		if (endDateInput != null && endTimeInput != null) {
			endDate = endDateInput.toDateTime(endTimeInput);
		}
		// only end date
		if (endDateInput != null && endTimeInput == null) {
			endDate = endDateInput.toDateTimeAtStartOfDay();
		}
		// ensure start date is before end date.
		if (startDate != null && endDate != null) {
			if (startDate.isAfter(endDate)) {
				DateTime temp = new DateTime();
				temp = endDate;
				endDate = startDate;
				startDate = temp;
			}
		}

		checkParameters();
		command = new CommandInfo(commandEnum, taskName, edittedName, startDate,
				endDate, index);
		logger.exiting(getClass().getName(), this.getClass().getName());
		return command;
	}

	private void checkParameters() {
		logger.entering(getClass().getName(), this.getClass().getName());
		if (commandEnum == CommandInfo.CommandKeyWords.add) {
			if(taskName == null){
				enforceTaskName();
			}
		}
		if (commandEnum == CommandInfo.CommandKeyWords.edit) {
			if(index == 0 && taskName == null){
				enforceTaskName();
			}
		}
		if (commandEnum == CommandInfo.CommandKeyWords.help) {
			clearAllParameters();
		}
		if (commandEnum == CommandInfo.CommandKeyWords.login) {
			clearAllParameters();
		}
		if (commandEnum == CommandInfo.CommandKeyWords.logout) {
			clearAllParameters();
		}
		if (commandEnum == CommandInfo.CommandKeyWords.mark) {
			if(index == 0){
				enforceTaskName();
			}
			edittedName = null;
			startDate = null;
			endDate = null;		

		}
		if (commandEnum == CommandInfo.CommandKeyWords.redo) {
			clearAllParameters();
		}
		if (commandEnum == CommandInfo.CommandKeyWords.remove) {
			if(index==0){
				enforceTaskName();
			}
			edittedName = null;
			startDate = null;
			endDate = null;		

		}
		if (commandEnum == CommandInfo.CommandKeyWords.rename) {
			if(taskName == null && index == 0){
				enforceTaskName();
				}
			startDate = null;
			endDate = null;		
		}
		if (commandEnum == CommandInfo.CommandKeyWords.search) {
			if(index==0){
				enforceTaskName();
				}
			if(endDate == null && startDate != null){
				endDate = startDate.plusDays(1);
			}
			edittedName = null;
		}
		if (commandEnum == CommandInfo.CommandKeyWords.sync) {
			clearAllParameters();
		}
		if (commandEnum == CommandInfo.CommandKeyWords.undo) {
			clearAllParameters();
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
	}


	private void enforceTaskName() {
		logger.entering(getClass().getName(), this.getClass().getName());
		taskName = "<Default task>";
		logger.exiting(getClass().getName(), this.getClass().getName());
	}


	private void clearAllParameters() {
		logger.entering(getClass().getName(), this.getClass().getName());
		taskName = null;
		edittedName = null;
		startDate = null;
		endDate = null;
		index = 0;
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

}
