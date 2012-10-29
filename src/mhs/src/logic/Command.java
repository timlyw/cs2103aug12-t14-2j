package mhs.src.logic;

import org.joda.time.DateTime;

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
	public static enum CommandKeyWords{
		add, remove,edit, search, sync, undo, login, logout, rename, redo, mark, help;
	}
	
	private String taskName;
	private String edittedName;
	private CommandKeyWords commandEnum;
	
	private DateTime startDate;
	private DateTime endDate;

	
	private int index;

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
	public Command(CommandKeyWords commandInput, String taskNameInput,
			String edittedNameInput, DateTime startDateInput, DateTime endDateInput, int indexInput) {
	
		commandEnum = commandInput;
		taskName = taskNameInput;
		edittedName = edittedNameInput;
		index = indexInput;
		startDate = startDateInput;
		endDate = endDateInput;
		System.out.println(toString());
		
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
	public CommandKeyWords getCommandEnum() {
		return commandEnum;
	}

	public String toString(){
		
		String outString = "";
		if(commandEnum!= null)
			outString = ("Command : " + commandEnum.name());
		if(taskName!= null)
			outString +=(" Task name : " + taskName);
		if(edittedName!= null)
			outString +=(" Editted name : " + edittedName);
		if(startDate!= null)
			outString += (" Start Date : " + startDate.toString());
		if(endDate!= null)
			outString += (" End Date : " + endDate.toString());
		outString += (" Index is : " + index);
		
		return outString;
	}
	
}
