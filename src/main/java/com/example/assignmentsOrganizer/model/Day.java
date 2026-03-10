package com.example.assignmentsOrganizer.model;

import com.example.assignmentsOrganizer.enums.Difficulty;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * This class stores information about assignments to be done that day, as well as information used
 * to help the organizer class
 */
public class Day {

    private int totalTimeLeft, timeLeftHard, timeLeftMedium, timeLeftEasy;
    private int totalTime, idealTimeHard, idealTimeMedium, idealTimeEasy;
    private LinkedList<Assignment> assignmentsForTheDay;

    public Day(int totalTime) {
        this.totalTime = totalTime;
        this.totalTimeLeft = totalTime;
        this.idealTimeHard = 0;
        this.timeLeftHard = 0;
        this.idealTimeMedium = 0;
        this.timeLeftMedium = 0;
        this.idealTimeEasy = 0;
        this.timeLeftEasy = 0;
        assignmentsForTheDay = new LinkedList<>();
    }

    public void resetTimeLeft() {
        timeLeftHard = idealTimeHard - timeLeftHard;
        timeLeftMedium = idealTimeMedium - timeLeftMedium;
        timeLeftEasy = idealTimeEasy - timeLeftEasy;
    }

    public LinkedList<Assignment> getAssignmentsForTheDay() {
        return assignmentsForTheDay;
    }

    public void addAssignments(LinkedList<Assignment> assignments, Difficulty difficulty) {

        int timeLeft = getTimeLeft(difficulty), assignmentDuration;
        Iterator<Assignment> iterator = assignments.iterator();
        Assignment assignment;

        while (iterator.hasNext()) {
            assignment = iterator.next();
            assignmentDuration = assignment.getDuration();
            if (assignmentDuration > timeLeft) {
                if (timeLeft != 0) assignmentsForTheDay.add(assignment.split(timeLeft));
                timeLeft = 0;
                break;
            } else {
                assignmentsForTheDay.add(assignment);
                timeLeft -= assignmentDuration;
                iterator.remove();
            }
        }

        if (timeLeft == 0) {
            setTimeLeft(timeLeft, difficulty);
        } else {
            throw new RuntimeException("timeLeft is " + timeLeft + ", not 0");
        }

    }

    public int getTotalTimeLeft() {
        return totalTimeLeft;
    }

    private void setTimeLeft(int newTimeLeft, Difficulty difficulty) {
        switch (difficulty) {
            case HARD:
                timeLeftHard = newTimeLeft;
                break;
            case MEDIUM:
                timeLeftMedium = newTimeLeft;
                break;
            case EASY:
                timeLeftEasy = newTimeLeft;
                break;
            default:
                throw new RuntimeException("Difficulty is null");
        }
    }

    public void setIdealTimeToTimeLeft() {
        idealTimeHard = timeLeftHard;
        idealTimeMedium = timeLeftMedium;
        idealTimeEasy = timeLeftEasy;
    }

    public void addToTimeLeft(int increment, Difficulty difficulty) {
        switch (difficulty) {
            case HARD:
                timeLeftHard += increment;
                break;
            case MEDIUM:
                timeLeftMedium += increment;
                break;
            case EASY:
                timeLeftEasy += increment;
                break;
            default:
                throw new RuntimeException("Difficulty is null");
        }
    }

    public void subtractFromTimeLeft(int decrement, Difficulty difficulty) {
        if (totalTimeLeft - decrement < 0) {
            throw new RuntimeException("totalTimeLeft would be negative");
        } else {
            addToTimeLeft(-decrement, difficulty);
            totalTimeLeft -= decrement;
        }
    }

    public int getTimeLeft(Difficulty difficulty) {
        return switch (difficulty) {
            case HARD -> timeLeftHard;
            case MEDIUM -> timeLeftMedium;
            case EASY -> timeLeftEasy;
            default -> throw new RuntimeException("Difficulty is null");
        };
    }

}
