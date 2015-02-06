package com.mobike.mobike.utils;

/**
 * Created by Andrea-PC on 04/02/2015.
 */
public class Event {
    private String name, date, creator, description;

    public Event(String name, String date, String creator, String description) {
        this.name = name;
        this.date = date;
        this.creator = creator;
        this.description = description;
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

    public String getDescription() { return description; }
}
