package com.mobike.mobike.utils;

/**
 * Created by Andrea-PC on 04/02/2015.
 */
public class Event {
    private String name, date, creator;

    public Event(String name, String date, String creator) {
        this.name = name;
        this.date = date;
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getCreator() {
        return creator;
    }
}
