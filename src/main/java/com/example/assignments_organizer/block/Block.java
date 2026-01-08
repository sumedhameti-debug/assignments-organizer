package com.example.assignments_organizer.block;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

public abstract class Block {

    protected Event block;

    public Block(Event block) {

        this.block = block;

    }

    abstract Event addBlockToCalendar(Calendar calendar, String calendarID);

}
