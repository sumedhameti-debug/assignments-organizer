package com.example.assignments_organizer;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedList;

public class StudyTimesList implements Iterable<Event> {

    private Node firstNode;
    private final String zone;
    private final Duration studyDuration, breakDuration;
    private int totalDuration;

    public StudyTimesList(LinkedList<Event> eventList, String zone, Duration studyDuration, Duration breakDuration) {

        this.zone = zone;
        this.studyDuration = studyDuration;
        this.breakDuration = breakDuration;
        this.totalDuration = 0;

        if (eventList.isEmpty()) {

            firstNode = null;

        } else {

            DateTime nextBlockStart, nextBlockEnd, currentBlockEnd;
            Node currentNode = null, newNode;
            long calendarEventEnd;

            for (Event currentEvent : eventList) {

                calendarEventEnd = currentEvent.getEnd().getDateTime().getValue();

                nextBlockStart = currentEvent.getStart().getDateTime();
                nextBlockEnd = computeBlockEnd(nextBlockStart, currentEvent);
                newNode = new Node(nextBlockStart, nextBlockEnd);

                if (currentNode != null) {
                    currentNode.setNext(newNode);
                } else {
                    firstNode = newNode;
                }

                currentNode = newNode;

                while (calendarEventEnd > currentNode.getEvent().getEnd().getDateTime().getValue()) {

                    currentBlockEnd = currentNode.getEvent().getEnd().getDateTime();
                    nextBlockStart = addToDateTime(currentBlockEnd, breakDuration);
                    nextBlockEnd = computeBlockEnd(nextBlockStart, currentEvent);
                    currentNode.setNext(new Node(nextBlockStart, nextBlockEnd));
                    currentNode = currentNode.getNext();

                }

            }

        }

    }

    public void testBlockList() {

        for (Event currentEvent : this) {

            System.out.print(currentEvent.getStart().getDateTime());
            System.out.print(" to ");
            System.out.println(currentEvent.getEnd().getDateTime());

        }

    }

    public void cutListShortAtStudyEvent(Event newLastEvent, int decreaseInDuration) {

        Node node = firstNode;

        while (node != null) {

            if (node.getEvent() == newLastEvent){

                EventDateTime end = new EventDateTime()
                    .setDateTime(
                            addToDateTime(
                                    node.getEvent().getStart().getDateTime(),
                                    Duration.ofMinutes(-decreaseInDuration)
                            )
                    );

                if (end.getDateTime().getValue() < node.getEvent().getStart().getDateTime().getValue()) {
                    throw new RuntimeException("The decrease in duration provided exceeds the event's length");
                }

                node.getEvent().setEnd(end);
                node.setNext(null);

                return;

            }

            node = node.getNext();

        }

        throw new RuntimeException("Provided event is not in the list");

    }

    public static int computeDifferenceInDuration(EventDateTime dateTimeStart, EventDateTime dateTimeEnd) {

        return (int) (dateTimeEnd.getDateTime().getValue() - dateTimeStart.getDateTime().getValue()) / 60000;

    }


    private DateTime computeBlockEnd(DateTime blockStart, Event calendarEvent) {

        DateTime calendarEventEnd = calendarEvent.getEnd().getDateTime();
        DateTime idealEnd = addToDateTime(blockStart, studyDuration);
        return (calendarEventEnd.getValue() > idealEnd.getValue()) ? idealEnd : calendarEventEnd;

    }

    public static DateTime addToDateTime(DateTime dateTime, Duration duration) {

        return new DateTime(
                Instant.ofEpochMilli(
                        dateTime.getValue()
                ).plus(duration).toEpochMilli()
        );

    }

    public void schedule(Day day) {

        int timeLeftAssignment;
        int studyTimeLength;
        Node currentNode = firstNode, newNode;
        Event currentEvent;

        for (Assignment assignment : day.getAssignmentsForTheDay()) {

            timeLeftAssignment = assignment.getDuration();

            while (timeLeftAssignment != 0) {

                if (currentNode == null) throw new RuntimeException("Not enough study time");

                currentEvent = currentNode.getEvent();
                studyTimeLength = StudyTimesList.computeDifferenceInDuration(currentEvent.getStart(), currentEvent.getEnd());

                if (studyTimeLength < timeLeftAssignment) {

                    newNode = new Node(
                            addToDateTime(
                                    currentEvent.getStart().getDateTime(),
                                    Duration.ofMinutes(timeLeftAssignment)
                            ),
                            currentEvent.getEnd().getDateTime()
                    );
                    newNode.setNext(currentNode.getNext());
                    currentEvent.setEnd(newNode.getEvent().getStart());
                    currentNode.setNext(newNode);

                    timeLeftAssignment = 0;

                } else {

                    timeLeftAssignment -= studyTimeLength;

                }

                currentEvent.setSummary(assignment.getName());

                currentNode = currentNode.next;

            }

        }

    }

    public void addToCalendar(Calendar calendar, String calendarID) {

        for (Event event : this) {

            if (event.getSummary() != null) {

                try {
                    calendar.events().insert(calendarID, event).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

        }

    }

    private class Node {

        private Event event;
        private String assignmentName;

        private Node next;

        private Node(DateTime dateTimeStart, DateTime dateTimeEnd) {

            event = new Event();

            EventDateTime eventDateTimeStart = new EventDateTime()
                    .setDateTime(dateTimeStart)
                    .setTimeZone(zone);
            event.setStart(eventDateTimeStart);

            EventDateTime eventDateTimeEnd = new EventDateTime()
                    .setDateTime(dateTimeEnd)
                    .setTimeZone(zone);
            event.setEnd(eventDateTimeEnd);

        }

        private Node getNext() {

            return this.next;

        }

        private void setNext(Node next) {

            this.next = next;

        }

        private Event getEvent() {

            return event;

        }

        private String getAssignmentName() {

            return assignmentName;

        }

        private void setAssignmentName(String assignmentName) {

            this.assignmentName = assignmentName;

        }

    }

    @Override
    public Iterator<Event> iterator() {

        return new BlockListIterator();

    }

    private class BlockListIterator implements Iterator<Event> {

        private Node current = null;

        @Override
        public boolean hasNext() {

            return current == null ? firstNode != null : current.getNext() != null;

        }

        @Override
        public Event next() {

            if (!hasNext()) throw new java.util.NoSuchElementException();
            current = current == null ? firstNode : current.getNext();
            return current.getEvent();

        }

        @Override
        public void remove () {

            throw new UnsupportedOperationException("Remove not supported");

        }

    }

}