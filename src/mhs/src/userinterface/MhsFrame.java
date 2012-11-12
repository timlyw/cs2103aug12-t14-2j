//@author A0088015H

package mhs.src.userinterface;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;

import mhs.src.common.HtmlCreator;
import mhs.src.common.MhsLogger;

/**
 * MhsFrame provides the interface to display program output to user and
 * to read user input, this is provided by:
 * 		1) Display Screen: Show program output to user
 * 		2) Feedback Screen: Show command feedback to user
 * 		3) Input Box: two format types available	
 * 			a) plain text input
 * 			b) password input
 * 
 * @author John Wong
 */

public class MhsFrame extends JFrame {
	
	// this variable is required by JFrame
	private static final long serialVersionUID = 1L;
	
	private static final String TRAY_MENU_EXIT = "Exit";
	
	// frame parameters
	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 450;
	private static final String FRAME_TITLE = "My Hot Secretary";
	private static final Color FRAME_BACKGROUND_COLOR = new Color(0, 0, 0);
	private static final Color TITLE_BACKGROUND_COLOR = new Color(255, 255, 255);
	private static final String ICON_FILE_NAME = "mhsIconSmall.png";
	private static final String TRAY_ICON_FILE_NAME = "mhsTrayIcon.png";

	// default margin, position and font parameters
	private static final int DEFAULT_CONSTRAINT_POSITION_X = 0;
	private static final int DEFAULT_CONSTRAINT_WEIGHT_X = 1;
	private static final int DEFAULT_CONSTRAINT_WIDTH = 1;
	private static final int DEFAULT_PADDING_WIDTH = 2;
	private static final int DEFAULT_FONT_SIZE = 14;
	private static final String DEFAULT_FONT_TYPE = "calibri";
	private static final int NO_PADDING = 0;

	// display screen sizing and position parameters
	private static final int TITLE_SCREEN_POSITION_Y = 3;
	private static final int TITLE_SCREEN_WEIGHT_Y = 1;
	private static final int TITLE_SCREEN_HEIGHT = 1;
	private static final int TITLE_SCREEN_TOP_PADDING = 0;
	private static final int TITLE_SCREEN_BOTTOM_PADDING = 1;
	
	// display screen sizing and position parameters
	private static final int DISPLAY_SCREEN_POSITION_Y = 0;
	private static final int DISPLAY_SCREEN_WEIGHT_Y = 50;
	private static final int DISPLAY_SCREEN_HEIGHT = 1;
	private static final int DISPLAY_SCREEN_TOP_PADDING = 1;
	private static final int DISPLAY_SCREEN_BOTTOM_PADDING = 0;
	private static final int DISPLAY_SIDE_PADDING = 5;

	// feedback screen sizing and position parameters
	private static final int FEEDBACK_SCREEN_POSITION_Y = 1;
	private static final int FEEDBACK_SCREEN_WEIGHT_Y = 1;
	private static final int FEEDBACK_SCREEN_HEIGHT = 1;
	private static final int FEEDBACK_SCREEN_TOP_PADDING = 0;
	private static final String CONTENT_TYPE_HTML = "text/html";

	// input box sizing and position parameters
	private static final int INPUT_BOX_POSITION_Y = 2;
	private static final int INPUT_BOX_WEIGHT_Y = 1;
	private static final int INPUT_BOX_HEIGHT = 1;
	private static final int INPUT_BOX_TOP_PADDING = 0;
	private static final int INPUT_BOX_BOTTOM_PADDING = 0;
	public static final Font INPUT_BOX_FONT = new Font(DEFAULT_FONT_TYPE,
			Font.BOLD, DEFAULT_FONT_SIZE);

	// string used to blank out input boxes after user hits enter
	private static final String BLANK = "";
	
	// panel used to contain all frame components
	private final JPanel framePanel = new JPanel();

	// display area for program title
	private final JEditorPane titleScreen = new JEditorPane();
	
	// display area for program output
	private final JEditorPane displayScreen = new JEditorPane();
	
	// display area for feedback output
	private final JEditorPane feedbackScreen = new JEditorPane();
	
	// input area for plain text
	private final JTextField inputBox = new JTextField();
	
	// used to format text for display and feedback
	private final HtmlCreator htmlCreator = new HtmlCreator();
	
	// used to keep track if input is enabled
	private boolean isInputDisabled = false;
	
	// singleton instance
	private static MhsFrame mhsFrameInstance = null;
	
	// logger used to log function calls
	private final Logger logger = MhsLogger.getLogger();
	
	// class name used for logging
	private static final String CLASS_NAME = "MhsFrame";
	
	TrayIcon trayIcon = null;
	
	/**
	 * MhsFrame's constructor is private, this function returns the 
	 * only instance of MhsFrame 
	 * 
	 * @return single instance of MhsFrame
	 */
	public static MhsFrame getInstance() {
		if(mhsFrameInstance == null) {
			mhsFrameInstance = new MhsFrame();
		}
		return mhsFrameInstance;
	}
	
	/**
	 * minimize MhsFrame
	 */
	public void minimize() {
		this.setExtendedState(JFrame.ICONIFIED);
	}
	
	/**
	 * makes the frame visible to user
	 */
	public void open() {
		startLog("open");
		this.setVisible(true);
		endLog("open");
	}
	
	/**
	 * hide frame from user
	 */
	public void close() {
		this.setVisible(false);
	}
	
	/**
	 * @return whether input is currently disabled
	 */
	public boolean inputDisabled() {
		return isInputDisabled;
	}
	
	/**
	 * @return height of the displayScreen
	 */
	public int getDisplayScreenHeight() {
		int displayScreenHeight = displayScreen.getHeight();
		return displayScreenHeight;
	}

	/**
	 * get the current command based on whether current input type
	 * 
	 * @return current user command
	 */
	public String getCommand() {
		return inputBox.getText();
	}
	
	/**
	 * add inputListener to listen for changes in input text
	 * 
	 * @param inputListener
	 */
	public void addInputChangedListener(DocumentListener inputListener) {
		inputBox.getDocument().addDocumentListener(inputListener);
	}
	
	/**
	 * add listener to observe mouse action on tray icon
	 * 
	 * @param trayListener
	 */
	public void addTrayListener(MouseListener trayListener) {
		trayIcon.addMouseListener(trayListener);
	}
	
	/**
	 * add keyListener to listen for when and which keys are pressed in input box
	 * 
	 * @param keyListener
	 */
	public void addInputKeyListener(KeyListener keyListener) {
		inputBox.addKeyListener(keyListener);
	}
	
	/**
	 * clear input from plainTextBox and passwordBox
	 */
	public void clearInput() {
		isInputDisabled = true;
		inputBox.setText(BLANK);
		isInputDisabled = false;
	}
	
	/**
	 * set the text for title screen
	 * 
	 * @param titleText
	 */
	public void setTitleText(String titleText) {
		String htmlText = htmlCreator.createTitleScreenHtml(titleText);
		titleScreen.setText(htmlText);
		titleScreen.repaint();
	}
	
	/**
	 * Show output to user in displayScreen
	 * 
	 * @param displayText
	 */
	public void setDisplayText(String displayText) {
		String htmlText = htmlCreator.createDisplayScreenHtml(displayText);
		displayScreen.setText(htmlText);
	}

	/**
	 * Show output to user in feedbackScreen
	 * 
	 * @param feedbackText
	 */
	public void setFeedbackText(String feedbackText) {
		String htmlText = htmlCreator.createFeedbackScreenHtml(feedbackText);
		feedbackScreen.setText(htmlText);
	}
	
	/**
	 * select input box based on current input format
	 */
	public void selectInputBox() {
		inputBox.requestFocus();	
	}
	
	/**
	 * set size of mhsFrame
	 * 
	 * @param width
	 * @param height
	 * @param maximized
	 */
	public void setSize(int width, int height, boolean maximized) {
		if(maximized) {
			this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		} else {
			setFrameSize(width, height);
			this.setLocationRelativeTo(null);
			this.setExtendedState(JFrame.NORMAL);
		}
	}
	
	/**
	 * @return if framePanel is initialized within MhsFrame
	 */
	public boolean isFramePanelInitialized() {
		return framePanel.getParent()== this.getContentPane();
	}

	/**
	 * @return if title screen is initialized within MhsFrame
	 */
	public boolean isTitleScreenInitialized() {
		return titleScreen.getParent().getParent() == framePanel;
	}
	
	/**
	 * @return if displayScreen is initialized within MhsFrame
	 */
	public boolean isDisplayScreenInitialized() {
		return displayScreen.getParent().getParent() == framePanel;
	}
	
	/**
	 * @return if feedbackScreen is initialized within MhsFrame
	 */
	public boolean isFeedbackScreenInitialized() {
		return feedbackScreen.getParent().getParent() == framePanel;
	}

	/**
	 * @return if inputBox is initialized within MhsFrame
	 */
	public boolean isInputBoxInitialized() {
		return inputBox.getParent().getParent() == framePanel;
	}
	
	
	/**
	 * private constructor to initialize an instance of MhsFrame
	 */
	private MhsFrame() {
		super(FRAME_TITLE);
		initFrame();
		//this.setUndecorated(true);
	}
	
	/**
	 * initialize frame properties and components
	 */
	private void initFrame() {
		startLog("initFrame");
		initFrameProperties();
		initFrameComponents();
		initTrayIcon();
		endLog("initFrame");
	}
	
	/**
	 * initialize tray icon in user's system tray
	 */
	private void initTrayIcon() {
		if(SystemTray.isSupported()) {
			createTrayIcon();
			addTrayIconToSystemTray(trayIcon);
			addTrayIconMenu(trayIcon);
		}
	}
	
	/**
	 * create a tray icon instance
	 */
	private void createTrayIcon() {
		ImageIcon icon = new ImageIcon(
				MhsFrame.class.getResource(TRAY_ICON_FILE_NAME));
		trayIcon = new TrayIcon(icon.getImage(), FRAME_TITLE);
	}

	/**
	 * add tray icon to user's system tray
	 * 
	 * @param icon
	 */
	private void addTrayIconToSystemTray(TrayIcon icon) {
		if(icon == null) {
			return;
		}
		try {
			SystemTray systemTray = SystemTray.getSystemTray();
			systemTray.add(icon);
		} catch (AWTException e) {
		}
	}
	
	/**
	 * add basic menu to tray icon
	 * 
	 * @param icon
	 */
	private void addTrayIconMenu(TrayIcon icon) {
		if(icon == null) {
			return;
		}
		PopupMenu trayMenu = new PopupMenu();
		
		MenuItem exitItem = new MenuItem(TRAY_MENU_EXIT);
		ClickTrayExitItem clickExit = new ClickTrayExitItem();
		exitItem.addActionListener(clickExit);
		
		trayMenu.add(exitItem);
		icon.setPopupMenu(trayMenu);
	}
	
	/**
	 * initialize frame size, background color, location, exit action and icon
	 */
	private void initFrameProperties() {
		setFrameSize(FRAME_WIDTH, FRAME_HEIGHT);
		setFrameBackground(FRAME_BACKGROUND_COLOR);
		setFrameLocationToCenter();
		setFrameToExitOnClose();
		setFrameIcon();
	}

	/**
	 * set frame to exit application when user closes frame
	 */
	private void setFrameToExitOnClose() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * @param width width of frame
	 * @param height height of frame
	 */
	private void setFrameSize(int width, int height) {
		this.setSize(width, height);
	}

	/**
	 * set location of frame to center of screen
	 */
	private void setFrameLocationToCenter() {
		this.setLocationRelativeTo(null);
	}

	/**
	 * @param backgroundColor
	 */
	private void setFrameBackground(Color backgroundColor) {
		this.setBackground(backgroundColor);
	}

	/**
	 * sets the frame's icon (appears on task bar as well)
	 */
	private void setFrameIcon() {
		ImageIcon icon = new ImageIcon(
				MhsFrame.class.getResource(ICON_FILE_NAME));
		this.setIconImage(icon.getImage());
	}

	/**
	 * initialize the frame's components
	 */
	private void initFrameComponents() {
		initFramePanel();
		initTitleScreen();
		initDisplayScreen();
		initFeedbackScreen();
		initInputBox();
	}
	
	/**
	 * add and format titleScreen
	 */
	private void initTitleScreen() {
		addTitleScreenToFramePanel();
		formatEditorPane(titleScreen);
		setTitleBackground(TITLE_BACKGROUND_COLOR);
	}
	
	/**
	 * set background of titleScreen
	 * 
	 * @param backgroundColor
	 */
	private void setTitleBackground(Color backgroundColor) {
		titleScreen.setBackground(TITLE_BACKGROUND_COLOR);
	}
	
	/**
	 * add titleScreen to framePanel
	 */
	private void addTitleScreenToFramePanel() {
		GridBagConstraints constraints = getDefaultConstraints(
				TITLE_SCREEN_POSITION_Y, TITLE_SCREEN_WEIGHT_Y,
				TITLE_SCREEN_HEIGHT);

		Box titleScreenContainer = createContainer(titleScreen,
				DEFAULT_PADDING_WIDTH, TITLE_SCREEN_TOP_PADDING,
				TITLE_SCREEN_BOTTOM_PADDING);
		framePanel.add(titleScreenContainer, constraints);
	}

	/**
	 * add and format the frame panel
	 */
	private void initFramePanel() {
		addFramePanelToFrame();
		setFramePanelLayout();
		setFramePanelBackground(FRAME_BACKGROUND_COLOR);
	}

	/**
	 * add and format the display screen
	 */
	private void initDisplayScreen() {
		addDisplayScreenToFramePanel();
		formatEditorPane(displayScreen);
	}

	/**
	 * add and format the feedback screen
	 */
	private void initFeedbackScreen() {
		addFeedbackScreenToPanel();
		formatEditorPane(feedbackScreen);
	}

	/**
	 * add the feedback screen to panel based on parameters defined at top of class
	 */
	private void addFeedbackScreenToPanel() {
		GridBagConstraints constraints = getDefaultConstraints(
				FEEDBACK_SCREEN_POSITION_Y, FEEDBACK_SCREEN_WEIGHT_Y,
				FEEDBACK_SCREEN_HEIGHT);

		Box feedbackScreenContainer = createContainer(feedbackScreen,
				DEFAULT_PADDING_WIDTH, FEEDBACK_SCREEN_TOP_PADDING,
				DEFAULT_PADDING_WIDTH);
		framePanel.add(feedbackScreenContainer, constraints);
	}

	/**
	 * add and format the password and plain text box to frame panel
	 */
	private void initInputBox() {
		addInputBoxToFramePanel();
		formatInputBox();
	}
	
	/**
	 * add the plain text box to framePanel
	 */
	private void formatInputBox() {
		inputBox.setFont(INPUT_BOX_FONT);
		EmptyBorder paddingBorder = createPaddingBorder(DEFAULT_PADDING_WIDTH,
				DEFAULT_PADDING_WIDTH, DEFAULT_PADDING_WIDTH);
		inputBox.setBorder(paddingBorder);
	}

	/**
	 * add plain text box to the frame panel
	 */
	private void addInputBoxToFramePanel() {
		GridBagConstraints constraints = getDefaultConstraints(
				INPUT_BOX_POSITION_Y, INPUT_BOX_WEIGHT_Y, INPUT_BOX_HEIGHT);

		Box inputBoxContainer = createContainer(inputBox,
				DEFAULT_PADDING_WIDTH, INPUT_BOX_TOP_PADDING,
				INPUT_BOX_BOTTOM_PADDING);
		framePanel.add(inputBoxContainer, constraints);
	}

	/**
	 * add displayScreen to the framePanel
	 */
	private void addDisplayScreenToFramePanel() {
		GridBagConstraints constraints = getDefaultConstraints(
				DISPLAY_SCREEN_POSITION_Y, DISPLAY_SCREEN_WEIGHT_Y,
				DISPLAY_SCREEN_HEIGHT);

		Box displayScreenContainer = createContainer(displayScreen,
				DEFAULT_PADDING_WIDTH, DISPLAY_SCREEN_TOP_PADDING,
				DISPLAY_SCREEN_BOTTOM_PADDING);
		framePanel.add(displayScreenContainer, constraints);
	}

	/**
	 * format a JEditorPane object
	 * 
	 * @param editorPane
	 */
	private void formatEditorPane(JEditorPane editorPane) {
		editorPane.setEditable(false);
		editorPane.setContentType(CONTENT_TYPE_HTML);
		EmptyBorder padding = createPaddingBorder(DISPLAY_SIDE_PADDING, NO_PADDING, NO_PADDING);
		editorPane.setBorder(padding);
	}
	
	/**
	 * add framePanel to the frame
	 */
	private void addFramePanelToFrame() {
		this.getContentPane().add(framePanel);
	}

	/**
	 * set framePanel layout to GridBagLayout as it allows good control over positioning
	 */
	private void setFramePanelLayout() {
		GridBagLayout gbLayout = new GridBagLayout();
		framePanel.setLayout(gbLayout);
	}

	/**
	 * @param backgroundColor
	 */
	private void setFramePanelBackground(Color backgroundColor) {
		framePanel.setBackground(FRAME_BACKGROUND_COLOR);
	}

	/**
	 * create a GridBagConstraints object to specify the position and size 
	 * of a component within a GridBagLayout
	 * 
	 * @param positionY
	 * @param weightY
	 * @param height
	 * @return created GridBagConstraint
	 */
	protected static GridBagConstraints getDefaultConstraints(int positionY,
			int weightY, int height) {
		GridBagConstraints gbc = new GridBagConstraints();
		setConstraintsPosition(gbc, DEFAULT_CONSTRAINT_POSITION_X, positionY);
		setConstraintsWeight(gbc, DEFAULT_CONSTRAINT_WEIGHT_X, weightY);
		setConstraintsSize(gbc, DEFAULT_CONSTRAINT_WIDTH, height);
		setConstraintsToFillAvailableSpace(gbc);
		return gbc;
	}

	/**
	 * set position of a GridBagConstraint
	 * 
	 * @param gbc
	 * @param positionX
	 * @param positionY
	 */
	private static void setConstraintsPosition(GridBagConstraints gbc, int positionX,
			int positionY) {
		gbc.gridx = positionX;
		gbc.gridy = positionY;
	}

	/**
	 * set weight parameters of a GridBagConstraint
	 * 
	 * @param gbc
	 * @param weightX
	 * @param weightY
	 */
	private static void setConstraintsWeight(GridBagConstraints gbc, int weightX,
			int weightY) {
		gbc.weightx = weightX;
		gbc.weighty = weightY;
	}

	/**
	 * set size of a GridBagConstraint
	 * 
	 * @param gbc
	 * @param width
	 * @param height
	 */
	private static void setConstraintsSize(GridBagConstraints gbc, int width,
			int height) {
		gbc.gridwidth = width;
		gbc.gridheight = height;
	}

	/**
	 * set the GridBagConstraint to fill all available space
	 * 
	 * @param gbc
	 */
	private static void setConstraintsToFillAvailableSpace(GridBagConstraints gbc) {
		gbc.fill = GridBagConstraints.BOTH;
	}

	/**
	 * create a container box around the component to create external margins
	 * 
	 * @param component
	 * @param padding
	 * @param topPadding
	 * @param bottomPadding
	 * @return
	 */
	private Box createContainer(Component component, int padding,
			int topPadding, int bottomPadding) {
		Box containerBox = Box.createHorizontalBox();
		EmptyBorder paddingBorder = createPaddingBorder(padding, topPadding,
				bottomPadding);
		containerBox.setBorder(paddingBorder);
		containerBox.add(component);
		return containerBox;
	}

	/**
	 * generate a padding border used to specify margins
	 * 
	 * @param padding
	 * @param topPadding
	 * @param bottomPadding
	 * @return
	 */
	private EmptyBorder createPaddingBorder(int padding, int topPadding,
			int bottomPadding) {
		EmptyBorder paddingBorder = new EmptyBorder(topPadding, padding,
				bottomPadding, padding);
		return paddingBorder;
	}
	
	private void startLog(String methodName) {
		logger.entering(CLASS_NAME, methodName);
	}
	
	private void endLog(String methodName) {
		logger.entering(CLASS_NAME, methodName);
	}
	
	private void closeApplication() {
		System.exit(0);
	}
	
	/**
	 * action listener to close application when user selects exit
	 */
	private class ClickTrayExitItem implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			closeApplication();
		}
	}
	
}
