package mhs.src.logic;

import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

import org.joda.time.DateTime;

/**
 * 
 * @author Cheong Kahou
 * A0086805X
 */

/**
 * This is the class to package the parameters into a command object. 
 */
public class CommandInfo {

	/**
	 * This is the enum of the different type of commands. 
	 */
	public static enum CommandKeyWords{
		add, remove,edit, search, sync, undo, login, logout, rename, redo, mark, help, unmark, p, n, displayfloating, displaydeadline, displaytimed;
	}
	
	private String taskName;
	private String edittedName;
	private CommandKeyWords commandEnum;
	
	private DateTime startDate;
	private DateTime endDate;
	private int index;
	
	private static final Logger logger = MhsLogger.getLogger();
	
	

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
	public CommandInfo(CommandKeyWords commandInput, String taskNameInput,
			String edittedNameInput, DateTime startDateInput, DateTime endDateInput, int indexInput) {
		logger.entering(getClass().getName(), this.getClass().getName());
		commandEnum = commandInput;
		taskName = taskNameInput;
		edittedName = edittedNameInput;
		index = indexInput;
		startDate = startDateInput;
		endDate = endDateInput;
		System.out.println(toString());
		logger.exiting(getClass().getName(), this.getClass().getName());
		
	}

	/**
	 * Default constructor setting all parameters to null.
	 */
	public CommandInfo() {
		logger.entering(getClass().getName(), this.getClass().getName());
		commandEnum = null;
		taskName = null;
		startDate = null;
		endDate = null;
		edittedName = null;
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	/**
	 * Getter for task name.
	 * @return Returns the task name. 
	 */
	public String getTaskName() {
		logger.entering(getClass().getName(), this.getClass().getName());
		logger.exiting(getClass().getName(), this.getClass().getName());
		return taskName;
	}

	/**
	 * Getter for editted name.
	 * @return Returns the editeed name. 
	 */
	public String getEdittedName() {
		logger.entering(getClass().getName(), this.getClass().getName());
		logger.exiting(getClass().getName(), this.getClass().getName());
		return edittedName;
	}

	/**
	 * Getter for end date.
	 * @return Returns the end date. 
	 */
	public DateTime getEndDate() {
		logger.entering(getClass().getName(), this.getClass().getName());
		logger.exiting(getClass().getName(), this.getClass().getName());
		return endDate;
	}

	/**
	 * Getter for start date.
	 * @return Returns the start date. 
	 */
	public DateTime getStartDate() {
		logger.entering(getClass().getName(), this.getClass().getName());
		logger.exiting(getClass().getName(), this.getClass().getName());
		return startDate;
	}

	/**
	 * Getter for the command.
	 * @return Returns the command. 
	 */
	public CommandKeyWords getCommandEnum() {
		logger.entering(getClass().getName(), this.getClass().getName());
		logger.exiting(getClass().getName(), this.getClass().getName());
		return commandEnum;
	}

	public int getIndex(){
		logger.entering(getClass().getName(), this.getClass().getName());
		logger.exiting(getClass().getName(), this.getClass().getName());
		return index;
	}
	public String toString(){
		logger.entering(getClass().getName(), this.getClass().getName());
	
		
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
		logger.exiting(getClass().getName(), this.getClass().getName());
		return outString;
	}
	
	public boolean isEqual(CommandInfo commandInfo1, CommandInfo commandInfo2){
		if(commandInfo1.commandEnum != commandInfo2.commandEnum){
			System.out.println("CommanEnum Error! " + commandInfo1.commandEnum.name() + " " + commandInfo2.commandEnum.name());
			return false;
		}
		if(commandInfo1.taskName != commandInfo1.taskName){
			System.out.println("Task Name Error! " + commandInfo1.taskName + " " + commandInfo2.taskName);
			return false;
		}
		if(commandInfo1.edittedName != commandInfo1.edittedName){
			System.out.println("Editted Name Error! " + commandInfo1.edittedName + " " + commandInfo2.edittedName);
			return false;
		}
		if(commandInfo1.startDate != commandInfo1.startDate){
			System.out.println("Start Date Error! " + commandInfo1.startDate.toString() + " " + commandInfo2.startDate.toString());
			return false;
		}
		if(commandInfo1.endDate != commandInfo1.endDate){
			System.out.println("End Date Error! " + commandInfo1.endDate.toString() + " " + commandInfo2.endDate.toString());
			return false;
		}
		if(commandInfo1.index != commandInfo1.index){
			System.out.println("Index Error! " + commandInfo1.index + " " + commandInfo2.index);
			return false;
		}
	
		return true;
	}
	
}
