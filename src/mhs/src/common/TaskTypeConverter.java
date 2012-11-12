//@author A0087048X
package mhs.src.common;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import mhs.src.storage.persistence.task.DeadlineTask;
import mhs.src.storage.persistence.task.FloatingTask;
import mhs.src.storage.persistence.task.Task;
import mhs.src.storage.persistence.task.TaskCategory;
import mhs.src.storage.persistence.task.TimedTask;

import org.joda.time.DateTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * TaskTypeConverter
 * 
 * Json converter for Task objects
 * 
 * - Serializes task to jObject - Deserializes jObject to Task - Support for
 * floating, timed and deadline tasks
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */

public class TaskTypeConverter implements JsonSerializer<Task>,
                JsonDeserializer<Task> {

        private static final Logger logger = MhsLogger.getLogger();

        private static final String JSON_KEY_IS_DELETED = "isDeleted";
        private static final String JSON_KEY_IS_DONE = "isDone";
        private static final String JSON_KEY_TASK_NAME = "taskName";
        private static final String JSON_KEY_TASK_ID = "taskId";
        private static final String JSON_KEY_G_CAL_TASK_ID = "gCalTaskId";
        private static final String JSON_KEY_G_CAL_TASK_UID = "gCalTaskUid";
        private static final String JSON_KEY_G_TASK_ID = "gTaskId";
        private static final String JSON_KEY_END_DATE_TIME = "endDateTime";
        private static final String JSON_KEY_START_DATE_TIME = "startDateTime";
        private static final String JSON_KEY_TASK_CATEGORY = "taskCategory";
        private static final String JSON_KEY_TASK_LAST_SYNC = "taskLastSync";
        private static final String JSON_KEY_TASK_UPDATED = "taskUpdated";
        private static final String JSON_KEY_TASK_CREATED = "taskCreated";

        @Override
        public JsonElement serialize(Task src, Type typeOfSrc,
                        JsonSerializationContext context) {
                logEnterMethod("serialize");
                logExitMethod("serialize");
                return context.serialize(src);
        }

        @Override
        public Task deserialize(JsonElement json, Type type,
                        JsonDeserializationContext context) throws JsonParseException {
                logEnterMethod("deserialize");
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
                logExitMethod("deserialize");
                return null;
        }

        /**
         * Converts jObject to floating task
         * 
         * @param jObject
         * @return floating task
         */
        private Task convertJObjectToFloatingTask(JsonObject jObject) {
                logEnterMethod("convertJObjectToFloatingTask");
                int floatingTaskId = jObject.get(JSON_KEY_TASK_ID).getAsInt();
                String floatingTaskName = convertJsonElementToString(
                                JSON_KEY_TASK_NAME, jObject);
                DateTime floatingTasktaskCreated = convertJsonElementToDateTime(
                                JSON_KEY_TASK_CREATED, jObject);
                DateTime floatingTasktaskUpdated = convertJsonElementToDateTime(
                                JSON_KEY_TASK_UPDATED, jObject);
                DateTime floatingTasktaskLastSync = convertJsonElementToDateTime(
                                JSON_KEY_TASK_LAST_SYNC, jObject);
                String floatingTaskGTaskId = convertJsonElementToString(
                                JSON_KEY_G_TASK_ID, jObject);
                boolean floatingTaskIsDone = jObject.get(JSON_KEY_IS_DONE)
                                .getAsBoolean();
                boolean floatingTaskIsDeleted = jObject.get(JSON_KEY_IS_DELETED)
                                .getAsBoolean();

                logExitMethod("convertJObjectToFloatingTask");
                return new FloatingTask(floatingTaskId, floatingTaskName,
                                TaskCategory.FLOATING, floatingTasktaskCreated,
                                floatingTasktaskUpdated, floatingTasktaskLastSync,
                                floatingTaskGTaskId, floatingTaskIsDone, floatingTaskIsDeleted);
        }

        /**
         * Converts jObject to deadline task
         * 
         * @param jObject
         * @return deadline task
         */
        private Task convertJObjectToDeadlineTask(JsonObject jObject) {
                logEnterMethod("convertJObjectToDeadlineTask");

                int deadlineTaskId = jObject.get(JSON_KEY_TASK_ID).getAsInt();
                String deadlineTaskName = jObject.get(JSON_KEY_TASK_NAME).getAsString();
                DateTime deadlineTasktaskCreated = convertJsonElementToDateTime(
                                JSON_KEY_TASK_CREATED, jObject);
                DateTime deadlineTasktaskUpdated = convertJsonElementToDateTime(
                                JSON_KEY_TASK_UPDATED, jObject);
                DateTime deadlineTasktaskLastSync = convertJsonElementToDateTime(
                                JSON_KEY_TASK_LAST_SYNC, jObject);
                DateTime deadlineTaskendDatetime = convertJsonElementToDateTime(
                                JSON_KEY_END_DATE_TIME, jObject);
                String deadlineTaskgCalTaskId = convertJsonElementToString(
                                JSON_KEY_G_CAL_TASK_ID, jObject);
                String deadlineTaskgCalTaskUid = convertJsonElementToString(
                                JSON_KEY_G_CAL_TASK_UID, jObject);
                boolean deadlineTaskIsDone = jObject.get(JSON_KEY_IS_DONE)
                                .getAsBoolean();
                boolean deadlineTaskIsDeleted = jObject.get(JSON_KEY_IS_DELETED)
                                .getAsBoolean();

                logExitMethod("convertJObjectToDeadlineTask");

                return new DeadlineTask(deadlineTaskId, deadlineTaskName,
                                TaskCategory.DEADLINE, deadlineTaskendDatetime,
                                deadlineTasktaskCreated, deadlineTasktaskUpdated,
                                deadlineTasktaskLastSync, deadlineTaskgCalTaskId,
                                deadlineTaskgCalTaskUid, deadlineTaskIsDone,
                                deadlineTaskIsDeleted);
        }

        /**
         * Converts jObject to Timed Task
         * 
         * @param jObject
         * @return timed task
         */
        private Task convertJObjectToTimedTask(JsonObject jObject) {
                logEnterMethod("convertJObjectToTimedTask");
                int timedTaskId = jObject.get(JSON_KEY_TASK_ID).getAsInt();
                String timedTaskName = jObject.get(JSON_KEY_TASK_NAME).getAsString();
                DateTime timedTasktaskCreated = convertJsonElementToDateTime(
                                JSON_KEY_TASK_CREATED, jObject);
                DateTime timedTasktaskUpdated = convertJsonElementToDateTime(
                                JSON_KEY_TASK_UPDATED, jObject);
                DateTime timedTasktaskLastSync = convertJsonElementToDateTime(
                                JSON_KEY_TASK_LAST_SYNC, jObject);
                DateTime timedTaskstartDatetime = convertJsonElementToDateTime(
                                JSON_KEY_START_DATE_TIME, jObject);
                DateTime timedTaskendDatetime = convertJsonElementToDateTime(
                                JSON_KEY_END_DATE_TIME, jObject);
                String timedTaskgCalTaskId = convertJsonElementToString(
                                JSON_KEY_G_CAL_TASK_ID, jObject);
                String timedTaskgCalTaskUid = convertJsonElementToString(
                                JSON_KEY_G_CAL_TASK_UID, jObject);
                boolean timedTaskIsDone = jObject.get(JSON_KEY_IS_DONE).getAsBoolean();
                boolean timedTaskIsDeleted = jObject.get(JSON_KEY_IS_DELETED)
                                .getAsBoolean();

                logExitMethod("convertJObjectToTimedTask");
                return new TimedTask(timedTaskId, timedTaskName, TaskCategory.TIMED,
                                timedTaskstartDatetime, timedTaskendDatetime,
                                timedTasktaskCreated, timedTasktaskUpdated,
                                timedTasktaskLastSync, timedTaskgCalTaskId,
                                timedTaskgCalTaskUid, timedTaskIsDone, timedTaskIsDeleted);
        }

        /**
         * Converts Json Element to string
         * 
         * @param jsonKey
         * @param jObject
         * @return
         */
        private String convertJsonElementToString(String jsonKey, JsonObject jObject) {
                logEnterMethod("convertJsonElementToString");
                String convertedString;
                if (jObject.get(jsonKey).isJsonNull()) {
                        convertedString = null;
                } else {
                        convertedString = jObject.get(jsonKey).getAsString();
                }
                logExitMethod("convertJsonElementToString");
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
                logEnterMethod("convertJsonElementToDateTime");
                if (jsonKey == null || jObject == null) {
                        return null;
                }

                DateTime convertedDateTime;
                if (jObject.get(jsonKey) == null || jObject.get(jsonKey).isJsonNull()) {
                        convertedDateTime = null;
                } else {
                        convertedDateTime = new DateTime(jObject.get(jsonKey).getAsString());
                }
                logExitMethod("convertJsonElementToDateTime");
                return convertedDateTime;
        }

        /**
         * Log Trace Method Entry
         * 
         * @param methodName
         */
        private void logEnterMethod(String methodName) {
                logger.entering(getClass().getName(), methodName);
        }

        /**
         * Log Trace Method Entry
         * 
         * @param methodName
         */

        private void logExitMethod(String methodName) {
                logger.exiting(getClass().getName(), methodName);
        }

}