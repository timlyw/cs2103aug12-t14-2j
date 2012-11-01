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
		logger.entering(getClass().getName(), this.getClass().getName());
		initializeGson();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	/**
	 * Gets gson instance
	 * 
	 * @return gson instance
	 */
	public static Gson getInstance() {
		if (instance == null) {
			instance = new MhsGson();
		}
		return gson;
	}

	private void initializeGson() {
		logger.entering(getClass().getName(), this.getClass().getName());
		gsonBuilder = new GsonBuilder();
		registerMhsGsonAdapters();
		gson = gsonBuilder.serializeNulls().create();
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

	private void registerMhsGsonAdapters() {
		logger.entering(getClass().getName(), this.getClass().getName());
		gsonBuilder.registerTypeAdapter(DateTime.class,
				new DateTimeTypeConverter());
		gsonBuilder.registerTypeAdapter(Task.class, new TaskTypeConverter());
		logger.exiting(getClass().getName(), this.getClass().getName());
	}

}
