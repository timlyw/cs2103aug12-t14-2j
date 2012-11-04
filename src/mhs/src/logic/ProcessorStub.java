package mhs.src.logic;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import mhs.src.common.HtmlCreator;
import mhs.src.storage.persistence.task.DeadlineTask;
import mhs.src.storage.persistence.task.FloatingTask;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;
import mhs.src.storage.persistence.task.TimedTask;

public class ProcessorStub {
	
	private ArrayList<StateListener> stateListeners = new ArrayList<StateListener>();
	private String commandFeedback = null;
	private HtmlCreator htmlCreator = new HtmlCreator();
	private String currentCommand = null;
	private String currentState = null;
	private boolean isPasswordExpected = false;
	private int lineLimit = 0;
	public int LINE_HEIGHT = 20;

	public String headerText = "My Hot Secretary ";
	
	public String getHeaderText() {
		String boldTitle = htmlCreator.makeBold(headerText);
		return boldTitle;
	}
	
	public void addStateListener(StateListener stateListener) {
		stateListeners.add(stateListener);
	}
	
	public void setLineLimit(int limit) {
		lineLimit = limit;
		populateEvents();
		updateStateListeners();
	}
	
	public void executeCommand() {
		String boldCommand = htmlCreator.makeBold(currentCommand);
		commandFeedback = "command " + boldCommand + " executed";
		
		populateEvents();
		
		if(currentCommand.equals("password")) {
			isPasswordExpected = true;
		} else {
			isPasswordExpected = false;	
		}
		
		updateStateListeners();
	}
	
	public void setCommand(String command) {
		currentCommand = command;
		String boldCommand = htmlCreator.makeBold(currentCommand);
		commandFeedback = "feedback for " + boldCommand;
		updateStateListeners();
	}
	
	public String getCommandFeedback() {
		return commandFeedback;
	}
	
	
	public boolean passwordExpected() {
		return isPasswordExpected;
	}
	
	public String getState() {
		return currentState;
	}
	
	public void updateStateListeners() {
		for(int i = 0; i < stateListeners.size(); i++) {
			StateListener stateListener = stateListeners.get(i);
			stateListener.stateChanged();
		}
	}
	
	private void populateEvents() {
		/*
		String boldEventTitle = htmlCreator.makeBold("Event Title [COMPLETED]");
		String startDate = "12 Oct 2012 3pm";
		String endDate = "15 Oct 2012 7pm";
		
		currentState = boldEventTitle + " " + startDate + " - " + endDate + htmlCreator.NEW_LINE; 
		
		for(int i = 0; i < lineLimit; i++) {
			currentState += "event" + Integer.toString(i);
			currentState += htmlCreator.NEW_LINE;
		}
		
		currentState += "end";
		*/

		if(lineLimit < 0 ) {
			return;
		}
		
		List<Task> taskList = new ArrayList<Task>();
		
		currentState = "";
		
		String title0 = "floating task";
		FloatingTask fTask = createFloatingTask(title0);
		
		String title0a = "floating task";
		FloatingTask fTask2 = createFloatingTask(title0a);
		fTask2.setDone(true);
		
		String title1 = "do laundry";
		String startTime1 = "2012-11-17T08:00:00+08:00";
		String endTime1 = "2012-11-17T09:00:00+08:00";
		TimedTask task1 = createTimedTask(title1, startTime1, endTime1);
		
		String title2 = "watch tv";
		String startTime2 = "2012-11-17T12:00:00+08:00";
		String endTime2 = "2012-11-17T13:00:00+08:00";
		TimedTask task2 = createTimedTask(title2, startTime2, endTime2);
		
		String title2b = "deadline task";
		String endTime2b = "2012-11-17T11:30:00+08:00";
		DeadlineTask task2b = createDeadlineTask(title2b, endTime2b);
		
		String title3 = "homework";
		String startTime3 = "2012-11-19T17:00:00+08:00";
		String endTime3 = "2012-11-19T18:00:00+08:00";
		TimedTask task3 = createTimedTask(title3, startTime3, endTime3);

		taskList.add((Task) fTask);
		taskList.add((Task) fTask2);
		taskList.add((Task) task1);
		taskList.add((Task) task2b);
		taskList.add((Task) task2);
		taskList.add((Task) task3);
		
		
	}
	
	
	private TimedTask createTimedTask(String title, String startTime, String endTime) {
		DateTime start = DateTime.parse(startTime);
		DateTime end = DateTime.parse(endTime);

		TimedTask task = new TimedTask(1, title, TaskCategory.TIMED, start, end,
				null, null, null, null, false, false);
		return task;
	}
	
	private FloatingTask createFloatingTask(String title) {
		FloatingTask task = new FloatingTask(5, title, TaskCategory.FLOATING,
				null, null, null, false, false);
		return task;
	}
	
	private DeadlineTask createDeadlineTask(String title, String endTime) {
		DateTime end = DateTime.parse(endTime);
		DeadlineTask task = new DeadlineTask(3, title, TaskCategory.DEADLINE,
				end, null, null, null, null, false, false);
		return task;
	}
}
