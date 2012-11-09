package mhs.src.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;
import mhs.src.common.exceptions.InvalidTaskFormatException;
import mhs.src.common.exceptions.TaskNotFoundException;

public class SyncPullSyncTimed extends TimerTask {

	private Syncronize syncronize;
	static final Logger logger = MhsLogger.getLogger();

	protected SyncPullSyncTimed(Syncronize syncronize) {
		this.syncronize = syncronize;
	}

	@Override
	public void run() {
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
	}

}
