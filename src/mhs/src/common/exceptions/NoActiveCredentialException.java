//@author A0087048X

package mhs.src.common.exceptions;

/**
 * NoActiveCredentialException
 * 
 * Custom Exception thrown no credential exists.
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */

@SuppressWarnings("serial")
public class NoActiveCredentialException extends Exception {

	public NoActiveCredentialException() {
	}

	public NoActiveCredentialException(String message) {
		super(message);
	}

	public NoActiveCredentialException(Throwable cause) {
		super(cause);
	}

	public NoActiveCredentialException(String message, Throwable cause) {
		super(message, cause);
	}

}
