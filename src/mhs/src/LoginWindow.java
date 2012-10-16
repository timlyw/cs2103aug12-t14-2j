package mhs.src;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;


public class LoginWindow extends JFrame {

	// this serial is required for a JFrame
	static final long serialVersionUID = 1L;
	
	// frame parameters
	static final String FRAME_TITLE = "Google Calendar Login";
	static final int FRAME_WIDTH = 300;
	static final int FRAME_HEIGHT = 150;

	// instructionLabel parameters
	static final int TOP_LABEL_Y_POSITION = 0;
	static final int BOTTOM_LABEL_Y_POSITION = 3;	
	static final int EMAIL_BOX_Y_POSITION = 1;
	static final int PASSWORD_BOX_Y_POSITION = 2;
	
	// general component parameters
	static final int COMPONENT_HEIGHT = 1;
	static final int COMPONENT_HEIGHT_IMPORTANCE = 0;
	static final int COMPONENT_EXTERNAL_PADDING = 2;
	static final int COMPONENT_INTERNAL_PADDING = 5;

	static final int ENTER_KEY = KeyEvent.VK_ENTER;
	static final Color FRAME_COLOR = new Color(200, 200, 200);
	static final Color LABEL_FONT_COLOR = new Color(0, 0, 0);
	
	static final String INSTRUCTIONS_TOP = "Please login with your email and password";
	static final String INSTRUCTIONS_BOTTOM = "(Hit Enter to proceed)";
	static final String INSTRUCTIONS_EMAIL_BOX = "Enter email address here and password below";
	
	final JPanel framePanel= new JPanel();
	final JLabel topLabel = new JLabel();
	final JTextField emailBox = new JTextField();
	final JPasswordField passwordBox = new JPasswordField();
	final JLabel bottomLabel = new JLabel();
	
	ActionListener _confirmInputAction = null;
	
	LoginWindow(ActionListener confirmInputAction) {
		super(FRAME_TITLE);
		initFrame();
		_confirmInputAction = confirmInputAction;
	}

	public void open() {
		this.setVisible(true);
	}

	public void close() {
		this.setVisible(false);
	}
	
	public String getEmail() {
		return emailBox.getText();
	}
	
	public String getPassword() {
		String password = new String(passwordBox.getPassword());
		return password;
	}

	// set the basic frame properties and the frame's components
	private void initFrame() {
		initFrameProperties();
		initFrameComponents();
		initEventHandlers();
		setEmailBoxText();
	}
	
	private void setEmailBoxText() {
		emailBox.setText(INSTRUCTIONS_EMAIL_BOX);
		emailBox.selectAll();
	}
	
	private void initEventHandlers() {
		InputBoxKeyListener inputListener = new InputBoxKeyListener();
		emailBox.addKeyListener(inputListener);
		passwordBox.addKeyListener(inputListener);
	}
	
	// set the default method to close the frame
	// set the size and position of the frame
	private void initFrameProperties() {
		this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		this.setLocationRelativeTo(null); // set frame to open in center of
											// screen
		this.setBackground(FRAME_COLOR);
	}
	
	// format and add instructionLabel
	// format and add emailBox
	// format and add passwordBox
	private void initFrameComponents() {
		initFramePanel();
		initTopLabel();
		initEmailBox();
		initPasswordBox();
		initBottomLabel();
	}
	
	private void initFramePanel() {
		// using a GridBagLayout allows more control over layout
		GridBagLayout gbLayout = new GridBagLayout();
		framePanel.setLayout(gbLayout);
		framePanel.setBackground(FRAME_COLOR);
		this.getContentPane().add(framePanel);
	}

	private void initTopLabel() {
		formatTopLabel();
		addTopLabel();
	}
	
	private void formatTopLabel() {	
		EmptyBorder paddingBorder = createPaddingBorder(COMPONENT_INTERNAL_PADDING);
		topLabel.setBorder(paddingBorder);
		topLabel.setText(INSTRUCTIONS_TOP);
		topLabel.setForeground(LABEL_FONT_COLOR);
	}
	
	private void addTopLabel() {
		GridBagConstraints instructionLabelGbc = createGridBagConstraint(
				TOP_LABEL_Y_POSITION, COMPONENT_HEIGHT,
				COMPONENT_HEIGHT_IMPORTANCE);
		Box instructionLabelContainer = createContainerBox(topLabel,
				COMPONENT_EXTERNAL_PADDING);
		framePanel.add(instructionLabelContainer, instructionLabelGbc);
	}
	
	
	private void initEmailBox() {
		formatEmailBox();
		addEmailBox();
	}
	

	private void formatEmailBox() {	
		EmptyBorder paddingBorder = createPaddingBorder(COMPONENT_INTERNAL_PADDING);
		emailBox.setBorder(paddingBorder);
	}
	
	private void addEmailBox() {
		GridBagConstraints emailBoxGbc = createGridBagConstraint(
				EMAIL_BOX_Y_POSITION, COMPONENT_HEIGHT,
				COMPONENT_HEIGHT_IMPORTANCE);
		Box emailBoxContainer = createContainerBox(emailBox,
				COMPONENT_EXTERNAL_PADDING);
		framePanel.add(emailBoxContainer, emailBoxGbc);
	}
	
	private void initPasswordBox() {
		formatPasswordBox();
		addPasswordBox();
	}
	
	private void formatPasswordBox() {
		EmptyBorder paddingBorder = createPaddingBorder(COMPONENT_INTERNAL_PADDING);
		passwordBox.setBorder(paddingBorder);
	}
	
	private void addPasswordBox() {
		GridBagConstraints passwordGbc = createGridBagConstraint(
				PASSWORD_BOX_Y_POSITION, COMPONENT_HEIGHT,
				COMPONENT_HEIGHT_IMPORTANCE);
		Box passwordBoxContainer = createContainerBox(passwordBox,
				COMPONENT_EXTERNAL_PADDING);
		framePanel.add(passwordBoxContainer, passwordGbc);
	}
	
	private void initBottomLabel() {
		formatBottomLabel();
		addBottomLabel();
	}
	
	private void formatBottomLabel() {	
		EmptyBorder paddingBorder = createPaddingBorder(COMPONENT_INTERNAL_PADDING);
		bottomLabel.setBorder(paddingBorder);
		bottomLabel.setText(INSTRUCTIONS_BOTTOM);
		bottomLabel.setForeground(LABEL_FONT_COLOR);
	}
	
	private void addBottomLabel() {
		GridBagConstraints instructionLabelGbc = createGridBagConstraint(
				BOTTOM_LABEL_Y_POSITION, COMPONENT_HEIGHT,
				COMPONENT_HEIGHT_IMPORTANCE);
		Box bottomLabelContainer = createContainerBox(bottomLabel,
				COMPONENT_EXTERNAL_PADDING);
		framePanel.add(bottomLabelContainer, instructionLabelGbc);
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
	
	class InputBoxKeyListener implements KeyListener {
		public void keyPressed(KeyEvent arg0) {
			if (arg0.getKeyCode() == ENTER_KEY) {
				close();
				if(_confirmInputAction != null) {
					_confirmInputAction.actionPerformed(null);
				}
			}
		}

		public void keyReleased(KeyEvent arg0) {}

		public void keyTyped(KeyEvent arg0) {}
	}
}
