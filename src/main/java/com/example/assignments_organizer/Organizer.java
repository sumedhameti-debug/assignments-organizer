package com.example.assignments_organizer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is where time is allocated in each day for different assignments based off duration, difficulty, and deadline
 */
public class Organizer {

    private LinkedList<Assignment> hardAssignments, mediumAssignments, easyAssignments;
    private Day[] sortedDaysHard, sortedDaysMedium, sortedDaysEasy, sortedDaysTotal;

    private LinkedList<Assignment> getCorrectAssignmentsByDifficultyList(Difficulty difficulty) {
        return switch (difficulty) {
            case HARD -> hardAssignments;
            case MEDIUM -> mediumAssignments;
            case EASY -> easyAssignments;
        };
    }

    public Day[] organize(int numDays, int[] dayInfo, LinkedList<Assignment> assignments) {

        hardAssignments = new LinkedList<>();
        mediumAssignments = new LinkedList<>();
        easyAssignments = new LinkedList<>();

        Difficulty[] difficulties = Difficulty.values();
        Day temp;
        int insertIndex, overtimeLength, duration, timeLeft, sumOfTimeLeft, timeForDay;

        Comparator<Day> totalTimeLeftComparator = Comparator.comparingInt(Day::getTotalTimeLeft).reversed(),
                hardTimeLeftComparator = Comparator.comparingInt(x -> x.getTimeLeft(Difficulty.HARD)),
                mediumTimeLeftComparator = Comparator.comparingInt(x -> x.getTimeLeft(Difficulty.MEDIUM)),
                easyTimeLeftComparator = Comparator.comparingInt(x -> x.getTimeLeft(Difficulty.EASY)),
                currentComparator;
        Comparator<Assignment> assignmentComparator = Comparator.comparingInt(Assignment::getLastDate).thenComparingInt(Assignment::getDuration);

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
        Day[] tempSortedDays = Arrays.copyOf(days, days.length);
        int length, totalTimeLeft, average;

        /**
         * Decide the ideal distribution of time for assignments of each difficulty without considering deadlines
         */
        for (Difficulty difficulty : difficulties) {

            tempAssignments = getCorrectAssignmentsByDifficultyList(difficulty);
            tempAssignments.sort(assignmentComparator);

            length = 0;
            tempSortedDays = Arrays.copyOf(days, numDays);
            Arrays.sort(tempSortedDays, totalTimeLeftComparator);
            for (Assignment assignment : tempAssignments) length += assignment.getDuration();
            for (int i = tempSortedDays.length; i > 0; i--) {
                timeForDay = Math.min(length/i, tempSortedDays[i - 1].getTotalTimeLeft());
                tempSortedDays[i - 1].addToTimeLeft(
                        timeForDay,
                        difficulty
                );
                length -= timeForDay;
            }

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

        /**
         * Outermost loop: loop over days
         */
        int maxKValue;
        sortedDaysTotal = Arrays.copyOf(days, days.length);
        for (int i = 0; i < daySummaries.length; i++) {

            /**
             * Attempt to schedule assignments according to the ideal distribution of assignments by difficulty
             */
            for (Difficulty difficulty : difficulties) {

                for (int j = i; j >= 0; j--) {

                    duration = daySummaries[i].getDuration(difficulty);
                    timeLeft = days[j].getTimeLeft(difficulty);

                    if (duration == 0) {

                        break;

                    } else if (timeLeft > duration) {

                        totalTimeLeft = days[j].getTotalTimeLeft();
                        if (duration > totalTimeLeft) {
                            days[j].subtractFromTimeLeft(totalTimeLeft, difficulty);
                            daySummaries[i].subtractFromDuration(totalTimeLeft, difficulty);
                        } else {
                            days[j].subtractFromTimeLeft(duration, difficulty);
                            daySummaries[i].setDuration(0, difficulty);
                        }

                    } else {

                        daySummaries[i].subtractFromDuration(timeLeft, difficulty);
                        days[j].subtractFromTimeLeft(Math.min(timeLeft, days[j].getTotalTimeLeft()), difficulty);

                    }

                }

            }

            for (Difficulty difficulty : difficulties) {

                /**
                 * Sort by how much time is left
                 */
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

                Arrays.sort(tempSortedDays, 0, i + 1, currentComparator);

                /**
                 * Distribute extra hours
                 */
                for (int j = i; j > 0 && daySummaries[i].getDuration(difficulty) != 0; j--) {

                    duration = daySummaries[i].getDuration(difficulty);
                    maxKValue = i - j + 1;

                    if (duration < maxKValue) {

                        for (int k = i; duration != 0; k--) {
                            tempSortedDays[k].subtractFromTimeLeft(1, difficulty);
                            daySummaries[i].subtractFromDuration(1, difficulty);
                            duration--;
                            if (k == 0) k = i;
                        }

                    } else {

                        timeLeft = tempSortedDays[j - 1].getTimeLeft(difficulty);
                        average = duration / maxKValue;

                        for (int k = 1; k <= maxKValue; k++) {
                            overtimeLength = Math.min(
                                    Math.min(
                                            timeLeft - tempSortedDays[j - k + 1].getTimeLeft(difficulty),
                                            tempSortedDays[j - k + 1].getTotalTimeLeft()
                                    ),
                                    average
                            );
                            tempSortedDays[j - k + 1].subtractFromTimeLeft(overtimeLength, difficulty);
                            daySummaries[i].subtractFromDuration(overtimeLength, difficulty);
                        }

                    }

                }


                Arrays.sort(sortedDaysTotal, 0, i + 1, totalTimeLeftComparator);

                if (daySummaries[i].getDuration(difficulty) != 0) {
                    for (int j = i; j >= 0; j--) {
                        sortedDaysTotal[j].subtractFromTimeLeft(
                                Math.min(daySummaries[i].getDuration(difficulty) / (j + 1), sortedDaysTotal[j].getTotalTimeLeft()),
                                difficulty
                        );
                    }
                }

            }

        }

        /**
         * Distribute the assignments
         */
        for (Day day : days) {

            day.resetTimeLeft();

            day.addAssignments(hardAssignments, Difficulty.HARD);
            day.addAssignments(mediumAssignments, Difficulty.MEDIUM);
            day.addAssignments(easyAssignments, Difficulty.EASY);

        }

        return days;

    }

}
