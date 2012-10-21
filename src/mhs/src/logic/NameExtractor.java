package mhs.src;

import java.util.LinkedList;
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

	// These are the regex statements.
	private static final String REGEX_BLANK = " ";
	private static final String REGEX_WHITE_SPACE = "\\s+";
	private static final String REGEX_QUOTATION_MARKS = "\"[^\"]*\"";
	private static final String REGEX_QUOTATION = "\"";

	/**
	 * These are the enum that are keywords relating to date/time.
	 */
	private enum SpecialKeyWords {
		at, by, from, to, on;
	}

	private int counter = 0;
	private Queue<String> nameList = new LinkedList<String>();

	/**
	 * This is the function to check the string if it is a name format.
	 * 
	 * @param printString
	 *            This is the string to be checked.
	 * 
	 * @return Returns a true if valid.
	 */
	public boolean checkNameFormat(String printString) {

		DateExtractor dateParser = new DateExtractor();
		TimeExtractor timeParser = new TimeExtractor();
		CommandExtractor commandParser = new CommandExtractor();

		if (!(timeParser.checkTimeFormat(printString)
				|| dateParser.checkDateFormat(printString) || commandParser
					.isCommand(printString))) {
			for (SpecialKeyWords k : SpecialKeyWords.values()) {
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
	 * @param processArray
	 *            This is a queue of the name formats.
	 * 
	 * @return Returns a string with the full task name.
	 */
	public Queue<String> processName(String parseString) {

		String[] processArray = parseString.split(REGEX_WHITE_SPACE);
		Queue<String> nameQueue = new LinkedList<String>();
		for (counter = 0; counter < processArray.length; counter++) {

			if (checkNameFormat(processArray[counter])) {
				nameQueue = setUpNameQueue(processArray);
				String name = "";
				while (!nameQueue.isEmpty()) {
					String command = nameQueue.poll();
					name += command + REGEX_BLANK;
				}
				name = name.trim();
				nameList.add(name);
			}

		}
		return nameList;
	}

	/**
	 * This is the function to process the name which is within quotation marks.
	 * 
	 * @param printString
	 *            This is the entire string that needs to be processed.
	 * 
	 * @return Returns the name highlighted in the first quotation marks
	 */
	public String processNameWithinQuotationMarks(String printString) {
		String name = "";
		if (hasQuotations(printString)) {
			while (hasQuotations(printString)) {
				Matcher matcher = Pattern.compile(REGEX_QUOTATION_MARKS)
						.matcher(printString);

				if (matcher.find()) {
					name = matcher.group();
					printString = printString.replace(name, "");
					name = name.replace(REGEX_QUOTATION, "");
					name = name.trim();
					nameList.add(name);

				}
			}
		}
		printString = printString.trim();
		return printString;
	}

	/**
	 * This is the function to set up a queue with all the name parameters in a
	 * row.
	 * 
	 * @param processArray
	 *            Takes in a string array.
	 * 
	 * @return Returns a queue with all the name parameters.
	 */
	private Queue<String> setUpNameQueue(String[] processArray) {
		int j;
		Queue<String> commandQueue = new LinkedList<String>();
		for (j = counter; j < processArray.length; j++) {
			if (checkNameFormat(processArray[j])) {
				commandQueue.add(processArray[j]);
			} else {
				break;
			}
		}
		counter = j - 1;
		return commandQueue;
	}

	/**
	 * This is the function to check if the string has any strings that are in
	 * quotation marks.
	 * 
	 * @param printString
	 *            This is the string to be checked.
	 * 
	 * @return Returns true if there are quotation marks.
	 */
	public boolean hasQuotations(String printString) {
		Matcher matcher = Pattern.compile(REGEX_QUOTATION_MARKS).matcher(
				printString);
		if (matcher.find()) {
			return true;
		}
		return false;
	}

}
