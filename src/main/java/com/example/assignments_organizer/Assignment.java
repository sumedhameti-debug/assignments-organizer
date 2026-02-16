package com.example.assignments_organizer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private int lastDate, duration;
    private String name;

    @JsonCreator
    public Assignment(@JsonProperty("lastDate") int lastDate, @JsonProperty("duration") int duration, @JsonProperty("difficulty") Difficulty difficulty, @JsonProperty("name") String name) {
        this.lastDate = lastDate;
        this.duration = duration;
        this.difficulty = difficulty;
        this.name = name;
    }

    public Assignment() {}

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

    public void setName(String name) {
        this.name = name;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
