/**
 * Mhs Logger Singleton Class
 * - Creates a single logger instance 
 * 
 * @author timlyw
 */
package mhs.src.common;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MhsLogger {

	private static final Level LEVEL_LOG_CONSOLE_LEVEL = Level.INFO;
	private static final Level LEVEL_LOG_LEVEL = Level.ALL;

	private static final String MHS_LOG_FILE = "MHS.log";
	private static final String LOGGER_NAME = "log_file";

	private static MhsLogger instance = null;
	private static FileHandler fileHandler;
	private static ConsoleHandler consoleHandler;
	private static Logger logger;

	private MhsLogger() {
		try {
			setUpLogger();
			setUpFileHandler();
			setUpConsoleHandler();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setUpLogger() {
		logger = Logger.getLogger(LOGGER_NAME);
		logger.setLevel(LEVEL_LOG_LEVEL);
	}

	private void setUpFileHandler() throws IOException {
		fileHandler = new FileHandler(MHS_LOG_FILE);
		logger.addHandler(fileHandler);
	}

	private void setUpConsoleHandler() {
		consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(LEVEL_LOG_CONSOLE_LEVEL);
		logger.addHandler(consoleHandler);
	}

	/**
	 * Getter for logger instance
	 * 
	 * @return instance of logger
	 */
	public static Logger getLogger() {
		if (instance == null) {
			instance = new MhsLogger();
		}
		return logger;
	}
	
	
}
