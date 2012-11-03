package mhs.src.ui;

import java.util.List;

import org.joda.time.DateTime;

import mhs.src.storage.Task;
import mhs.src.storage.TaskCategory;

public class TaskListHtmlCreator {
	
	public String createTaskListHtml(List<Task> taskList, int lineLimit) {
		String taskListHtml = "";
		
		DateTime prevTaskDateTime;
		
		int lineCount = 0;
		
		for(int i = 0; i < taskList.size() && lineCount < lineLimit; i++) {
			Task task = taskList.get(i);
			if(isTimed(task)) {
				taskListHtml += task.toHtmlString();
			} else if(isDeadline(task)) {
				taskListHtml += task.toHtmlString();
			} else if(isFloating(task)) {
				
			} else {
				continue;
			}
			
			taskListHtml += task.toHtmlString();
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
	
	
	
}
