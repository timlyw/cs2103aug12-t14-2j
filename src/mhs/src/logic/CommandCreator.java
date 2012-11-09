//@author A0088669A

package mhs.src.logic;

import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.logic.CommandInfo.CommandKeyWords;

import java.util.Stack;

/**
 * Creates commands based on type
 * 
 * @author A0088669A
 * 
 */
public class CommandCreator {
	private static final String MESSAGE_INVALID_COMMAND = "Invalid Command";
	private static final String MESSAGE_NOTHING_TO_UNDO = "Nothing to undo";
	private static final String MESSAGE_NOTHING_TO_REDO = "Nothing to redo";

	private Command currentCommand;
	private Command previousCommand;
	private Stack<Command> undoListCommands;
	private Stack<Command> redoListCommands;
	private static final Logger logger = MhsLogger.getLogger();
	private String commandFeedback = "feedback";
	private String currentState = "state";

	public CommandCreator() {
		logEnterMethod("CommandCreator");
		undoListCommands = new Stack<Command>();
		redoListCommands = new Stack<Command>();
		logExitMethod("CommandCreator");
	}

	/**
	 * Creates Command by type
	 * 
	 * @param userCommand
	 * @return output String
	 */
	public String createCommand(CommandInfo userCommand) {
		logEnterMethod("createCommand");
		String userOutputString = new String();
		assert (userCommand != null);
		if (userCommand.getCommandEnum() != null) {
			if (userCommand.getIndex() != 0) {
				userOutputString = executeCommandByIndex(userCommand);
			} else {
				userOutputString = executeCommand(userCommand);
				// Add to undo stack if undoable
			}
		} else {
			previousCommand.executeByIndex(userCommand.getIndex() - 1);
			currentState = previousCommand.getCurrentState();
			commandFeedback = previousCommand.getCommandFeedback();
			pushToUndoStack(previousCommand);
		}
		previousCommand = currentCommand;
		logExitMethod("CommandCreator");
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
			currentCommand.executeCommand();
			break;
		case remove:
			currentCommand = new CommandRemove(userCommand);
			currentCommand.executeCommand();
			break;
		case edit:
			currentCommand = new CommandEdit(userCommand);
			currentCommand.executeCommand();
			break;
		case search:
			currentCommand = new CommandSearch(userCommand);
			currentCommand.executeCommand();
			break;
		case floating:
			currentCommand = new CommandSearch(userCommand);
			currentCommand.executeCommand();
			break;
		case deadline:
			currentCommand = new CommandSearch(userCommand);
			currentCommand.executeCommand();
			break;
		case timed:
			currentCommand = new CommandSearch(userCommand);
			currentCommand.executeCommand();
			break;
		case home:
			currentCommand = new CommandSearch(userCommand);
			currentCommand.executeCommand();
			break;
		case undo:
			userOutputString = undoLastCommand();
			break;
		case redo:
			userOutputString = redoLastCommand();
			break;
		case mark:
			currentCommand = new CommandMark(userCommand);
			currentCommand.executeCommand();
			break;
		case unmark:
			currentCommand = new CommandUnmark(userCommand);
			currentCommand.executeCommand();
			break;
		case rename:
			currentCommand = new CommandRename(userCommand);
			currentCommand.executeCommand();
			break;
		case next:
			Command.displayNext();
			break;
		case previous:
			Command.displayPrev();
			break;
		default:
			userOutputString = MESSAGE_INVALID_COMMAND;
			break;
		}
		updateDisplayIndex(userCommand);
		pushToUndoStack(currentCommand);
		currentState = currentCommand.getCurrentState();
		commandFeedback = currentCommand.getCommandFeedback();
		logger.exiting(getClass().getName(), this.getClass().getName());
		return userOutputString;
	}

	private String redoLastCommand() {
		String userOutputString = new String();
		if (redoListCommands.isEmpty()) {
			userOutputString = MESSAGE_NOTHING_TO_REDO;
		} else {
			Command redoCommand = redoListCommands.pop();
			redoCommand.redo();
		}
		return userOutputString;
	}

	/**
	 * Pushes comamnd to undo stack if undoable
	 */
	private void pushToUndoStack(Command inputCommand) {
		logEnterMethod("pushToUndoStack");
		if (currentCommand.isUndoable()) {
			undoListCommands.push(inputCommand);
		}
		logExitMethod("pushToRedoStack");
	}

	/**
	 * Ensures that writing display brings the screen back to the first page
	 * 
	 * @param userCommand
	 */
	private void updateDisplayIndex(CommandInfo userCommand) {
		logEnterMethod("updateDisplayIndex");
		if (userCommand.getCommandEnum() == CommandKeyWords.search
				|| userCommand.getCommandEnum() == CommandKeyWords.floating
				|| userCommand.getCommandEnum() == CommandKeyWords.deadline
				|| userCommand.getCommandEnum() == CommandKeyWords.timed
				|| userCommand.getCommandEnum() == CommandKeyWords.home) {

			Command.resetDisplayIndex();
		}
		logExitMethod("updateDisplayIndex");
	}

	/**
	 * Pops from undo stack and calls the undo method
	 * 
	 * @return
	 */
	private String undoLastCommand() {
		logEnterMethod("undoLastCommand");
		String userOutputString;
		if (undoListCommands.isEmpty()) {
			userOutputString = MESSAGE_NOTHING_TO_UNDO;
		} else {
			Command undoCommand = undoListCommands.pop();
			redoListCommands.push(undoCommand);
			userOutputString = undoCommand.undo();
		}
		logExitMethod("undoLastCommand");
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
		logEnterMethod("executeCommandByIndex");
		String userOutputString = new String();
		int local_index = userCommand.getIndex();
		int index = local_index - 1;
		switch (userCommand.getCommandEnum()) {
		case add:
			currentCommand = new CommandAdd();
			break;
		case remove:
			currentCommand = new CommandRemove(Command.matchedTasks);
			currentCommand.executeByIndexAndType(index);
			break;
		case edit:
			currentCommand = new CommandEdit(Command.matchedTasks, userCommand);
			currentCommand.executeByIndexAndType(index);
			break;
		case mark:
			currentCommand = new CommandMark(Command.matchedTasks);
			currentCommand.executeByIndexAndType(index);
			break;
		case unmark:
			currentCommand = new CommandUnmark(Command.matchedTasks);
			currentCommand.executeByIndexAndType(index);
			break;
		case rename:
			currentCommand = new CommandRename(Command.matchedTasks,
					userCommand);
			currentCommand.executeByIndexAndType(index);
			break;
		default:
			commandFeedback = MESSAGE_INVALID_COMMAND;
			break;
		}
		pushToUndoStack(currentCommand);
		currentState = currentCommand.getCurrentState();
		commandFeedback = currentCommand.getCommandFeedback();
		logExitMethod("executeCommandByIndex");
		return userOutputString;
	}

	public String getState() {
		return currentState;
	}

	public String getFeedback() {
		return commandFeedback;
	}

	/**
	 * Logger enter method
	 * 
	 * @param methodName
	 */
	private void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}

	/**
	 * Logger exit method
	 * 
	 * @param methodName
	 */
	private void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}

}
