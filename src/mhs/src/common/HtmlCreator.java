package mhs.src.common;

import com.sun.xml.internal.ws.util.StringUtils;


public class HtmlCreator {
	private static final String HTML_FEEDBACK = "<html><font color='#383838' size='5' face='calibri'>%1$s</font></html>";
	private static final String HTML_DISPLAY = "<html><font size='5' face='calibri'>%1$s</font></html>";
	private static final String HTML_TITLE = "<html><center><font size='5' face='calibri'>%1$s</font></center></html>";

	public static final String BLUE = "#3300CC";
	public static final String PURPLE = "#660066";
	public static final String ORANGE = "#FF6600";
	public static final String LIGHT_BLUE = "#3333FF";
	public static final String GRAY = "#7A7A59";
	public static final String LIGHT_GRAY = "#B8B8B8";

	private static final String FORMAT_BOLD = "<b>%1$s</b>";
	private static final String FORMAT_COLOR = "<font color=%1$s>%2$s</font>";

	public static final String NEW_LINE = "<br/>";
	
	public static final int DEFAULT_LINE_HEIGHT = 32;

	public String createFeedbackScreenHtml(String htmlBody) {
		String htmlText = String.format(HTML_FEEDBACK, htmlBody);
		return htmlText;
	}

	public String createTitleScreenHtml(String htmlBody) {
		String htmlText = String.format(HTML_TITLE, htmlBody);
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

	public String color(String htmlText, String color) {
		String boldString = String.format(FORMAT_COLOR, color, htmlText);
		return boldString;
	}
	
	public static int countNewLine(String htmlString) {
		int count = 0;
		int index = 0;
		while ((index = htmlString.indexOf(NEW_LINE, index)) != -1) {
		   count++;
		   index++;
		}
		return count;
	}
}
