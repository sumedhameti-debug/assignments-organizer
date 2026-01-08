package com.example.assignments_organizer;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.*;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;


import java.io.*;
import java.util.Collections;
import java.util.List;

import com.example.assignments_organizer.block.*;

@SpringBootApplication
public class AssignmentsOrganizerApplication {

    private static final HttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            System.out.println("Class: AssignmentsOrganizerApplication\nMethod: none, in static");
            throw new RuntimeException(e);
        }
    }

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String TOKENS_DIRECTORY_PATH = System.getProperty("user.home") + "/.assignments_organizer/tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

    public static Credential getCredentials() {

        try {

            InputStream in = AssignmentsOrganizerApplication.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (in == null) throw new RuntimeException("credentials.json not found in resources!");

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT,
                    JSON_FACTORY,
                    clientSecrets,
                    SCOPES
            )
                    .setDataStoreFactory(dataStoreFactory)
                    .setAccessType("offline") // ensures refresh token
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        } catch (IOException e) {

            System.out.println("Class: AssignmentsOrganizerApplication\nMethod: getCredentials");
            throw new RuntimeException(e);

        }

    }

    public static Calendar buildCalendar(Credential credential) {

        return new Calendar.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                credential
        ).build();

    }

    public static void main(String[] args) {

        SpringApplication.run(AssignmentsOrganizerApplication.class, args);

        // Retrieve user credentials
        Credential credential = getCredentials();

        // Retrieve calendar
        Calendar calendar = buildCalendar(credential);

        // Retrieve the assignments calendar
        CalendarListEntry assignmentsCalendar = getAssignmentsCalendar(calendar);

        // Retrieve the events list for the next week
        List<Event> eventList = retrieveEvents(calendar, assignmentsCalendar);

    }

    public static List<Event> retrieveEvents(Calendar calendar, CalendarListEntry assignmentsCalendar) {

        try {
            return calendar.events().list(assignmentsCalendar.getId())
                    .setTimeMin(new DateTime(System.currentTimeMillis()))
                    .setTimeMax(new DateTime(System.currentTimeMillis() + 7L * 24*60*60*1000)) // next 7 days
                    .setOrderBy("startTime")  // sorts events chronologically
                    .setSingleEvents(true)    // expands recurring events
                    .execute()
                    .getItems();
        } catch (IOException e) {
            System.out.println("Class: AssignmentsOrganizerApplication\nMethod: retrieveEvents");
            throw new RuntimeException(e);
        }

    }

    public static CalendarListEntry getAssignmentsCalendar(Calendar calendar) {

        CalendarList calendarList = null;
        try {
            calendarList = calendar.calendarList().list().execute();
        } catch (IOException e) {
            System.out.println("Class: AssignmentsOrganizerApplication\nMethod: getAssignmentsCalendar");
            throw new RuntimeException(e);
        }

        List<CalendarListEntry> calendars = calendarList.getItems();

        for (CalendarListEntry currentCalendar : calendars) {
            if (currentCalendar.getSummary().equals("Assignments")) {
                return currentCalendar;
            }
        }

        return null;

    }

}