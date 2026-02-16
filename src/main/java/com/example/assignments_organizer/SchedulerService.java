package com.example.assignments_organizer;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Service
public class SchedulerService {

    private final AssignmentRepository repository;

    private static final HttpTransport HTTP_TRANSPORT;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String TOKENS_DIRECTORY_PATH = System.getProperty("user.home") + "/.assignments_organizer/tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            System.out.println("Class: SchedulerService\nMethod: none, in static");
            throw new RuntimeException(e);
        }
    }

    public SchedulerService(AssignmentRepository repository) {

        this.repository = repository;

    }

    public void runScheduler() {

        // Retrieve user credentials
        Credential credential = getCredentials();

        // Retrieve overall calendar
        Calendar calendar = buildCalendar(credential);

        // Retrieve the input and output calendar
        CalendarListEntry inputCalendar = getCalendar(calendar, "Study Times");
        CalendarListEntry outputCalendar = getCalendar(calendar, "Study Events");

        // Get the assignments from the database
        LinkedList<Assignment> assignments = new LinkedList<>(repository.findAll());

        int totalStudyTime = 0;
        for (Assignment assignment : assignments) totalStudyTime += assignment.getDuration();

        // Retrieve the events list for the next week
        ArrayOfEventsListsBuilder arrayOfEventListsBuilder = new ArrayOfEventsListsBuilder(calendar, inputCalendar, assignments.getLast().getLastDate(), totalStudyTime, Duration.ofMinutes(31), Duration.ofMinutes(15));
        LinkedList<Event>[] arrayOfEventLists = arrayOfEventListsBuilder.getEvents();
        int lastDayToStudy = arrayOfEventLists.length + 1;
        for (Assignment assignment : assignments) {
            if (assignment.getLastDate() > lastDayToStudy) {
                assignment.setLastDate(lastDayToStudy);
            }
            System.out.println(assignment);
        }

        // Separate the events list into study blocks
        StudyTimesList[] studyTimesLists = arrayOfEventListsBuilder.getStudyTimesLists();

        // Organize the assignments
        Organizer organizer = new Organizer();
        Day[] days = organizer.organize(lastDayToStudy, arrayOfEventListsBuilder.getTotalStudyTimePerDay(), assignments);

        System.out.println(days.length);
        System.out.println(studyTimesLists.length);

        // Schedule the assignments into events
        for (int i = 0; i < lastDayToStudy - 1; i++) studyTimesLists[i].schedule(days[i]);

        // Update the calendar
        String pageToken = null, calendarId = outputCalendar.getId();

        try {
            do {
                Events events = calendar.events().list(calendarId)
                        .setPageToken(pageToken)
                        .setSingleEvents(true)   // expands recurring events
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

    private static Credential getCredentials() {

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

    private static Calendar buildCalendar(Credential credential) {

        return new Calendar.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                credential
        ).build();

    }

    private static CalendarListEntry getCalendar(Calendar calendar, String calendarName) {

        CalendarList calendarList = null;
        try {
            calendarList = calendar.calendarList().list().execute();
        } catch (IOException e) {
            System.out.println("Class: AssignmentsOrganizerApplication\nMethod: getCalendar");
            throw new RuntimeException(e);
        }

        List<CalendarListEntry> calendars = calendarList.getItems();

        for (CalendarListEntry currentCalendar : calendars) {
            if (currentCalendar.getSummary().equals(calendarName)) {
                return currentCalendar;
            }
        }

        return null;

    }

}
