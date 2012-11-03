/**
 * DatabaseFactory 
 * 
 * Singleton DatabaseFactory that controls creation of single database object with set parameters
 * 
 *  Usage
 *  - Call getDatabaseFactory to setup factory settings with Database parameters
 *  - Use getDatabaseInstance to get DatabaseInstance
 * 
 */
package mhs.src.storage;

import java.io.IOException;

import com.google.gdata.util.ServiceException;

public class DatabaseFactory {

	private static final String PARAMETER_TASK_RECORD_FILE_NAME = "taskRecordFileName";
	private static final String EXCEPTION_MESSAGE_DATABASE_FACTORY_NOT_INSTANTIATED = "DatabaseFactory not instantiated. Call getDatabaseFactory first.";
	private static final String EXCEPTION_MESSAGE_NULL_PARAMETER = "%1$s cannot be null!";
	private static DatabaseFactory instance;
	private static Database databaseInstance;
	private static String taskRecordFileName;
	private static boolean disableSync = false;

	public synchronized static DatabaseFactory getDatabaseFactory(
			String taskRecordFileName, boolean disableSync) throws IOException,
			ServiceException, DatabaseAlreadyInstantiatedException {
		if (instance == null) {
			instance = new DatabaseFactory(taskRecordFileName, disableSync);
		}
		if (databaseInstance == null) {
			databaseInstance = new Database(DatabaseFactory.taskRecordFileName,
					DatabaseFactory.disableSync);
		}
		return instance;
	}

	public synchronized static Database getDatabaseInstance()
			throws IllegalArgumentException, IOException, ServiceException,
			DatabaseFactoryNotInstantiatedException {
		if (databaseInstance == null) {
			throw new DatabaseFactoryNotInstantiatedException(
					EXCEPTION_MESSAGE_DATABASE_FACTORY_NOT_INSTANTIATED);

		}
		return databaseInstance;
	}

	private DatabaseFactory(String taskRecordFileName, boolean disableSync) {
		if (taskRecordFileName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_TASK_RECORD_FILE_NAME));
		}
		DatabaseFactory.taskRecordFileName = taskRecordFileName;
		DatabaseFactory.disableSync = disableSync;
	}
}
