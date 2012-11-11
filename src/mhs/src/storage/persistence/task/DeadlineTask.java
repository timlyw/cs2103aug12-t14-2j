//@author A0087048X

package mhs.src.storage.persistence.task;

import mhs.src.common.HtmlCreator;

import org.joda.time.DateTime;

import com.google.api.services.calendar.model.Event;

/**
 * DeadlineTask
 * 
 * Deadline Task Object
 * 
 * - Inherits from base class Task<br>
 * - Task with an endDateTime<br>
 * - Syncs with google calendar
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */

public class DeadlineTask extends Task {

        private String gCalTaskId;
        private String gCalTaskUid;
        private DateTime endDateTime;

        /**
         * Constructor with TaskCategory taskCategory
         * 
         * @param taskId
         * @param taskName
         * @param taskCategory
         * @param endDt
         * @param createdDt
         * @param updatedDt
         * @param syncDt
         * @param gCalTaskId
         * @param isDone
         * @param isDeleted
         */
        public DeadlineTask(int taskId, String taskName, TaskCategory taskCategory,
                        DateTime endDt, DateTime createdDt, DateTime updatedDt,
                        DateTime syncDt, String gCalTaskId, String gCalTaskUid,
                        boolean isDone, boolean isDeleted) {
                super(taskId, taskName, taskCategory, createdDt, updatedDt, syncDt,
                                isDone, isDeleted);
                setGcalTaskId(gCalTaskId);
                setGcalTaskUid(gCalTaskUid);
                setEndDateTime(endDt);
        }

        /**
         * Construct synced DeadlineTask from Google CalendarEventEntry
         * 
         * @param taskId
         * @param gCalEntry
         */
        public DeadlineTask(int taskId, Event gCalEntry, DateTime syncDateTime) {
                super(taskId, gCalEntry.getSummary(), TaskCategory.DEADLINE,
                                syncDateTime, syncDateTime, syncDateTime, false, false);
                setGcalTaskId(gCalEntry.getId());
                setGcalTaskUid(gCalEntry.getICalUID());
                setEndDateTime(new DateTime(gCalEntry.getEnd().toString()));
        }

        /**
         * Return endDateTime for startDateTime
         */
        public DateTime getStartDateTime() {
                return endDateTime;
        }

        public DateTime getEndDateTime() {
                return endDateTime;
        }

        public void setEndDateTime(DateTime endDateTime) {
                this.endDateTime = endDateTime;
        }

        public String getgCalTaskId() {
                return gCalTaskId;
        }

        public void setGcalTaskId(String gCalTaskId) {
                this.gCalTaskId = gCalTaskId;
        }

        public String getgCalTaskUid() {
                return gCalTaskUid;
        }

        public void setGcalTaskUid(String gCalTaskUid) {
                this.gCalTaskUid = gCalTaskUid;
        }

        public String toString() {
                String taskToString = "";
                if (taskId != null) {
                        taskToString += "taskId=" + taskId;
                }
                if (taskName != null) {
                        taskToString += "taskName=" + taskName;
                }
                if (taskCategory != null) {
                        taskToString += "taskCategory=" + taskCategory.getValue();
                }
                if (endDateTime != null) {
                        taskToString += "endDateTime=" + endDateTime.toString();
                }
                if (taskCreated != null) {
                        taskToString += "taskCreated=" + taskCreated.toString();
                }
                if (taskUpdated != null) {
                        taskToString += "taskUpdated=" + taskUpdated.toString();
                }
                if (taskLastSync != null) {
                        taskToString += "taskLastSync=" + taskLastSync.toString();
                }
                if (gCalTaskId != null) {
                        taskToString += "gCalTaskId=" + gCalTaskId;
                }
                if (isDone != null) {
                        taskToString += "isDone=" + isDone.toString();
                }
                if (isDeleted != null) {
                        taskToString += "isDeleted=" + isDeleted.toString();
                }
                return taskToString;
        }

        /**
         * @author John Wong
         */
        public String toHtmlString() {
                String dateString = "";
                HtmlCreator htmlCreator = new HtmlCreator();

                dateString = htmlCreator.color(dateString, HtmlCreator.BLUE);
                String timeString = getTimeString(endDateTime);

                String boldTaskName = taskName;
                String htmlString = timeString + ": " + boldTaskName;

                if (isDone()) {
                        htmlString = htmlCreator.color(htmlString + " [completed]",
                                        HtmlCreator.LIGHT_GRAY);
                }

                return htmlString;
        }

        private String getTimeString(DateTime date) {
                String timeString = "";

                if (date.getMinuteOfHour() == 0) {
                        timeString = date.toString("h aa");
                } else {
                        timeString = date.toString("h.mm aa");
                }

                timeString = timeString.toLowerCase();

                return timeString;
        }

}