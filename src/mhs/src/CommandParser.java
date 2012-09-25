package mhs.src;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Partial;

public class CommandParser {

	public Command getParsedCommand(String command) {

		DateExtractor dateParser = new DateExtractor();
		CommandExtractor commandParser = new CommandExtractor();
		TimeExtractor timeExtractor = new TimeExtractor();
		NameExtractor nameExtractor = new NameExtractor();

		Scanner inputString = new Scanner(System.in);
		String process = inputString.nextLine();
		String[] processArray = process.split(" ");

		boolean taskNameFlag = false;
		boolean timeFlag = false;
		boolean dateFlag = false;

		String command = null;
		String taskName = null;
		String edittedName = null;

		DateTime startDate = null;
		Partial startTime = null;
		DateTime endDate = null;
		Partial endTime = null;

		int j;

		for (int i = 0; i < processArray.length; i++) {

			if (CommandExtractor.checkCommandFormat(processArray[i])) {
				command = commandParser.getCommand();
			} else if (nameExtractor.checkNameFormat(processArray[i])) {
			} else if (timeParser.checkTimeFormat(processArray[i])) {
				System.out.println(processArray[i] + " is a time");
				if (!timeFlag) {
					startTime = timeParser.processTime(processArray[i]);
					timeFlag = true;
				} else if (timeFlag) {
					endTime = timeParser.processTime(processArray[i]);
				}
				System.out.println("output is " + startTime.toString());

			} else if (dateParser.checkDateFormat(processArray[i])) {
				Queue<String> commandQueue = new LinkedList<String>();
				for (j = i; j < processArray.length; j++) {
					if (dateParser.checkDateFormat(processArray[j])) {
						System.out.println(processArray[j] + " is a date");
						commandQueue.add(processArray[j]);
					} else {
						break;
					}
				}

				// int counter = dateParser.getCounter();
				i = j - 1;
				// i -= counter;
				if (!dateFlag) {
					startDate = dateParser.processDate(commandQueue);
					dateFlag = true;
				} else if (dateFlag) {
					endDate = dateParser.processDate(commandQueue);
				}
				System.out.println("output is " + startDate.toString());

			} else {
				System.out.println("invalid command");
			}

		}

		setUpCommandObject(command, taskName, edittedName, startDate,
				startTime, endDate, endTime);
	}

	private static void setUpCommandObject(String command, String taskName,
			String edittedName, LocalDate startDate, LocalTime startTime,
			LocalDate endDate, LocalTime endTime) {

		Command object = new Command(command, taskName, edittedName, startDate,
				startTime, endDate, endTime);

	}
}