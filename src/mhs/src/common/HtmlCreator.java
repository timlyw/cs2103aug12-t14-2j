package mhs.src.common;

import java.util.List;

import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;

import org.joda.time.DateTime;

public class HtmlCreator {
	private static final String HTML_FEEDBACK = "<html><font color='#383838' size='5' face='calibri'>%1$s</font></html>";
	private static final String HTML_DISPLAY = "<html><font size='5' face='calibri'>%1$s</font></html>";
	private static final String HTML_TITLE = "<html><center><font size='5' face='calibri'>%1$s</font></center></html>";
	
	public static final String BLUE = "#3300CC";
	public static final String PURPLE = "#660066";
	public static final String ORANGE = "#FF6600";
	public static final String LIGHT_BLUE = "#3333FF";
	public static final String GRAY = "#7A7A59";
	public static final String LIGHT_GRAY = "#B8B8B8";
	
	
	private static final String FORMAT_BOLD = "<b>%1$s</b>";
	private static final String FORMAT_COLOR = "<font color=%1$s>%2$s</font>";
	
	Task lastTask = null;
	
	public final String NEW_LINE = "<br/>";
	
	public String createFeedbackScreenHtml(String htmlBody) {
		String htmlText = String.format(HTML_FEEDBACK, htmlBody);
		return htmlText;
	}
	
	public String createTitleScreenHtml(String htmlBody) {
		String htmlText = String.format(HTML_TITLE, htmlBody);
		return htmlText;
	}
	
	public String createDisplayScreenHtml(String htmlBody) {
		String htmlText = String.format(HTML_DISPLAY, htmlBody);
		return htmlText;
	}
	
	public String makeBold(String htmlText) {
		String boldString = String.format(FORMAT_BOLD, htmlText);
		return boldString;
	}
	
	public String color(String htmlText, String color) {
		String boldString = String.format(FORMAT_COLOR, color, htmlText);
		return boldString;
	}
	
	public Task getLastTaskDisplayed() {
		return lastTask;
	}
	
	public String createTaskListHtml(List<Task> taskList, int limit) {
		String taskListHtml = "";
		
		DateTime prevTaskDateTime = null;
		
		int lineCount = 0;
		
		for(int i = 0; i < taskList.size() && lineCount < limit; i++) {
			Task task = taskList.get(i);
			DateTime currTaskDateTime = null;
			
			if(isTimed(task)) {
				currTaskDateTime = task.getStartDateTime();
			} else if(isDeadline(task)) {
				currTaskDateTime = task.getEndDateTime();
			} else if(isFloating(task)) {
				if(i == 0) {
					taskListHtml += color("Floating Tasks:", LIGHT_BLUE) + NEW_LINE;
					lineCount += 2;
				}
				
				currTaskDateTime = null;
			} else {
				continue;
			}
			
			if(!dateIsEqual(prevTaskDateTime, currTaskDateTime) && currTaskDateTime != null) {
				if(i > 0) {
					taskListHtml += NEW_LINE;
					lineCount++;
				}
				String dateString = getDateString(currTaskDateTime);
				dateString = color(dateString, BLUE);
				taskListHtml +=  dateString + NEW_LINE;
			}
			
			prevTaskDateTime = currTaskDateTime;
			String indexString = color(Integer.toString(i + 1) + ". ", GRAY);
			taskListHtml += indexString + task.toHtmlString() + NEW_LINE;
			lineCount += 2;
			lastTask = task;
		}
		
		return taskListHtml;
	}
	
	public boolean isTimed(Task task) {
		if(task.getTaskCategory() == TaskCategory.TIMED) {
			return true;
		} 
		return false;
	}
	
	public boolean isDeadline(Task task) {
		if(task.getTaskCategory() == TaskCategory.DEADLINE) {
			return true;
		} 
		return false;
	}
	
	public boolean isFloating(Task task) {
		if(task.getTaskCategory() == TaskCategory.FLOATING) {
			return true;
		} 
		return false;
	}
	
	private boolean dateIsEqual(DateTime date1, DateTime date2) {
		if(date1 == null || date2 == null) {
			return false;
		}
		
		if(date1.getDayOfYear() == date2.getDayOfYear() && date1.getYear() == date2.getYear()) {
			return true;
		}
		return false;
	}
	
	private String getDateString(DateTime date) {
		return date.toString("dd MMM yy");
	}
	
}
