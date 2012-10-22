/**
 * Json converter for Task
 * 
 * @author timlyw
 */

package mhs.src.storage;

import java.lang.reflect.Type;

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
		return context.serialize(src);
	}

	@Override
	public Task deserialize(JsonElement json, Type type,
			JsonDeserializationContext context) throws JsonParseException {

		JsonObject jObject = (JsonObject) json.getAsJsonObject();

		String gCalTaskId;
		DateTime startDatetime;
		DateTime endDatetime;
		DateTime taskCreated;
		DateTime taskUpdated;
		DateTime taskLastSync;

		if (jObject.get(JSON_KEY_TASK_CREATED).isJsonNull()) {
			taskCreated = null;
		} else {
			taskCreated = new DateTime(jObject.get(JSON_KEY_TASK_CREATED)
					.getAsString());
		}
		if (jObject.get(JSON_KEY_TASK_UPDATED).isJsonNull()) {
			taskUpdated = null;
		} else {
			taskUpdated = new DateTime(jObject.get(JSON_KEY_TASK_UPDATED)
					.getAsString());
		}
		if (jObject.get(JSON_KEY_TASK_LAST_SYNC).isJsonNull()) {
			taskLastSync = null;
		} else {
			taskLastSync = new DateTime(jObject.get(JSON_KEY_TASK_LAST_SYNC)
					.getAsString());
		}

		switch (jObject.get(JSON_KEY_TASK_CATEGORY).getAsString()) {
		case "TIMED":
			if (jObject.get(JSON_KEY_START_DATE_TIME).isJsonNull()) {
				startDatetime = null;
			} else {
				startDatetime = new DateTime(jObject.get(
						JSON_KEY_START_DATE_TIME).getAsString());
			}
			if (jObject.get(JSON_KEY_END_DATE_TIME).isJsonNull()) {
				endDatetime = null;
			} else {
				endDatetime = new DateTime(jObject.get(JSON_KEY_END_DATE_TIME)
						.getAsString());
			}
			if (jObject.get(JSON_KEY_G_CAL_TASK_ID).isJsonNull()) {
				gCalTaskId = null;
			} else {
				gCalTaskId = jObject.get(JSON_KEY_G_CAL_TASK_ID).getAsString();
			}
			return new TimedTask(jObject.get(JSON_KEY_TASK_ID).getAsInt(),
					jObject.get(JSON_KEY_TASK_NAME).getAsString(), jObject.get(
							JSON_KEY_TASK_CATEGORY).getAsString(),
					startDatetime, endDatetime, taskCreated, taskUpdated,
					taskLastSync, gCalTaskId, jObject.get(JSON_KEY_IS_DONE)
							.getAsBoolean(), jObject.get(JSON_KEY_IS_DELETED)
							.getAsBoolean());
		case "DEADLINE":
			if (jObject.get(JSON_KEY_END_DATE_TIME).isJsonNull()) {
				endDatetime = null;
			} else {
				endDatetime = new DateTime(jObject.get(JSON_KEY_END_DATE_TIME)
						.getAsString());
			}
			if (jObject.get(JSON_KEY_G_CAL_TASK_ID).isJsonNull()) {
				gCalTaskId = null;
			} else {
				gCalTaskId = jObject.get(JSON_KEY_G_CAL_TASK_ID).getAsString();
			}
			return new DeadlineTask(jObject.get(JSON_KEY_TASK_ID).getAsInt(),
					jObject.get(JSON_KEY_TASK_NAME).getAsString(), jObject.get(
							JSON_KEY_TASK_CATEGORY).getAsString(), endDatetime,
					taskCreated, taskUpdated, taskLastSync, gCalTaskId, jObject
							.get(JSON_KEY_IS_DONE).getAsBoolean(), jObject.get(
							JSON_KEY_IS_DELETED).getAsBoolean());
		case "FLOATING":
			return new FloatingTask(jObject.get(JSON_KEY_TASK_ID).getAsInt(),
					jObject.get(JSON_KEY_TASK_NAME).getAsString(), jObject.get(
							JSON_KEY_TASK_CATEGORY).getAsString(), taskCreated,
					taskUpdated, taskLastSync, jObject.get(JSON_KEY_IS_DONE)
							.getAsBoolean(), jObject.get(JSON_KEY_IS_DELETED)
							.getAsBoolean());
		default:
			break;
		}
		return null;
	}
}
