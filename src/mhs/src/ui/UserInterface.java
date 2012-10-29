package mhs.src.ui;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import mhs.src.logic.ProcessorStub;
import mhs.src.logic.StateListener;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.NativeSwing;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;

public class UserInterface {
	
	MhsFrame mhsFrame;
	ProcessorStub processor = new ProcessorStub();
	HtmlCreator htmlCreator = new HtmlCreator();
	private static final int ENTER_KEY = KeyEvent.VK_ENTER;
	int lineLimit;
	
	public UserInterface() {
		initSwingUtilities();
		initMhsFrame();
		waitForMhsFrameToinit();
		initListeners();
	}
	
	public void openMhsFrame() {
    	mhsFrame.open();
    	mhsFrame.selectInputBox();
	}
	
	private void updateLineLimit() {
		int initialLineLimit = lineLimit;
		int displayScreenHeight = mhsFrame.getDisplayScreenHeight();
		int processorLineHeight = processor.LINE_HEIGHT;
		lineLimit = displayScreenHeight / processorLineHeight;
		
		if(lineLimit != initialLineLimit) {
			processor.setLineLimit(lineLimit);
		}
	}
	
	private void initMhsFrame() {
		MhsFrameThread mhsFrameThread = new MhsFrameThread();
		SwingUtilities.invokeLater(mhsFrameThread);
	}
	
	private void waitForMhsFrameToinit() {
		while(mhsFrame == null);
	}
	
	private void initListeners() {
		initProcessorStateListener();
		initInputTextChangedListener();
		initInputKeyListener();
		initFrameListener();
	}
	
	private void initFrameListener() {
		FrameListener frameListener = new FrameListener();
		mhsFrame.addComponentListener(frameListener);
	}
	
	private void initInputKeyListener() {
		InputKeyListener inputKeyListener = new InputKeyListener();
		mhsFrame.addInputKeyListener(inputKeyListener);
	}
	
	private void initInputTextChangedListener() {
		InputTextChangedListener inputListener = new InputTextChangedListener();
		mhsFrame.addInputChangedListener(inputListener);
	}
	
	private void initProcessorStateListener() {
		ProcessorStateListener processorStateListener = new ProcessorStateListener();
		processor.addStateListener(processorStateListener);
	}
	
	private void initSwingUtilities() {
		NativeInterface.open();
		NativeSwing.initialize();
	    UIUtils.setPreferredLookAndFeel();
	}

	private void updateProcesorCommand() {
		String command = mhsFrame.getCommand();
		if(command.isEmpty()) {
			return;
		}
		
		processor.setCommand(command);
	}
	
	private void updateInputType() {
		if(processor.passwordExpected()) {
			mhsFrame.setInputToPassword();
		} else {
			mhsFrame.setInputToPlainText();
		}
		
	}
	
	private void executeCommandInProcessor() {
		processor.executeCommand();
		mhsFrame.clearInput();
	}
	
	private class ProcessorStateListener implements StateListener {
		public void updateState() {
			updateDisplayScreen();
			updateFeedbackText();
			updateInputType();
		}
		
		private void updateDisplayScreen() {
			String displayText = processor.getState();
			if(displayText != null) {
				mhsFrame.setDisplayText(displayText);
			}
		}
		
		private void updateFeedbackText() {
			String feedbackText = processor.getCommandFeedback();
			if(feedbackText != null) {
				mhsFrame.setFeedbackText(feedbackText);
			}
		}
	}
	
	private class MhsFrameThread implements Runnable {
		public void run() {
        	mhsFrame = new MhsFrame();
		}
	}
	
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
}
