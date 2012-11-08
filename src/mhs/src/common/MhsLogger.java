//@author A0087048X

package mhs.src.common;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MhsLogger
 * 
 * Creates and maintains Mhs Logger Singleton
 * 
 * Settings:
 * 
 * LEVEL_LOG_CONSOLE_LEVEL : Adjusts level of logging to output to console 
 * LEVEL_LOG_LEVEL		   : Adjusts level of logging to log to file
 * 
 * @author Timothy Lim Yi Wen A0087048X
 * 
 */
public class MhsLogger {

	private static final Level LEVEL_LOG_CONSOLE_LEVEL = Level.INFO;
	private static final Level LEVEL_LOG_LEVEL = Level.ALL;

	private static final String MHS_LOG_FILE = "MHS.log";
	private static final String LOGGER_NAME = "log_file";

	private static MhsLogger instance = null;
	private static FileHandler fileHandler;
	private static ConsoleHandler consoleHandler;
	private static Logger logger;

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

	/**
	 * Private Constructor for MhsLogger
	 */
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

	/**
	 * Sets up logger instance
	 */
	private void setUpLogger() {
		logger = Logger.getLogger(LOGGER_NAME);
		logger.setLevel(LEVEL_LOG_LEVEL);
		logger.setUseParentHandlers(false);
	}

	/**
	 * Sets up file handler for MHS logger instance
	 * 
	 * @throws IOException
	 */
	private void setUpFileHandler() throws IOException {
		fileHandler = new FileHandler(MHS_LOG_FILE);
		logger.addHandler(fileHandler);
	}

	/**
	 * Set up console handler for MHS logger instance
	 */
	private void setUpConsoleHandler() {
		consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(LEVEL_LOG_CONSOLE_LEVEL);
		logger.addHandler(consoleHandler);
	}

}
