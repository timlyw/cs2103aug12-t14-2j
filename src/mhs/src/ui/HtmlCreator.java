package mhs.src.ui;

public class HtmlCreator {
	private static final String HTML_FEEDBACK = "<html><font color='#383838' size='5' face='calibri'>%1$s</font></html>";
	private static final String HTML_DISPLAY = "<html><font size='5' face='calibri'>%1$s</font></html>";
	private static final String FORMAT_BOLD = "<b>%1$s</b>";
	public final String NEW_LINE = "<br/>";
	
	public String createFeedbackScreenHtml(String htmlBody) {
		String htmlText = String.format(HTML_FEEDBACK, htmlBody);
		return htmlText;
	}
	
	public String createDisplayScreenHtml(String htmlBody) {
		String htmlText = String.format(HTML_DISPLAY, htmlBody);
		return htmlText;
	}
	
	public String makeBold(String htmlText) {
		String boldString = String.format(FORMAT_BOLD, htmlText);
		return boldString;
	}
}
