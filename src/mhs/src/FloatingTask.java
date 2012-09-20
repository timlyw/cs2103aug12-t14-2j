package mhs.src;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;

public class FloatingTask extends Task {

	public FloatingTask(int taskId, String taskName, String taskCategory,
			DateTime createdDt, DateTime updatedDt, DateTime syncDt,
			String gCalTaskId, boolean isDone, boolean isDeleted) {
		super(taskId, taskName, taskCategory, createdDt, updatedDt, syncDt,
				gCalTaskId, isDone, isDeleted);
	}

	/*
	 * Record methods
	 */
	/**
	 * Return task properties for record file insertion
	 * 
	 * @return
	 */
	public Map<String, String> getTaskProperties() {

		Map<String, String> taskProperties = new LinkedHashMap<String, String>();

		taskProperties.put("taskId", taskId.toString());
		taskProperties.put("taskName", taskName);
		taskProperties.put("taskCategory", taskCategory.getValue());
		taskProperties.put("taskCreated", taskCreated.toString());
		taskProperties.put("taskUpdated", taskUpdated.toString());
		taskProperties.put("taskLastSync", taskLastSync.toString());
		taskProperties.put("gCalTaskId", gCalTaskId);
		taskProperties.put("isDone", isDone.toString());
		taskProperties.put("isDeleted", isDeleted.toString());

		return taskProperties;
	}

}