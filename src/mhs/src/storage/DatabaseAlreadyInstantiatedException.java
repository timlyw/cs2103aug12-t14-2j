//@author A0087048X

package mhs.src.storage;

/**
 * DatabaseAlreadyInstantiatedException
 * 
 * Custom Exception thrown when instantiating new database factory when
 * singleton database is already instantiated.
 * 
 * @author Timothy Lim Yi Wen A0087048X
 * 
 */

@SuppressWarnings("serial")
public class DatabaseAlreadyInstantiatedException extends Exception {

	public DatabaseAlreadyInstantiatedException() {
	}

	public DatabaseAlreadyInstantiatedException(String message) {
		super(message);
	}

	public DatabaseAlreadyInstantiatedException(Throwable cause) {
		super(cause);
	}

	public DatabaseAlreadyInstantiatedException(String message, Throwable cause) {
		super(message, cause);
	}

}
