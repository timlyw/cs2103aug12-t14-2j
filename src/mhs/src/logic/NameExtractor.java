package mhs.src.logic;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 
 * @author Cheong Kahou
 *A0086805X
 */

/**
 * This is the class to extract out the name.
 *
 */
public class NameExtractor {
	
	//These are the regex statements. 
	private static final String REGEX_WHITE_SPACE = " ";
	private static final String REGEX_QUOTATION_MARKS = "\"[^\"]*\"";

	/**
	 * These are the enum that are keywords relating to date/time.
	 */
	private enum keywords {
		at, by, from, to;
	}

	/**
	 * This is the function to check the string if it is a name format. 
	 * 
	 * @param printString This is the string to be checked. 
	 * 
	 * @return Returns a true if valid. 
	 */
	public boolean checkNameFormat(String printString) {

		DateExtractor dateParser = new DateExtractor();
		TimeExtractor timeParser = new TimeExtractor();
		CommandExtractor commandParser = new CommandExtractor();
		
		if (!(timeParser.checkTimeFormat(printString)
				|| dateParser.checkDateFormat(printString)
				|| commandParser.isCommand(printString))) {
			for (keywords k : keywords.values()) {
				if (printString.equals(k.name())) {
					return false;
				}
			}
			return true;
		}
		return false;

	}

	/**
	 * This is the function to process the name.
	 * 
	 * @param commandQueue This is a queue of the name formats.
	 * 
	 * @return Returns a string with the full task name. 
	 */
	public String processName(Queue<String> commandQueue) {
		String name = "";

		while (!commandQueue.isEmpty()) {
			String command = commandQueue.poll();
			name += command + REGEX_WHITE_SPACE;
		}
		name = name.trim();
		return name;
	}

	/**
	 * This is the function to process the name which is within quotation marks.
	 * 
	 * @param printString This is the entire string that needs to be processed. 
	 * 
	 * @return Returns the name highlighted in the first quotation marks 
	 */
	public String processNameWithinQuotationMarks(String printString) {
		String name = "";

		Matcher matcher = Pattern.compile(REGEX_QUOTATION_MARKS).matcher(
				printString);

		if(matcher.find()) {
			name = matcher.group();
		}

		return name;
	}

	/**
	 * This is the function to check if the string has any strings that are in quotation marks.
	 * 
	 * @param printString This is the string to be checked. 
	 * 
	 * @return Returns true if there are quotation marks. 
	 */
	public boolean hasQuotations(String printString) {
		Matcher matcher = Pattern.compile(REGEX_QUOTATION_MARKS).matcher(
				printString);
		if(matcher.find()) {
			return true;
		}
		return false;
	}

}