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
	// private static final String MESSAGE_NOTHING_TO_UNDO = "Nothing to undo";
	// private static final String MESSAGE_NOTHING_TO_REDO = "Nothing to redo";

	private static CommandCreator commandCreator;
	private Command currentCommand;
	private Command previousCommand;
	private Stack<Command> undoListCommands;
	private Stack<Command> redoListCommands;
	private static final Logger logger = MhsLogger.getLogger();
	private String commandFeedback = "feedback";
	private String currentState = "state";

	private CommandCreator() {
		logEnterMethod("CommandCreator");
		undoListCommands = new Stack<Command>();
		redoListCommands = new Stack<Command>();
		logExitMethod("CommandCreator");
	}

	public static CommandCreator getCommandCreator() {
		if (commandCreator == null) {
			commandCreator = new CommandCreator();
		}
		return commandCreator;
	}

	/**
	 * Creates Command by type
	 * 
	 * @param userCommand
	 * @return output String
	 */
	public void createCommand(CommandInfo userCommand) {
		logEnterMethod("createCommand");
		assert (userCommand != null);
		executeByTypeOfCommand(userCommand);
		storeCurrentCommand();
		logExitMethod("CommandCreator");
	}

	/**
	 * Checks whether command is index/non index based and accordingly creates
	 * it.
	 * 
	 * @param userCommand
	 */
	private void executeByTypeOfCommand(CommandInfo userCommand) {
		if (userCommand.getCommandEnum() != null) {
			if (userCommand.getIndex() != 0) {
				executeCommandByIndex(userCommand);
			} else {
				System.out.println("test 4");
				executeCommand(userCommand);
			}
		} else {
			executeByIndexOnly(userCommand);
		}
	}

	/**
	 * Store current command
	 */
	private void storeCurrentCommand() {
		previousCommand = currentCommand;
	}

	/**
	 * Execute command by index only
	 * 
	 * @param userCommand
	 */
	private void executeByIndexOnly(CommandInfo userCommand) {
		previousCommand.executeByIndex(userCommand.getIndex() - 1);
		updateDisplay(previousCommand);
		pushToUndoStack(previousCommand);
	}

	/**
	 * Updates Display
	 * 
	 * @param inputcommand
	 */
	private void updateDisplay(Command inputcommand) {
		System.out.println("test 7");
		System.out.println("in " + inputcommand.getCurrentState());

		System.out.println("in command " + inputcommand);
		currentState = inputcommand.getCurrentState();
		System.out.println("test 7.5");
		commandFeedback = inputcommand.getCommandFeedback();
		System.out.println("test 8");
	}

	/**
	 * Creates an object of respective command based on type enum. Executes
	 * command
	 * 
	 * @param userCommand
	 * @return output String
	 */
	private String executeCommand(CommandInfo userCommand) {
		logEnterMethod("executeMethod");
		String userOutputString = new String();
		switch (userCommand.getCommandEnum()) {
		case add:
			System.out.println("test 5");
			currentCommand = new CommandAdd(userCommand);
			currentCommand.executeCommand();
			System.out.println("test 6");
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
			undoLastCommand();
			break;
		case redo:
			redoLastCommand();
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
		updateDisplay(currentCommand);
		logExitMethod("executeCommand");
		return userOutputString;
	}

	/**
	 * Redo the last command
	 * 
	 * @return
	 */
	private void redoLastCommand() {
		logEnterMethod("redoLastCommand");
		if (redoListCommands.isEmpty()) {
			// MESSAGE_NOTHING_TO_REDO;
		} else {
			Command redoCommand = redoListCommands.pop();
			redoCommand.redo();
		}
		logExitMethod("redoLastCommand");
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
	private void undoLastCommand() {
		logEnterMethod("undoLastCommand");
		if (undoListCommands.isEmpty()) {
			// MESSAGE_NOTHING_TO_UNDO;
		} else {
			Command undoCommand = undoListCommands.pop();
			redoListCommands.push(undoCommand);
			undoCommand.undo();
		}
		logExitMethod("undoLastCommand");
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
		int index = userCommand.getIndex() - 1;
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
		updateDisplay(currentCommand);
		logExitMethod("executeCommandByIndex");
		return userOutputString;
	}

	/**
	 * Return current state
	 * 
	 * @return
	 */
	public String getState() {
		return currentState;
	}

	/**
	 * Return Feedback string
	 * 
	 * @return
	 */
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
