package mhs.src;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameExtractor {
	private enum keywords {
		at, by, from, to;
	}

	public static boolean checkNameFormat(String printString) {

		DateExtractor dateParser = new DateExtractor();
		TimeExtractor timeParser = new TimeExtractor();
		CommandExtractor commandParser = new CommandExtractor();
		if (!(timeParser.checkTimeFormat(printString)
				|| dateParser.checkDateFormat(printString)
				|| commandParser.isCommand(printString))) {
			for (keywords k : keywords.values()) {
				if (printString != k.name()) {
					return true;
				}
			}
		}
		return false;

	}

	public static String processName(Queue<String> commandQueue) {
		String name = "";

		while (!commandQueue.isEmpty()) {
			String command = commandQueue.poll();
			name += command + " ";
		}

		return name;
	}

	public String getNameWithinQuotationMarks(String printString) {
		String name = "";

		Matcher matcher = Pattern.compile("\"[^\"]*\"" + "|'[^']*'").matcher(
				printString);

		while (matcher.find()) {
			name = matcher.group();
		}

		return name;
	}

	public boolean hasQuotations(String printString) {
		Matcher matcher = Pattern.compile("\"[^\"]*\"" + "|'[^']*'").matcher(
				printString);
		while (matcher.find()) {
			return true;
		}
		return false;
	}

}
