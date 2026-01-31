package com.example.assignments_organizer;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.LinkedList;

public class ArrayOfEventsListsBuilder {

    private Calendar calendar;
    private CalendarListEntry calendarListEntry;
    private ZoneId zone; // user timezone
    private LocalDate today;
    private int[] totalStudyTimePerDay;
    private LinkedList<Event>[] events;
    private StudyTimesList[] studyTimesLists;

    public ArrayOfEventsListsBuilder(Calendar calendar, CalendarListEntry calendarListEntry, int days, long totalTime, Duration studyDuration, Duration breakDuration) {

        this.calendar = calendar;
        this.calendarListEntry = calendarListEntry;
        this.zone = ZoneId.of(this.calendarListEntry.getTimeZone());
        this.today = LocalDate.now(this.zone);
        this.totalStudyTimePerDay = new int[days];
        this.studyTimesLists = new StudyTimesList[days];
        this.events = retrieveEvents(days, totalTime, studyDuration, breakDuration);

    }

    public StudyTimesList[] getStudyTimesLists() {
        return studyTimesLists;
    }

    public int[] getTotalStudyTimePerDay() {
        return totalStudyTimePerDay;
    }

    public LinkedList<Event>[] getEvents() {
        return events;
    }

    private LinkedList<Event>[] retrieveEvents(int days, long totalTime, Duration studyDuration, Duration breakDuration) {

        totalTime *= 60000;

        if (days < 1 || totalTime < 0) throw new IllegalArgumentException("An input is too small");

        LinkedList<Event>[] arrayOfEventsLists = new LinkedList[days];

        long totalTimeAtPreviousIteration = totalTime;

        for (int day = 0; day < days; day++) {

            if (day == 0) {
                arrayOfEventsLists[day] = retrieveEventsSameDay();
            } else {
                arrayOfEventsLists[day] = retrieveEventsLaterDay(day);
            }

            studyTimesLists[day] = new StudyTimesList(arrayOfEventsLists[day], zone.toString(), studyDuration, breakDuration);

            for (Event event : studyTimesLists[day]) {

                totalTime -= (event.getEnd().getDateTime().getValue() - event.getStart().getDateTime().getValue());

                if (totalTime == 0) {
                    totalStudyTimePerDay[day] = (int) (totalTimeAtPreviousIteration - totalTime)/60000;
                    return Arrays.copyOfRange(arrayOfEventsLists, 0, day + 1);
                } else if (totalTime < 0) {
                    studyTimesLists[day].cutListShortAtStudyEvent(event, (int) totalTime/60000);
                    totalStudyTimePerDay[day] = (int) totalTimeAtPreviousIteration/60000;
                    return Arrays.copyOfRange(arrayOfEventsLists, 0, day + 1);
                }

            }

            totalStudyTimePerDay[day] = (int) (totalTimeAtPreviousIteration - totalTime)/60000;
            totalTimeAtPreviousIteration = totalTime;

        }

        throw new RuntimeException("Not enough time to study");

    }

    private LinkedList<Event> retrieveEventsLaterDay(long dayNumber) {

        try {
            DateTime startTime = new DateTime(this.today.plusDays(dayNumber).atStartOfDay(this.zone).toInstant().toEpochMilli());
            DateTime endTime = new DateTime(this.today.plusDays(dayNumber + 1).atStartOfDay(this.zone).toInstant().toEpochMilli());

            return new LinkedList<>(this.calendar.events().list(this.calendarListEntry.getId())
                    .setTimeMin(startTime)
                    .setTimeMax(endTime)
                    .setOrderBy("startTime")  // sorts events chronologically
                    .setSingleEvents(true)    // expands recurring events
                    .execute()
                    .getItems());
        } catch (IOException e) {
            System.out.println("Class: AssignmentsOrganizerApplication\nMethod: retrieveEvents");
            throw new RuntimeException(e);
        }

    }

    private LinkedList<Event> retrieveEventsSameDay() {

        try {
            DateTime startTime = new DateTime(this.today.atStartOfDay(this.zone).toInstant().toEpochMilli());
            DateTime endTime = new DateTime(this.today.plusDays(1).atStartOfDay(this.zone).toInstant().toEpochMilli());
            DateTime currentTime = new DateTime(System.currentTimeMillis()); // Change this to be centralized everywhere in the program and round up to nearest minute

            LinkedList<Event> events = new LinkedList<>(this.calendar.events().list(this.calendarListEntry.getId())
                    .setTimeMin(startTime)
                    .setTimeMax(endTime)
                    .setOrderBy("startTime")  // sorts events chronologically
                    .setSingleEvents(true)    // expands recurring events
                    .execute()
                    .getItems());

            events.removeIf(event -> event.getEnd().getDateTime().getValue() < currentTime.getValue());

            if (events.isEmpty()) return events;

            Event firstEvent = events.getFirst();
            if (firstEvent.getStart().getDateTime().getValue() < currentTime.getValue()) {
                firstEvent.setStart(new EventDateTime().setDateTime(currentTime));
            }

            // TODO If an event has already started

            return events;
        } catch (IOException e) {
            System.out.println("Class: AssignmentsOrganizerApplication\nMethod: retrieveEvents");
            throw new RuntimeException(e);
        }

    }


}
