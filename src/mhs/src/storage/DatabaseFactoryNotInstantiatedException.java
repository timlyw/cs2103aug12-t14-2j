package mhs.src.storage;

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
