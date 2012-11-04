package mhs.src.logic;

import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import java.util.Stack;

/**
 * Creates commands based on type
 * 
 * @author Shekhar
 * 
 */
public class CommandCreator {
	private static final String MESSAGE_INVALID_COMMAND = "Invalid Command";
	private Command currentCommand;
	private Command previousCommand;
	private Stack<Command> undoListCommands;
	private static final Logger logger = MhsLogger.getLogger();

	public CommandCreator() {
		undoListCommands = new Stack<Command>();
	}

	/**
	 * Creates Command by type
	 * 
	 * @param userCommand
	 * @return output String
	 */
	public String createCommand(CommandInfo userCommand) {
		logger.entering(getClass().getName(), this.getClass().getName());
		String userOutputString = new String();
		if (userCommand.getCommandEnum() != null) {
			if (userCommand.getIndex() != 0) {
				userOutputString = executeCommandByIndex(userCommand);
			} else {
				userOutputString = executeCommand(userCommand);
				// Add to undo stack if undoable
			}
		} else {
			userOutputString = previousCommand.executeByIndex(userCommand
					.getIndex() - 1);

		}
		previousCommand = currentCommand;
		logger.exiting(getClass().getName(), this.getClass().getName());
		return userOutputString;
	}

	/**
	 * Creates an object of respective command based on type enum. Executes
	 * command
	 * 
	 * @param userCommand
	 * @return output String
	 */
	private String executeCommand(CommandInfo userCommand) {
		logger.entering(getClass().getName(), this.getClass().getName());
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
			userOutputString = undoCommand.undo();
			break;
		case mark:
			currentCommand = new CommandMark(userCommand);
			userOutputString = currentCommand.executeCommand();
			break;
		/*
		 * case unmark: currentCommand = new CommandUnmark(userCommand);
		 * userOutputString = currentCommand.executeCommand(); break;
		 */
		case help:
			userOutputString = MESSAGE_INVALID_COMMAND;
			break;
		case rename:
			userOutputString = MESSAGE_INVALID_COMMAND;
			break;
		default:
			userOutputString = MESSAGE_INVALID_COMMAND;
			break;
		}
		if (currentCommand.isUndoable()) {
			undoListCommands.push(currentCommand);
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return userOutputString;
	}

	/**
	 * Creates an object based on given type. Executes based on index. Uses
	 * previous list of tasks
	 * 
	 * @param userCommand
	 * @return output String
	 */
	private String executeCommandByIndex(CommandInfo userCommand) {
		logger.entering(getClass().getName(), this.getClass().getName());
		String userOutputString = new String();
		int local_index = userCommand.getIndex();
		switch (userCommand.getCommandEnum()) {
		case add:
			userOutputString = MESSAGE_INVALID_COMMAND;
			break;
		case remove:
			currentCommand = new CommandRemove(previousCommand.matchedTasks);
			userOutputString = currentCommand
					.executeByIndexAndType(local_index - 1);
			break;
		case edit:
			currentCommand = new CommandEdit(previousCommand.matchedTasks,
					userCommand);
			userOutputString = currentCommand
					.executeByIndexAndType(local_index - 1);
			break;
		case search:
			userOutputString = MESSAGE_INVALID_COMMAND;
			break;
		case undo:
			userOutputString = MESSAGE_INVALID_COMMAND;
			break;
		case mark:
			currentCommand = new CommandMark(previousCommand.matchedTasks);
			userOutputString = currentCommand
					.executeByIndexAndType(local_index - 1);
			break;
		/*
		 * case unmark: currentCommand = new
		 * CommandUnmark(previousCommand.matchedTasks); userOutputString =
		 * currentCommand .executeByIndexAndType(local_index - 1); break;
		 */
		case help:
			userOutputString = MESSAGE_INVALID_COMMAND;
			break;
		case rename:
			userOutputString = MESSAGE_INVALID_COMMAND;
			break;
		default:
			userOutputString = MESSAGE_INVALID_COMMAND;
			break;
		}
		if (currentCommand.isUndoable()) {
			undoListCommands.push(currentCommand);
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return userOutputString;
	}

}
