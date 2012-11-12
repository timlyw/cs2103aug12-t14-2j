//@author A0088015H

package mhs.src.common;

/**
 * This class contains the constants and methods to create basic HTML for MHS
 * 
 * @author John Wong
 * 
 */

public class HtmlCreator {
	// value returned by string method indexOf
	private static final int STRING_NOT_FOUND = -1;
	
	// preset font parameters
	private static final String DEFAULT_FONT = "calibri";
	private static final String DEFAULT_FONT_SIZE = "5";
	private static final String SMALL_FONT = "3";
	public static final String LARGE_FONT = "6";

	// preset colors
	public static final String BLUE = "#0094f0";
	public static final String RED = "#ff0000";
	public static final String PURPLE = "#548dd4";
	public static final String ORANGE = "#FF6600";
	public static final String LIGHT_BLUE = "#0094f0";
	public static final String GRAY = "#a7a798";
	public static final String LIGHT_GRAY = "#B8B8B8";
	
	// presets to create a table format
	public static final String TABLE_START = "<table>";
	public static final String TABLE_END = "</table>";
	private static final String TABLE_ROW_3 = "<tr><td>$1%s</td><td>$2%s</td><td>$3%s</td></tr>";

	// string to format a bold string
	private static final String FORMAT_BOLD = "<b>%1$s</b>";
	
	// string to format color a string
	private static final String FORMAT_COLOR = "<font color=%1$s>%2$s</font>";

	// string to create a new line
	public static final String NEW_LINE = "<br/>";

	// default height of one display line
	public static final int DEFAULT_LINE_HEIGHT = 30;

	// default width of one character
	public static final int DEFAULT_CHAR_WIDTH = 30;	
	
	// presets to format fonts
	public static final String FONT_SIZE_FORMAT = "<font face='courier' size=%1$s>%2$s</font>";
	public static final String FONT_SIZE_FORMAT_CALIBRI = "<font face='calibri' size=%1$s>%2$s</font>";

	// preset to create formatted feedback
	private static final String HTML_FEEDBACK = "<html><font color='#383838' size='"
			+ DEFAULT_FONT_SIZE
			+ "' face='"
			+ DEFAULT_FONT
			+ "'>%1$s</font></html>";
	
	// preset to create formatted display
	private static final String HTML_DISPLAY = "<html><font size='"
			+ DEFAULT_FONT_SIZE + "' face='" + DEFAULT_FONT
			+ "'>%1$s</font></html>";
	
	// preset to created formatted title
	private static final String HTML_TITLE = "<html><div align='right'><font color='"
			+ GRAY
			+ "' size='"
			+ SMALL_FONT
			+ "' face='"
			+ DEFAULT_FONT
			+ "'>%1$s</font></div></html>";

	/**
	 * reduce size of a string if it exceeds specifed max width
	 * 
	 * @param htmlBody
	 * @param maxWidth
	 * @return shortened string
	 */
	public static String shortenString(String htmlBody, int maxWidth) {
		if (htmlBody.length() > maxWidth) {
			htmlBody = htmlBody.substring(0, maxWidth);
		}

		return htmlBody;
	}

	/**
	 * set size of input string to LARGE_FONT
	 * 
	 * @param htmlBody
	 * @return enlarged font
	 */
	public String largeFont(String htmlBody) {
		return String.format(FONT_SIZE_FORMAT, LARGE_FONT, htmlBody);
	}

	/**
	 * set size of input string to SMALL_FONT
	 * 
	 * @param htmlBody
	 * @return html formatted font
	 */
	public String smallFont(String htmlBody) {
		return String.format(FONT_SIZE_FORMAT_CALIBRI, SMALL_FONT, htmlBody);
	}

	/**
	 * create formatted feedback string
	 * 
	 * @param htmlBody
	 * @return html formatted string
	 */
	public String createFeedbackScreenHtml(String htmlBody) {
		String htmlText = String.format(HTML_FEEDBACK, htmlBody);
		return htmlText;
	}

	/**
	 * create formatted string for title
	 * 
	 * @param htmlBody
	 * @return html formatted string
	 */
	public String createTitleScreenHtml(String htmlBody) {
		String htmlText = String.format(HTML_TITLE, htmlBody);
		return htmlText;
	}

	/**
	 * create formatted string for display
	 * 
	 * @param htmlBody
	 * @return html formatted string
	 */
	public String createDisplayScreenHtml(String htmlBody) {
		String htmlText = String.format(HTML_DISPLAY, htmlBody);
		return htmlText;
	}

	/**
	 * @param htmlText
	 * @return html bolded string
	 */
	public String makeBold(String htmlText) {
		String boldString = String.format(FORMAT_BOLD, htmlText);
		return boldString;
	}

	/**
	 * @param htmlText
	 * @param color
	 * @return html colored string
	 */
	public String color(String htmlText, String color) {
		String boldString = String.format(FORMAT_COLOR, color, htmlText);
		return boldString;
	}

	/**
	 * count number of new lines in htmlString
	 * 
	 * @param htmlString
	 * @return number of new lines
	 */
	public static int countNewLine(String htmlString) {
		int count = 0;
		int index = 0;
		while ((index = htmlString.indexOf(NEW_LINE, index)) != STRING_NOT_FOUND) {
			count++;
			index++;
		}
		return count;
	}

	/**
	 * create a html formatted table row with 3 columns
	 * 
	 * @param str1
	 * @param str2
	 * @param str3
	 * @return html formatted string
	 */
	public String createTableRow(String str1, String str2, String str3) {
		return String.format(TABLE_ROW_3, str1, str2, str3);
	}
}
