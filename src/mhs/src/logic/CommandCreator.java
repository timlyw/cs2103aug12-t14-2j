//@author A0088669A

package mhs.src.logic;

import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.logic.CommandInfo.CommandKeyWords;
import mhs.src.logic.command.Command;
import mhs.src.logic.command.CommandAdd;
import mhs.src.logic.command.CommandEdit;
import mhs.src.logic.command.CommandMark;
import mhs.src.logic.command.CommandRemove;
import mhs.src.logic.command.CommandRename;
import mhs.src.logic.command.CommandSearch;
import mhs.src.logic.command.CommandUnmark;

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
	private static final String MESSAGE_HOME_PAGE = "<b>Commands:</b> add/delete/edit/mark/rename/login <br/><b>Type 'help' for more info.</b>";

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
		currentState = inputcommand.getCurrentState();
		commandFeedback = inputcommand.getCommandFeedback();
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
			executeAdd(userCommand);
			break;
		case remove:
			executeRemove(userCommand);
			break;
		case edit:
			executeEdit(userCommand);
			break;
		case search:
			executeSearch(userCommand);
			break;
		case floating:
			executeSearch(userCommand);
			break;
		case deadline:
			executeSearch(userCommand);
			break;
		case timed:
			executeSearch(userCommand);
			break;
		case home:
			executeSearch(userCommand);
			break;
		case undo:
			undoLastCommand();
			break;
		case redo:
			redoLastCommand();
			break;
		case mark:
			executeMark(userCommand);
			break;
		case unmark:
			executeUnmark(userCommand);
			break;
		case rename:
			executeRename(userCommand);
			break;
		case next:
			nextPage();
			break;
		case previous:
			previousPage();
			break;
		default:
			commandFeedback = MESSAGE_INVALID_COMMAND;
			break;
		}
		updateDisplayIndex(userCommand);
		pushToUndoStack(currentCommand);
		logExitMethod("executeCommand");
		return userOutputString;
	}

	private void previousPage() {
		Command.displayPrev();
		updateDisplay(currentCommand);
	}

	private void nextPage() {
		Command.displayNext();
		updateDisplay(currentCommand);
	}

	/**
	 * executes Rename
	 * 
	 * @param userCommand
	 */
	private void executeRename(CommandInfo userCommand) {
		currentCommand = new CommandRename(userCommand);
		currentCommand.executeCommand();
		updateDisplay(currentCommand);
	}

	/**
	 * executes Unmark
	 * 
	 * @param userCommand
	 */
	private void executeUnmark(CommandInfo userCommand) {
		currentCommand = new CommandUnmark(userCommand);
		currentCommand.executeCommand();
		updateDisplay(currentCommand);
	}

	/**
	 * executes Mark
	 * 
	 * @param userCommand
	 */
	private void executeMark(CommandInfo userCommand) {
		currentCommand = new CommandMark(userCommand);
		currentCommand.executeCommand();
		updateDisplay(currentCommand);
	}

	/**
	 * executes Search
	 * 
	 * @param userCommand
	 */
	private void executeSearch(CommandInfo userCommand) {
		currentCommand = new CommandSearch(userCommand);
		currentCommand.executeCommand();
		if (userCommand.getCommandEnum() == CommandKeyWords.home) {
			currentState = currentCommand.getCurrentState();
			commandFeedback = MESSAGE_HOME_PAGE;
			return;
		}
		updateDisplay(currentCommand);
	}

	/**
	 * executes Edit
	 * 
	 * @param userCommand
	 */
	private void executeEdit(CommandInfo userCommand) {
		currentCommand = new CommandEdit(userCommand);
		currentCommand.executeCommand();
		updateDisplay(currentCommand);
	}

	/**
	 * executes Remove
	 * 
	 * @param userCommand
	 */
	private void executeRemove(CommandInfo userCommand) {
		currentCommand = new CommandRemove(userCommand);
		currentCommand.executeCommand();
		updateDisplay(currentCommand);
	}

	/**
	 * executes Add
	 * 
	 * @param userCommand
	 */
	private void executeAdd(CommandInfo userCommand) {
		currentCommand = new CommandAdd(userCommand);
		currentCommand.executeCommand();
		updateDisplay(currentCommand);
	}

	/**
	 * Redo the last command
	 * 
	 * @return
	 */
	private void redoLastCommand() {
		logEnterMethod("redoLastCommand");
		if (redoListCommands.isEmpty()) {
			updateRedoDisplay();
		} else {
			Command redoCommand = redoListCommands.pop();
			redoCommand.redo();
			updateDisplay(currentCommand);
		}
		logExitMethod("redoLastCommand");
	}

	/**
	 * Refreshes display for redo
	 */
	private void updateRedoDisplay() {
		currentState = currentCommand.getCurrentState();
		commandFeedback = MESSAGE_NOTHING_TO_REDO;
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
			updateUndoDisplay();
		} else {
			Command undoCommand = undoListCommands.pop();
			redoListCommands.push(undoCommand);
			undoCommand.undo();
			updateDisplay(currentCommand);
		}
		logExitMethod("undoLastCommand");
	}

	/**
	 * refreshes display for undo
	 */
	private void updateUndoDisplay() {
		currentState = currentCommand.getCurrentState();
		commandFeedback = MESSAGE_NOTHING_TO_UNDO;
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
