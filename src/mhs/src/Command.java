package mhs.src;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class Command {

	public enum command{
		add, remove,edit, search, sync,undo;
	}
	
	private String taskName;
	private String edittedName;
	private command commandEnum;
	
	private DateTime startDate;
	private DateTime endDate;

	public Command(String commandInput, String taskNameInput,
			String edittedNameInput, LocalDate startDateInput,
			LocalTime startTimeInput, LocalDate endDateInput,
			LocalTime endTimeInput) {
		for(command c: command.values()){
			if(commandInput == c.name()){
				commandEnum = c;
			}
		}
		taskName = taskNameInput;
		edittedName = edittedNameInput;


		if ((startDateInput == null && startTimeInput == null)) {
			startDate = null;
		}
		if (startDateInput != null && startTimeInput!=null) {
			startDate = startDateInput.toDateTime(startTimeInput);
		}
		if(startDateInput !=null && startTimeInput==null){
			startDate = startDateInput.toDateTimeAtStartOfDay();
		}
		if(startDateInput == null && startTimeInput != null){
			startDateInput = LocalDate.now();
			startDate = startDateInput.toDateTime(startTimeInput);
		}
		
		if(endDateInput == null && endTimeInput != null){
			endDateInput = LocalDate.now();
			endDate = endDateInput.toDateTime(endTimeInput);
		}
		if (endDateInput == null && endTimeInput == null) {
			endDate = null;
		}
		
		if (endDateInput != null && endTimeInput != null) {
			endDate = endDateInput.toDateTime(endTimeInput);
		}
		if(endDateInput !=null && endTimeInput==null){
			endDate = endDateInput.toDateTimeAtStartOfDay();
		}

	}

	public Command() {
		commandEnum = null;
		taskName = null;
		startDate = null;
		endDate = null;
		edittedName = null;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getEdittedName() {
		return edittedName;
	}

	public void setEdittedName(String edittedName) {
		this.edittedName = edittedName;
	}

	public DateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(DateTime endDate) {
		this.endDate = endDate;
	}

	public DateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(DateTime startDate) {
		this.startDate = startDate;
	}

	public command getCommandEnum() {
		return commandEnum;
	}

	public void setCommandEnum(command commandEnum) {
		this.commandEnum = commandEnum;
	}
	
	
}
