package mhs.src.storage;

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
