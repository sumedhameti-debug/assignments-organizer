package com.example.assignments_organizer.model;

import com.example.assignments_organizer.enums.Difficulty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * An instance of this class stores information about an assignment
 */
@Entity
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private LocalDateTime lastDateLocalDateTime;
    private int lastDate, duration;
    private String name;

    @JsonCreator
    public Assignment(@JsonProperty("lastDateLocalDateTime") LocalDateTime lastDateLocalDateTime, @JsonProperty("duration") int duration, @JsonProperty("difficulty") Difficulty difficulty, @JsonProperty("name") String name) {
        this.lastDateLocalDateTime = lastDateLocalDateTime;
        this.duration = duration;
        this.difficulty = difficulty;
        this.name = name;
    }

    public Assignment(int lastDate, @JsonProperty("duration") int duration, @JsonProperty("difficulty") Difficulty difficulty, @JsonProperty("name") String name) {
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

    public LocalDateTime getLastDateLocalDateTime() {
        return lastDateLocalDateTime;
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

    public void setLastDateToDaysFromToday(LocalDateTime today) {
        lastDate = (int) ChronoUnit.DAYS.between(today, lastDateLocalDateTime) + 1;
    }

    public void setLastDateLocalDateTime(LocalDateTime lastDateLocalDateTime) {
        this.lastDateLocalDateTime = lastDateLocalDateTime;
    }

}
