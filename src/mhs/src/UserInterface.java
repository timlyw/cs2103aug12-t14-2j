/*
 * This class is a graphical user interface consisting of
 * 		1) A display screen to show output to user
 * 		2) An input JTextField to get user input
 * 		3) A pop-up helper screen to provide user with real-time feedback
 */

package mhs.src;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class UserInterface extends JFrame {

	// this serial is required for a JFrame
	private static final long serialVersionUID = 1L;

	// frame parameters
	private static final String FRAME_TITLE = "My Hot Secretary";
	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 450;

	// displayScreen parameters
	private static final int DISPLAY_SCREEN_HEIGHT = 1;
	private static final int DISPLAY_SCREEN_HEIGHT_IMPORTANCE = 1;
	private static final int DISPLAY_SCREEN_Y_POSITION = 0;
	private static final int DISPLAY_SCREEN_EXTERNAL_PADDING = 2;
	private static final int DISPLAY_SCREEN_INTERNAL_PADDING = 5;

	// inputBox parameters
	private static final int INPUT_BOX_HEIGHT = 1;
	private static final int INPUT_BOX_HEIGHT_IMPORTANCE = 0;
	private static final int INPUT_BOX_Y_POSITION = 1;
	private static final int INPUT_BOX_EXTERNAL_PADDING = 2;
	private static final int INPUT_BOX_INTERNAL_PADDING = 5;

	// helper parameters
	private static final int HELPER_LEFT_PADDING = 5;
	private static final int HELPER_TOP_PADDING = 5;
	
	private static final int ENTER_KEY = KeyEvent.VK_ENTER;
	private static final Color FRAME_COLOR = new Color(100, 100, 255);

	private JPanel framePanel = new JPanel(); // framePanel will be used to contain all other display components
	private JTextArea displayScreen = new JTextArea(); // used to display command response
	private JTextField inputBox = new JTextField(); // used to get user input
	private JPasswordField passwordBox = new JPasswordField();
	private JPopupMenu helperScreen = new JPopupMenu(); // used to display feedback to user

	private Processor processor = new Processor();

	UserInterface() {
		super(FRAME_TITLE);
		initFrame();
	}

	public void open() {
		this.setVisible(true);
	}

	public void setInputBoxToActive() {
		inputBox.requestFocusInWindow();
		inputBox.requestFocus();
	}

	// set the basic frame properties and the frame's components
	private void initFrame() {
		initFrameProperties();
		initFrameComponents();
		initEventHandlers();
	}

	private void initEventHandlers() {
		InputBoxTextChangedListener inputBoxTextChangedListener = new InputBoxTextChangedListener();
		inputBox.getDocument().addDocumentListener(inputBoxTextChangedListener);

		InputBoxKeyListener inputBoxKeyListener = new InputBoxKeyListener();
		inputBox.addKeyListener(inputBoxKeyListener);
	}

	// set the default method to close the frame
	// set the size and position of the frame
	private void initFrameProperties() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		this.setLocationRelativeTo(null); // set frame to open in center of
											// screen
		this.setBackground(FRAME_COLOR);
	}

	// add framePanel to frame
	// add and format displayScreen
	// add and format inputBox
	private void initFrameComponents() {
		formatAndAddFramePanel();
		formatAndAddDisplayScreen();
		formatAndAddInputBox();
	}

	private void showHelper(String helperText) {
		String[] helperTextArr = helperText.split("\n");
		addHelperLines(helperTextArr);
		refreshHelperScreen();
	}

	private void refreshHelperScreen() {
		helperScreen.setVisible(false);
		int totalTopPadding = HELPER_TOP_PADDING + inputBox.getHeight()
				+ helperScreen.getHeight() / 2;
		helperScreen.show(inputBox, HELPER_LEFT_PADDING, -totalTopPadding);
		helperScreen.setVisible(true);
	}

	private void addHelperLines(String[] helperTextArr) {
		helperScreen.removeAll();

		for (int i = 0; i < helperTextArr.length; i++) {
			JMenuItem helperLine = new JMenuItem();
			formatHelperLine(helperLine);
			helperLine.setText(helperTextArr[i]);
			helperScreen.add(helperLine);
		}
	}

	private void formatHelperLine(JMenuItem helperLine) {
		for (int j = 0; j < helperLine.getMouseListeners().length; j++) {
			helperLine.removeMouseListener(helperLine.getMouseListeners()[j]);
		}
	}

	private void hideHelper() {
		helperScreen.setVisible(false);
	}

	// set the layout of frame panel to a GridBagLayout
	// add the framePanel to the contentPane of this frame
	private void formatAndAddFramePanel() {
		// using a GridBagLayout allows more control over layout
		GridBagLayout gbLayout = new GridBagLayout();
		framePanel.setLayout(gbLayout);
		framePanel.setBackground(FRAME_COLOR);
		this.getContentPane().add(framePanel);
	}

	// format and add displayScreen to framePanel
	private void formatAndAddDisplayScreen() {
		formatDisplayScreen();
		addDisplayScreen();
	}

	// add displayScreen to framePanel
	private void addDisplayScreen() {
		GridBagConstraints screenGbc = createGridBagConstraint(
				DISPLAY_SCREEN_Y_POSITION, DISPLAY_SCREEN_HEIGHT,
				DISPLAY_SCREEN_HEIGHT_IMPORTANCE);
		Box displayScreenContainer = createContainerBox(displayScreen,
				DISPLAY_SCREEN_EXTERNAL_PADDING);
		framePanel.add(displayScreenContainer, screenGbc);
	}

	private void formatDisplayScreen() {
		EmptyBorder paddingBorder = createPaddingBorder(DISPLAY_SCREEN_INTERNAL_PADDING);
		displayScreen.setBorder(paddingBorder);
		displayScreen.setEditable(false);
	}

	// format and add inputBox to framePanel
	private void formatAndAddInputBox() {
		formatInputBox();
		addInputBox();
	}

	// add inputBox to framePanel
	private void addInputBox() {
		GridBagConstraints inputGbc = createGridBagConstraint(
				INPUT_BOX_Y_POSITION, INPUT_BOX_HEIGHT,
				INPUT_BOX_HEIGHT_IMPORTANCE);
		Box inputBoxContainer = createContainerBox(inputBox,
				INPUT_BOX_EXTERNAL_PADDING);
		Box passwordBoxContainer = createContainerBox(passwordBox,
				INPUT_BOX_EXTERNAL_PADDING);
		framePanel.add(inputBoxContainer, inputGbc);
		framePanel.add(passwordBoxContainer, inputGbc);
	} 

	private void formatInputBox() {
		EmptyBorder paddingBorder = createPaddingBorder(INPUT_BOX_INTERNAL_PADDING);
		inputBox.setBorder(paddingBorder);
	}

	// create a gridBagConstraint which is used to specify layout parameters
	private GridBagConstraints createGridBagConstraint(int yPosition,
			int height, int heightImportance) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = yPosition;
		gbc.weightx = 1;
		gbc.weighty = heightImportance;
		gbc.gridwidth = 1;
		gbc.gridheight = height;
		gbc.fill = GridBagConstraints.BOTH;

		return gbc;
	}

	// this creates a box around the input component, with the specified amount
	// of padding
	private Box createContainerBox(Component component, int padding) {
		Box containerBox = Box.createHorizontalBox();
		EmptyBorder paddingBorder = createPaddingBorder(padding);
		containerBox.setBorder(paddingBorder);
		containerBox.add(component);
		return containerBox;
	}

	private EmptyBorder createPaddingBorder(int padding) {
		EmptyBorder paddingBorder = new EmptyBorder(padding, padding, padding,
				padding);
		return paddingBorder;
	}
	
	private void switchToPasswordBox() {
		inputBox.setVisible(false);
		passwordBox.setVisible(true);
		passwordBox.requestFocus();
	}
	
	private void switchToInputBox() {
		inputBox.setVisible(true);
		passwordBox.setVisible(false);
		inputBox.requestFocus();
	}
	
	private void selectInputType() {
		if(processor.isPasswordExpected()) {
			switchToPasswordBox();
		} else {
			switchToInputBox();
		}	
	}
	
	private String getInput() {
		String input;
		if(processor.isPasswordExpected()) {
			input = new String(passwordBox.getPassword());
		} else {
			input = inputBox.getText();
		}
		return input;
	}

	// meant to listen to inputBox for changes in text
	// when text is changed, it gets feedback on the text from processor
	// then displays the feedback to user
	class InputBoxTextChangedListener implements DocumentListener {
		public void changedUpdate(DocumentEvent arg0) {
			displayCommandFeddback();
		}

		public void insertUpdate(DocumentEvent arg0) {
			displayCommandFeddback();
		}

		public void removeUpdate(DocumentEvent arg0) {
			displayCommandFeddback();
		}

		private void displayCommandFeddback() {
			String command = inputBox.getText();
			if (command.equals("")) {
				hideHelper();
				return;
			}
			String feedback = processor.getCommandFeedback(command);
			showHelper(feedback);
			setInputBoxToActive();
		}
	}

	// meant to listen to inputBox for keys pressed
	// if the key pressed is enter
	// command in inputBox is executed through processor
	// the response of processor is then shown to the user
	class InputBoxKeyListener implements KeyListener {
		public void keyPressed(KeyEvent arg0) {
			if (arg0.getKeyCode() == ENTER_KEY) {
				sendCommandToProcessor();
				hideHelper();
				inputBox.setText("");
				passwordBox.setText("");
				selectInputType();
			}
		}

		public void keyReleased(KeyEvent arg0) {
		}

		public void keyTyped(KeyEvent arg0) {
		}

		private void sendCommandToProcessor() {
			String command = getInput();
			String response = processor.executeCommand(command);
			displayScreen.setText(response);
		}
	}
}
