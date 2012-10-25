package mhs.src.logic;

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

	private int counter;
	private Queue<String> nameList;
	private static NameExtractor nameExtractor;

	private NameExtractor() {
		nameList = new LinkedList<String>();
		counter = 0;
	}

	public static NameExtractor getNameExtractor() {
		if (nameExtractor == null) {
			nameExtractor = new NameExtractor();
		}
		return nameExtractor;
	}

	/**
	 * This is the function to check the string if it is a name format.
	 * 
	 * @param printString
	 *            This is the string to be checked.
	 * 
	 * @return Returns a true if valid.
	 */
	public boolean checkNameFormat(String printString) {

		DateExtractor dateExtractor = DateExtractor.getDateExtractor();
		TimeExtractor timeExtractor = TimeExtractor.getTimeExtractor();
		CommandExtractor commandExtractor = CommandExtractor
				.getCommandExtractor();

		if (!(timeExtractor.checkTimeFormat(printString)
				|| dateExtractor.checkDateFormat(printString) || commandExtractor
					.isCommand(printString))) {
			if (!isSpecialKeyWord(printString)) {
				return true;
			}
		}
		return false;

	}

	private boolean isSpecialKeyWord(String printString) {
		for (SpecialKeyWords k : SpecialKeyWords.values()) {
			if (printString.equals(k.name())) {
				return true;
			}
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
				addNameToList(name);
			}

		}
		return nameList;
	}

	private void addNameToList(String name) {
		name = name.trim();
		nameList.add(name);
	}

	/**
	 * This is the function to process the name which is within quotation marks.
	 * 
	 * @param printString
	 *            This is the entire string that needs to be processed.
	 * 
	 * @return Returns the string with everything within quotations removed
	 */
	public String processQuotationMarks(String printString) {
		String name = "";
		nameList = new LinkedList<String>();
		if (hasQuotations(printString)) {
			while (hasQuotations(printString)) {
				Matcher matcher = Pattern.compile(REGEX_QUOTATION_MARKS)
						.matcher(printString);

				if (matcher.find()) {
					name = matcher.group();
					printString = removeNameInQuotationMarks(printString, name);
					setNameInQuotationMarks(name);

				}
			}
		}
		printString = printString.trim();
		return printString;
	}

	private void setNameInQuotationMarks(String name) {
		name = name.replace(REGEX_QUOTATION, "");
		addNameToList(name);
	}

	private String removeNameInQuotationMarks(String printString, String name) {
		printString = printString.replace(name, "");
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
	private boolean hasQuotations(String printString) {
		Matcher matcher = Pattern.compile(REGEX_QUOTATION_MARKS).matcher(
				printString);
		if (matcher.find()) {
			return true;
		}
		return false;
	}

}
