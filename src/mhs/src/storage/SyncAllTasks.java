package mhs.src.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.InvalidTaskFormatException;
import mhs.src.common.exceptions.TaskNotFoundException;

import com.google.gdata.util.ServiceException;

/**
 * SyncAllTasks
 * 
 * Sync Task Operation for SyncAllTasks tasks
 * 
 * @author Timothy Lim Yi Wen A0087048X
 *
 */
public class SyncAllTasks implements Callable<Boolean> {
	private Syncronize syncronize;
	static final Logger logger = MhsLogger.getLogger();

	public SyncAllTasks(Syncronize syncronize) {
		logEnterMethod("SyncAllTask");
		this.syncronize = syncronize;
		logExitMethod("SyncAllTask");
	}

	@Override
	public Boolean call() {
		logEnterMethod("call");
		try {
			syncronize.pullSync();
			syncronize.pushSync();
			Database.saveTaskRecordFile();
		} catch (NullPointerException e) {
			logger.log(Level.FINER, e.getMessage());
		} catch (UnknownHostException e) {
			syncronize.disableRemoteSync();
			logger.log(Level.FINER, e.getMessage());			
		} catch (IOException e) {
			logger.log(Level.FINER, e.getMessage());
		} catch (TaskNotFoundException e) {
			logger.log(Level.FINER, e.getMessage());
		} catch (InvalidTaskFormatException e) {
			logger.log(Level.FINER, e.getMessage());
		} catch (ServiceException e) {
			logger.log(Level.FINER, e.getMessage());
		}
		logExitMethod("call");
		return true;
	}


	/**
	 * Log Methods
	 */

	/**
	 * Log trace entry method
	 * 
	 * @param methodName
	 */
	void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}

	/**
	 * Log trace exit method
	 * 
	 * @param methodName
	 */
	void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}
}
