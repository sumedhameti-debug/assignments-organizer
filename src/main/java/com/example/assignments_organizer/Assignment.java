package com.example.assignments_organizer;

public class Assignment {

    private int lastDate, duration;
    private Difficulty difficulty;
    private String name;

    public Assignment(int lastDate, int duration, Difficulty difficulty, String name) {
        this.lastDate = lastDate;
        this.duration = duration;
        this.difficulty = difficulty;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Name: " + name + "\t\tDuration: " + duration + "\t\tDifficulty " + difficulty + "\t\tDue: " + lastDate;
    }

    public Assignment split(int newAssignmentDuration) {

        if (newAssignmentDuration >= duration) {
            throw new IllegalArgumentException("The first new assignment isn't shorter than the existing assignment.");
        }

        duration -= newAssignmentDuration;
        return new Assignment(lastDate, newAssignmentDuration, difficulty, name);

    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getLastDate() {
        return lastDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setLastDate(int lastDate) {
        this.lastDate = lastDate;
    }

    public String getName() {
        return name;
    }

}
