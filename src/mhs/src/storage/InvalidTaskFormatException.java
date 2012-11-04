/**
 * Exception for invalid task format
 * 
 * Usage
 * - when task is defined incorrectly for that instance 
 * ( missing required parameters for task type )
 * 
 * @author timlyw 
 */
package mhs.src.storage;

@SuppressWarnings("serial")
public class InvalidTaskFormatException extends Exception {

	public InvalidTaskFormatException() {
	}

	public InvalidTaskFormatException(String message) {
		super(message);
	}

	public InvalidTaskFormatException(Throwable cause) {
		super(cause);
	}

	public InvalidTaskFormatException(String message, Throwable cause) {
		super(message, cause);
	}

}
