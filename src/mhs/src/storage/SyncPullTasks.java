package mhs.src.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.InvalidTaskFormatException;
import mhs.src.common.exceptions.TaskNotFoundException;

public class SyncPullTasks implements Callable<Boolean> {
	private Syncronize syncronize;
	static final Logger logger = MhsLogger.getLogger();

	public SyncPullTasks(Syncronize syncronize) {
		logEnterMethod("SyncPullTasks");
		this.syncronize = syncronize;
		logExitMethod("SyncPullTasks");
	}

	@Override
	public Boolean call() throws Exception {
		logEnterMethod("call");
		try {
			Database.syncronize.pullSync();
			syncronize.database.saveTaskRecordFile();
		} catch (UnknownHostException e) {
			logger.log(Level.FINER, e.getMessage());
			syncronize.disableRemoteSync();
		} catch (TaskNotFoundException e) {
			// SilentFailSync Policy
			logger.log(Level.FINER, e.getMessage());
		} catch (InvalidTaskFormatException e) {
			// SilentFailSyncPolicy
			logger.log(Level.FINER, e.getMessage());
		} catch (IOException e) {
			logger.log(Level.FINER, e.getMessage());
		}
		logExitMethod("call");
		return true;
	}

	void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}

	void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}

}
