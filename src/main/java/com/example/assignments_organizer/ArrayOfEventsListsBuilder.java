package com.example.assignments_organizer;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedList;

public class ArrayOfEventsListsBuilder {

    private Calendar calendar;
    private CalendarListEntry calendarListEntry;
    private ZoneId zone; // user timezone
    private LocalDate today;

    public ArrayOfEventsListsBuilder(Calendar calendar, CalendarListEntry calendarListEntry) {

        this.calendar = calendar;
        this.calendarListEntry = calendarListEntry;
        this.zone = ZoneId.of(this.calendarListEntry.getTimeZone());
        this.today = LocalDate.now(this.zone);

    }

    public LinkedList<Event>[] retrieveEvents(int days) {

        LinkedList<Event>[] arrayOfEventsLists = new LinkedList[days];

        arrayOfEventsLists[0] = retrieveEventsSameDay();

        for (int day = 1; day < days; day++) {

            arrayOfEventsLists[day] = retrieveEventsLaterDay(day);

        }

        return arrayOfEventsLists;

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
            DateTime currentTime = new DateTime(System.currentTimeMillis());

            LinkedList<Event> events = new LinkedList<>(this.calendar.events().list(this.calendarListEntry.getId())
                    .setTimeMin(startTime)
                    .setTimeMax(endTime)
                    .setOrderBy("startTime")  // sorts events chronologically
                    .setSingleEvents(true)    // expands recurring events
                    .execute()
                    .getItems());

            events.removeIf(event -> event.getEnd().getDateTime().getValue() < currentTime.getValue());

            return events;
        } catch (IOException e) {
            System.out.println("Class: AssignmentsOrganizerApplication\nMethod: retrieveEvents");
            throw new RuntimeException(e);
        }

    }


}
