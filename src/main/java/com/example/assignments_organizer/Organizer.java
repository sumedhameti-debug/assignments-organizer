package com.example.assignments_organizer;

import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class Organizer {

    private LinkedList<Assignment> hardAssignments = new LinkedList<>(), mediumAssignments = new LinkedList<>(), easyAssignments = new LinkedList<>();
    private Day[] sortedDaysHard, sortedDaysMedium, sortedDaysEasy, sortedDaysTotal;

    private LinkedList<Assignment> getCorrectAssignmentsByDifficultyList(@NonNull Difficulty difficulty) {
        return switch (difficulty) {
            case HARD -> hardAssignments;
            case MEDIUM -> mediumAssignments;
            case EASY -> easyAssignments;
        };
    }

    public Day[] organize(int numDays, int[] dayInfo, LinkedList<Assignment> assignments) {

        Difficulty[] difficulties = Difficulty.values();
        Day temp;
        int insertIndex, overtimeLength, duration, timeLeft, sumOfTimeLeft, timeForDay;

        Comparator<Day> totalTimeLeftComparator = Comparator.comparingInt(Day::getTotalTimeLeft).reversed(),
                hardTimeLeftComparator = Comparator.comparingInt(x -> x.getTimeLeft(Difficulty.HARD)),
                mediumTimeLeftComparator = Comparator.comparingInt(x -> x.getTimeLeft(Difficulty.MEDIUM)),
                easyTimeLeftComparator = Comparator.comparingInt(x -> x.getTimeLeft(Difficulty.EASY)),
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
        Day[] tempSortedDays = Arrays.copyOf(days, days.length);
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
                timeForDay = Math.min(length/i, tempSortedDays[i - 1].getTotalTimeLeft());
                tempSortedDays[i - 1].addToTimeLeft(
                        timeForDay,
                        difficulty
                );
                length -= timeForDay;
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

        int maxKValue;
        sortedDaysTotal = Arrays.copyOf(days, days.length);
        for (int i = 0; i < daySummaries.length; i++) {

            // a

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

            // b


            for (Difficulty difficulty : difficulties) {

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
                        sortedDaysTotal[j].addToTimeLeft(
                                Math.min(daySummaries[i].getDuration(difficulty) / (j + 1), sortedDaysTotal[j].getTotalTimeLeft()),
                                difficulty
                        );
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

    private void increaseSortedArraySectionSize(Comparator<Day> currentComparator, Day[] tempSortedDays, int i) {
        int insertIndex;
        Day temp;
        insertIndex = Arrays.binarySearch(
                tempSortedDays,
                0,
                i + 1,
                tempSortedDays[i],
                currentComparator
        );
        if (insertIndex < 0) insertIndex = -insertIndex - 1;
        temp = tempSortedDays[i];
        System.arraycopy(tempSortedDays, insertIndex, tempSortedDays, insertIndex + 1, i - insertIndex);
        tempSortedDays[insertIndex] = temp;
    }

}
