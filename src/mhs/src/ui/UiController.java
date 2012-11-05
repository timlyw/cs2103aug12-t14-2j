package mhs.src.ui;

/**
 * This class controls the interaction between MhsFrame and Processor
 * It makes use of the observer pattern to listen for changes in MhsFrame and
 * Processor, this allows it to update both classes at the appropriate times
 * 
 * @author John Wong
 */

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Logger;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import mhs.src.common.MhsLogger;
import mhs.src.logic.Processor;
import mhs.src.logic.StateListener;

public class UiController {
	
	// mhsFrame used to handle user input and display output to user
	private MhsFrame mhsFrame;
	
	// processor used to process user commands
	private Processor processor = new Processor();
	
	// enter key constant used to check when user hits enter key
	private static final int ENTER_KEY = KeyEvent.VK_ENTER;
	
	// lineLimit used to determine number of lines to output
	private int lineLimit;
	
	// logger used to log function calls
	private final Logger logger = MhsLogger.getLogger();
	
	// class name used for logging
	private static final String CLASS_NAME = "UiController";
	
	/**
	 * sets up the mhsFrame and event listeners
	 */
	public UiController() {
		startLog("constructor");
		initMhsFrame();
		initListeners();
		endLog("constructor");
	}
	
	/**
	 * display the mhsFrame to user
	 */
	public void openMhsFrame() {
		startLog("openMhsFrame");
    	mhsFrame.open();
    	mhsFrame.selectInputBox();
    	updateLineLimit();
    	processor.showHome();
    	endLog("openMhsFrame");
	}
	
	/**
	 * updates the limit on the number of lines to be displayed based on the
	 * height of the display area in mhsFrame
	 */
	private void updateLineLimit() {
		startLog("updateLineLimit");
		int initialLineLimit = lineLimit;
		lineLimit = calculateNewLineLimit();
		
		if(lineLimit != initialLineLimit) {
			processor.setLineLimit(lineLimit);
		}
		endLog("updateLineLimit");
	}
	
	/**
	 * calculates max number of lines to be displayed
	 * 
	 * @return calculated new line limit
	 */
	private int calculateNewLineLimit() {
		int displayScreenHeight = mhsFrame.getDisplayScreenHeight();
		int processorLineHeight = processor.LINE_HEIGHT;
		int newLineLimit = displayScreenHeight / processorLineHeight;
		return newLineLimit;
	}
	
	/**
	 * create a new instance of mhsFrame
	 */
	private void initMhsFrame() {
		startLog("initMhsFrame");
    	mhsFrame = MhsFrame.getInstance();
    	endLog("initMhsFrame");
	}
	
	/**
	 * initialize event listeners
	 */
	private void initListeners() {
		startLog("initListeners");
		initProcessorStateListener();
		initInputTextChangedListener();
		initInputKeyListener();
		initFrameListener();
		endLog("initListeners");
	}
	
	/**
	 * initialize listener to observe for changes in frame size
	 */
	private void initFrameListener() {
		FrameListener frameListener = new FrameListener();
		mhsFrame.addComponentListener(frameListener);
	}
	
	/**
	 * initialize key listener to observe when user presses the enter key
	 */
	private void initInputKeyListener() {
		InputKeyListener inputKeyListener = new InputKeyListener();
		mhsFrame.addInputKeyListener(inputKeyListener);
	}
	
	/**
	 * initialize text changed listener to observe when input text has changed
	 */
	private void initInputTextChangedListener() {
		InputTextChangedListener inputListener = new InputTextChangedListener();
		mhsFrame.addInputChangedListener(inputListener);
	}
	
	/**
	 * initialize processor state listener to observe when processor has an updated state
	 */
	private void initProcessorStateListener() {
		ProcessorStateListener processorStateListener = new ProcessorStateListener();
		processor.addStateListener(processorStateListener);
	}
	
	/**
	 * retrieve the current command from mhsFrame and update the command in processor
	 */
	private void updateProcesorCommand() {
		startLog("updateProcessorCommand");
		if(mhsFrame.inputDisabled()) {
			return;
		}
		String command = mhsFrame.getCommand();
		processor.setCommand(command);
		endLog("updateProcessorCommand");
	}
	
	/**
	 * set the input format to password or plain text depending on whether processor
	 * is expecting a password or ordinary commands
	 */
	private void updateInputType() {
		startLog("updatedInputType");
		if(processor.passwordExpected()) {
			mhsFrame.setInputToPassword();
		} else {
			mhsFrame.setInputToPlainText();
		}
		endLog("updateInputType");
	}
	
	private void updateMhsFrame() {
		updateTitleScreen();
		updateDisplayScreen();
		updateFeedbackText();
		updateInputType();
	}
	
	private void updateTitleScreen() {
		startLog("updateTitleScreen");
		String titleText = processor.getHeaderText();
		if(titleText != null) {
			mhsFrame.setTitleText(titleText);
		}
		endLog("updateTitleScreen");
	}

	/** 
	 * updates the feedback text in mhsFrame to the current command feedback in the processor
	 */
	private void updateFeedbackText() {
		startLog("updateFeedbackText");
		String feedbackText = processor.getCommandFeedback();
		if(feedbackText != null) {
			mhsFrame.setFeedbackText(feedbackText);
		}
		endLog("updateFeedbackText");
	}
	
	/**
	 * updates the display screen to show the current state of the processor
	 */
	private void updateDisplayScreen() {
		startLog("updateDisplayScreen");
		String displayText = processor.getState();
		if(displayText != null) {
			mhsFrame.setDisplayText(displayText);
		}
		endLog("updateDisplayScreen");
	}
	
	/**
	 * calls processor to execute the current command
	 */
	private void executeCommandInProcessor() {
		startLog("executeCommandInProcessor");
		processor.executeCommand();
		mhsFrame.clearInput();
		endLog("executeCommandInProcessor");
	}
	
	/**
	 * this class observes if there are any changes in the state of processor
	 */
	private class ProcessorStateListener implements StateListener {
		/**
		 * updates display text, feedback text and input format 
		 */
		public void stateChanged() {
			updateMhsFrame();
		}
	}
	
	/**
	 * this class observes if there are any changes in the observed component
	 * it updates the processor's current command if there is
	 */
	private class InputTextChangedListener implements DocumentListener {
		public void changedUpdate(DocumentEvent arg0) {
			updateProcesorCommand();
		}

		public void insertUpdate(DocumentEvent arg0) {
			updateProcesorCommand();
		}

		public void removeUpdate(DocumentEvent arg0) {
			updateProcesorCommand();
		}
	}
	
	/**
	 * observe when the user presses the enter key, calls the processor
	 * to execute the current command when this occurs
	 */
	private class InputKeyListener implements KeyListener {
		public void keyPressed(KeyEvent arg0) {
			if (arg0.getKeyCode() == ENTER_KEY) {
				executeCommandInProcessor();
			}
		}

		public void keyReleased(KeyEvent arg0) {
		}

		public void keyTyped(KeyEvent arg0) {
		}
	}
	
	/**
	 * observes when the size of mhsFrame changes, updates the line limit
	 * to be displayed to make maximum use of available space
	 */
	private class FrameListener implements ComponentListener {
		public void componentHidden(ComponentEvent arg0) {
		}

		public void componentMoved(ComponentEvent arg0) {
		}

		public void componentResized(ComponentEvent arg0) {
			updateLineLimit();
		}

		public void componentShown(ComponentEvent arg0) {
		}
		
	}
	
	private void startLog(String methodName) {
		logger.entering(CLASS_NAME, methodName);
	}
	
	private void endLog(String methodName) {
		logger.entering(CLASS_NAME, methodName);
	}
}
