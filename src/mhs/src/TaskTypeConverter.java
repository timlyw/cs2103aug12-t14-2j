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

		DateTime startDatetime;
		DateTime endDatetime;
		DateTime taskCreated;
		DateTime taskUpdated;
		DateTime taskLastSync;

		switch (jObject.get("taskCategory").getAsString()) {
		case "TIMED":
			startDatetime = new DateTime(jObject.get("startDateTime")
					.getAsString());
			endDatetime = new DateTime(jObject.get("endDateTime").getAsString());
			taskCreated = new DateTime(jObject.get("taskCreated").getAsString());
			taskUpdated = new DateTime(jObject.get("taskUpdated").getAsString());
			taskLastSync = new DateTime(jObject.get("taskLastSync")
					.getAsString());
			return new TimedTask(jObject.get("taskId").getAsInt(), jObject.get(
					"taskName").getAsString(), jObject.get("taskCategory")
					.getAsString(), startDatetime, endDatetime, taskCreated,
					taskUpdated, taskLastSync, jObject.get("gCalTaskId")
							.getAsString(), jObject.get("isDone")
							.getAsBoolean(), jObject.get("isDeleted")
							.getAsBoolean());
		case "DEADLINE":
			
			endDatetime = new DateTime(jObject.get("endDateTime").getAsString());
			taskCreated = new DateTime(jObject.get("taskCreated").getAsString());
			taskUpdated = new DateTime(jObject.get("taskUpdated").getAsString());
			taskLastSync = new DateTime(jObject.get("taskLastSync")
					.getAsString());

			return new DeadlineTask(jObject.get("taskId").getAsInt(), jObject
					.get("taskName").getAsString(), jObject.get("taskCategory")
					.getAsString(), endDatetime, taskCreated, taskUpdated,
					taskLastSync, jObject.get("gCalTaskId").getAsString(),
					jObject.get("isDone").getAsBoolean(), jObject.get(
							"isDeleted").getAsBoolean());
		case "FLOATING":
			taskCreated = new DateTime(jObject.get("taskCreated").getAsString());
			taskUpdated = new DateTime(jObject.get("taskUpdated").getAsString());
			taskLastSync = new DateTime(jObject.get("taskLastSync")
					.getAsString());

			return new FloatingTask(
					jObject.get("taskId").getAsInt(), 
					jObject.get("taskName").getAsString(), 
					jObject.get("taskCategory").getAsString(), 
					taskCreated, 
					taskUpdated, 
					taskLastSync,
					jObject.get("isDone").getAsBoolean(), 
					jObject.get("isDeleted").getAsBoolean());
		default:
			break;
		}
		return null;
	}
}
