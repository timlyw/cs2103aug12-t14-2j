package mhs.src.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.InvalidTaskFormatException;
import mhs.src.common.exceptions.TaskNotFoundException;

/**
 * SyncPullSyncTimed
 * 
 * Sync Task Operation for timed pull sync tasks
 * 
 * @author Timothy Lim Yi Wen A0087048X
 *
 */
public class SyncPullSyncTimed extends TimerTask {

	private Syncronize syncronize;
	static final Logger logger = MhsLogger.getLogger();

	protected SyncPullSyncTimed(Syncronize syncronize) {
		logEnterMethod("SyncPullSyncTimed");
		logExitMethod("SyncPullSyncTimed");
		this.syncronize = syncronize;
	}

	@Override
	public void run() {
		logEnterMethod("run");
		logger.log(Level.INFO, "Executing timed pull sync task");
		try {
			Database.syncronize.pullSync();
			Database.saveTaskRecordFile();
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
		logExitMethod("run");
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
