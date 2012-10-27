package mhs.src.logic;

import java.util.Stack;

public class CommandCreator {
	private static final String MESSAGE_UNDO_CONFIRM = "Undo Successful";
	private static final String MESSAGE_INVALID_COMMAND = "Invalid Command";
	private Command currentCommand;
	private Command previousCommand;
	private Stack<Command> undoListCommands;

	public CommandCreator() {
		undoListCommands = new Stack<Command>();
	}
	
	public String createCommand(CommandInfo userCommand) {
		String userOutputString = new String();
		if (userCommand.getCommandEnum() != null) {
			if (userCommand.getIndex() != 0) {
				userOutputString = executeCommandByIndex(userCommand);
			} else {
				userOutputString = executeCommand(userCommand);
				// Add to undo stack if undoable
				if (currentCommand.isUndoable()) {
					undoListCommands.push(currentCommand);
				}
			}
		} else {
			userOutputString = previousCommand.executeByIndex(userCommand
					.getIndex());

		}
		previousCommand = currentCommand;
		return userOutputString;
	}

	private String executeCommand(CommandInfo userCommand) {
		String userOutputString = new String();
		switch (userCommand.getCommandEnum()) {
		case add:
			currentCommand = new CommandAdd(userCommand);
			userOutputString = currentCommand.executeCommand();
			break;
		case remove:
			currentCommand = new CommandRemove(userCommand);
			userOutputString = currentCommand.executeCommand();
			break;
		case edit:
			currentCommand = new CommandEdit(userCommand);
			userOutputString = currentCommand.executeCommand();
			break;
		case search:
			currentCommand = new CommandSearch(userCommand);
			userOutputString = currentCommand.executeCommand();
			break;
		case undo:
			Command undoCommand = undoListCommands.pop();
			undoCommand.undo();
			userOutputString = MESSAGE_UNDO_CONFIRM;
			break;
		case mark:
			currentCommand = new CommandMark(userCommand);
			userOutputString = currentCommand.executeCommand();
			break;
		case help:
			break;
		case rename:
			break;
		default:
			break;
		}
		return userOutputString;
	}

	private String executeCommandByIndex(CommandInfo userCommand) {
		String userOutputString = new String();
		switch (userCommand.getCommandEnum()) {
		case add:
			userOutputString = MESSAGE_INVALID_COMMAND;
			break;
		case remove:
			currentCommand = new CommandRemove(userCommand);
			userOutputString = currentCommand.executeByIndex(userCommand
					.getIndex());
			break;
		case edit:
			currentCommand = new CommandEdit(userCommand);
			userOutputString = currentCommand.executeByIndex(userCommand
					.getIndex());
			break;
		case search:
			currentCommand = new CommandSearch(userCommand);
			userOutputString = currentCommand.executeByIndex(userCommand
					.getIndex());
			break;
		case undo:
			userOutputString = MESSAGE_INVALID_COMMAND;
			break;
		case mark:
			currentCommand = new CommandMark(userCommand);
			userOutputString = currentCommand.executeByIndex(userCommand
					.getIndex());
			break;
		case help:
			userOutputString = MESSAGE_INVALID_COMMAND;
			break;
		case rename:
			userOutputString = MESSAGE_INVALID_COMMAND;
			break;
		default:
			break;
		}
		return userOutputString;
	}

}
