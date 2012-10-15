package mhs.src;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
/**
 * 
 * @author Cheong Kahou
 * A0086805X
 */

/**
 * This is the class to package the parameters into a command object. 
 */
public class Command {

	/**
	 * This is the enum of the different type of commands. 
	 */
	public enum command{
		add, remove,edit, search, sync,undo;
	}
	
	private String taskName;
	private String edittedName;
	private command commandEnum;
	
	private DateTime startDate;
	private DateTime endDate;

	/**
	 * This is the constructor to set up the command object. 
	 * 
	 * @param commandInput This is the command.
	 * @param taskNameInput This is the name of the task.
	 * @param edittedNameInput This is the editted name of the task.
	 * @param startDateInput This is the start date.
	 * @param startTimeInput This is the start time.
	 * @param endDateInput This is the end date. 
	 * @param endTimeInput This is the end time. 
	 */
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
		
		if(startDate != null && endDate != null){
			if(startDate.isAfter(endDate)){
			DateTime temp = new DateTime();
			temp = startDate; 
			endDate = startDate; 
			startDate = temp;
			}
		}
	}

	/**
	 * Default constructor setting all parameters to null.
	 */
	public Command() {
		commandEnum = null;
		taskName = null;
		startDate = null;
		endDate = null;
		edittedName = null;
	}

	/**
	 * Getter for task name.
	 * @return Returns the task name. 
	 */
	public String getTaskName() {
		return taskName;
	}

	/**
	 * Getter for editted name.
	 * @return Returns the editeed name. 
	 */
	public String getEdittedName() {
		return edittedName;
	}

	/**
	 * Getter for end date.
	 * @return Returns the end date. 
	 */
	public DateTime getEndDate() {
		return endDate;
	}

	/**
	 * Getter for start date.
	 * @return Returns the start date. 
	 */
	public DateTime getStartDate() {
		return startDate;
	}

	/**
	 * Getter for the command.
	 * @return Returns the command. 
	 */
	public command getCommandEnum() {
		return commandEnum;
	}

	
}
