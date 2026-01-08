package com.example.assignments_organizer.block;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;

public class StudyBlock extends Block {

    public StudyBlock(Event studyBlock) {

        super(studyBlock);

    }

    @Override
    public Event addBlockToCalendar(Calendar calendar, String calendarID) {

        try {
            return calendar.events().insert(calendarID, super.block).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}