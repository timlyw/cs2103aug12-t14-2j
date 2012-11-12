//@author A0086805X
package mhs.src.logic.parser;

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

		if (parseString == null) {
			return false;
		}
		DateExtractor dateExtractor = DateExtractor.getDateExtractor();
		TimeExtractor timeExtractor = TimeExtractor.getTimeExtractor();
		if (!(timeExtractor.checkTimeFormat(parseString) || dateExtractor
				.checkDateFormat(parseString))) {
			if (!isSpecialKeyWord(parseString)) {
				logExitMethod("checkNameFormat");
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
		assert(printString != null);
		for (SpecialKeyWords k : SpecialKeyWords.values()) {
			if (printString.equalsIgnoreCase(k.name())) {
				logExitMethod("isSpecialKeyWord");
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
	public Queue<String> extractName(String parseString) {
		logEnterMethod("extractName");
		if (parseString == null) {
			return null;
		}
		try {
			String[] processArray = parseString.split(REGEX_WHITE_SPACE);
			for (counter = 0; counter < processArray.length; counter++) {
				if (isCommandAtFirstLocation(processArray)) {
					counter++;
				}
				if (checkNameFormat(processArray[counter])) {
					String name = processName(processArray);
					addNameToList(name);
				}
			}
		} catch (NullPointerException e) {
			logger.log(Level.FINER, e.getMessage());
			return nameList;
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.log(Level.FINER, e.getMessage());
			return nameList;
		}
		logExitMethod("extractName");
		return nameList;
	}

	/**
	 * Method to process the name from the array.
	 * 
	 * @param processArray
	 *            Array to be parsed.
	 * 
	 * @return Returns the first name in the array.
	 */
	private String processName(String[] processArray) {
		logEnterMethod("processName");
		assert(processArray != null);
		Queue<String> nameQueue;
		nameQueue = setUpNameQueue(processArray);
		String name = "";
		while (!nameQueue.isEmpty()) {
			String command = nameQueue.poll();
			name += command + REGEX_BLANK;
		}
		logExitMethod("processName");
		return name;
	}

	/**
	 * Method to check if the first location is a command.
	 * 
	 * @param processArray
	 * @return
	 */
	private boolean isCommandAtFirstLocation(String[] processArray) {
		logEnterMethod("isCommandAtFirstLocation");
		assert(processArray != null);
		logExitMethod("isCommandAtFirstLocation");
		return CommandExtractor.getCommandExtractor().checkCommand(
				processArray[0])
				&& counter == 0;
	}

	/**
	 * Method to format the name and add it to the list.
	 * 
	 * @param name
	 */
	private void addNameToList(String name) {
		logEnterMethod("addNameToList");
		assert(name != null);
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
	public String extractNameInQuotationMarks(String parseString) {
		logEnterMethod("extractNameInQuotationMarks");

		if (parseString == null) {
			return null;
		}
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

		logExitMethod("extractNameInQuotationMarks");
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
		assert(name != null);
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
		assert(name != null);
		assert(printString != null);
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
		assert(processArray != null);
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
		assert(processArray != null);
		assert(commandQueue != null);

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
		assert(processArray != null);
		assert(commandQueue != null);

		for (SpecialKeyWords k : SpecialKeyWords.values()) {
			if (isSpecialKeyWordExcludingTo(processArray, j, k)) {
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
	 * Check if the the string is a special key word excluding to.
	 * 
	 * @param processArray
	 * @param j
	 * @param k
	 * @return
	 */
	private boolean isSpecialKeyWordExcludingTo(String[] processArray, int j,
			SpecialKeyWords k) {
		logEnterMethod("isSpecialKeyWordExcludingTo");
		assert(processArray != null);
		logExitMethod("isSpecialKeyWordExcludingTo");
		return processArray[j].equalsIgnoreCase(k.name())
				&& !processArray[j].equalsIgnoreCase(SpecialKeyWords.to.name());
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
		assert(printString != null);
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
