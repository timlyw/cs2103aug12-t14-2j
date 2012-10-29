package mhs.src.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public class HtmlScreen extends JPanel {
	private static final long serialVersionUID = 1L;
    private final JWebBrowser browser = new JWebBrowser();
    private final JPanel componentPanel = new JPanel();
    private static final int BROWSER_POSITION_Y = 0;
    private static final int BROWSER_WEIGHT_Y = 1;
    private static final int BROWSER_HEIGHT = 1;
    private static final int DEFAULT_FONT_SIZE = 18;
	private static final String DEFAULT_FONT_TYPE = "calibri";
	public static final Font DEFAULT_FONT = new Font(DEFAULT_FONT_TYPE,
			Font.BOLD, DEFAULT_FONT_SIZE);
    
	public HtmlScreen() {
	    super(new BorderLayout());
	    setComponentPanelLayout();
	    addComponentPanelToFrame();
	    addBrowserToPanel();
	    removeAllToolBars();
	}
	
	public void navigate(String location) {
		browser.navigate(location);
	}
	
	public void setHtml(String html) {
		browser.setHTMLContent(html);
	}
	
	private void removeAllToolBars() {
	    browser.setMenuBarVisible(false);
	    browser.setLocationBarVisible(false);
	    browser.setButtonBarVisible(false);
	    browser.setStatusBarVisible(false);
	}
	
	private void setComponentPanelLayout() {
		GridBagLayout gbLayout = new GridBagLayout();
	    componentPanel.setLayout(gbLayout);
	}
	
	private void addBrowserToPanel() {
		GridBagConstraints constraints = MhsFrame.getDefaultConstraints(
				BROWSER_POSITION_Y, BROWSER_WEIGHT_Y, BROWSER_HEIGHT);

	    JTextField heightAdjuster = new JTextField();
	    heightAdjuster.setFont(DEFAULT_FONT);
	    componentPanel.add(heightAdjuster, constraints);
	    
	    componentPanel.add(browser, constraints);
	}
	
	private void addComponentPanelToFrame() {
	    this.add(componentPanel, BorderLayout.CENTER);
	}
}

