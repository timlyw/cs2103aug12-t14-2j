package mhs.src;

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

		if (jObject.get("taskCreated").isJsonNull()) {
			taskCreated = null;
		} else {
			taskCreated = new DateTime(jObject.get("taskCreated").getAsString());
		}
		if (jObject.get("taskUpdated").isJsonNull()) {
			taskUpdated = null;
		} else {
			taskUpdated = new DateTime(jObject.get("taskUpdated").getAsString());
		}
		if (jObject.get("taskLastSync").isJsonNull()) {
			taskLastSync = null;
		} else {
			taskLastSync = new DateTime(jObject.get("taskLastSync")
					.getAsString());
		}

		switch (jObject.get("taskCategory").getAsString()) {
		case "TIMED":
			if (jObject.get("startDateTime").isJsonNull()) {
				startDatetime = null;
			} else {
				startDatetime = new DateTime(jObject.get("startDateTime")
						.getAsString());
			}
			if (jObject.get("endDateTime").isJsonNull()) {
				endDatetime = null;
			} else {
				endDatetime = new DateTime(jObject.get("endDateTime")
						.getAsString());
			}
			if (jObject.get("gCalTaskId").isJsonNull()) {
				gCalTaskId = null;
			} else {
				gCalTaskId = jObject.get("gCalTaskId").getAsString();
			}
			return new TimedTask(jObject.get("taskId").getAsInt(), jObject.get(
					"taskName").getAsString(), jObject.get("taskCategory")
					.getAsString(), startDatetime, endDatetime, taskCreated,
					taskUpdated, taskLastSync, gCalTaskId, jObject
							.get("isDone").getAsBoolean(), jObject.get(
							"isDeleted").getAsBoolean());
		case "DEADLINE":
			if (jObject.get("endDateTime").isJsonNull()) {
				endDatetime = null;
			} else {
				endDatetime = new DateTime(jObject.get("endDateTime")
						.getAsString());
			}
			if (jObject.get("gCalTaskId").isJsonNull()) {
				gCalTaskId = null;
			} else {
				gCalTaskId = jObject.get("gCalTaskId").getAsString();
			}
			return new DeadlineTask(jObject.get("taskId").getAsInt(), jObject
					.get("taskName").getAsString(), jObject.get("taskCategory")
					.getAsString(), endDatetime, taskCreated, taskUpdated,
					taskLastSync, gCalTaskId, jObject.get("isDone")
							.getAsBoolean(), jObject.get("isDeleted")
							.getAsBoolean());
		case "FLOATING":
			return new FloatingTask(jObject.get("taskId").getAsInt(), jObject
					.get("taskName").getAsString(), jObject.get("taskCategory")
					.getAsString(), taskCreated, taskUpdated, taskLastSync,
					jObject.get("isDone").getAsBoolean(), jObject.get(
							"isDeleted").getAsBoolean());
		default:
			break;
		}
		return null;
	}
}
