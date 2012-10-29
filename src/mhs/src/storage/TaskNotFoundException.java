/**
 * Exception for Task not found
 * 
 * Usage
 * - when task specifically requested by taskId is not found
 * 
 * @author timlyw
 */
package mhs.src.storage;

@SuppressWarnings("serial")
public class TaskNotFoundException extends Exception {

	public TaskNotFoundException() {
	}

	public TaskNotFoundException(String message) {
		super(message);
	}

	public TaskNotFoundException(Throwable cause) {
		super(cause);
	}

	public TaskNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
