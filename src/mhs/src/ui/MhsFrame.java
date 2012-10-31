package mhs.src.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;

public class MhsFrame extends JFrame {

	private static enum INPUT_TYPE {PLAIN_TEXT, PASSWORD};
	
	private static final long serialVersionUID = 1L;
	private static final int FRAME_WIDTH = 550;
	private static final int FRAME_HEIGHT = 450;
	private static final String FRAME_TITLE = "My Hot Secretary";
	private static final Color FRAME_BACKGROUND_COLOR = new Color(0, 0, 0);

	private static final int DEFAULT_CONSTRAINT_POSITION_X = 0;
	private static final int DEFAULT_CONSTRAINT_WEIGHT_X = 1;
	private static final int DEFAULT_CONSTRAINT_WIDTH = 1;
	private static final int DEFAULT_PADDING_WIDTH = 2;
	private static final int DEFAULT_FONT_SIZE = 14;
	private static final String DEFAULT_FONT_TYPE = "calibri";

	private static final int DISPLAY_SCREEN_POSITION_Y = 0;
	private static final int DISPLAY_SCREEN_WEIGHT_Y = 1;
	private static final int DISPLAY_SCREEN_HEIGHT = 1;
	private static final int DISPLAY_SCREEN_BOTTOM_PADDING = 0;

	private static final int FEEDBACK_SCREEN_POSITION_Y = 1;
	private static final int FEEDBACK_SCREEN_WEIGHT_Y = 0;
	private static final int FEEDBACK_SCREEN_HEIGHT = 1;
	private static final int FEEDBACK_SCREEN_TOP_PADDING = 0;

	private static final int INPUT_BOX_POSITION_Y = 2;
	private static final int INPUT_BOX_WEIGHT_Y = 0;
	private static final int INPUT_BOX_HEIGHT = 1;
	private static final int INPUT_BOX_TOP_PADDING = 0;
	public static final Font INPUT_BOX_FONT = new Font(DEFAULT_FONT_TYPE,
			Font.BOLD, DEFAULT_FONT_SIZE);

	private static final String BLANK = "";
	
	private final JPanel framePanel = new JPanel();
	private final HtmlScreen displayScreen = new HtmlScreen();
	private final HtmlScreen feedbackScreen = new HtmlScreen();
	private final JTextField plainTextBox = new JTextField();
	private final JPasswordField passwordBox = new JPasswordField();
	
	private final HtmlCreator htmlCreator = new HtmlCreator();
	private INPUT_TYPE inputType = null;
	
	public MhsFrame() {
		super(FRAME_TITLE);
		initFrame();
	}

	public void open() {
		this.setVisible(true);
	}
	
	public int getDisplayScreenHeight() {
		int displayScreenHeight = displayScreen.getHeight();
		return displayScreenHeight;
	}

	public String getCommand() {
		String command;
		if(inputType == INPUT_TYPE.PLAIN_TEXT) {
			command = plainTextBox.getText();
		} else {
			char[] password = passwordBox.getPassword();
			command = new String(password);
		}
		return command;
	}
	
	public void addInputChangedListener(DocumentListener inputListener) {
		plainTextBox.getDocument().addDocumentListener(inputListener);
		passwordBox.getDocument().addDocumentListener(inputListener);
	}
	
	public void addInputKeyListener(KeyListener keyListener) {
		plainTextBox.addKeyListener(keyListener);
		passwordBox.addKeyListener(keyListener);
	}
	
	public void clearInput() {
		plainTextBox.setText(BLANK);
		passwordBox.setText(BLANK);
	}
	
	public void setDisplayText(String displayText) {
		String htmlText = htmlCreator.createDisplayScreenHtml(displayText);
		displayScreen.setHtml(htmlText);
	}

	public void setFeedbackText(String feedbackText) {
		String htmlText = htmlCreator.createFeedbackScreenHtml(feedbackText);
		feedbackScreen.setHtml(htmlText);
	}
	
	public void setInputToPlainText() {
		inputType = INPUT_TYPE.PLAIN_TEXT;
		selectInputBox();
	}
	
	public void setInputToPassword() {
		plainTextBox.setVisible(false);
		inputType = INPUT_TYPE.PASSWORD;
		selectInputBox();
	}
	
	public void selectInputBox() {
		if(inputType == INPUT_TYPE.PLAIN_TEXT) {
			selectPlainTextBox();
		} else {
			selectPasswordBox();
		}
	}
	
	private void selectPlainTextBox() {
		plainTextBox.requestFocus();	
	}
	
	private void selectPasswordBox() {
		passwordBox.requestFocus();
	}
	
	private void initFrame() {
		initFrameProperties();
		initFrameComponents();
	}

	private void initFrameProperties() {
		setFrameSize(FRAME_WIDTH, FRAME_HEIGHT);
		setFrameBackground(FRAME_BACKGROUND_COLOR);
		setFrameLocationToCenter();
		setFrameToExitOnClose();
		setFrameIcon();
	}

	private void setFrameToExitOnClose() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void setFrameSize(int width, int height) {
		this.setSize(width, height);
	}

	private void setFrameLocationToCenter() {
		this.setLocationRelativeTo(null);
	}

	private void setFrameBackground(Color backgroundColor) {
		this.setBackground(backgroundColor);
	}

	private void setFrameIcon() {
		ImageIcon icon = new ImageIcon(
				MhsFrame.class.getResource("mhsIconSmall.png"));
		this.setIconImage(icon.getImage());
	}

	private void initFrameComponents() {
		initFramePanel();
		initDisplayScreen();
		initFeedbackScreen();
		initInputBox();
	}

	private void initFramePanel() {
		addFramePanelToFrame();
		setFramePanelLayout();
		setFramePanelBackground(FRAME_BACKGROUND_COLOR);
	}

	private void initDisplayScreen() {
		addDisplayScreenToFramePanel();
	}

	private void initFeedbackScreen() {
		addFeedbackScreenToPanel();
	}

	private void addFeedbackScreenToPanel() {
		GridBagConstraints constraints = getDefaultConstraints(
				FEEDBACK_SCREEN_POSITION_Y, FEEDBACK_SCREEN_WEIGHT_Y,
				FEEDBACK_SCREEN_HEIGHT);

		Box feedbackScreenContainer = createContainer(feedbackScreen,
				DEFAULT_PADDING_WIDTH, FEEDBACK_SCREEN_TOP_PADDING,
				DEFAULT_PADDING_WIDTH);
		framePanel.add(feedbackScreenContainer, constraints);
	}

	private void initInputBox() {
		addPlainTextBoxToFramePanel();
		formatPlainTextBox();
		addPasswordBoxToFramePanel();
		formatPasswordBox();
		setInputToPlainText();
	}

	private void addPasswordBoxToFramePanel() {
		GridBagConstraints constraints = getDefaultConstraints(
				INPUT_BOX_POSITION_Y, INPUT_BOX_WEIGHT_Y, INPUT_BOX_HEIGHT);

		Box inputBoxContainer = createContainer(passwordBox,
				DEFAULT_PADDING_WIDTH, INPUT_BOX_TOP_PADDING,
				DEFAULT_PADDING_WIDTH);
		framePanel.add(inputBoxContainer, constraints);
	}
	
	private void formatPlainTextBox() {
		plainTextBox.setFont(INPUT_BOX_FONT);
		EmptyBorder paddingBorder = createPaddingBorder(DEFAULT_PADDING_WIDTH,
				DEFAULT_PADDING_WIDTH, DEFAULT_PADDING_WIDTH);
		plainTextBox.setBorder(paddingBorder);
	}
	
	private void formatPasswordBox() {
		passwordBox.setFont(INPUT_BOX_FONT);
		EmptyBorder paddingBorder = createPaddingBorder(DEFAULT_PADDING_WIDTH,
				DEFAULT_PADDING_WIDTH, DEFAULT_PADDING_WIDTH);
		passwordBox.setBorder(paddingBorder);
	}

	private void addPlainTextBoxToFramePanel() {
		GridBagConstraints constraints = getDefaultConstraints(
				INPUT_BOX_POSITION_Y, INPUT_BOX_WEIGHT_Y, INPUT_BOX_HEIGHT);

		Box inputBoxContainer = createContainer(plainTextBox,
				DEFAULT_PADDING_WIDTH, INPUT_BOX_TOP_PADDING,
				DEFAULT_PADDING_WIDTH);
		framePanel.add(inputBoxContainer, constraints);
	}

	private void addDisplayScreenToFramePanel() {
		GridBagConstraints constraints = getDefaultConstraints(
				DISPLAY_SCREEN_POSITION_Y, DISPLAY_SCREEN_WEIGHT_Y,
				DISPLAY_SCREEN_HEIGHT);

		Box displayScreenContainer = createContainer(displayScreen,
				DEFAULT_PADDING_WIDTH, DEFAULT_PADDING_WIDTH,
				DISPLAY_SCREEN_BOTTOM_PADDING);
		framePanel.add(displayScreenContainer, constraints);
	}

	private void addFramePanelToFrame() {
		this.getContentPane().add(framePanel);
	}

	private void setFramePanelLayout() {
		GridBagLayout gbLayout = new GridBagLayout();
		framePanel.setLayout(gbLayout);
	}

	private void setFramePanelBackground(Color backgroundColor) {
		framePanel.setBackground(FRAME_BACKGROUND_COLOR);
	}

	protected static GridBagConstraints getDefaultConstraints(int positionY,
			int weightY, int height) {
		GridBagConstraints gbc = new GridBagConstraints();
		setConstraintsPosition(gbc, DEFAULT_CONSTRAINT_POSITION_X, positionY);
		setConstraintsWeight(gbc, DEFAULT_CONSTRAINT_WEIGHT_X, weightY);
		setConstraintsSize(gbc, DEFAULT_CONSTRAINT_WIDTH, height);
		setConstraintsToFillAvailableSpace(gbc);
		return gbc;
	}

	private static void setConstraintsPosition(GridBagConstraints gbc, int positionX,
			int positionY) {
		gbc.gridx = positionX;
		gbc.gridy = positionY;
	}

	private static void setConstraintsWeight(GridBagConstraints gbc, int weightX,
			int weightY) {
		gbc.weightx = weightX;
		gbc.weighty = weightY;
	}

	private static void setConstraintsSize(GridBagConstraints gbc, int width,
			int height) {
		gbc.gridwidth = width;
		gbc.gridheight = height;
	}

	private static void setConstraintsToFillAvailableSpace(GridBagConstraints gbc) {
		gbc.fill = GridBagConstraints.BOTH;
	}

	private Box createContainer(Component component, int padding,
			int topPadding, int bottomPadding) {
		Box containerBox = Box.createHorizontalBox();
		EmptyBorder paddingBorder = createPaddingBorder(padding, topPadding,
				bottomPadding);
		containerBox.setBorder(paddingBorder);
		containerBox.add(component);
		return containerBox;
	}

	private EmptyBorder createPaddingBorder(int padding, int topPadding,
			int bottomPadding) {
		EmptyBorder paddingBorder = new EmptyBorder(topPadding, padding,
				bottomPadding, padding);
		return paddingBorder;
	}
}
