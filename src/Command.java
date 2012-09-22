import org.joda.time.DateTime;


public class Command {
	// add enum for command

	private String command;
	private String taskName;
	private String edittedName;

	private static DateTime startDate;
	private static DateTime endDate;

	public Command(String commandInput, String taskNameInput,
			String edittedNameInput, DateTime startDateInput,
			DateTime startTimeInput, DateTime endDateInput,
			DateTime endTimeInput) {


	}

	public Command() {
		setCommand(null);
		setTaskName(null);
		setStartDate(null);
		setEndDate(null);
		setEdittedName(null);
	}


	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
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

	public static DateTime getEndDate() {
		return endDate;
	}

	public static void setEndDate(DateTime endDate) {
		Command.endDate = endDate;
	}

	public static DateTime getStartDate() {
		return startDate;
	}

	public static void setStartDate(DateTime startDate) {
		Command.startDate = startDate;
	}
	
	

}
