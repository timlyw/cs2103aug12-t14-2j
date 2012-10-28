package mhs.src.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public class HtmlScreen extends JPanel {
	private static final long serialVersionUID = 1L;
    private final JWebBrowser browser = new JWebBrowser();
    private final JPanel componentPanel = new JPanel();
    
	public HtmlScreen() {
	    super(new BorderLayout());
	    setComponentPanelLayout();
	    addComponentPanelToFrame();
	    addBrowserToPanel();
	    removeAllToolBars();
	}
	
	private void removeAllToolBars() {
	    browser.setMenuBarVisible(false);
	    browser.setLocationBarVisible(false);
	    browser.setButtonBarVisible(false);
	    browser.setStatusBarVisible(false);
	}
	
	private void setComponentPanelLayout() {
	    BorderLayout panelLayout = new BorderLayout();
	    componentPanel.setLayout(panelLayout);
	}
	
	private void addBrowserToPanel() {
	    componentPanel.add(browser, BorderLayout.CENTER);
	}
	
	private void addComponentPanelToFrame() {
	    this.add(componentPanel, BorderLayout.CENTER);
	}
	
	public void navigate(String location) {
		browser.navigate(location);
	}
	
	public void setHtml(String html) {
		browser.setHTMLContent(html);
	}
}

