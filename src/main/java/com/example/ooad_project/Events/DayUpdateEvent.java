package com.example.ooad_project.Events;

public class DayUpdateEvent {

    private final int day;

    public DayUpdateEvent(int day) {
        this.day = day;
    }

    public int getDay() {
        return day;
    }

}
