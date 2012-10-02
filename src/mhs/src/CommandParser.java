package mhs.src;

import java.util.LinkedList;
import java.util.Queue;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class CommandParser {

	public Command getParsedCommand(String process) {

		DateExtractor dateParser = new DateExtractor();
		TimeExtractor timeParser = new TimeExtractor();
		CommandExtractor commandParser= new CommandExtractor();
		NameExtractor nameParser = new NameExtractor();

		boolean taskNameFlag = false;
		boolean timeFlag = false;
		boolean dateFlag= false;
		
		String command = null;
		String taskName = null;
		String edittedName = null;
		String tempName = null;
		
		LocalDate startDate = null;
		LocalDate endDate = null;
		LocalTime startTime = null;
		LocalTime endTime = null;
	
		while(nameParser.hasQuotations(process)){
		tempName = nameParser.getNameWithinQuotationMarks(process);
		if(tempName != ""){
			if(!taskNameFlag){
				taskName = tempName.replace("\"", "");
			}
			else{
				edittedName = tempName.replace("\"", "");
			}
			process = process.replace(tempName, "");
			process = process.trim();
			System.out.println(taskName);
			taskNameFlag = true;
		}
		}
		String[] processArray = process.split("\\s+");


		int j;

		
		for (int i = 0; i < processArray.length; i++) {
			
			if(i==0){
				if(commandParser.isCommand(processArray[0])){
				command = commandParser.getCommand(processArray[0]);
				}
				else {
				command = "add";
				}
			}

			
			if (nameParser.checkNameFormat(processArray[i])) {
				Queue<String> commandQueue = new LinkedList<String>();
				for (j = i; j < processArray.length; j++) {
					if (nameParser.checkNameFormat(processArray[j])) {
						commandQueue.add(processArray[j]);
					} else {
						break;
					}
				}
				if(!taskNameFlag){
					taskName = nameParser.processName(commandQueue);
				}
				else {
					edittedName = nameParser.processName(commandQueue);
				}
				taskNameFlag = true;
				i=j-1;
				System.out.println(taskName);
			
			}
			else if (timeParser.checkTimeFormat(processArray[i])) {
				if(!timeFlag){
				startTime = timeParser.processTime(processArray[i]);
				timeFlag = true;
				}
				else if(timeFlag){
				endTime = timeParser.processTime(processArray[i]);
				}
				

			} else if (dateParser.checkDateFormat(processArray[i])) {
				Queue<String> commandQueue = new LinkedList<String>();
				for (j = i; j < processArray.length; j++) {
					if (dateParser.checkDateFormat(processArray[j])) {
						commandQueue.add(processArray[j]);
					} else {
						break;
					}
				}
				i = j - 1;
				if(!dateFlag){
				startDate = dateParser.processDate(commandQueue);
				dateFlag = true;
				}
				else if(dateFlag){
				endDate = dateParser.processDate(commandQueue);
				}
				

			} else {
				System.out.println("invalid command");
			}

		}

		return setUpCommandObject(command, taskName, edittedName, startDate, startTime, endDate, endTime);

	}

	private static Command setUpCommandObject(String command, String taskName,
			String edittedName, LocalDate startDate, LocalTime startTime,
			LocalDate endDate, LocalTime endTime) {

		Command object = new Command(command, taskName, edittedName, startDate,
				startTime, endDate, endTime);
		return object;
	}
}