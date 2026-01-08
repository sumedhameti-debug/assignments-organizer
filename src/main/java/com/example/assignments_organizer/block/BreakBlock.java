package com.example.assignments_organizer.block;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

public class BreakBlock extends Block {

    public BreakBlock(Event studyBlock) {

        super(studyBlock);

    }

    @Override
    Event addBlockToCalendar(Calendar calendar, String calendarID) {

        return null;

    }


}
