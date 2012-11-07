//@author A0087048X
/**
 * InvalidTaskFormatException
 * 
 * Custom Exception thrown when task format is invalid.
 * 
 * Usage
 * - when task is defined incorrectly for that instance 
 * ( missing required parameters for task type )
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */
package mhs.src.common.exceptions;

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
