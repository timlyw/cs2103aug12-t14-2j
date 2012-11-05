package mhs.src.logic;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mhs.src.common.MhsLogger;

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
	private static final Logger logger = MhsLogger.getLogger();

	private NameExtractor() {
		logger.entering(getClass().getName(), this.getClass().getName());
		nameList = new LinkedList<String>();
		counter = 0;
		logger.exiting(getClass().getName(), this.getClass().getName());
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
	 * @param parseString
	 *            This is the string to be checked.
	 * 
	 * @return Returns a true if valid.
	 */
	public boolean checkNameFormat(String parseString) {
		logger.entering(getClass().getName(), this.getClass().getName());

		DateExtractor dateExtractor = DateExtractor.getDateExtractor();
		TimeExtractor timeExtractor = TimeExtractor.getTimeExtractor();
		CommandExtractor commandExtractor = CommandExtractor
				.getCommandExtractor();
		assert (parseString != null);
		if (!(timeExtractor.checkTimeFormat(parseString)
				|| dateExtractor.checkDateFormat(parseString) || commandExtractor
					.isCommand(parseString))) {
			if (!isSpecialKeyWord(parseString)) {
				logger.exiting(getClass().getName(), this.getClass().getName());
				return true;
			}
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return false;

	}

	private boolean isSpecialKeyWord(String printString) {
		logger.entering(getClass().getName(), this.getClass().getName());
		for (SpecialKeyWords k : SpecialKeyWords.values()) {
			if (printString.equals(k.name())) {
				logger.exiting(getClass().getName(), this.getClass().getName());
				return true;
			}
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		assert (parseString != null);
		try {
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
		} catch (NullPointerException e) {
			logger.exiting(getClass().getName(), this.getClass().getName());
			return null;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return nameList;
	}

	private void addNameToList(String name) {
		logger.entering(getClass().getName(), this.getClass().getName());
		name = name.trim();
		nameList.add(name);
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	/**
	 * This is the function to process the name which is within quotation marks.
	 * 
	 * @param parseString
	 *            This is the entire string that needs to be processed.
	 * 
	 * @return Returns the string with everything within quotations removed
	 */
	public String processQuotationMarks(String parseString) {
		logger.entering(getClass().getName(), this.getClass().getName());
		assert (parseString != null);
		String name = "";
		nameList = new LinkedList<String>();
		if (hasQuotations(parseString)) {
			while (hasQuotations(parseString)) {
				Matcher matcher = Pattern.compile(REGEX_QUOTATION_MARKS)
						.matcher(parseString);

				if (matcher.find()) {
					name = matcher.group();
					parseString = removeNameInQuotationMarks(parseString, name);
					setNameInQuotationMarks(name);

				}
			}
		}
		try {
			parseString = parseString.trim();
		} catch (NullPointerException e) {
			return parseString;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return parseString;
	}

	private void setNameInQuotationMarks(String name) {
		logger.entering(getClass().getName(), this.getClass().getName());
		name = name.replace(REGEX_QUOTATION, "");
		addNameToList(name);
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private String removeNameInQuotationMarks(String printString, String name) {
		logger.entering(getClass().getName(), this.getClass().getName());
		printString = printString.replace(name, "");
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		int j;
		Queue<String> commandQueue = new LinkedList<String>();
		for (j = counter; j < processArray.length; j++) {

			for (SpecialKeyWords k : SpecialKeyWords.values()) {
				if (processArray[j].equalsIgnoreCase(k.name())
						&& !processArray[j].equalsIgnoreCase(k.to.name())) {
					try {
						if (checkNameFormat(processArray[j + 1])) {
							commandQueue.add(processArray[j]);
							System.out.println(processArray[j]);
							j++;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("oob");
					}
				}
			}

			if (checkNameFormat(processArray[j])) {
				commandQueue.add(processArray[j]);
			} else {
				break;
			}
		}
		counter = j - 1;
		logger.exiting(getClass().getName(), this.getClass().getName());
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
		logger.entering(getClass().getName(), this.getClass().getName());
		try {
			Matcher matcher = Pattern.compile(REGEX_QUOTATION_MARKS).matcher(
					printString);
			if (matcher.find()) {
				logger.exiting(getClass().getName(), this.getClass().getName());
				return true;
			}
		} catch (NullPointerException e) {
			return false;
		}
		logger.exiting(getClass().getName(), this.getClass().getName());
		return false;
	}

}
