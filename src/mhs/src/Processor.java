package mhs.src;

import java.util.List;

public class Processor {

	private Command userCommand;
	private List<Task> matchedTasks;
	private CommandParser commandParser;

	public String getCommandFeedback(String command) {
		return "Command feedback for " + command;
	}

	public String executeCommand(String command) {
		Command userCommand = commandParser.getParsedCommand(command);
		String screenOutput = processCommand(userCommand);
		return screenOutput;
	}

	private String processCommand(Command userCommand) {
		return null;

	}
}
