package mhs.src.common;

public class HtmlCreator {
	private static final String DEFAULT_FONT = "calibri";
	private static final String DEFAULT_FONT_SIZE = "5";
	private static final String SMALL_FONT = "3";
	
	public static final String BLUE = "#0094f0";
	public static final String PURPLE = "#548dd4";
	public static final String ORANGE = "#FF6600";
	public static final String LIGHT_BLUE = "#0094f0";
	public static final String GRAY = "#a7a798";
	public static final String LIGHT_GRAY = "#B8B8B8";

	private static final String HTML_FEEDBACK = "<html><font color='#383838' size='" + DEFAULT_FONT_SIZE + "' face='" + DEFAULT_FONT  + "'>%1$s</font></html>";
	private static final String HTML_DISPLAY = "<html><font size='" + DEFAULT_FONT_SIZE + "' face='" + DEFAULT_FONT  + "'>%1$s</font></html>";
	private static final String HTML_TITLE = "<html><div align='right'><font color='" + GRAY + "' size='" + SMALL_FONT + "' face='" + DEFAULT_FONT  + "'>%1$s</font></div></html>";

	private static final String FORMAT_BOLD = "<b>%1$s</b>";
	private static final String FORMAT_COLOR = "<font color=%1$s>%2$s</font>";

	public static final String NEW_LINE = "<br/>";
	
	public static final int DEFAULT_LINE_HEIGHT = 30;
	
	public static final String LARGE_FONT = "6";
	public static final String FONT_SIZE_FORMAT = "<font face='courier' size=%1$s>%2$s</font>";

	public String largeFont(String htmlBody) {
		return String.format(FONT_SIZE_FORMAT, LARGE_FONT, htmlBody);
	}
	
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
