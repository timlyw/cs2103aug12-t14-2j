package mhs.src.logic;

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
	enum CommandKeyWord {
		add("add"), remove("remove"), delete("remove"), update("edit"), edit(
				"edit"), postpone("edit"), search("search"), find("search"), display(
				"search"), sync("sync"), undo("undo"), redo("redo"), rename(
				"rename"), login("login"), signin("login"), logout("logout"), signout(
				"logout"), help("help"), mark("mark"), check("mark");

		private final String command;

		CommandKeyWord(String command) {
			this.command = command;
		}

	}

	private static final String REGEX_WHITE_SPACE = "\\s+";
	private String commandString;

	/**
	 * This is a function to check if a string is a command.
	 * 
	 * @param printString
	 *            This is the string to be checked.
	 * 
	 * @return Returns if the string is a command type.
	 */
	public boolean isCommand(String parseString) {
		for (CommandKeyWord c : CommandKeyWord.values()) {
			if (parseString.equalsIgnoreCase(c.name())) {
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
	public String setCommand(String parseString) {
		String[] processArray = parseString.split(REGEX_WHITE_SPACE);
		if (isCommand(processArray[0])) {
			for (CommandKeyWord c : CommandKeyWord.values()) {
				if (processArray[0].equalsIgnoreCase(c.name())) {
					commandString = c.command;
				}
			}
		}
		else {
			commandString = CommandKeyWord.add.name();
		}
		return commandString;
	}
}
