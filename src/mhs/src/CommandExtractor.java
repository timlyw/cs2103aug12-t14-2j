package mhs.src;

/**
 * 
 * @author Cheong Kahou
 *A0086805X
 *
 */

/**
 * 
 * This is a class that checks string if they are commands and set the commands.
 * 
 */
public class CommandExtractor {

	/**
	 * 
	 * These are the enum commands that are used and the different keywords the
	 * user may enter.
	 */
	enum commands {
		add("add"), remove("remove"), delete("remove"), update("edit"), edit(
				"edit"), postpone("edit"), search("search"), find("search"), display(
				"search"), sync("sync"), undo("undo"), redo("redo"), rename(
				"rename"), login("login"), logout("logout"), help("help");

		private final String command;

		commands(String command) {
			this.command = command;
		}

	}

	private String commandString;

	/**
	 * This is a function to check if a string is a command.
	 * 
	 * @param printString
	 *            This is the string to be checked.
	 * 
	 * @return Returns if the string is a command type.
	 */
	public boolean isCommand(String printString) {
		for (commands c : commands.values()) {
			if (printString.equalsIgnoreCase(c.name())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This is the function to set the command.
	 * 
	 * @param printString
	 *            This the command String to be set.
	 * 
	 * @return Returns the command that is set.
	 */
	public String setCommand(String printString) {
		for (commands c : commands.values()) {
			if (printString.equalsIgnoreCase(c.name())) {
				commandString = c.command;
			}
		}
		return commandString;
	}
}
