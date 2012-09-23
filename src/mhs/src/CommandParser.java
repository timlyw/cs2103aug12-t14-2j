package mhs.src;

import CommandExtractor;
import DateExtractor;
import TimeExtractor;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import org.joda.time.DateTime;
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
		boolean dateFlag= false;
		
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
			} 
			else if (nameExtractor.checkNameFormat(processArray[i])) {
			} 
			else if (TimeExtractor.checkTimeFormat(processArray[i])) {
				System.out.println(processArray[i] + " is a time");
				if(!timeFlag){
				startTime = TimeExtractor.processTime(processArray[i]);
				timeFlag = true;
				}
				else if(timeFlag){
				endTime = TimeExtractor.processTime(processArray[i]);
				}
				System.out.println("output is " + startTime.toString());

			} else if (DateExtractor.checkDateFormat(processArray[i])) {


			} else {
				System.out.println("invalid command");
			}

		}
		return null;
	}

}
