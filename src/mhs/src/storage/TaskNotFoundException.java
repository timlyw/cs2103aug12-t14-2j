package mhs.src.storage;

/**
 * TaskNotFoundException
 * 
 * Custom Exception thrown when task specified by taskId is not found
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */
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
