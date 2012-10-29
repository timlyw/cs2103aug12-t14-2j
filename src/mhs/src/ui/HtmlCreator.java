package mhs.src.ui;

public class HtmlCreator {
	private static final String FONT_TYPE_DEFAULT = "calibri;";
	private static final String COLOR_GRAY = "color: #383838;";
	private static final String STYLE_FEEDBACK = "margin-top:-8px; margin-left:-7px; font-family=" + FONT_TYPE_DEFAULT + COLOR_GRAY;
	private static final String STYLE_DISPLAY = "margin-top:-8px; margin-left:-7px; font-family=" + FONT_TYPE_DEFAULT;
	private static final String HTML_FEEDBACK = "<html><body style='overflow:hidden;'><div style='%1$s'>%2$s</div></body></html>";
	private static final String HTML_DISPLAY = "<html><body style='overflow:hidden;'><div style='%1$s'>%2$s</div></body></html>";
	private static final String FORMAT_BOLD = "<b>%1$s</b>";
	public final String NEW_LINE = "<br/>";
	
	public String createFeedbackScreenHtml(String htmlBody) {
		String htmlText = String.format(HTML_FEEDBACK, STYLE_FEEDBACK, htmlBody);
		return htmlText;
	}
	
	public String createDisplayScreenHtml(String htmlBody) {
		String htmlText = String.format(HTML_DISPLAY, STYLE_DISPLAY, htmlBody);
		return htmlText;
	}
	
	public String makeBold(String htmlText) {
		String boldString = String.format(FORMAT_BOLD, htmlText);
		return boldString;
	}
}
