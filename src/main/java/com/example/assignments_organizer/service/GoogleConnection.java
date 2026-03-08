package com.example.assignments_organizer.service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Builds authenticated Google Calendar clients and provides calendar lookup utilities
 */
@Component
public class GoogleConnection {

    private final HttpTransport HTTP_TRANSPORT;

    public GoogleConnection() {

        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            System.out.println("Error in GoogleConnection Constructor");
            throw new RuntimeException(e);
        }

    }

    public Calendar buildCalendar(String token) {

        return new Calendar.Builder(
                HTTP_TRANSPORT,
                GsonFactory.getDefaultInstance(),
                new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(token)
        )
                .setApplicationName("Assignments Organizer")
                .build();

    }

    public CalendarListEntry getCalendarListEntry(Calendar calendar, String calendarName) {

        CalendarList calendarList = null;
        try {
            calendarList = calendar.calendarList().list().execute();
        } catch (IOException e) {
            System.out.println("Class: AssignmentsOrganizerApplication\nMethod: getCalendarListEntry");
            throw new RuntimeException(e);
        }

        List<CalendarListEntry> calendars = calendarList.getItems();

        for (CalendarListEntry currentCalendar : calendars) {
            if (currentCalendar.getId().equals(calendarName)) {
                return currentCalendar;
            }
        }

        return null;

    }

}
