//@author A0087048X

package mhs.src.storage;

import java.io.IOException;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.DatabaseAlreadyInstantiatedException;
import mhs.src.common.exceptions.DatabaseFactoryNotInstantiatedException;

import com.google.gdata.util.ServiceException;

/**
 * DatabaseFactory
 * 
 * Singleton DatabaseFactory that controls creation of hot-single database
 * object with set parameters
 * 
 * Usage - Call getDatabaseFactory to setup factory settings with Database
 * parameters - Use getDatabaseInstance to get DatabaseInstance
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */

public class DatabaseFactory {

	private static final String PARAMETER_TASK_RECORD_FILE_NAME = "taskRecordFileName";
	private static final String EXCEPTION_MESSAGE_DATABASE_FACTORY_ALREADY_INSTANTIATED = "DatabaseFactory already instantiated. Destory DatabaseFactory first.";
	private static final String EXCEPTION_MESSAGE_DATABASE_FACTORY_NOT_INSTANTIATED = "DatabaseFactory not instantiated. Call getDatabaseFactory first.";
	private static final String EXCEPTION_MESSAGE_NULL_PARAMETER = "%1$s cannot be null!";
	private static DatabaseFactory instance;
	private static Database databaseInstance;
	private static String taskRecordFileName;
	private static boolean disableSync = false;

	private static final Logger logger = MhsLogger.getLogger();

	/**
	 * Initialize DatabaseFactory parameters for initializing database singleton
	 * 
	 * @param taskRecordFileName
	 * @param disableSync
	 * @return DatabaseFactory Instance
	 * @throws IOException
	 * @throws ServiceException
	 * @throws DatabaseAlreadyInstantiatedException
	 */
	public synchronized static DatabaseFactory getDatabaseFactory(
			String taskRecordFileName, boolean disableSync) throws IOException,
			ServiceException, DatabaseAlreadyInstantiatedException {
		logEnterMethod("DatabaseFactory");

		if (instance == null) {
			instance = new DatabaseFactory(taskRecordFileName, disableSync);
		} else {
			throw new DatabaseAlreadyInstantiatedException(
					EXCEPTION_MESSAGE_DATABASE_FACTORY_ALREADY_INSTANTIATED);
		}
		
		assert(databaseInstance == null);
		
		if (databaseInstance == null) {
			databaseInstance = new Database(DatabaseFactory.taskRecordFileName,
					DatabaseFactory.disableSync);
		}
		logExitMethod("DatabaseFactory");
		return instance;
	}

	/**
	 * Returns single Database instance
	 * 
	 * @return Database instance
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws DatabaseFactoryNotInstantiatedException
	 */
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

	/**
	 * Destroy Database and DatabaseFactory Instance
	 */
	public synchronized static void destroy() {
		logEnterMethod("destroy");
		instance = null;
		databaseInstance = null;
		logExitMethod("destroy");
	}

	/**
	 * Constructor for Database Factory
	 * 
	 * @param taskRecordFileName
	 * @param disableSync
	 */
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
