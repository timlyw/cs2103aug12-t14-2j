package mhs.src.logic;

import java.util.ArrayList;

import org.joda.time.DateTime;

import mhs.src.storage.TaskCategory;
import mhs.src.storage.TimedTask;
import mhs.src.ui.HtmlCreator;

public class ProcessorStub {
	
	private ArrayList<StateListener> stateListeners = new ArrayList<StateListener>();
	private String commandFeedback = null;
	private HtmlCreator htmlCreator = new HtmlCreator();
	private String currentCommand = null;
	private String currentState = null;
	private boolean isPasswordExpected = false;
	private int lineLimit = 0;
	public int LINE_HEIGHT = 30;

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
		
		currentState = "";
		String title1 = "do laundry";
		String startTime1 = "2012-11-17T08:00:00+08:00";
		String endTime1 = "2012-11-17T09:00:00+08:00";
		TimedTask task1 = createTask(title1, startTime1, endTime1);
		
		currentState += task1.toHtmlString();
		
	}
	
	
	private TimedTask createTask(String title, String startTime, String endTime) {
		DateTime start = DateTime.parse(startTime);
		DateTime end = DateTime.parse(endTime);

		TimedTask task = new TimedTask(1, title, TaskCategory.TIMED, start, end,
				null, null, null, null, false, false);
		return task;
	}
}
