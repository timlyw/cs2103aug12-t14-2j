/**
 * Json converter for JodaTime DateTime object
 *  
 *  - Serializes JodaTime DateTime to jObject
 *  - Deerializes jObject to JodaTime DateTime
 *  
 * @author timlyw
 */

package mhs.src.storage;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import mhs.src.common.MhsLogger;

import org.joda.time.DateTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateTimeTypeConverter implements JsonSerializer<DateTime>,
		JsonDeserializer<DateTime> {

	private static final Logger logger = MhsLogger.getLogger();

	@Override
	public JsonElement serialize(DateTime src, Type srcType,
			JsonSerializationContext context) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return new JsonPrimitive(src.toString());
	}

	@Override
	public DateTime deserialize(JsonElement json, Type type,
			JsonDeserializationContext context) throws JsonParseException {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return new DateTime(json.getAsString());
	}
}
