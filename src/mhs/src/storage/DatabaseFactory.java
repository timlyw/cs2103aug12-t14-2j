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
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

import com.google.gdata.util.ServiceException;

public class DatabaseFactory {

	private static final String PARAMETER_TASK_RECORD_FILE_NAME = "taskRecordFileName";
	private static final String EXCEPTION_MESSAGE_DATABASE_FACTORY_NOT_INSTANTIATED = "DatabaseFactory not instantiated. Call getDatabaseFactory first.";
	private static final String EXCEPTION_MESSAGE_NULL_PARAMETER = "%1$s cannot be null!";
	private static DatabaseFactory instance;
	private static Database databaseInstance;
	private static String taskRecordFileName;
	private static boolean disableSync = false;

	private static final Logger logger = MhsLogger.getLogger();

	public synchronized static DatabaseFactory getDatabaseFactory(
			String taskRecordFileName, boolean disableSync) throws IOException,
			ServiceException, DatabaseAlreadyInstantiatedException {
		logEnterMethod("DatabaseFactory");
		if (instance == null) {
			instance = new DatabaseFactory(taskRecordFileName, disableSync);
		}
		if (databaseInstance == null) {
			databaseInstance = new Database(DatabaseFactory.taskRecordFileName,
					DatabaseFactory.disableSync);
		}
		logExitMethod("DatabaseFactory");
		return instance;
	}

	public synchronized static Database getDatabaseInstance()
			throws IllegalArgumentException, IOException, ServiceException,
			DatabaseFactoryNotInstantiatedException {
		logEnterMethod("getDatabaseInstance");
		if (databaseInstance == null) {
			throw new DatabaseFactoryNotInstantiatedException(
					EXCEPTION_MESSAGE_DATABASE_FACTORY_NOT_INSTANTIATED);

		}
		logExitMethod("getDatabaseInstance");
		return databaseInstance;
	}

	public synchronized static void destroy() {
		logEnterMethod("destroy");
		instance = null;
		databaseInstance = null;
		logExitMethod("destroy");
	}

	private DatabaseFactory(String taskRecordFileName, boolean disableSync) {
		logEnterMethod("DatabaseFactory");
		if (taskRecordFileName == null) {
			throw new IllegalArgumentException(String.format(
					EXCEPTION_MESSAGE_NULL_PARAMETER,
					PARAMETER_TASK_RECORD_FILE_NAME));
		}
		DatabaseFactory.taskRecordFileName = taskRecordFileName;
		DatabaseFactory.disableSync = disableSync;
		logExitMethod("DatabaseFactory");
	}

	private static void logExitMethod(String methodName) {
		logger.exiting("mhs.src.DatabaseFactory.DatabaseFactory", methodName);
	}

	private static void logEnterMethod(String methodName) {
		logger.entering("mhs.src.DatabaseFactory.DatabaseFactory", methodName);
	}

}
