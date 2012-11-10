package mhs.src.storage.persistence.remote;


import java.io.IOException;
import java.util.List;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.gdata.util.ResourceNotFoundException;


public class GoogleTasks {
	String DEFAULT_TASK_LIST_ID = "@default";
	String STATUS_COMPLETED = "completed";
	String STATUS_NEEDS_ACTION = "needsAction";
	
	Tasks taskService = null;
	String taskListId = null;
	
	public GoogleTasks(HttpTransport httpTransport, JsonFactory jsonFactory, HttpRequestInitializer httpRequestInitializer) {
		initTaskService(httpTransport, jsonFactory, httpRequestInitializer);
		initTaskListId();
	}
	
	public void initTaskService(HttpTransport httpTransport, JsonFactory jsonFactory, HttpRequestInitializer httpRequestInitializer) {
		taskService = new Tasks(httpTransport, jsonFactory, httpRequestInitializer);
	}
	
	public void initTaskListId() {
		taskListId = DEFAULT_TASK_LIST_ID;
	}
	
	public List<Task> retrieveTasks() throws IOException, ResourceNotFoundException {
		com.google.api.services.tasks.model.Tasks retrievedTaskList = taskService.tasks().list(taskListId).execute();
		return retrievedTaskList.getItems();
	}
	
	public Task createTask(String title, boolean completed) throws IOException {
		Task task = new Task();
		task.setTitle(title);
		setCompleted(task, completed);
		Task createdTask = taskService.tasks().insert(taskListId, task).execute();
		return createdTask;
	}

	public void updateTask(String taskId, String title, boolean completed) throws IOException, ResourceNotFoundException {
		Task taskToBeUpdated = retrieveTask(taskId);
		taskToBeUpdated.setTitle(title);
		setCompleted(taskToBeUpdated, completed);
		taskService.tasks().update(taskListId, taskId, taskToBeUpdated).execute();
	}
	
	public Task retrieveTask(String taskId) throws IOException, ResourceNotFoundException {
		Task task = taskService.tasks().get(taskListId, taskId).execute();
		return task;
	}
	
	public void deleteTask(String taskId) throws IOException, ResourceNotFoundException {
		
		taskService.tasks().delete(taskListId, taskId).execute();
	}
	
	private void setCompleted(Task task, boolean completed) {
		if(completed) {
			task.setStatus(STATUS_COMPLETED);
		} else {
			task.setStatus(STATUS_NEEDS_ACTION);
		}
	}
}