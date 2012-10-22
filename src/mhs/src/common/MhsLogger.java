package mhs.src.common;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class MhsLogger {

	private static final String MHS_LOG_FILE = "MHS.log";
	private static final String LOGGER_NAME = "log_file";

	private static MhsLogger instance = null;
	private static FileHandler fileHandler;
	private static Logger logger;

	private MhsLogger() {
		try {
			fileHandler = new FileHandler(MHS_LOG_FILE);
			logger = Logger.getLogger(LOGGER_NAME);
			logger.addHandler(fileHandler);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Logger getLogger() {
		if (instance == null) {
			instance = new MhsLogger();
		}
		return logger;
	}
}
