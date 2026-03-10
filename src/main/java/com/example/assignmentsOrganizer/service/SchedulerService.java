package com.example.assignmentsOrganizer.service;

import com.example.assignmentsOrganizer.model.Assignment;
import com.example.assignmentsOrganizer.model.Day;
import com.example.assignmentsOrganizer.model.ScheduleRequest;
import com.example.assignmentsOrganizer.repository.AssignmentRepository;
import com.example.assignmentsOrganizer.util.ArrayOfEventsListsBuilder;
import com.example.assignmentsOrganizer.util.StudyTimesList;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;

/**
 * The scheduling logic happens here
 */
@Service
public class SchedulerService {

    private final AssignmentRepository repository;
    private final GoogleConnection googleConnection;

    public SchedulerService(AssignmentRepository repository, GoogleConnection googleConnection) {

        this.repository = repository;
        this.googleConnection = googleConnection;

    }

    public void runScheduler(String token, ScheduleRequest request) {

        DateTime currentTime = new DateTime(System.currentTimeMillis());

        Calendar calendar = googleConnection.buildCalendar(token);

        CalendarListEntry inputCalendar = googleConnection.getCalendarListEntry(calendar, request.getInputCalendarId());
        CalendarListEntry outputCalendar = googleConnection.getCalendarListEntry(calendar, request.getOutputCalendarId());

        ZoneId zone = ZoneId.of(inputCalendar.getTimeZone());

        LinkedList<Assignment> assignments = new LinkedList<>(repository.findAll(Sort.by(Sort.Direction.ASC, "lastDate")));
        LocalDateTime today = LocalDateTime.now();
        for (Assignment assignment : assignments) {
            assignment.setLastDateToDaysFromToday(today);
            if (assignment.getLastDate() < 1) repository.deleteById(assignment.getId());
        }

        LocalDateTime curr = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTime.getValue()), zone);
        for (Assignment assignment : assignments) assignment.setLastDateToDaysFromToday(curr);

        int totalStudyTime = 0;
        for (Assignment assignment : assignments) totalStudyTime += assignment.getDuration();

        ArrayOfEventsListsBuilder arrayOfEventListsBuilder = new ArrayOfEventsListsBuilder(
                calendar,
                inputCalendar,
                assignments.getLast().getLastDate(),
                totalStudyTime,
                Duration.ofMinutes(request.getStudyDuration()),
                Duration.ofMinutes(request.getBreakDuration()),
                currentTime
        );
        LinkedList<Event>[] arrayOfEventLists = arrayOfEventListsBuilder.getEvents();
        int lastDayToStudy = arrayOfEventLists.length + 1;
        for (Assignment assignment : assignments) {
            if (assignment.getLastDate() > lastDayToStudy) {
                assignment.setLastDate(lastDayToStudy);
            }
        }

        StudyTimesList[] studyTimesLists = arrayOfEventListsBuilder.getStudyTimesLists();

        Organizer organizer = new Organizer();
        Day[] days = organizer.organize(lastDayToStudy, arrayOfEventListsBuilder.getTotalStudyTimePerDay(), assignments);

        for (int i = 0; i < lastDayToStudy - 1; i++) studyTimesLists[i].schedule(days[i]);

        String pageToken = null, calendarId = outputCalendar.getId();

        try {
            do {
                Events events = calendar.events().list(calendarId)
                        .setPageToken(pageToken)
                        .setSingleEvents(true)
                        .execute();
                for (Event event : events.getItems()) {
                    calendar.events().delete(calendarId, event.getId()).execute();
                }
                pageToken = events.getNextPageToken();
            } while (pageToken != null);
        } catch (IOException e) {
            System.out.println(e);
        }

        for (int i = 0; i < lastDayToStudy - 1; i++) studyTimesLists[i].addToCalendar(calendar, outputCalendar.getId());

    }

}
