/**
 * MhsGson
 * - Singleton Gson for use in MHS
 * 
 * @author timlyw
 */
package mhs.src.storage;

import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MhsGson {

	private static final Logger logger = MhsLogger.getLogger();
	private static MhsGson instance;
	private static Gson gson;
	private static GsonBuilder gsonBuilder;

	private MhsGson() {
		logEnterMethod("MhsGson");
		initializeGson();
		logExitMethod("MhsGson");
	}

	/**
	 * Gets gson instance
	 * 
	 * @return gson instance
	 */
	public static Gson getInstance() {
		logEnterMethod("getInstance");
		if (instance == null) {
			instance = new MhsGson();
		}
		logExitMethod("getInstance");
		return gson;
	}

	private void initializeGson() {
		logEnterMethod("initializeGson");
		gsonBuilder = new GsonBuilder();
		registerMhsGsonAdapters();
		gson = gsonBuilder.serializeNulls().create();
		logExitMethod("initializeGson");
	}

	private void registerMhsGsonAdapters() {
		logEnterMethod("registerMhsGsonAdapters");
		gsonBuilder.registerTypeAdapter(DateTime.class,
				new DateTimeTypeConverter());
		gsonBuilder.registerTypeAdapter(Task.class, new TaskTypeConverter());
		logExitMethod("initializeGson");
	}

	private static void logEnterMethod(String methodName) {
		logger.entering("mhs.src.storage.MhsGson", methodName);
	}

	private static void logExitMethod(String methodName) {
		logger.exiting("mhs.src.storage.MhsGson", methodName);
	}

}
