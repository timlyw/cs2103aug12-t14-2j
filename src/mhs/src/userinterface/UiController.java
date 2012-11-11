//@author A0088015H

package mhs.src.userinterface;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

import mhs.src.common.HtmlCreator;
import mhs.src.common.MhsLogger;
import mhs.src.logic.Processor;
import mhs.src.logic.StateListener;
import mhs.src.storage.persistence.local.ConfigFile;

/**
 * UiController controls the interaction between MhsFrame and Processor It makes
 * use of the observer pattern to listen for changes in MhsFrame and Processor,
 * this allows it to update both classes at the appropriate times
 * 
 * @author John Wong
 */

public class UiController {

	private static final int HOT_KEY_LETTER = (int) 'X';

	private static final int HOT_KEY_ALT = JIntellitype.MOD_ALT;

	private static final String MHS_FRAME_MAXIMIZED = "mhsFrameMaximized";

	private static final String MHS_FRAME_HEIGHT = "mhsFrameHeight";

	private static final String MHS_FRAME_WIDTH = "mhsFrameWidth";
	
	// mhsFrame used to handle user input and display output to user
	private MhsFrame mhsFrame;

	// processor used to process user commands
	private Processor processor = Processor.getProcessor();

	// enter key constant used to check when user hits enter key
	private static final int ENTER_KEY = KeyEvent.VK_ENTER;

	// lineLimit used to determine number of lines to output
	private int lineLimit;

	// logger used to log function calls
	private final Logger logger = MhsLogger.getLogger();

	// class name used for logging
	private static final String CLASS_NAME = "UiController";

	int mhsFrameWidth = 600;
	int mhsFrameHeight = 450;
	boolean mhsFrameMaximized = true;

	ConfigFile configFile = null;

	/**
	 * sets up the mhsFrame and event listeners
	 */
	public UiController() {
		startLog("constructor");
		initMhsFrame();
		initListeners();
		loadMhsParameters();
		//initHotKey();
		endLog("constructor");
	}

	private void initHotKey() {
		// Initialize JIntellitype
		JIntellitype.getInstance();
		JIntellitype.getInstance().registerHotKey(1, HOT_KEY_ALT,
				HOT_KEY_LETTER);
		MhsHotKeyListener mhsHotkeyListener = new MhsHotKeyListener();
		JIntellitype.getInstance().addHotKeyListener(mhsHotkeyListener);
	}

	private void loadMhsParameters() {
		try {
			configFile = new ConfigFile();
			String widthString = configFile.getConfigParameter(MHS_FRAME_WIDTH);
			String heightString = configFile
					.getConfigParameter(MHS_FRAME_HEIGHT);
			String maximizedString = configFile
					.getConfigParameter(MHS_FRAME_MAXIMIZED);
			if (widthString == null || heightString == null
					|| maximizedString == null) {
				return;
			}
			mhsFrameWidth = Integer.parseInt(widthString);
			mhsFrameHeight = Integer.parseInt(heightString);
			mhsFrameMaximized = Boolean.parseBoolean(maximizedString);

		} catch (IOException e) {

		}
	}

	/**
	 * display the mhsFrame to user
	 */
	public void showUserInterface() {
		startLog("openMhsFrame");
		showHomePage();
		openMhsFrame();
		endLog("openMhsFrame");
	}

	private void showHomePage() {
		updateLineLimit();
		processor.showHome();
	}

	private void openMhsFrame() {
		mhsFrame.open();
		mhsFrame.selectInputBox();
		updateMhsFrameSize();
	}

	public void updateMhsFrameSize() {
		mhsFrame.setSize(mhsFrameWidth, mhsFrameHeight, mhsFrameMaximized);
	}

	private void mhsFrameResized() {
		updateLineLimit();
		storeMhsParameters();
	}

	private void storeMhsParameters() {
		updateMhsFrameMaximized();
		updateMhsFrameDimensionst();
		saveParameters();
	}

	private void saveParameters() {
		if (configFile == null) {
			return;
		}
		try {
			configFile.setConfigParameter(MHS_FRAME_WIDTH,
					Integer.toString(mhsFrameWidth));
			configFile.setConfigParameter(MHS_FRAME_HEIGHT,
					Integer.toString(mhsFrameHeight));
			configFile.setConfigParameter(MHS_FRAME_MAXIMIZED,
					Boolean.toString(mhsFrameMaximized));
		} catch (IOException e) {

		}
	}

	private void updateMhsFrameMaximized() {
		if (mhsFrame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
			mhsFrameMaximized = true;
		} else {
			mhsFrameMaximized = false;
		}
	}

	private void updateMhsFrameDimensionst() {
		if (!mhsFrameMaximized) {
			mhsFrameWidth = mhsFrame.getWidth();
			mhsFrameHeight = mhsFrame.getHeight();
		}
	}

	/**
	 * updates the limit on the number of lines to be displayed based on the
	 * height of the display area in mhsFrame
	 */
	private void updateLineLimit() {
		startLog("updateLineLimit");
		int initialLineLimit = lineLimit;
		lineLimit = calculateNewLineLimit();

		if (lineLimit != initialLineLimit) {
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
		int newLineLimit = displayScreenHeight
				/ HtmlCreator.DEFAULT_LINE_HEIGHT;
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
		initTrayListener();
		initWindowListener();
		endLog("initListeners");
	}

	private void initWindowListener() {
		MhsWindowListener mhsWindowListener = new MhsWindowListener();
		mhsFrame.addWindowListener(mhsWindowListener);
	}

	private void initTrayListener() {
		TrayClickListener trayListener = new TrayClickListener();
		mhsFrame.addTrayListener(trayListener);
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
	 * initialize processor state listener to observe when processor has an
	 * updated state
	 */
	private void initProcessorStateListener() {
		ProcessorStateListener processorStateListener = new ProcessorStateListener();
		processor.addStateListener(processorStateListener);
	}

	/**
	 * retrieve the current command from mhsFrame and update the command in
	 * processor
	 */
	private void updateProcesorCommand() {
		startLog("updateProcessorCommand");
		if (mhsFrame.inputDisabled()) {
			return;
		}
		String command = mhsFrame.getCommand();
		processor.setCommand(command);
		endLog("updateProcessorCommand");
	}

	private void updateMhsFrame() {
		updateTitleScreen();
		updateDisplayScreen();
		updateFeedbackText();
		updateMhsFrameDimensionst();
		mhsFrame.repaint();
	}

	private void updateTitleScreen() {
		startLog("updateTitleScreen");
		String titleText = processor.getLoginDisplayFieldText();
		if (titleText != null) {
			mhsFrame.setTitleText(titleText);
		}
		endLog("updateTitleScreen");
	}

	/**
	 * updates the feedback text in mhsFrame to the current command feedback in
	 * the processor
	 */
	private void updateFeedbackText() {
		startLog("updateFeedbackText");
		String feedbackText = processor.getCommandFeedback();
		if (feedbackText != null) {
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
		if (displayText != null) {
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
	 * this class observes if there are any changes in the observed component it
	 * updates the processor's current command if there is
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
	 * observe when the user presses the enter key, calls the processor to
	 * execute the current command when this occurs
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
	 * observes when the size of mhsFrame changes, updates the line limit to be
	 * displayed to make maximum use of available space
	 */
	private class FrameListener implements ComponentListener {
		public void componentHidden(ComponentEvent arg0) {
		}

		public void componentMoved(ComponentEvent arg0) {
		}

		public void componentResized(ComponentEvent arg0) {
			mhsFrameResized();
		}

		public void componentShown(ComponentEvent arg0) {
		}

	}

	private class TrayClickListener implements MouseListener {
		public void mouseClicked(MouseEvent arg0) {
			openMhsFrame();
		}

		public void mousePressed(MouseEvent arg0) {
			openMhsFrame();
		}

		public void mouseEntered(MouseEvent arg0) {
		}

		public void mouseExited(MouseEvent arg0) {
		}

		public void mouseReleased(MouseEvent arg0) {
		}

	}

	private class MhsWindowListener implements WindowListener {
		public void windowActivated(WindowEvent arg0) {
		}

		public void windowClosed(WindowEvent arg0) {
		}

		public void windowClosing(WindowEvent arg0) {
		}

		public void windowDeactivated(WindowEvent arg0) {
		}

		public void windowDeiconified(WindowEvent arg0) {
		}

		public void windowIconified(WindowEvent arg0) {
			mhsFrame.close();
		}

		public void windowOpened(WindowEvent arg0) {
		}

	}

	private class MhsHotKeyListener implements HotkeyListener {
		public void onHotKey(int arg0) {
			openMhsFrame();
		}
	}

	private void startLog(String methodName) {
		logger.entering(CLASS_NAME, methodName);
	}

	private void endLog(String methodName) {
		logger.entering(CLASS_NAME, methodName);
	}
}
