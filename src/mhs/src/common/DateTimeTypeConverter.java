//@author A0087048X

package mhs.src.common;

import java.lang.reflect.Type;
import java.util.logging.Logger;


import org.joda.time.DateTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * DateTimeTypeConverter
 * 
 * Json converter for JodaTime DateTime object
 * 
 * - Serializes JodaTime DateTime to jObject
 * 
 * - Deerializes jObject to JodaTime DateTime
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */

public class DateTimeTypeConverter implements JsonSerializer<DateTime>,
		JsonDeserializer<DateTime> {

	private static final Logger logger = MhsLogger.getLogger();

	@Override
	public JsonElement serialize(DateTime src, Type srcType,
			JsonSerializationContext context) {
		logEnterMethod("serialize");
		assert(src != null);
		assert(srcType != null);
		logExitMethod("serialize");
		return new JsonPrimitive(src.toString());
	}

	@Override
	public DateTime deserialize(JsonElement json, Type type,
			JsonDeserializationContext context) throws JsonParseException {
		logEnterMethod("deserialize");
		assert(json != null);
		logExitMethod("deserialize");
		return new DateTime(json.getAsString());
	}

	private void logEnterMethod(String methodName) {
		logger.entering(getClass().getName(), methodName);
	}

	private void logExitMethod(String methodName) {
		logger.exiting(getClass().getName(), methodName);
	}

}
