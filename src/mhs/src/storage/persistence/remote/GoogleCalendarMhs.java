package mhs.src.storage.persistence.remote;

import java.io.IOException;
import java.util.List;

import mhs.src.storage.persistence.task.Task;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.model.Event;
import com.google.gdata.util.ResourceNotFoundException;

public class GoogleCalendarMhs {
        private static final String COMPLETED_TASKS_CALENDAR_TITLE = "Completed Tasks (MHS)";
        private GoogleCalendar defaultCalendar;
        private GoogleCalendar completedCalendar;

        public GoogleCalendarMhs(HttpTransport httpTransport,
                        JsonFactory jsonFactory,
                        HttpRequestInitializer httpRequestInitializer) throws IOException {
                defaultCalendar = new GoogleCalendar(httpTransport, jsonFactory,
                                httpRequestInitializer);
                String completedCalendarId = defaultCalendar
                                .createCalendar(COMPLETED_TASKS_CALENDAR_TITLE);
                completedCalendar = new GoogleCalendar(httpTransport, jsonFactory,
                                httpRequestInitializer);
                completedCalendar.setCalendarId(completedCalendarId);
        }

        public Event createEvent(Task newTask) throws IOException {
                if (newTask.isFloating()) {
                        return null;
                }
                String title = newTask.getTaskName();
                String startTime = newTask.getStartDateTime().toString();
                String endTime = newTask.getEndDateTime().toString();
                Event createdEvent = null;
                if (newTask.isDone()) {
                        completedCalendar.createEvent(title, startTime, endTime);
                } else {
                        defaultCalendar.createEvent(title, startTime, endTime);
                }

                return createdEvent;
        }

        public Event retrieveEvent(String eventId) throws IOException,
                        ResourceNotFoundException {
                Event retrievedEvent = null;
                retrievedEvent = defaultCalendar.retrieveEvent(eventId);
                if (retrievedEvent == null) {
                        retrievedEvent = completedCalendar.retrieveEvent(eventId);
                }

                return retrievedEvent;
        }

        public List<Event> retrieveDefaultEvents(String minDate, String maxDate)
                        throws IOException, ResourceNotFoundException {
                return defaultCalendar.retrieveEvents(minDate, maxDate);
        }

        public List<Event> retrieveCompletedEvents(String minDate, String maxDate)
                        throws IOException, ResourceNotFoundException {
                return completedCalendar.retrieveEvents(minDate, maxDate);
        }

        public Event updateEvent(Task updatedTask) throws IOException,
                        ResourceNotFoundException {
                String eventId = updatedTask.getgCalTaskId();

                deleteEvent(eventId);
                return createEvent(updatedTask);
        }

        public void deleteEvent(String eventId) throws IOException,
                        ResourceNotFoundException {
                defaultCalendar.deleteEvent(eventId);
                completedCalendar.deleteEvent(eventId);
        }

        public boolean isDeleted(Event event) {
                return defaultCalendar.isDeleted(event);
        }

        public void deleteEvents(String startTime, String endTime)
                        throws IOException, ResourceNotFoundException {
                defaultCalendar.deleteEvents(startTime, endTime);
                completedCalendar.deleteEvents(startTime, endTime);
        }

        public void isEventCompleted() {

        }
}