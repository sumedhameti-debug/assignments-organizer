package com.example.assignments_organizer;

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
import java.time.Duration;
import java.util.*;

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
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

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

    public static CalendarListEntry getCalendar(Calendar calendar, String calendarName) {

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

    private static LinkedList<Assignment> hardAssignments = new LinkedList<>(), mediumAssignments = new LinkedList<>(), easyAssignments = new LinkedList<>();
    private static Day[] sortedDaysHard, sortedDaysMedium, sortedDaysEasy;

    private static LinkedList<Assignment> getCorrectAssignmentsByDifficultyList(Difficulty difficulty) {
        return switch (difficulty) {
            case HARD -> hardAssignments;
            case MEDIUM -> mediumAssignments;
            case EASY -> easyAssignments;
            default -> throw new RuntimeException("No difficulty provided");
        };
    }

    public static Day[] organize(int numDays, int[] dayInfo, LinkedList<Assignment> assignments) {

        Difficulty[] difficulties = Difficulty.values();
        Day temp;
        int insertIndex, overtimeLength, duration, timeLeft, sumOfTimeLeft;

        Comparator<Day> totalTimeLeftComparator = Comparator.comparingInt(Day::getTotalTimeLeft),
                hardTimeLeftComparator = Comparator.comparingInt(x -> x.getTotalTimeLeft() - x.getTimeLeft(Difficulty.HARD)),
                mediumTimeLeftComparator = Comparator.comparingInt(x -> x.getTotalTimeLeft() - x.getTimeLeft(Difficulty.MEDIUM)),
                easyTimeLeftComparator = Comparator.comparingInt(x -> x.getTotalTimeLeft() - x.getTimeLeft(Difficulty.EASY)),
                currentComparator; // Fix code duplication
        Comparator<Assignment> assignmentComparator = Comparator.comparingInt(Assignment::getLastDate).thenComparingInt(Assignment::getDuration);

        // 1b & 1d
        Day[] days = new Day[numDays];
        DaySummary[] daySummaries = new DaySummary[numDays];
        for (int i = 0; i < numDays; i++) {
            days[i] = new Day(dayInfo[i]);
            daySummaries[i] = new DaySummary();
        }
        Difficulty assignmentDifficulty;
        for (Assignment assignment : assignments) {
            assignmentDifficulty = assignment.getDifficulty();
            getCorrectAssignmentsByDifficultyList(assignmentDifficulty).add(assignment);
            daySummaries[assignment.getLastDate() - 1].addToDuration(assignment.getDuration(), assignmentDifficulty);
        }

        LinkedList<Assignment> tempAssignments;
        Day[] tempSortedDays;
        int length, totalTimeLeft, average;

        for (Difficulty difficulty : difficulties) {

            // 1e
            tempAssignments = getCorrectAssignmentsByDifficultyList(difficulty);
            tempAssignments.sort(assignmentComparator);

            // Step 2
            length = 0;
            tempSortedDays = Arrays.copyOf(days, numDays);
            Arrays.sort(tempSortedDays, totalTimeLeftComparator);
            for (Assignment assignment : tempAssignments) length += assignment.getDuration();
            for (int i = tempSortedDays.length; i > 0; i--) {
                average = length/i;
                totalTimeLeft = tempSortedDays[i - 1].getTotalTimeLeft();
                tempSortedDays[i - 1].addToTimeLeft(
                        Math.min(average, totalTimeLeft),
                        difficulty
                );
            }

            // 1c
            currentComparator = switch (difficulty) {
                case HARD -> hardTimeLeftComparator;
                case MEDIUM -> mediumTimeLeftComparator;
                case EASY -> easyTimeLeftComparator;
            };
            Arrays.sort(tempSortedDays, currentComparator);
            switch (difficulty) {
                case HARD:
                    sortedDaysHard = tempSortedDays;
                    break;
                case MEDIUM:
                    sortedDaysMedium = tempSortedDays;
                    break;
                case EASY:
                    sortedDaysEasy = tempSortedDays;
                    break;
                default:
                    throw new RuntimeException("No difficulty");
            }

        }

        for (Day day : days) day.setIdealTimeToTimeLeft();

        // Step 3

        for (int i = 0; i < daySummaries.length; i++) {

            // a

            for (Difficulty difficulty : difficulties) {

                for (int j = i; j >= 0; j--) {

                    duration = daySummaries[i].getDuration(difficulty);
                    timeLeft = days[j].getTimeLeft(difficulty);
                    if (duration == 0) {
                        break;
                    } else if (timeLeft > duration) {
                        days[j].subtractFromTimeLeft(duration, difficulty);
                        daySummaries[i].setDuration(0, difficulty);
                    } else {
                        daySummaries[i].subtractFromDuration(timeLeft, difficulty);
                        days[j].subtractFromTimeLeft(Math.min(timeLeft, days[j].getTotalTimeLeft()), difficulty);
                    }

                }

            }

            System.out.println(days[i].getTimeLeft(Difficulty.HARD));

            // b

            for (Difficulty difficulty : difficulties) {

                // 1

                switch (difficulty) {
                    case HARD:
                        tempSortedDays = sortedDaysHard;
                        currentComparator = hardTimeLeftComparator;
                        break;
                    case MEDIUM:
                        tempSortedDays = sortedDaysMedium;
                        currentComparator = mediumTimeLeftComparator;
                        break;
                    case EASY:
                        tempSortedDays = sortedDaysEasy;
                        currentComparator = easyTimeLeftComparator;
                        break;
                    default: throw new RuntimeException("No difficulty");
                };

                insertIndex = Arrays.binarySearch(
                        tempSortedDays,
                        0,
                        i + 1,
                        tempSortedDays[i],
                        currentComparator
                );
                if (insertIndex < 0) insertIndex = -insertIndex - 1;

                sumOfTimeLeft = 0;
                for (int j = i; j >= 0; j--) sumOfTimeLeft += tempSortedDays[j].getTimeLeft(difficulty);

                temp = tempSortedDays[i];
                System.arraycopy(tempSortedDays, insertIndex, tempSortedDays, insertIndex + 1, i - insertIndex);
                tempSortedDays[insertIndex] = temp;

                if (daySummaries[i].getDuration(difficulty) != 0){
                    for (int j = i; j >= 0; j--) { // 2
                        System.out.println(temp.getTimeLeft(difficulty) + "ss");
                        timeLeft = tempSortedDays[j].getTimeLeft(difficulty);
                        overtimeLength = Math.min(
                                tempSortedDays[j].getTotalTimeLeft(),
                                (daySummaries[i].getDuration(difficulty) - sumOfTimeLeft) / (j + 1) + timeLeft
                        );
                        tempSortedDays[j].subtractFromTimeLeft(
                                overtimeLength,
                                difficulty
                        );
                        if (difficulty == Difficulty.HARD) System.out.println(j + " " + tempSortedDays[j].getTotalTimeLeft());
                        daySummaries[i].subtractFromDuration(overtimeLength, difficulty);
                        sumOfTimeLeft -= timeLeft;
                    }
                }

            }

        }

        for (Difficulty difficulty : difficulties) for (Assignment assignment : getCorrectAssignmentsByDifficultyList(difficulty)) System.out.println(assignment);

        for (Day day : days) {

            System.out.println(day.getTimeLeft(Difficulty.HARD));

            day.resetTimeLeft();

            System.out.println(day.getTimeLeft(Difficulty.HARD));

            day.addAssignments(hardAssignments, Difficulty.HARD);
            day.addAssignments(mediumAssignments, Difficulty.MEDIUM);
            day.addAssignments(easyAssignments, Difficulty.EASY);

        }

        return days;

    }

    public static void main(String[] args) {

        SpringApplication.run(AssignmentsOrganizerApplication.class, args);

        // Retrieve user credentials
        Credential credential = getCredentials();

        // Retrieve overall calendar
        Calendar calendar = buildCalendar(credential);

        // Retrieve the input and output calendar
        CalendarListEntry inputCalendar = getCalendar(calendar, "Study Times");
        CalendarListEntry outputCalendar = getCalendar(calendar, "Study Events");

        // TODO
        // Get the assignments from the database
        LinkedList<Assignment> assignments = new LinkedList<>();
        assignments.add(new Assignment(3, 300, Difficulty.HARD, "1"));
        assignments.add(new Assignment(1, 20, Difficulty.MEDIUM, "2"));
        assignments.add(new Assignment(4, 45, Difficulty.MEDIUM, "3"));
        assignments.add(new Assignment(5, 60, Difficulty.EASY, "4"));

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

}