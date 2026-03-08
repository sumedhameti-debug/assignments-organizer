package com.example.assignments_organizer.model;

/**
 * Objects of this class contain data that the frontend sends to the backend during a schedule request
 */
public class ScheduleRequest {

    private String inputCalendarId, outputCalendarId;
    private int studyDuration, breakDuration;

    public void setInputCalendarId() {
        this.inputCalendarId = inputCalendarId;
    }

    public void setOutputCalendarId() {
        this.outputCalendarId = outputCalendarId;
    }

    public String getInputCalendarId() {
        return inputCalendarId;
    }

    public String getOutputCalendarId() {
        return outputCalendarId;
    }

    public int getStudyDuration() {
        return studyDuration;
    }

    public void setStudyDuration(int studyDuration) {
        this.studyDuration = studyDuration;
    }

    public int getBreakDuration() {
        return breakDuration;
    }

    public void setBreakDuration(int breakDuration) {
        this.breakDuration = breakDuration;
    }

}