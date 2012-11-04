//@author A0087048X

package mhs.src.storage;

/**
 * DatabaseFactoryNotInstantiatedException
 * 
 * Custom Exception thrown when getDatabaseInstance is called before
 * instantiating database factory.
 * 
 * @author Timothy Lim Yi Wen A0087048X
 * 
 */

@SuppressWarnings("serial")
public class DatabaseFactoryNotInstantiatedException extends Exception {
	public DatabaseFactoryNotInstantiatedException() {
	}

	public DatabaseFactoryNotInstantiatedException(String message) {
		super(message);
	}

	public DatabaseFactoryNotInstantiatedException(Throwable cause) {
		super(cause);
	}

	public DatabaseFactoryNotInstantiatedException(String message,
			Throwable cause) {
		super(message, cause);
	}

}
