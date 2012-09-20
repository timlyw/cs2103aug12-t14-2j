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

	private static final String CLASSNAME = "CLASSNAME";
	private static final String INSTANCE = "INSTANCE";

	@Override
	public JsonElement serialize(Task src, Type typeOfSrc,
			JsonSerializationContext context) {

		JsonObject retValue = new JsonObject();
		String className = src.getClass().getCanonicalName();
		retValue.addProperty(CLASSNAME, className);
		JsonElement elem = context.serialize(src);
		retValue.add(INSTANCE, elem);
		System.out.println("!" + retValue);
		return retValue;
	}

	@Override
	public Task deserialize(JsonElement json, Type type,
			JsonDeserializationContext context) throws JsonParseException {

		JsonObject jobject = (JsonObject) json;

		switch (jobject.get("taskCategory").getAsString()) {
		case "DEADLINE":
			return new DeadlineTask(jobject.get("taskId").getAsInt(), jobject
					.get("taskName").getAsString(), jobject.get("taskCategory")
					.getAsString(), new DateTime(jobject.get("endDateTime")
					.getAsString()), new DateTime(jobject.get("taskCreated")
					.getAsString()), new DateTime(jobject.get("taskUpdated")
					.getAsString()), new DateTime(jobject.get("taskLastSync")
					.getAsString()), jobject.get("gCalTaskId").getAsString(),
					jobject.get("isDone").getAsBoolean(), jobject.get(
							"isDeleted").getAsBoolean());
		case "TIMED":
			return new TimedTask(jobject.get("taskId").getAsInt(), jobject.get(
					"taskName").getAsString(), jobject.get("taskCategory")
					.getAsString(), new DateTime(jobject.get("startDateTime")
					.getAsString()), new DateTime(jobject.get("endDateTime")
					.getAsString()), new DateTime(jobject.get("taskCreated")
					.getAsString()), new DateTime(jobject.get("taskUpdated")
					.getAsString()), new DateTime(jobject.get("taskLastSync")
					.getAsString()), jobject.get("gCalTaskId").getAsString(),
					jobject.get("isDone").getAsBoolean(), jobject.get(
							"isDeleted").getAsBoolean());
		case "FLOATING":
			return new FloatingTask(jobject.get("taskId").getAsInt(), jobject
					.get("taskName").getAsString(), jobject.get("taskCategory")
					.getAsString(), new DateTime(jobject.get("taskCreated")
					.getAsString()), new DateTime(jobject.get("taskUpdated")
					.getAsString()), new DateTime(jobject.get("taskLastSync")
					.getAsString()), jobject.get("gCalTaskId").getAsString(),
					jobject.get("isDone").getAsBoolean(), jobject.get(
							"isDeleted").getAsBoolean());
		default:
			break;
		}
		return null;
	}
}
