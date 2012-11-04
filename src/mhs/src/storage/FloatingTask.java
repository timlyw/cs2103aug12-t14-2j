//@author A0087048X

package mhs.src.storage;

import mhs.src.common.HtmlCreator;

import org.joda.time.DateTime;

/**
 * FloatingTask
 * 
 * Floating Task Object
 * 
 * - Inherits from base class Task
 * - Not synced with google calendar
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */
public class FloatingTask extends Task {

	/**
	 * Constructor with String taskCategory
	 * 
	 * @param taskId
	 * @param taskName
	 * @param taskCategory
	 * @param createdDt
	 * @param updatedDt
	 * @param syncDt
	 * @param isDone
	 * @param isDeleted
	 */
	public FloatingTask(int taskId, String taskName, String taskCategory,
			DateTime createdDt, DateTime updatedDt, DateTime syncDt,
			boolean isDone, boolean isDeleted) {
		super(taskId, taskName, taskCategory, createdDt, updatedDt, syncDt,
				null, isDone, isDeleted);
	}

	/**
	 * Constructor with TaskCategory taskCategory
	 * 
	 * @param taskId
	 * @param taskName
	 * @param taskCategory
	 * @param createdDt
	 * @param updatedDt
	 * @param syncDt
	 * @param isDone
	 * @param isDeleted
	 */
	public FloatingTask(int taskId, String taskName, TaskCategory taskCategory,
			DateTime createdDt, DateTime updatedDt, DateTime syncDt,
			boolean isDone, boolean isDeleted) {
		super(taskId, taskName, taskCategory, createdDt, updatedDt, syncDt,
				null, isDone, isDeleted);
	}
	
	/**
	 * @author John Wong
	 */	
	public String toHtmlString() {
		HtmlCreator htmlCreator = new HtmlCreator();

		String boldTaskName = htmlCreator.makeBold(taskName);
		String htmlString = boldTaskName;
		
		if(isDone()) {
			htmlString = htmlCreator.color(taskName + " [completed]", HtmlCreator.LIGHT_GRAY);
		}
		
		return htmlString;
	}
}