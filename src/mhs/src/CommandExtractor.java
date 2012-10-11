package mhs.src;

public class CommandExtractor {

	enum commands {
		add("add"), remove("remove"), delete("remove"), update("edit"), edit(
				"edit"), postpone("edit"), search("search"), find("search"), display("search"), sync(
				"sync"), undo("undo");

		private final String command;

		commands(String command) {
			this.command = command;
		}

	}

	private String commandString;

	public boolean isCommand(String printString){
		for (commands c : commands.values()) {
			if (printString.equalsIgnoreCase(c.name())) {
				return true;
			}
		}
			return false;
	}
		
	public String getCommand(String printString) {
		for (commands c : commands.values()) {
			if (printString.equalsIgnoreCase(c.name())) {
				commandString = c.command;
				return commandString;
			}
		}
		commandString = "add";
		return commandString;
	}
}
