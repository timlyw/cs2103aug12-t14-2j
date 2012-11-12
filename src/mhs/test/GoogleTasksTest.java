//@author A0088015

package mhs.test;

import java.util.List;

import com.google.api.services.tasks.model.Task;

import mhs.src.storage.persistence.remote.GoogleTasks;
import mhs.src.storage.persistence.remote.MhsGoogleOAuth2;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the creation, retrival, updating and deletion operations for 
 * the GoogleTasks class for single and multiple events
 * 
 * @author John Wong
 */

public class GoogleTasksTest {
	
	/**
	 * test the creation, retrieval, updating and deletion operations for a single task
	 * @throws Exception
	 */
	@Test
	public void testCrudForSingleTask() throws Exception {
		// initialize login
		MhsGoogleOAuth2.getInstance();
		MhsGoogleOAuth2.authorizeCredentialAndStoreInCredentialStore();
		GoogleTasks gTasks = new GoogleTasks(MhsGoogleOAuth2.getHttpTransport(), MhsGoogleOAuth2.getJsonFactory(), MhsGoogleOAuth2.getCredential());
		
		// test create task
		String title = "createGoogleTasksTest";
		Task createdTask = gTasks.createTask(title, false);
		String createdTaskId = createdTask.getId();
		
		// test retrieve task
		Task retrievedTask = gTasks.retrieveTask(createdTaskId);
		assertEquals(title, retrievedTask.getTitle());
		assertFalse(gTasks.isCompleted(retrievedTask));
		
		// test update task
		String updatedTitle = "updateGoogleTasksTest";
		gTasks.updateTask(createdTaskId, updatedTitle, true);
		Task retrievedUpdatedTask = gTasks.retrieveTask(createdTaskId);
		
		assertEquals(updatedTitle, retrievedUpdatedTask.getTitle());
		assertTrue(gTasks.isCompleted(retrievedUpdatedTask));

		// test delete task
		gTasks.deleteTask(retrievedUpdatedTask.getId());
		gTasks.isDeleted(retrievedUpdatedTask);
	}
	
	/**
	 * test retrival of multiple tasks
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCrudForMultipleTasks() throws Exception {
		// initialize login
		MhsGoogleOAuth2.getInstance();
		MhsGoogleOAuth2.authorizeCredentialAndStoreInCredentialStore();
		GoogleTasks gTasks = new GoogleTasks(MhsGoogleOAuth2.getHttpTransport(), MhsGoogleOAuth2.getJsonFactory(), MhsGoogleOAuth2.getCredential());
		
		// test retrieve tasks
		List<Task> taskList = gTasks.retrieveTasks();
		int initialSize = taskList.size();
		String title = "createGoogleTasksTest";
		Task createdTask1 = gTasks.createTask(title, false);
		Task createdTask2 = gTasks.createTask(title, false);
		
		List<Task> updatedTaskList = gTasks.retrieveTasks();
		int updatedSize = updatedTaskList.size();
		assertEquals(initialSize + 2, updatedSize);
		gTasks.deleteTask(createdTask1.getId());
		gTasks.deleteTask(createdTask2.getId());
	}
}