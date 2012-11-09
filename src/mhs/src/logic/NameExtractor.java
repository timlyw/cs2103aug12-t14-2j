//@author A0086805X
package mhs.src.logic;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mhs.src.common.MhsLogger;

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
	 * These are the enum that are keywords relating to date/time that should
	 * not be used in name parsing.
	 */
	private enum SpecialKeyWords {
		at, by, from, to, on;
	}

	private int counter;
	private Queue<String> nameList;
	private static NameExtractor nameExtractor;
	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * This is the constructor for NameExtractor
	 */
	private NameExtractor() {
		logEnterMethod("NameExtractor");
		nameList = new LinkedList<String>();
		counter = 0;

		logExitMethod("NameExtractor");
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
		logEnterMethod("checkNameFormat");

		DateExtractor dateExtractor = DateExtractor.getDateExtractor();
		TimeExtractor timeExtractor = TimeExtractor.getTimeExtractor();
		assert (parseString != null);
		if (!(timeExtractor.checkTimeFormat(parseString) || dateExtractor
				.checkDateFormat(parseString))) {
			if (!isSpecialKeyWord(parseString)) {
				logger.exiting(getClass().getName(), this.getClass().getName());
				return true;
			}
		}
		logExitMethod("checkNameFormat");
		return false;

	}

	/**
	 * This is a method to check if a string is a special key word .
	 * 
	 * @param printString
	 * @return
	 */
	private boolean isSpecialKeyWord(String printString) {
		logEnterMethod("isSpecialKeyWord");
		for (SpecialKeyWords k : SpecialKeyWords.values()) {
			if (printString.equals(k.name())) {
				logger.exiting(getClass().getName(), this.getClass().getName());
				return true;
			}
		}
		logExitMethod("isSpecialKeyWord");
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
		logEnterMethod("processName");
		assert (parseString != null);
		try {
			String[] processArray = parseString.split(REGEX_WHITE_SPACE);
			Queue<String> nameQueue = new LinkedList<String>();

			for (counter = 0; counter < processArray.length; counter++) {
				if (CommandExtractor.getCommandExtractor().checkCommand(
						processArray[0]) && counter==0) {
					counter++;
				}
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
			logger.log(Level.FINER, e.getMessage());
			return null;
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.log(Level.FINER, e.getMessage());
			return null;
		}
		logExitMethod("processName");
		return nameList;
	}

	/**
	 * Method to format the name and add it to the list.
	 * 
	 * @param name
	 */
	private void addNameToList(String name) {
		logEnterMethod("addNameToList");
		name = name.trim();
		nameList.add(name);
		logExitMethod("addNameToList");
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
		logEnterMethod("processQuotationMarks");
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
			logger.log(Level.FINER, e.getMessage());
			return parseString;
		}
		logExitMethod("processQuotationMarks");
		return parseString;
	}

	/**
	 * Method to set the name that is within quotation marks.
	 * 
	 * @param name
	 *            This is the name within quotation marks.
	 */
	private void setNameInQuotationMarks(String name) {
		logEnterMethod("setNameInQuotationMarks");
		name = name.replace(REGEX_QUOTATION, "");
		addNameToList(name);
		logExitMethod("setNameInQuotationMarks");
	}

	/**
	 * Method to remove the string within quotation marks from the main string
	 * to be parsed.
	 * 
	 * @param printString
	 *            This is the string to be parsed.
	 * @param name
	 *            This is the name within quotation marks.
	 * 
	 * @return Returns the printString with the name removed.
	 */
	private String removeNameInQuotationMarks(String printString, String name) {
		logEnterMethod("removeNameInQuotationMarks");
		printString = printString.replace(name, "");
		logExitMethod("removeNameInQuotationMarks");
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
		logEnterMethod("setUpNameQueue");
		int j;

		Queue<String> commandQueue = new LinkedList<String>();
		for (j = counter; j < processArray.length; j++) {

			j = addSpecialKeyWordsToQueue(processArray, j, commandQueue);
			j = addIndexToQueue(processArray, j, commandQueue);

			if (checkNameFormat(processArray[j])) {
				commandQueue.add(processArray[j]);
			} else {
				break;
			}
		}
		counter = j - 1;
		logExitMethod("setUpNameQueue");
		return commandQueue;
	}

	/**
	 * Method to handle the case in which a number is added to the name.
	 * 
	 * @param processArray
	 *            The array of strings that is being processed.
	 * @param j
	 *            The index of where the array is now.
	 * @param commandQueue
	 *            The queue of names.
	 * 
	 * @return Returns the new index of the processArray.
	 */
	private int addIndexToQueue(String[] processArray, int j,
			Queue<String> commandQueue) {
		logEnterMethod("setUpNameQueue");
		DateExtractor dateExtractor = DateExtractor.getDateExtractor();
		if (isInteger(processArray[j])) {
			try {
				if (!(dateExtractor.checkDateFormat(processArray[j + 1]))) {
					commandQueue.add(processArray[j]);
					j++;
				}

			} catch (ArrayIndexOutOfBoundsException e) {
				logger.log(Level.FINER, e.getMessage());
				commandQueue.add(processArray[j]);
			}
		}
		logExitMethod("setUpNameQueue");

		return j;
	}

	/**
	 * Method to handle the case where special key words are included in the
	 * name.
	 * 
	 * @param processArray
	 *            The array of strings that is being processed.
	 * @param j
	 *            The index of where the array is now.
	 * @param commandQueue
	 *            The queue of names.
	 * 
	 * @return Returns the new index of the processArray.
	 */
	private int addSpecialKeyWordsToQueue(String[] processArray, int j,
			Queue<String> commandQueue) {
		logEnterMethod("addSpecialKeyWordsToQueue");

		for (SpecialKeyWords k : SpecialKeyWords.values()) {
			if (processArray[j].equalsIgnoreCase(k.name())
					&& !processArray[j].equalsIgnoreCase(k.to.name())) {
				try {
					if (checkNameFormat(processArray[j + 1])) {
						commandQueue.add(processArray[j]);
						j++;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					logger.log(Level.FINER, e.getMessage());

				}
			}
		}
		logExitMethod("addSpecialKeyWordsToQueue");
		return j;
	}

	/**
	 * This is the function to check if the string is an integer.
	 * 
	 * @param printString
	 *            This is the string to be checked.
	 * 
	 * @return Returns true if the string is an int.
	 */
	private boolean isInteger(String printString) {
		logEnterMethod("isInteger");
		try {
			Integer.parseInt(printString);
			logExitMethod("isInteger");
			return true;
		} catch (NumberFormatException e) {
			logExitMethod("isInteger");
			return false;
		}
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
		logEnterMethod("hasQuotations");
		try {
			Matcher matcher = Pattern.compile(REGEX_QUOTATION_MARKS).matcher(
					printString);
			if (matcher.find()) {
				logExitMethod("hasQuotations");
				return true;
			}
		} catch (NullPointerException e) {
			logger.log(Level.FINER, e.getMessage());
			return false;
		}
		logExitMethod("hasQuotations");
		return false;
	}

	/**
	 * Logger enter method
	 * 
	 * @param methodName
	 */
	void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}

	/**
	 * Logger enter method
	 * 
	 * @param methodName
	 */
	void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}
}
