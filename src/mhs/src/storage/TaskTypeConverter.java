/**
 * Json converter for Task
 * 
 * - Serializes task to jObject
 * - Deserializes jObject to Task
 * - Support for floating, timed and deadline tasks
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
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TaskTypeConverter implements JsonSerializer<Task>,
		JsonDeserializer<Task> {

	private static final Logger logger = MhsLogger.getLogger();

	private static final String JSON_KEY_IS_DELETED = "isDeleted";
	private static final String JSON_KEY_IS_DONE = "isDone";
	private static final String JSON_KEY_TASK_NAME = "taskName";
	private static final String JSON_KEY_TASK_ID = "taskId";
	private static final String JSON_KEY_G_CAL_TASK_ID = "gCalTaskId";
	private static final String JSON_KEY_END_DATE_TIME = "endDateTime";
	private static final String JSON_KEY_START_DATE_TIME = "startDateTime";
	private static final String JSON_KEY_TASK_CATEGORY = "taskCategory";
	private static final String JSON_KEY_TASK_LAST_SYNC = "taskLastSync";
	private static final String JSON_KEY_TASK_UPDATED = "taskUpdated";
	private static final String JSON_KEY_TASK_CREATED = "taskCreated";

	@Override
	public JsonElement serialize(Task src, Type typeOfSrc,
			JsonSerializationContext context) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return context.serialize(src);
	}

	@Override
	public Task deserialize(JsonElement json, Type type,
			JsonDeserializationContext context) throws JsonParseException {

		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		JsonObject jObject = (JsonObject) json.getAsJsonObject();

		switch (jObject.get(JSON_KEY_TASK_CATEGORY).getAsString()) {
		case "TIMED":
			return convertJObjectToTimedTask(jObject);
		case "DEADLINE":
			return convertJObjectToDeadlineTask(jObject);
		case "FLOATING":
			return convertJObjectToFloatingTask(jObject);
		default:
			break;
		}
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());		
		return null;
	}

	/**
	 * Converts jObject to floating task
	 * @param jObject
	 * @return floating task
	 */
	private Task convertJObjectToFloatingTask(JsonObject jObject) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		DateTime floatingTasktaskCreated = convertJsonElementToDateTime(
				JSON_KEY_TASK_CREATED, jObject);
		DateTime floatingTasktaskUpdated = convertJsonElementToDateTime(
				JSON_KEY_TASK_UPDATED, jObject);
		DateTime floatingTasktaskLastSync = convertJsonElementToDateTime(
				JSON_KEY_TASK_LAST_SYNC, jObject);
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return new FloatingTask(jObject.get(JSON_KEY_TASK_ID).getAsInt(),
				jObject.get(JSON_KEY_TASK_NAME).getAsString(), jObject.get(
						JSON_KEY_TASK_CATEGORY).getAsString(),
				floatingTasktaskCreated, floatingTasktaskUpdated,
				floatingTasktaskLastSync, jObject.get(JSON_KEY_IS_DONE)
						.getAsBoolean(), jObject.get(JSON_KEY_IS_DELETED)
						.getAsBoolean());
	}

	/**
	 * Converts jObject to deadline task
	 * @param jObject
	 * @return deadline task
	 */
	private Task convertJObjectToDeadlineTask(JsonObject jObject) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		String deadlineTaskgCalTaskId;
		DateTime deadlineTaskendDatetime;
		DateTime deadlineTasktaskCreated;
		DateTime deadlineTasktaskUpdated;
		DateTime deadlineTasktaskLastSync;

		deadlineTasktaskCreated = convertJsonElementToDateTime(
				JSON_KEY_TASK_CREATED, jObject);
		deadlineTasktaskUpdated = convertJsonElementToDateTime(
				JSON_KEY_TASK_UPDATED, jObject);
		deadlineTasktaskLastSync = convertJsonElementToDateTime(
				JSON_KEY_TASK_LAST_SYNC, jObject);
		deadlineTaskendDatetime = convertJsonElementToDateTime(
				JSON_KEY_END_DATE_TIME, jObject);
		deadlineTaskgCalTaskId = convertJsonElementToString(
				JSON_KEY_G_CAL_TASK_ID, jObject);

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());

		return new DeadlineTask(jObject.get(JSON_KEY_TASK_ID).getAsInt(),
				jObject.get(JSON_KEY_TASK_NAME).getAsString(), jObject.get(
						JSON_KEY_TASK_CATEGORY).getAsString(),
				deadlineTaskendDatetime, deadlineTasktaskCreated,
				deadlineTasktaskUpdated, deadlineTasktaskLastSync,
				deadlineTaskgCalTaskId, jObject.get(JSON_KEY_IS_DONE)
						.getAsBoolean(), jObject.get(JSON_KEY_IS_DELETED)
						.getAsBoolean());
	}

	/**
	 * Converts jObject to Timed Task
	 * @param jObject
	 * @return timed task
	 */
	private Task convertJObjectToTimedTask(JsonObject jObject) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		String timedTaskgCalTaskId;
		DateTime timedTaskstartDatetime;
		DateTime timedTaskendDatetime;
		DateTime timedTasktaskCreated;
		DateTime timedTasktaskUpdated;
		DateTime timedTasktaskLastSync;

		timedTasktaskCreated = convertJsonElementToDateTime(
				JSON_KEY_TASK_CREATED, jObject);
		timedTasktaskUpdated = convertJsonElementToDateTime(
				JSON_KEY_TASK_UPDATED, jObject);
		timedTasktaskLastSync = convertJsonElementToDateTime(
				JSON_KEY_TASK_LAST_SYNC, jObject);

		timedTaskstartDatetime = convertJsonElementToDateTime(
				JSON_KEY_START_DATE_TIME, jObject);
		timedTaskendDatetime = convertJsonElementToDateTime(
				JSON_KEY_END_DATE_TIME, jObject);
		timedTaskgCalTaskId = convertJsonElementToString(
				JSON_KEY_G_CAL_TASK_ID, jObject);

		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return new TimedTask(jObject.get(JSON_KEY_TASK_ID).getAsInt(), jObject
				.get(JSON_KEY_TASK_NAME).getAsString(), jObject.get(
				JSON_KEY_TASK_CATEGORY).getAsString(), timedTaskstartDatetime,
				timedTaskendDatetime, timedTasktaskCreated,
				timedTasktaskUpdated, timedTasktaskLastSync,
				timedTaskgCalTaskId, jObject.get(JSON_KEY_IS_DONE)
						.getAsBoolean(), jObject.get(JSON_KEY_IS_DELETED)
						.getAsBoolean());
	}

	/**
	 * Converts Json Element to string
	 * 
	 * @param jsonKey
	 * @param jObject
	 * @return
	 */
	private String convertJsonElementToString(String jsonKey, JsonObject jObject) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		String convertedString;
		if (jObject.get(jsonKey).isJsonNull()) {
			convertedString = null;
		} else {
			convertedString = jObject.get(jsonKey).getAsString();
		}
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return convertedString;
	}

	/**
	 * Converts Json Element to DateTime
	 * 
	 * @param jsonKey
	 * @param jObject
	 * @return DateTime
	 */
	private DateTime convertJsonElementToDateTime(String jsonKey,
			JsonObject jObject) {
		logger.entering(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		
		if(jsonKey == null || jObject == null){
			return null;
		}
		
		DateTime convertedDateTime;
		if (jObject.get(jsonKey) == null || jObject.get(jsonKey).isJsonNull()) {
			convertedDateTime = null;
		} else {
			convertedDateTime = new DateTime(jObject.get(jsonKey).getAsString());
		}
		logger.exiting(getClass().getName(), new Exception().getStackTrace()[0].getMethodName());
		return convertedDateTime;
	}
}
