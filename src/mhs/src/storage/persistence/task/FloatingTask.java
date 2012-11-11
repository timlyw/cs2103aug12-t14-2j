//@author A0087048X

package mhs.src.storage.persistence.task;

import mhs.src.common.HtmlCreator;

import org.joda.time.DateTime;

/**
 * FloatingTask
 * 
 * Floating Task Object
 * 
 * - Inherits from base class Task - Not synced with google calendar
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */
public class FloatingTask extends Task {

	private String gTaskId;

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
			String gTaskId, boolean isDone, boolean isDeleted) {
		super(taskId, taskName, taskCategory, createdDt, updatedDt, syncDt,
				isDone, isDeleted);
		this.setGTaskId(gTaskId);
	}

	/**
	 * Construct synced floating task with google task 
	 * 
	 * @param taskId
	 * @param googleTask
	 * @param syncDateTime
	 */
	public FloatingTask(int taskId,
			com.google.api.services.tasks.model.Task googleTask,
			DateTime syncDateTime) {
		super(taskId, googleTask.getTitle(), TaskCategory.FLOATING,
				syncDateTime, syncDateTime, syncDateTime, false, false);
		setGTaskId(googleTask.getId());
	}

	public String getGTaskId() {
		return gTaskId;
	}

	public void setGTaskId(String gTaskId) {
		this.gTaskId = gTaskId;
	}

	/**
	 * @author John Wong
	 */
	public String toHtmlString() {
		HtmlCreator htmlCreator = new HtmlCreator();

		String boldTaskName = taskName;
		String htmlString = boldTaskName;

		if (isDone()) {
			htmlString = htmlCreator.color(taskName + " [completed]",
					HtmlCreator.LIGHT_GRAY);
		}

		return htmlString;
	}

}