package mhs.src.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gdata.util.ServiceException;

import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.InvalidTaskFormatException;
import mhs.src.common.exceptions.TaskNotFoundException;
import mhs.src.storage.Syncronize;
import mhs.src.storage.persistence.task.Task;

public class SyncPushTask implements Callable<Boolean> {
	private Task localTaskToSync;
	private Syncronize syncronize;
	static final Logger logger = MhsLogger.getLogger();

	public SyncPushTask(Task taskToSync, Syncronize syncronize) {
		logEnterMethod("SyncPushTask");
		this.localTaskToSync = taskToSync;
		this.syncronize = syncronize;
		logExitMethod("SyncPushTask");
	}

	@Override
	public Boolean call() throws Exception {
		logEnterMethod("call");
		try {
			syncronize.pushSyncTask(localTaskToSync);
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

	void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}

	void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}

}
